package com.example.viewmodel

import android.content.Context
import android.provider.MediaStore
import android.media.MediaPlayer
import android.media.MediaMetadataRetriever
import android.os.Environment
import java.io.File
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.content.ContentUris
import com.example.model.Song
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("kaku_next_settings", Context.MODE_PRIVATE)

    // Configuration states
    private val _audioHiFi = MutableStateFlow(prefs.getBoolean("prefs_audio_hifi", true))
    val audioHiFi = _audioHiFi.asStateFlow()

    private val _customTitle = MutableStateFlow(prefs.getString("prefs_custom_title", "Lune") ?: "Lune")
    val customTitle = _customTitle.asStateFlow()

    private val _customSections = MutableStateFlow(prefs.getBoolean("prefs_custom_sections", false))
    val customSections = _customSections.asStateFlow()

    private val _useCustomColors = MutableStateFlow(prefs.getBoolean("prefs_use_custom_colors", false))
    val useCustomColors = _useCustomColors.asStateFlow()

    private val _selectedCustomColor = MutableStateFlow(prefs.getLong("prefs_selected_custom_color", 0xFF4CAF50)) // Green by default (checked circle from Image 5)
    val selectedCustomColor = _selectedCustomColor.asStateFlow()

    private val _amoledScreen = MutableStateFlow(prefs.getBoolean("prefs_amoled_screen", false))
    val amoledScreen = _amoledScreen.asStateFlow()

    private val _opticalVibration = MutableStateFlow(prefs.getBoolean("prefs_optical_vibration", false))
    val opticalVibration = _opticalVibration.asStateFlow()

    private val _songInfo = MutableStateFlow(prefs.getBoolean("prefs_song_info", true))
    val songInfo = _songInfo.asStateFlow()

    private val _cinematicPlayer = MutableStateFlow(prefs.getBoolean("prefs_cinematic_player", false))
    val cinematicPlayer = _cinematicPlayer.asStateFlow()

    private val _coverShape = MutableStateFlow(prefs.getString("prefs_cover_shape", "Por defecto") ?: "Por defecto")
    val coverShape = _coverShape.asStateFlow()

    private val _coverSize = MutableStateFlow(prefs.getString("prefs_cover_size", "100%") ?: "100%")
    val coverSize = _coverSize.asStateFlow()

    private val _buttonStyle = MutableStateFlow(prefs.getString("prefs_button_style", "Por defecto") ?: "Por defecto")
    val buttonStyle = _buttonStyle.asStateFlow()

    private val _filledIcons = MutableStateFlow(prefs.getBoolean("prefs_filled_icons", false))
    val filledIcons = _filledIcons.asStateFlow()

    private val _customControlsColor = MutableStateFlow(prefs.getBoolean("prefs_custom_controls_color", false))
    val customControlsColor = _customControlsColor.asStateFlow()

    private val _progressBarStyle = MutableStateFlow(prefs.getString("prefs_progress_bar_style", "Por defecto") ?: "Por defecto")
    val progressBarStyle = _progressBarStyle.asStateFlow()

    private val _playerBackgroundStyle = MutableStateFlow(prefs.getString("prefs_player_background_style", "Apagado") ?: "Apagado")
    val playerBackgroundStyle = _playerBackgroundStyle.asStateFlow()

    private val _playerBackgroundPreset = MutableStateFlow(prefs.getString("prefs_player_background_preset", "Gradiente 3") ?: "Gradiente 3")
    val playerBackgroundPreset = _playerBackgroundPreset.asStateFlow()

    fun setAudioHiFi(value: Boolean) {
        _audioHiFi.value = value
        prefs.edit().putBoolean("prefs_audio_hifi", value).apply()
    }

    fun setCustomTitle(value: String) {
        _customTitle.value = value
        prefs.edit().putString("prefs_custom_title", value).apply()
    }

    fun setCustomSections(value: Boolean) {
        _customSections.value = value
        prefs.edit().putBoolean("prefs_custom_sections", value).apply()
    }

    fun setUseCustomColors(value: Boolean) {
        _useCustomColors.value = value
        prefs.edit().putBoolean("prefs_use_custom_colors", value).apply()
    }

    fun setSelectedCustomColor(value: Long) {
        _selectedCustomColor.value = value
        prefs.edit().putLong("prefs_selected_custom_color", value).apply()
    }

    fun setAmoledScreen(value: Boolean) {
        _amoledScreen.value = value
        prefs.edit().putBoolean("prefs_amoled_screen", value).apply()
    }

    fun setOpticalVibration(value: Boolean) {
        _opticalVibration.value = value
        prefs.edit().putBoolean("prefs_optical_vibration", value).apply()
    }

    fun setSongInfo(value: Boolean) {
        _songInfo.value = value
        prefs.edit().putBoolean("prefs_song_info", value).apply()
    }

    fun setCinematicPlayer(value: Boolean) {
        _cinematicPlayer.value = value
        prefs.edit().putBoolean("prefs_cinematic_player", value).apply()
    }

    fun setCoverShape(value: String) {
        _coverShape.value = value
        prefs.edit().putString("prefs_cover_shape", value).apply()
    }

    fun setCoverSize(value: String) {
        _coverSize.value = value
        prefs.edit().putString("prefs_cover_size", value).apply()
    }

    fun setButtonStyle(value: String) {
        _buttonStyle.value = value
        prefs.edit().putString("prefs_button_style", value).apply()
    }

    fun setFilledIcons(value: Boolean) {
        _filledIcons.value = value
        prefs.edit().putBoolean("prefs_filled_icons", value).apply()
    }

    fun setCustomControlsColor(value: Boolean) {
        _customControlsColor.value = value
        prefs.edit().putBoolean("prefs_custom_controls_color", value).apply()
    }

    fun setProgressBarStyle(value: String) {
        _progressBarStyle.value = value
        prefs.edit().putString("prefs_progress_bar_style", value).apply()
    }

    fun setPlayerBackgroundStyle(value: String) {
        _playerBackgroundStyle.value = value
        prefs.edit().putString("prefs_player_background_style", value).apply()
    }

    fun setPlayerBackgroundPreset(value: String) {
        _playerBackgroundPreset.value = value
        prefs.edit().putString("prefs_player_background_preset", value).apply()
    }

    private val availableSongs = emptyList<Song>()

    private val _songsFlow = MutableStateFlow<List<Song>>(emptyList())
    val songsFlow: StateFlow<List<Song>> = _songsFlow.asStateFlow()

    // Playback State delegado a PlaybackManager
    val currentSong: StateFlow<Song?> = com.example.service.PlaybackManager.currentSong
    val isPlaying: StateFlow<Boolean> = com.example.service.PlaybackManager.isPlaying
    val currentPosition: StateFlow<Int> = com.example.service.PlaybackManager.currentPosition

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

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private fun getAttributedContext(): Context {
        val app = getApplication<Application>()
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            app.createAttributionContext("default")
        } else {
            app
        }
    }

    // ACTIONS
    fun selectSong(song: Song, forcePlay: Boolean = true) {
        if (forcePlay) {
            com.example.service.PlaybackManager.playSong(song, _songsFlow.value, getAttributedContext())
        } else {
            com.example.service.PlaybackManager.selectSongWithoutPlaying(song)
        }
    }

    fun togglePlayPause() {
        com.example.service.PlaybackManager.togglePlayPause(getAttributedContext())
    }

    fun playNextSong() {
        com.example.service.PlaybackManager.playNext(getAttributedContext())
    }

    fun playPreviousSong() {
        com.example.service.PlaybackManager.playPrevious(getAttributedContext())
    }

    fun seekTo(seconds: Int) {
        com.example.service.PlaybackManager.seekTo(seconds)
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(songId: String) {
        _songsFlow.update { list ->
            list.map { song ->
                val newSong = if (song.id == songId) song.copy(isFavorite = !song.isFavorite) else song
                if (song.id == songId) {
                    com.example.service.PlaybackManager.updateCurrentSong(newSong)
                }
                newSong
            }
        }
        com.example.service.PlaybackManager.updatePlaylist(_songsFlow.value)
        
        // Sync playlists containing this song
        _playlistsFlow.update { lists ->
            lists.map { playlist ->
                playlist.copy(songs = playlist.songs.map { s ->
                    val newSong = if (s.id == songId) s.copy(isFavorite = !s.isFavorite) else s
                    if (s.id == songId) {
                        com.example.service.PlaybackManager.updateCurrentSong(newSong)
                    }
                    newSong
                })
            }
        }
    }

    fun setPermissionStatus(granted: Boolean) {
        _hasStoragePermission.value = granted
    }

    private fun scanFilesDirectly(dir: File, depth: Int, result: MutableList<File>) {
        if (depth > 12) return // depth limit to avoid stack overflow
        val list = try { dir.listFiles() } catch (e: Exception) { null } ?: return
        for (file in list) {
            val name = file.name
            if (file.isDirectory) {
                // Ignore standard OS-specific config/application folders to keep search fast and secure
                val lowerName = name.lowercase()
                if (lowerName == "android" || lowerName == "data" || lowerName == "obb" ||
                    name.startsWith(".android") || name.startsWith(".gradle") || 
                    name.startsWith(".git") || name.startsWith(".google")
                ) {
                    continue
                }
                scanFilesDirectly(file, depth + 1, result)
            } else {
                val ext = file.extension.lowercase()
                if (ext in listOf("mp3", "m4a", "wav", "ogg", "flac", "aac", "m4p")) {
                    result.add(file)
                }
            }
        }
    }

    fun scanLocalSongs(context: Context) {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                val songsList = mutableListOf<Song>()
                val seenRawPaths = mutableSetOf<String>()
                val seenIds = mutableSetOf<String>()

                // 1. Scan via MediaStore Audio Media
                try {
                    val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    val selection = null 
                    val projection = arrayOf(
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.DATA
                    )
                    val cursor = context.contentResolver.query(uri, projection, selection, null, null)
                    if (cursor != null) {
                        val idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                        val titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                        val artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                        val albumCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                        val durationCol = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                        val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)

                        while (cursor.moveToNext()) {
                            val id = if (idCol >= 0) cursor.getLong(idCol).toString() else ""
                            if (id.isEmpty()) continue
                            
                            val title = if (titleCol >= 0) cursor.getString(titleCol) ?: "Pista Desconocida" else "Pista Desconocida"
                            val artist = if (artistCol >= 0) cursor.getString(artistCol) ?: "Artista Desconocido" else "Artista Desconocido"
                            val album = if (albumCol >= 0) cursor.getString(albumCol) ?: "Álbum Desconocido" else "Álbum Desconocido"
                            val durationMs = if (durationCol >= 0) cursor.getLong(durationCol) else 0L
                            val filePath = if (dataCol >= 0) cursor.getString(dataCol) ?: "" else ""

                            // Generate the secure content URI for this media item
                            val mediaUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toLong())
                            val pathUriString = mediaUri.toString()

                            val extension = filePath.substringAfterLast('.', "").lowercase()
                            val isSupported = filePath.isEmpty() || extension in listOf("mp3", "m4a", "wav", "ogg", "flac", "aac", "m4p")

                            if (isSupported && !seenIds.contains("ms_$id")) {
                                seenIds.add("ms_$id")
                                if (filePath.isNotEmpty()) {
                                    seenRawPaths.add(filePath.lowercase())
                                }
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
                                        id = "ms_$id",
                                        title = title,
                                        artist = artist,
                                        album = album,
                                        duration = durationStr,
                                        durationSeconds = if (durationSecs > 0) durationSecs else 180,
                                        lyrics = localLyrics,
                                        coverColor = pickedColor,
                                        filePath = pathUriString
                                    )
                                )
                            }
                        }
                        cursor.close()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // 2. Scan via MediaStore.Files (Broad directory search including Download and Music folders)
                try {
                    val uri = MediaStore.Files.getContentUri("external")
                    val projection = arrayOf(
                        MediaStore.Files.FileColumns._ID,
                        MediaStore.Files.FileColumns.DISPLAY_NAME,
                        MediaStore.Files.FileColumns.MIME_TYPE,
                        MediaStore.Files.FileColumns.DATA,
                        MediaStore.Files.FileColumns.SIZE
                    )
                    val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE 'audio/%' OR " +
                                    "${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE '%.mp3' OR " +
                                    "${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE '%.wav' OR " +
                                    "${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE '%.m4a' OR " +
                                    "${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE '%.ogg' OR " +
                                    "${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE '%.flac' OR " +
                                    "${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE '%.aac'"

                    val cursor = context.contentResolver.query(uri, projection, selection, null, null)
                    if (cursor != null) {
                        val idCol = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)
                        val nameCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                        val mimeCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)
                        val dataCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                        val sizeCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)

                        val retriever = MediaMetadataRetriever()
                        while (cursor.moveToNext()) {
                            val id = if (idCol >= 0) cursor.getLong(idCol).toString() else ""
                            if (id.isEmpty()) continue
                            
                            val name = if (nameCol >= 0) cursor.getString(nameCol) ?: "" else ""
                            val filePath = if (dataCol >= 0) cursor.getString(dataCol) ?: "" else ""

                            val pathLower = filePath.lowercase()
                            val uniqueId = "fs_$id"
                            if (seenIds.contains(uniqueId) || seenIds.contains("ms_$id") || (filePath.isNotEmpty() && seenRawPaths.contains(pathLower))) {
                                continue
                            }

                            val mediaUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id.toLong())
                            val pathUriString = mediaUri.toString()

                            var title = name.substringBeforeLast('.', name)
                            if (title.isEmpty()) title = "Pista Sin Nombre"
                            var artist = "Artista Desconocido"
                            var album = "Álbum Desconocido"
                            var durationSeconds = 180

                            try {
                                retriever.setDataSource(context, mediaUri)
                                val rTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                                val rArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                                val rAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                                val rDurationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()

                                if (!rTitle.isNullOrBlank()) title = rTitle
                                if (!rArtist.isNullOrBlank()) artist = rArtist
                                if (!rAlbum.isNullOrBlank()) album = rAlbum
                                if (rDurationMs != null && rDurationMs > 0) {
                                    durationSeconds = (rDurationMs / 1000).toInt()
                                }
                            } catch (e: Exception) {
                                // use filename fallback
                            }

                            seenIds.add(uniqueId)
                            if (filePath.isNotEmpty()) {
                                seenRawPaths.add(pathLower)
                            }

                            val mins = durationSeconds / 60
                            val secs = durationSeconds % 60
                            val durationStr = String.format("%01d:%02d", mins, secs)

                            val localLyrics = """
                                [00:00] Reproduciendo tu archivo local (Sincronizado de Descargas)
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
                                    id = uniqueId,
                                    title = title,
                                    artist = artist,
                                    album = album,
                                    duration = durationStr,
                                    durationSeconds = durationSeconds,
                                    lyrics = localLyrics,
                                    coverColor = pickedColor,
                                    filePath = pathUriString
                                )
                            )
                        }
                        try { retriever.release() } catch (e: Exception) { }
                        cursor.close()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // 3. Scan via Direct Filesystem (Fallback for older storage setups)
                withContext(Dispatchers.IO) {
                    try {
                        val fileResults = mutableListOf<File>()
                        val roots = listOfNotNull(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                            File(Environment.getExternalStorageDirectory(), "Download"),
                            File(Environment.getExternalStorageDirectory(), "Music"),
                            File("/sdcard/Download"),
                            File("/sdcard/Music"),
                            File("/storage/emulated/0/Download"),
                            File("/storage/emulated/0/Music")
                        )

                        for (root in roots.distinctBy { it.absolutePath }) {
                            if (root.exists() && root.isDirectory) {
                                scanFilesDirectly(root, 0, fileResults)
                            }
                        }

                        // Process found files
                        val retriever = MediaMetadataRetriever()
                        for (file in fileResults) {
                            val path = file.absolutePath
                            val pathLower = path.lowercase()
                            if (!seenRawPaths.contains(pathLower)) {
                                seenRawPaths.add(pathLower)
                                var title = file.nameWithoutExtension
                                var artist = "Artista Desconocido"
                                var album = "Álbum Desconocido"
                                var durationSeconds = 180

                                try {
                                    retriever.setDataSource(path)
                                    val rTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                                    val rArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                                    val rAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                                    val rDurationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()

                                    if (!rTitle.isNullOrBlank()) title = rTitle
                                    if (!rArtist.isNullOrBlank()) artist = rArtist
                                    if (!rAlbum.isNullOrBlank()) album = rAlbum
                                    if (rDurationMs != null && rDurationMs > 0) {
                                        durationSeconds = (rDurationMs / 1000).toInt()
                                    }
                                } catch (e: Exception) {
                                    // ignore and use fallback values
                                }

                                val mins = durationSeconds / 60
                                val secs = durationSeconds % 60
                                val durationStr = String.format("%01d:%02d", mins, secs)

                                val localLyrics = """
                                    [00:00] Reproduciendo tu archivo local (Escaneado Directo)
                                    [00:08] Canción: $title
                                    [00:15] Artista: $artist
                                    [00:22] Sintonizando el pulso de tu código analógico
                                    [00:30] Sonando con fidelidad premium en Kaku Next
                                """.trimIndent()

                                val colors = listOf(0xFF00F0FF, 0xFFFF007F, 0xFFBD93F9, 0xFF4EFE80, 0xFFFFB86C, 0xFFF1FA8C)
                                val colorIdx = path.hashCode().coerceAtLeast(0) % colors.size
                                val pickedColor = colors[colorIdx]

                                val id = "fs_direct_" + path.hashCode()

                                withContext(Dispatchers.Main) {
                                    songsList.add(
                                        Song(
                                            id = id,
                                            title = title,
                                            artist = artist,
                                            album = album,
                                            duration = durationStr,
                                            durationSeconds = durationSeconds,
                                            lyrics = localLyrics,
                                            coverColor = pickedColor,
                                            filePath = path
                                        )
                                    )
                                }
                            }
                        }
                        try { retriever.release() } catch (e: Exception) { }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // Update flows on main thread
                if (songsList.isNotEmpty()) {
                    _songsFlow.value = songsList
                    com.example.service.PlaybackManager.setPlaylist(songsList)
                    com.example.service.PlaybackManager.selectSongWithoutPlaying(songsList[0])
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
            } finally {
                _isScanning.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}

data class Playlist(
    val id: String,
    val name: String,
    val description: String,
    val songs: List<Song>
)
