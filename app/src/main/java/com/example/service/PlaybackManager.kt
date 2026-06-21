package com.example.service

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import com.example.model.Song
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Singleton que gestiona el estado y componente de la reproducción.
 * Esto asegura que tanto el ViewModel (UI) como el Foreground Service (Notification/Lockscreen)
 * compartan exactamente la misma instancia de reproducción y estados reactivos.
 */
object PlaybackManager {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition.asStateFlow()

    private val _playlist = MutableStateFlow<List<Song>>(emptyList())
    val playlist: StateFlow<List<Song>> = _playlist.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var timerJob: Job? = null

    init {
        // Observación del estado para reproducir/pausar temporizadores
        scope.launch {
            _isPlaying.collect { playing ->
                if (playing) {
                    _currentSong.value?.let { startTimer() }
                } else {
                    stopTimer()
                }
            }
        }
    }

    fun setPlaylist(list: List<Song>) {
        _playlist.value = list
    }

    fun updateCurrentSong(song: Song) {
        if (_currentSong.value?.id == song.id) {
            _currentSong.value = song
        }
    }

    fun updatePlaylist(list: List<Song>) {
        _playlist.value = list
    }

    fun selectSongWithoutPlaying(song: Song) {
        _currentSong.value = song
        _currentPosition.value = 0
        _isPlaying.value = false
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun restoreSongAndPosition(song: Song, positionSeconds: Int) {
        _currentSong.value = song
        _currentPosition.value = positionSeconds
        _isPlaying.value = false
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun playSong(song: Song, list: List<Song>, context: Context) {
        _currentSong.value = song
        _playlist.value = list
        _currentPosition.value = 0
        _isPlaying.value = true

        playActual(song, context)
        notifyService(context, MusicService.ACTION_PLAY)
    }

    private fun playActual(song: Song, context: Context) {
        try {
            mediaPlayer?.release()
            mediaPlayer = null

            if (song.filePath != null) {
                mediaPlayer = MediaPlayer().apply {
                    setOnErrorListener { _, what, extra ->
                        android.util.Log.e("PlaybackManager", "Error de MediaPlayer: what=$what, extra=$extra")
                        _isPlaying.value = false
                        stopTimer()
                        true
                    }
                    if (song.filePath.startsWith("content://")) {
                        setDataSource(context, android.net.Uri.parse(song.filePath))
                    } else {
                        setDataSource(song.filePath)
                    }
                    prepare()
                    seekTo(_currentPosition.value * 1000)
                    if (_isPlaying.value) {
                        start()
                    }
                    setOnCompletionListener {
                        playNext(context)
                    }
                }
            }
            startTimer()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun togglePlayPause(context: Context) {
        val playing = !_isPlaying.value
        _isPlaying.value = playing

        val song = _currentSong.value
        if (song != null) {
            if (song.filePath != null) {
                try {
                    if (playing) {
                        if (mediaPlayer == null) {
                            playActual(song, context)
                        } else {
                            mediaPlayer?.start()
                        }
                        notifyService(context, MusicService.ACTION_PLAY)
                    } else {
                        mediaPlayer?.pause()
                        notifyService(context, MusicService.ACTION_PAUSE)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                // Simulado
                if (playing) {
                    notifyService(context, MusicService.ACTION_PLAY)
                } else {
                    notifyService(context, MusicService.ACTION_PAUSE)
                }
            }
        }
        if (playing) {
            startTimer()
        } else {
            stopTimer()
        }
    }

    fun playNext(context: Context) {
        val current = _currentSong.value ?: return
        val list = _playlist.value
        if (list.isEmpty()) return
        val currentIndex = list.indexOfFirst { it.id == current.id }
        if (currentIndex != -1 && currentIndex < list.size - 1) {
            playSong(list[currentIndex + 1], list, context)
        } else {
            if (list.isNotEmpty()) {
                playSong(list[0], list, context)
            }
        }
    }

    fun playPrevious(context: Context) {
        val current = _currentSong.value ?: return
        val list = _playlist.value
        if (list.isEmpty()) return
        val currentIndex = list.indexOfFirst { it.id == current.id }
        if (currentIndex > 0) {
            playSong(list[currentIndex - 1], list, context)
        } else {
            if (list.isNotEmpty()) {
                playSong(list.last(), list, context)
            }
        }
    }

    fun seekTo(seconds: Int) {
        val current = _currentSong.value ?: return
        val capped = seconds.coerceIn(0, current.durationSeconds)
        _currentPosition.value = capped

        if (current.filePath != null) {
            try {
                mediaPlayer?.seekTo(capped * 1000)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (true) {
                delay(1000)
                val currentSongVal = _currentSong.value ?: break

                if (currentSongVal.filePath != null && mediaPlayer?.isPlaying == true) {
                    val posSec = (mediaPlayer?.currentPosition ?: 0) / 1000
                    _currentPosition.value = posSec
                    if (posSec >= currentSongVal.durationSeconds) {
                        // El completion listener se encargará de playNext(context), pero como respaldo:
                        // No hacemos nada para no duplicar acciones
                    }
                } else {
                    // Temporizador simulado
                    if (_currentPosition.value >= currentSongVal.durationSeconds) {
                        // Reproduce la siguiente canción
                        playNext(mediaPlayer?.let { android.app.PendingIntent.getActivity(null, 0, Intent(), 0); null } ?: getSandboxContext() ?: return@launch)
                    } else {
                        _currentPosition.value += 1
                    }
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun release() {
        stopTimer()
        mediaPlayer?.release()
        mediaPlayer = null
        _isPlaying.value = false
    }

    private var sandboxContext: Context? = null
    fun setSandboxContext(context: Context) {
        sandboxContext = context.applicationContext
    }
    private fun getSandboxContext(): Context? {
        return sandboxContext
    }

    private fun notifyService(context: Context, action: String) {
        val intent = Intent(context, MusicService::class.java).apply {
            this.action = action
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
