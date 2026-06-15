package com.example.viewmodel

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

    private val availableSongs = listOf(
        Song(
            id = "1",
            title = "Neon Horizon",
            artist = "CyberSunset",
            album = "Grid Runner EP",
            duration = "3:12",
            durationSeconds = 192,
            lyrics = """
                [00:10] Corriendo en la red, sin mirar atrás
                [00:25] Las luces de neón empiezan a brillar
                [00:40] El asfalto húmedo refleja la ciudad
                [01:00] Siento el pulso eléctrico de la eternidad
                
                [01:15] ¡Horizonte de neón! Llevame hasta el fin
                [01:30] Todo lo que quise siempre estuvo aquí
                [01:45] Los sintetizadores cantan para ti
                
                [02:10] Susurros digitales en la oscuridad
                [02:25] Perdiendo la noción de la realidad
                [02:40] El futuro es nuestro, no hay velocidad
                [02:55] En este viaje eterno hacia la inmensidad
            """.trimIndent(),
            coverColor = 0xFF00F0FF // Cyan
        ),
        Song(
            id = "2",
            title = "Retro Pulse",
            artist = "FutureCops",
            album = "Outrun '86",
            duration = "4:05",
            durationSeconds = 245,
            lyrics = """
                [00:15] El motor ruge en la autopista central
                [00:32] Un ritmo analógico que viaja digital
                [00:50] Rompiendo barreras, no hay marcha atrás
                
                [01:15] Siente el retro pulso en tu corazón
                [01:30] Bajo la tormenta de esta gran ciudad de neón
                [01:48] No hay reglas que seguir, pura vibración
                
                [02:10] Destellos magenta en el retrovisor
                [02:30] El cielo está ardiendo con nuestro calor
                [02:50] No duerme la noche, no duerme el dolor
            """.trimIndent(),
            coverColor = 0xFFFF007F // Magenta
        ),
        Song(
            id = "3",
            title = "Midnight Drive",
            artist = "TokyoGlow",
            album = "Shibuya Nights",
            duration = "2:48",
            durationSeconds = 168,
            lyrics = null, // No lyrics to verify the "No lyrics available" state
            coverColor = 0xFFBD93F9 // Lavender
        ),
        Song(
            id = "4",
            title = "Digital Rain",
            artist = "MatrixCode",
            album = "Cybernetic Dreams",
            duration = "3:30",
            durationSeconds = 210,
            lyrics = """
                [00:12] Gotas de código caen sin parar
                [00:28] En la pantalla verde veo tu mirar
                [00:45] Algoritmos lentos bailando en espiral
                
                [01:05] Lluvia digital en mi CPU
                [01:25] Entre los bytes solo brillas tú
                [01:45] El sistema colapsa bajo tu luz
                
                [02:15] Recomponiendo sectores en mi memoria
                [02:35] Escribiendo juntos una nueva historia
            """.trimIndent(),
            coverColor = 0xFF4EFE80 // Green Neon
        ),
        Song(
            id = "5",
            title = "Echoes of Void",
            artist = "Andromeda",
            album = "Multiverse Suite",
            duration = "5:15",
            durationSeconds = 315,
            lyrics = null, // No lyrics
            coverColor = 0xFFFFB86C // Orange Neon
        ),
        Song(
            id = "6",
            title = "Synth Whisper",
            artist = "LaserBlade",
            album = "Neon Dynasty",
            duration = "3:54",
            durationSeconds = 234,
            lyrics = """
                [00:08] Un susurro en la frecuencia modulada
                [00:22] Tu silueta de holograma difuminada
                [00:40] Grabando en mi cinta tu voz dorada
                
                [01:02] Es el susurro del sintetizador
                [01:20] Que esconde los ecos de nuestro amor
                [01:40] El tiempo nos borra como un error
            """.trimIndent(),
            coverColor = 0xFFF1FA8C // Yellow Neon
        )
    )

    private val _songsFlow = MutableStateFlow(availableSongs)
    val songsFlow: StateFlow<List<Song>> = _songsFlow.asStateFlow()

    // Playback State
    private val _currentSong = MutableStateFlow<Song?>(availableSongs[0]) // Starts with the first song active
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition.asStateFlow()

    // Playlist States
    private val _playlistsFlow = MutableStateFlow(
        listOf(
            Playlist("1", "Cyberwave Hits", "El sonido del futuro hoy", availableSongs.take(3)),
            Playlist("2", "Neon Nocturno", "Beats para conducir de noche", listOf(availableSongs[2], availableSongs[4], availableSongs[5])),
            Playlist("3", "Favoritos", "Tus pistas cyberpunk preferidas", listOf(availableSongs[0], availableSongs[1]))
        )
    )
    val playlistsFlow: StateFlow<List<Playlist>> = _playlistsFlow.asStateFlow()

    // Search query state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Storage permission state
    private val _hasStoragePermission = MutableStateFlow<Boolean?>(null) // null = not decided, true = granted, false = denied
    val hasStoragePermission: StateFlow<Boolean?> = _hasStoragePermission.asStateFlow()

    private var playbackJob: Job? = null

    init {
        // Observe playback state to run active mock audio playback
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
                if (_currentPosition.value >= currentSongVal.durationSeconds) {
                    // Song finished, auto play next
                    playNextSong()
                } else {
                    _currentPosition.value += 1
                }
            }
        }
    }

    private fun stopPlaybackTimer() {
        playbackJob?.cancel()
        playbackJob = null
    }

    // ACTIONS
    fun selectSong(song: Song, forcePlay: Boolean = true) {
        _currentSong.value = song
        _currentPosition.value = 0
        if (forcePlay) {
            _isPlaying.value = true
        }
    }

    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }

    fun playNextSong() {
        val current = _currentSong.value ?: return
        val list = _songsFlow.value
        val currentIndex = list.indexOfFirst { it.id == current.id }
        if (currentIndex != -1 && currentIndex < list.size - 1) {
            selectSong(list[currentIndex + 1], _isPlaying.value)
        } else {
            // Loop back to start
            selectSong(list[0], _isPlaying.value)
        }
    }

    fun playPreviousSong() {
        val current = _currentSong.value ?: return
        val list = _songsFlow.value
        val currentIndex = list.indexOfFirst { it.id == current.id }
        if (currentIndex > 0) {
            selectSong(list[currentIndex - 1], _isPlaying.value)
        } else {
            // Go to end of list
            selectSong(list.last(), _isPlaying.value)
        }
    }

    fun seekTo(seconds: Int) {
        val current = _currentSong.value ?: return
        val capped = seconds.coerceIn(0, current.durationSeconds)
        _currentPosition.value = capped
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
}

data class Playlist(
    val id: String,
    val name: String,
    val description: String,
    val songs: List<Song>
)
