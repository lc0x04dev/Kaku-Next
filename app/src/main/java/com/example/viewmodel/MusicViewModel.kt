package com.example.viewmodel

import android.content.Context
import android.provider.MediaStore
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.Song
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MusicViewModel : ViewModel() {

    private val availableSongs = emptyList<Song>()

    private val _songsFlow = MutableStateFlow<List<Song>>(emptyList())
    val songsFlow: StateFlow<List<Song>> = _songsFlow.asStateFlow()

    // Playback State
    private val _currentSong = MutableStateFlow<Song?>(null) // Starts empty until song is scanned/loaded
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition.asStateFlow()

    // Playlist States
    private val _playlistsFlow = MutableStateFlow<List<Playlist>>(emptyList())
    val playlistsFlow: StateFlow<List<Playlist>> = _playlistsFlow.asStateFlow()

    // Search query state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Storage permission state
    private val _hasStoragePermission = MutableStateFlow<Boolean?>(null) // null = not decided, true = granted, false = denied
    val hasStoragePermission: StateFlow<Boolean?> = _hasStoragePermission.asStateFlow()

    // Sync banner visibility state - Hide once local music is scanned and found!
    private val _showSyncBanner = MutableStateFlow(true)
    val showSyncBanner: StateFlow<Boolean> = _showSyncBanner.asStateFlow()

    private var playbackJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null

    init {
        // Observe playback state to run active mock or real audio playback
        viewModelScope.launch {
            _isPlaying.collect { play ->
                if (play) {
                    startPlaybackTimer()
                } else {
                    stopPlaybackTimer()
                }
            }
        }
    }

    private fun startPlaybackTimer() {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val currentSongVal = _currentSong.value ?: break
                
                if (currentSongVal.filePath != null && mediaPlayer?.isPlaying == true) {
                    val posSec = (mediaPlayer?.currentPosition ?: 0) / 1000
                    _currentPosition.value = posSec
                    if (posSec >= currentSongVal.durationSeconds) {
                        playNextSong()
                    }
                } else {
                    // Simulated timer for preloaded cloud songs
                    if (_currentPosition.value >= currentSongVal.durationSeconds) {
                        playNextSong()
                    } else {
                        _currentPosition.value += 1
                    }
                }
            }
        }
    }

    private fun stopPlaybackTimer() {
        playbackJob?.cancel()
        playbackJob = null
    }

    private fun playActualSong(song: Song) {
        try {
            mediaPlayer?.release()
            mediaPlayer = null
            
            if (song.filePath != null) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(song.filePath)
                    prepare()
                    seekTo(_currentPosition.value * 1000)
                    if (_isPlaying.value) {
                        start()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ACTIONS
    fun selectSong(song: Song, forcePlay: Boolean = true) {
        _currentSong.value = song
        _currentPosition.value = 0
        if (forcePlay) {
            _isPlaying.value = true
            playActualSong(song)
        } else {
            _isPlaying.value = false
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    fun togglePlayPause() {
        val playing = !_isPlaying.value
        _isPlaying.value = playing
        
        val song = _currentSong.value
        if (song?.filePath != null) {
            try {
                if (playing) {
                    if (mediaPlayer == null) {
                        playActualSong(song)
                    } else {
                        mediaPlayer?.start()
                    }
                } else {
                    mediaPlayer?.pause()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playNextSong() {
        val current = _currentSong.value ?: return
        val list = _songsFlow.value
        if (list.isEmpty()) return
        val currentIndex = list.indexOfFirst { it.id == current.id }
        if (currentIndex != -1 && currentIndex < list.size - 1) {
            selectSong(list[currentIndex + 1], _isPlaying.value)
        } else {
            // Loop back to start
            if (list.isNotEmpty()) {
                selectSong(list[0], _isPlaying.value)
            }
        }
    }

    fun playPreviousSong() {
        val current = _currentSong.value ?: return
        val list = _songsFlow.value
        if (list.isEmpty()) return
        val currentIndex = list.indexOfFirst { it.id == current.id }
        if (currentIndex > 0) {
            selectSong(list[currentIndex - 1], _isPlaying.value)
        } else {
            // Go to end of list
            if (list.isNotEmpty()) {
                selectSong(list.last(), _isPlaying.value)
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

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(songId: String) {
        _songsFlow.update { list ->
            list.map { song ->
                if (song.id == songId) song.copy(isFavorite = !song.isFavorite) else song
            }
        }
        // Sync current song if it's the one modified
        _currentSong.update { current ->
            if (current?.id == songId) current.copy(isFavorite = !current.isFavorite) else current
        }
        // Sync playlists containing this song
        _playlistsFlow.update { lists ->
            lists.map { playlist ->
                playlist.copy(songs = playlist.songs.map { s ->
                    if (s.id == songId) s.copy(isFavorite = !s.isFavorite) else s
                })
            }
        }
    }

    fun setPermissionStatus(granted: Boolean) {
        _hasStoragePermission.value = granted
    }

    fun scanLocalSongs(context: Context) {
        viewModelScope.launch {
            try {
                val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
                val projection = arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.DATA
                )
                val cursor = context.contentResolver.query(uri, projection, selection, null, null)
                val songsList = mutableListOf<Song>()
                
                if (cursor != null) {
                    val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                    val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                    val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idCol).toString()
                        val title = cursor.getString(titleCol) ?: "Pista Desconocida"
                        val artist = cursor.getString(artistCol) ?: "Artista Desconocido"
                        val album = cursor.getString(albumCol) ?: "Álbum Desconocido"
                        val durationMs = cursor.getLong(durationCol)
                        val filePath = cursor.getString(dataCol)
                        
                        val extension = filePath?.substringAfterLast('.', "")?.lowercase() ?: ""
                        val isSupported = extension in listOf("mp3", "m4a", "wav", "ogg", "flac", "aac", "m4p", "mp4")

                        if (isSupported) {
                            val durationSecs = (durationMs / 1000).toInt()
                            val mins = durationSecs / 60
                            val secs = durationSecs % 60
                            val durationStr = String.format("%01d:%02d", mins, secs)
                            
                            val localLyrics = """
                                [00:00] Reproduciendo tu archivo local
                                [00:08] Canción: $title
                                [00:15] Artista: $artist
                                [00:22] Sintonizando el pulso de tu código analógico
                                [00:30] Sonando con fidelidad premium en Kaku Next
                            """.trimIndent()

                            val colors = listOf(0xFF00F0FF, 0xFFFF007F, 0xFFBD93F9, 0xFF4EFE80, 0xFFFFB86C, 0xFFF1FA8C)
                            val colorIdx = id.hashCode().coerceAtLeast(0) % colors.size
                            val pickedColor = colors[colorIdx]

                            songsList.add(
                                Song(
                                    id = id,
                                    title = title,
                                    artist = artist,
                                    album = album,
                                    duration = durationStr,
                                    durationSeconds = if (durationSecs > 0) durationSecs else 180,
                                    lyrics = localLyrics,
                                    coverColor = pickedColor,
                                    filePath = filePath
                                )
                            )
                        }
                    }
                    cursor.close()
                }

                if (songsList.isNotEmpty()) {
                    _songsFlow.value = songsList
                    _currentSong.value = songsList[0]
                    _currentPosition.value = 0
                    _showSyncBanner.value = false
                    
                    _playlistsFlow.update {
                        listOf(
                            Playlist("local_1", "Descargas de Música", "Archivos de audio escaneados en tu dispositivo", songsList),
                            Playlist("local_2", "Mezclas Recientes", "Canciones locales sugeridas", songsList.shuffled().take(songsList.size.coerceAtMost(5)))
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

data class Playlist(
    val id: String,
    val name: String,
    val description: String,
    val songs: List<Song>
)
