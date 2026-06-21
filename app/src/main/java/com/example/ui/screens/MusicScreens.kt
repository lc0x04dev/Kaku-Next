package com.example.ui.screens

import android.os.Build
import android.widget.Toast
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.model.Song
import com.example.ui.theme.*
import com.example.viewmodel.MusicViewModel
import com.example.viewmodel.Playlist

// Helper format function for duration seconds
fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%01d:%02d", mins, secs)
}

@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier,
    onSongSelected: (Song) -> Unit
) {
    val context = LocalContext.current
    val songs by viewModel.songsFlow.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val hasPermission by viewModel.hasStoragePermission.collectAsState()
    val showSyncBanner by viewModel.showSyncBanner.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO)
    } else {
        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Notificaciones de reproducción activadas", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(context) {
        // 1. Request notification permission independently on startup if we are on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasNotificationPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasNotificationPermission) {
                notificationLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // 2. Check and sync local song access status
        val permissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_MEDIA_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        viewModel.setPermissionStatus(permissionGranted)
        if (permissionGranted) {
            viewModel.scanLocalSongs(context)
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { map ->
        val storageGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            map[android.Manifest.permission.READ_MEDIA_AUDIO] == true
        } else {
            map[android.Manifest.permission.READ_EXTERNAL_STORAGE] == true
        }

        viewModel.setPermissionStatus(storageGranted)
        if (storageGranted) {
            viewModel.scanLocalSongs(context)
        }

        val toastMessage = if (storageGranted) {
            "Acceso concedido a música local"
        } else {
            "Permiso de almacenamiento denegado"
        }

        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Inicio",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.testTag("app_title_home")
                )
                Text(
                    text = "Sintonizando el pulso del código",
                    color = NeonLavender.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Permission Card Block
        if (songs.isEmpty()) {
            item {
                AnimatedContent(
                    targetState = hasPermission,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    label = "permission_banner"
                ) { status ->
                    when (status) {
                        true -> {
                            // Permission already granted
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isScanning) Color(0xFF0F1A1E)
                                        else if (songs.isEmpty()) Color(0xFF221910)
                                        else Color(0xFF0F1E19)
                                    )
                                    .border(
                                        1.dp,
                                        if (isScanning) Color(0xFF00D2FF).copy(alpha = 0.5f)
                                        else if (songs.isEmpty()) Color(0xFFFFB86C).copy(alpha = 0.5f)
                                        else Color(0xFF00FF87).copy(alpha = 0.5f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isScanning) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = Color(0xFF00D2FF),
                                                strokeWidth = 2.dp
                                            )
                                        } else if (songs.isEmpty()) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = "Sincronización Completa",
                                                tint = Color(0xFFFFB86C),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Biblioteca Sincronizada",
                                                tint = Color(0xFF00FF87),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = if (isScanning) "Sincronizando música local..."
                                                   else if (songs.isEmpty()) "Sincronización Completa"
                                                   else "Biblioteca Sincronizada",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = if (isScanning) {
                                            "Escaneando almacenamiento y buscando todo tipo de archivos de audio (MP3, M4A, WAV, FLAC, OGG, AAC)..."
                                        } else if (songs.isEmpty()) {
                                            "Kaku Next completó la búsqueda de todo tipo de archivos de audio (MP3, WAV, M4A, FLAC, OGG, AAC) en tu almacenamiento, pero no se ha detectado ninguna canción local en este momento. Intenta descargar música a tus carpetas públicas."
                                        } else {
                                            "Se han encontrado ${songs.size} canciones locales en tu dispositivo (MP3, WAV, M4A, FLAC, OGG, AAC) listas para reproducir con alta fidelidad."
                                        },
                                        color = if (isScanning) Color(0xFFB0D0FF)
                                                else if (songs.isEmpty()) Color(0xFFFFDDBB)
                                                else Color(0xFFB0C0B8),
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                        else -> {
                            // Not decided or false
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.FolderOpen,
                                            contentDescription = "Almacenamiento",
                                            tint = NeonCyan,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Accede a tu música local",
                                            color = TextWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Para reproducir tus archivos locales de audio, Kaku Next necesita el permiso de lectura multimedia.",
                                        color = TextGray,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(
                                        onClick = { launcher.launch(permissionsToRequest) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.background
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(44.dp)
                                            .testTag("request_permission_button")
                                    ) {
                                        Text(
                                            text = "Conceder permiso",
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section Title: Quick Play
        if (songs.isNotEmpty()) {
            item {
                Text(
                    text = "Pistas Populares",
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonMagenta,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Song list content
            items(songs) { song ->
                val isActive = currentSong?.id == song.id
                SongRowItem(
                    song = song,
                    isActive = isActive,
                    isPlaying = isPlaying && isActive,
                    onClick = { onSongSelected(song) },
                    onFavoriteToggle = { viewModel.toggleFavorite(song.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        } else {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Sin música",
                        tint = TextMuted,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tu biblioteca está vacía",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Las canciones escaneadas en tu dispositivo se mostrarán aquí.",
                        color = TextGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SongRowItem(
    song: Song,
    isActive: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer)
            .border(
                width = 1.dp,
                color = if (isActive) Color(song.coverColor) else Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
            .testTag("song_item_${song.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Album Art Simulation with neon glow background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(song.coverColor).copy(alpha = 0.2f))
                    .border(1.5.dp, Color(song.coverColor), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (isPlaying) {
                    // Custom micro equalizer bars animation
                    EqualizerIcon(color = Color(song.coverColor))
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Música",
                        tint = Color(song.coverColor)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Title / Artist Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = song.title,
                    color = if (isActive) Color(song.coverColor) else TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${song.artist} • ${song.album}",
                    color = TextGray,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Duration and Favorite Button
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = song.duration,
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 6.dp)
                )

                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("favorite_toggle_${song.id}")
                ) {
                    Icon(
                        imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (song.isFavorite) NeonMagenta else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// Simulated equalizer wave
@Composable
fun EqualizerIcon(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "eq_anim")
    val h1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "h3"
    )

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight(h1)
                .clip(RoundedCornerShape(1.dp))
                .background(color)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight(h2)
                .clip(RoundedCornerShape(1.dp))
                .background(color)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight(h3)
                .clip(RoundedCornerShape(1.dp))
                .background(color)
        )
    }
}

@Composable
fun PlayerScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val progressBarStyle by viewModel.progressBarStyle.collectAsState()
    val playerBackgroundStyle by viewModel.playerBackgroundStyle.collectAsState()
    val playerBackgroundPreset by viewModel.playerBackgroundPreset.collectAsState()

    if (currentSong == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text("Selecciona una pista para reproducir", color = TextGray)
        }
        return
    }

    val song = currentSong!!
    val accentColor = Color(song.coverColor)
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    val backgroundModifier = when {
        playerBackgroundStyle == "Apagado" -> {
            Modifier.background(MaterialTheme.colorScheme.background)
        }
        playerBackgroundStyle == "Fondo animado" -> {
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.15f,
                targetValue = 0.35f,
                animationSpec = infiniteRepeatable(
                    animation = tween(4000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseAlpha"
            )
            val baseAccent = if (isSystemInDarkTheme()) Color(0xFF121212) else MaterialTheme.colorScheme.background
            Modifier.background(
                Brush.radialGradient(
                    colors = listOf(accentColor.copy(alpha = pulseAlpha), baseAccent),
                    radius = 2000f
                )
            )
        }
        playerBackgroundStyle == "Static Colors" && playerBackgroundPreset == "Programar 1" -> {
            Modifier.background(Brush.verticalGradient(listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))))
        }
        playerBackgroundStyle == "Static Colors" && playerBackgroundPreset == "Programar 2" -> {
            Modifier.background(Brush.verticalGradient(listOf(Color(0xFF3A6073), Color(0xFF16222F))))
        }
        playerBackgroundStyle == "Static Colors" && playerBackgroundPreset == "Gradiente 3" -> {
            Modifier.background(Brush.verticalGradient(listOf(Color(0xFF2D1B4E), Color(0xFF160E2A))))
        }
        playerBackgroundStyle == "Static Colors" && playerBackgroundPreset == "Fondo Paisaje" -> {
            Modifier.drawBehind {
                drawRect(color = Color(0xFF0B1028))
                drawCircle(color = Color.White.copy(alpha = 0.4f), center = androidx.compose.ui.geometry.Offset(100f, 150f), radius = 2f)
                drawCircle(color = Color.White.copy(alpha = 0.6f), center = androidx.compose.ui.geometry.Offset(250f, 320f), radius = 3f)
                drawCircle(color = Color.White.copy(alpha = 0.3f), center = androidx.compose.ui.geometry.Offset(450f, 180f), radius = 1.8f)
                drawCircle(color = Color.White.copy(alpha = 0.7f), center = androidx.compose.ui.geometry.Offset(600f, 250f), radius = 2.5f)
                drawCircle(color = Color.White.copy(alpha = 0.5f), center = androidx.compose.ui.geometry.Offset(800f, 120f), radius = 2.1f)
                
                drawCircle(
                    color = Color(0xFFFFEB3B).copy(alpha = 0.05f),
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.75f, 250f),
                    radius = 120f
                )
                drawCircle(
                    color = Color(0xFFECEFF1).copy(alpha = 0.8f),
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.75f, 250f),
                    radius = 35f
                )
                drawCircle(
                    color = Color(0xFF0B1028),
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.75f - 10f, 250f),
                    radius = 35f
                )

                val backPath = androidx.compose.ui.graphics.Path()
                backPath.moveTo(0f, size.height)
                backPath.lineTo(size.width * 0.2f, size.height * 0.45f)
                backPath.lineTo(size.width * 0.55f, size.height * 0.62f)
                backPath.lineTo(size.width * 0.85f, size.height * 0.4f)
                backPath.lineTo(size.width, size.height * 0.55f)
                backPath.lineTo(size.width, size.height)
                backPath.close()
                drawPath(path = backPath, color = Color(0xFF121B3A))

                val frontPath = androidx.compose.ui.graphics.Path()
                frontPath.moveTo(0f, size.height)
                frontPath.lineTo(size.width * 0.4f, size.height * 0.57f)
                frontPath.lineTo(size.width * 0.75f, size.height * 0.52f)
                frontPath.lineTo(size.width, size.height * 0.65f)
                frontPath.lineTo(size.width, size.height)
                frontPath.close()
                drawPath(path = frontPath, color = Color(0xFF1E284C))
            }
        }
        else -> {
            Modifier.background(MaterialTheme.colorScheme.background)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .then(backgroundModifier)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Segmented selector for Cover/Lyrics View
        Row(
            modifier = Modifier
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val options = listOf("Portada", "Letra")
            options.forEachIndexed { index, title ->
                val selected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (selected) MaterialTheme.colorScheme.primary else TextGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Horizontal Pager container
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("player_pager")
        ) { page ->
            if (page == 0) {
                // Page 0: Cover artwork/image
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Atmospheric glowing vinyl/album art cover (Immersive design spec)
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Glow backdrop aura
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            Color.Transparent,
                                            NeonMagenta.copy(alpha = 0.15f)
                                        )
                                    )
                                )
                        )

                        // Main outer core
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            // Inner card disc frame
                            Box(
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFF1C1C1C), Color(0xFF0C0C0C))
                                        )
                                    )
                                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = "Music Note",
                                    tint = accentColor.copy(alpha = 0.5f),
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // Page 1: Lyrics list Box
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // LYRICS CONTAINER (Verified Lyrics box with absolute minimum borders)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer) // bg-[#0C0C0C]
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp)) // border border-white/5
                            .padding(20.dp)
                            .testTag("lyrics_container")
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Lyrics Verified",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified Icon",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            if (!song.lyrics.isNullOrBlank()) {
                                LazyColumn(
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    items(song.lyrics.split("\n")) { line ->
                                        val lineClean = line.trim()
                                        if (lineClean.isNotEmpty()) {
                                            // Match line containing '[' or ']' or styled specially
                                            val isHighlighted = lineClean.contains("[") || lineClean.contains("]") || lineClean.contains("código") || lineClean.contains("silencio")
                                            Text(
                                                text = lineClean.replace("[", "").replace("]", ""),
                                                color = if (isHighlighted) Color.White else TextGray,
                                                fontSize = if (isHighlighted) 17.sp else 14.sp,
                                                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
                                                textAlign = TextAlign.Center,
                                                lineHeight = if (isHighlighted) 22.sp else 19.sp,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SearchOff,
                                            contentDescription = "Sin Letra",
                                            tint = TextMuted,
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "No hay letra disponible para esta canción.",
                                            color = TextGray,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 16.dp).testTag("no_lyrics_message")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Title and Artist Info (aligned left / start as per mock-up)
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = song.title,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-0.5).sp,
                modifier = Modifier.testTag("player_song_title")
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${song.artist} — ${song.album}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = NeonLavender.copy(alpha = 0.8f) // matching text-[#E0B0FF] opacity-80
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Progress Slider & Timers
        Column(modifier = Modifier.fillMaxWidth()) {
            if (progressBarStyle == "Por defecto") {
                Slider(
                    value = currentPosition.toFloat(),
                    onValueChange = { viewPosition ->
                        viewModel.seekTo(viewPosition.toInt())
                    },
                    valueRange = 0f..song.durationSeconds.toFloat(),
                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("playback_slider")
                )
            } else {
                CustomInteractiveSlider(
                    value = currentPosition.toFloat(),
                    onValueChange = { viewPosition ->
                        viewModel.seekTo(viewPosition.toInt())
                    },
                    valueRange = 0f..song.durationSeconds.toFloat(),
                    styleName = progressBarStyle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("playback_slider")
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(currentPosition),
                    fontSize = 11.sp,
                    color = TextGray
                )
                Text(
                    text = song.duration,
                    fontSize = 11.sp,
                    color = TextGray
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Controls (Forward, Backward, Play/Pause, Favorite)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 36.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Favorite Button
            IconButton(
                onClick = { viewModel.toggleFavorite(song.id) },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorito",
                    tint = if (song.isFavorite) NeonMagenta else TextWhite,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Skip Previous
            IconButton(
                onClick = { viewModel.playPreviousSong() },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("skip_prev_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Anterior",
                    tint = TextWhite,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Floating Play / Pause Large Neon Button
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(accentColor, NeonMagenta)
                        )
                    )
                    .clickable(onClick = { viewModel.togglePlayPause() })
                    .testTag("play_pause_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                    tint = DeepDark,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Skip Next
            IconButton(
                onClick = { viewModel.playNextSong() },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("skip_next_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Siguiente",
                    tint = TextWhite,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Queue/Equalizer Icon decorative
            IconButton(
                onClick = {},
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.PlaylistPlay,
                    contentDescription = "Cola",
                    tint = TextWhite,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
fun PlaylistsScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier,
    onSongSelected: (Song) -> Unit
) {
    val playlists by viewModel.playlistsFlow.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }

    AnimatedContent(
        targetState = selectedPlaylist,
        transitionSpec = {
            if (targetState != null) {
                slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
            } else {
                slideInHorizontally { width -> -width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> width } + fadeOut()
            }
        },
        label = "playlist_transition",
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) { playlist ->
        if (playlist == null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp)
            ) {
                item {
                    Text(
                        text = "Listas de Reproducción",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 18.dp)
                    )
                }

                if (playlists.isEmpty()) {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 60.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QueueMusic,
                                contentDescription = "Sin listas",
                                tint = TextGray,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Sin listas de reproducción",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Permite acceso a tus archivos de audio locales para sincronizar y comenzar.",
                                color = TextGray,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                } else {
                    items(playlists) { item ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable { selectedPlaylist = item }
                                .padding(16.dp)
                                .testTag("playlist_item_${item.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(NeonMagenta, NeonLavender)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QueueMusic,
                                        contentDescription = null,
                                        tint = DeepDark,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = item.description,
                                        color = TextGray,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${item.songs.size} pistas",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Ver lista",
                                    tint = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Playlist detail view
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                // Back button row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    IconButton(
                        onClick = { selectedPlaylist = null },
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("playlist_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextWhite
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ver biblioteca",
                        color = NeonLavender,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Playlist Banner Details
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(NeonMagenta, MaterialTheme.colorScheme.primary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = null,
                            tint = DeepDark,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = playlist.name,
                            color = TextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = playlist.description,
                            color = TextGray,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${playlist.songs.size} canciones en total",
                            color = NeonMagenta,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // List of songs within this playlist
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(playlist.songs) { song ->
                        val isActive = currentSong?.id == song.id
                        SongRowItem(
                            song = song,
                            isActive = isActive,
                            isPlaying = isPlaying && isActive,
                            onClick = { onSongSelected(song) },
                            onFavoriteToggle = { viewModel.toggleFavorite(song.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier,
    onSongSelected: (Song) -> Unit
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val songs by viewModel.songsFlow.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    val filteredSongs = remember(searchQuery, songs) {
        if (searchQuery.isBlank()) {
            songs
        } else {
            songs.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.artist.contains(searchQuery, ignoreCase = true) ||
                        it.album.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Buscar",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Custom Cyberspace Styled Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_input_field"),
            placeholder = { Text("Buscar por canción, artista o álbum...", color = TextMuted) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.updateSearchQuery("") },
                        modifier = Modifier.testTag("clear_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Limpiar",
                            tint = NeonMagenta
                        )
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primaryContainer,
                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Search Results Or Empty State
        if (filteredSongs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = "Sin resultados",
                        tint = TextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (songs.isEmpty()) {
                        Text(
                            text = "Biblioteca sin sincronizar",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Concede acceso al almacenamiento para sincronizar y reproducir tus canciones.",
                            color = TextGray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    } else {
                        Text(
                            text = "Sin pistas para \"$searchQuery\"",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Prueba con un término o palabra clave diferente.",
                            color = TextGray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                item {
                    Text(
                        text = if (searchQuery.isEmpty()) "Todas las pistas" else "Resultados de la búsqueda",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(filteredSongs) { song ->
                    val isActive = currentSong?.id == song.id
                    SongRowItem(
                        song = song,
                        isActive = isActive,
                        isPlaying = isPlaying && isActive,
                        onClick = { onSongSelected(song) },
                        onFavoriteToggle = { viewModel.toggleFavorite(song.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun SongsScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier,
    onSongSelected: (Song) -> Unit
) {
    val songs by viewModel.songsFlow.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Text(
            text = "Canciones",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Tu biblioteca de pistas locales",
            color = NeonLavender.copy(alpha = 0.8f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        if (songs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Sin música",
                        tint = TextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Biblioteca sin sincronizar",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Concede acceso al almacenamiento desde el Inicio para sincronizar tus canciones locales y reproducirlas.",
                        color = TextGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                item {
                    Text(
                        text = "Todas tus pistas locales (${songs.size})",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                items(songs) { song ->
                    val isActive = currentSong?.id == song.id
                    SongRowItem(
                        song = song,
                        isActive = isActive,
                        isPlaying = isPlaying && isActive,
                        onClick = { onSongSelected(song) },
                        onFavoriteToggle = { viewModel.toggleFavorite(song.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomInteractiveSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    styleName: String,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        contentAlignment = Alignment.Center
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val rangeLen = valueRange.endInclusive - valueRange.start
        val progress = if (rangeLen > 0) (value - valueRange.start) / rangeLen else 0f
        val currentProgress = progress.coerceIn(0f, 1f)

        val haptic = LocalHapticFeedback.current
        var isDragging by remember { mutableStateOf(false) }

        val interactionModifier = Modifier.pointerInput(widthPx, rangeLen) {
            detectTapGestures(
                onPress = { offset ->
                    val tappedProgress = (offset.x / widthPx).coerceIn(0f, 1f)
                    val newValue = valueRange.start + tappedProgress * rangeLen
                    onValueChange(newValue)
                }
            )
        }.pointerInput(widthPx, rangeLen) {
            detectHorizontalDragGestures(
                onDragStart = { isDragging = true },
                onDragEnd = { isDragging = false },
                onDragCancel = { isDragging = false },
                onHorizontalDrag = { change, dragAmount ->
                    change.consume()
                    val currentPx = currentProgress * widthPx
                    val nextPx = (currentPx + dragAmount).coerceIn(0f, widthPx)
                    val nextProgress = nextPx / widthPx
                    val newValue = valueRange.start + nextProgress * rangeLen
                    onValueChange(newValue)
                }
            )
        }

        val infiniteTransition = rememberInfiniteTransition(label = "prog_live")
        val phase by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2f * Math.PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "phase"
        )

        Canvas(modifier = Modifier.fillMaxSize().then(interactionModifier)) {
            val width = size.width
            val height = size.height
            val centerY = height / 2f
            val activeWidth = width * currentProgress

            when (styleName) {
                "Estilo 1" -> {
                    drawLine(
                        color = Color.White.copy(alpha = 0.15f),
                        start = androidx.compose.ui.geometry.Offset(0f, centerY),
                        end = androidx.compose.ui.geometry.Offset(width, centerY),
                        strokeWidth = 6.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFFFF5722),
                        start = androidx.compose.ui.geometry.Offset(0f, centerY),
                        end = androidx.compose.ui.geometry.Offset(activeWidth, centerY),
                        strokeWidth = 6.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    drawRect(
                        color = Color.White,
                        topLeft = androidx.compose.ui.geometry.Offset(activeWidth - 4.dp.toPx(), centerY - 8.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(8.dp.toPx(), 16.dp.toPx())
                    )
                }
                "Estilo 2" -> {
                    val path = androidx.compose.ui.graphics.Path()
                    path.moveTo(0f, centerY)
                    for (x in 0..width.toInt()) {
                        val progressRatio = x / width
                        val envelope = if (progressRatio < currentProgress) {
                            progressRatio / (currentProgress.coerceAtLeast(0.01f))
                        } else {
                            (1f - progressRatio) / ((1f - currentProgress).coerceAtLeast(0.01f))
                        }
                        val y = centerY + Math.sin(x * 0.05 + phase).toFloat() * 10.dp.toPx() * envelope
                        path.lineTo(x.toFloat(), y)
                    }
                    drawPath(
                        path = path,
                        color = Color(0xFF8A2BE2),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = Color.White,
                        center = androidx.compose.ui.geometry.Offset(activeWidth, centerY),
                        radius = 6.dp.toPx()
                    )
                }
                "Estilo 3" -> {
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = androidx.compose.ui.geometry.Offset(0f, centerY),
                        end = androidx.compose.ui.geometry.Offset(width, centerY),
                        strokeWidth = 8.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFF00FFCC),
                        start = androidx.compose.ui.geometry.Offset(0f, centerY),
                        end = androidx.compose.ui.geometry.Offset(activeWidth, centerY),
                        strokeWidth = 8.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    drawLine(
                        color = Color.White,
                        start = androidx.compose.ui.geometry.Offset(0f, centerY),
                        end = androidx.compose.ui.geometry.Offset(activeWidth, centerY),
                        strokeWidth = 2.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
                "Estilo 4" -> {
                    val path = androidx.compose.ui.graphics.Path()
                    path.moveTo(0f, centerY)
                    for (x in 0..width.toInt()) {
                        var y = centerY
                        if (x < activeWidth) {
                            y = centerY + Math.sin(x * 0.04 - phase).toFloat() * 6.dp.toPx()
                        }
                        path.lineTo(x.toFloat(), y)
                    }
                    drawPath(
                        path = path,
                        color = Color(0xFFFF5722).copy(alpha = 0.4f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )

                    val activePath = androidx.compose.ui.graphics.Path()
                    activePath.moveTo(0f, centerY)
                    for (x in 0..activeWidth.toInt()) {
                        val y = centerY + Math.sin(x * 0.04 - phase).toFloat() * 6.dp.toPx()
                        activePath.lineTo(x.toFloat(), y)
                    }
                    drawPath(
                        path = activePath,
                        color = Color(0xFFFF5722),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )
                    drawCircle(
                        color = Color.White,
                        center = androidx.compose.ui.geometry.Offset(activeWidth, centerY + Math.sin(activeWidth * 0.04 - phase).toFloat() * 6.dp.toPx()),
                        radius = 6.dp.toPx()
                    )
                }
                "Barco de papel" -> {
                    val path = androidx.compose.ui.graphics.Path()
                    path.moveTo(0f, centerY)
                    for (x in 0..width.toInt()) {
                        val y = centerY + 3.dp.toPx() + Math.sin(x * 0.03 + phase).toFloat() * 5.dp.toPx()
                        path.lineTo(x.toFloat(), y)
                    }
                    drawPath(
                        path = path,
                        color = Color.White.copy(alpha = 0.15f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                    )

                    val activePath = androidx.compose.ui.graphics.Path()
                    activePath.moveTo(0f, centerY)
                    for (x in 0..activeWidth.toInt()) {
                        val y = centerY + 3.dp.toPx() + Math.sin(x * 0.03 + phase).toFloat() * 5.dp.toPx()
                        activePath.lineTo(x.toFloat(), y)
                    }
                    drawPath(
                        path = activePath,
                        color = Color(0xFF00E5FF),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                    )

                    val boatY = centerY + 3.dp.toPx() + Math.sin(activeWidth * 0.03 + phase).toFloat() * 5.dp.toPx()
                    val boatPath = androidx.compose.ui.graphics.Path()
                    boatPath.moveTo(activeWidth - 10.dp.toPx(), boatY)
                    boatPath.lineTo(activeWidth - 6.dp.toPx(), boatY + 4.dp.toPx())
                    boatPath.lineTo(activeWidth + 6.dp.toPx(), boatY + 4.dp.toPx())
                    boatPath.lineTo(activeWidth + 10.dp.toPx(), boatY)
                    boatPath.lineTo(activeWidth, boatY - 6.dp.toPx())
                    boatPath.close()

                    drawPath(path = boatPath, color = Color.White)
                }
                "Estilo 6 (Nave OVNI)" -> {
                    drawLine(
                        color = Color.White.copy(alpha = 0.15f),
                        start = androidx.compose.ui.geometry.Offset(0f, centerY),
                        end = androidx.compose.ui.geometry.Offset(width, centerY),
                        strokeWidth = 6.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFFFF5722),
                        start = androidx.compose.ui.geometry.Offset(0f, centerY),
                        end = androidx.compose.ui.geometry.Offset(activeWidth, centerY),
                        strokeWidth = 6.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )

                    val ufoHover = Math.sin(phase.toDouble()).toFloat() * 3.dp.toPx()
                    val ufoX = activeWidth
                    val ufoY = centerY - 15.dp.toPx() + ufoHover

                    val beamPath = androidx.compose.ui.graphics.Path()
                    beamPath.moveTo(ufoX - 4.dp.toPx(), ufoY + 2.dp.toPx())
                    beamPath.lineTo(ufoX - 12.dp.toPx(), centerY)
                    beamPath.lineTo(ufoX + 12.dp.toPx(), centerY)
                    beamPath.lineTo(ufoX + 4.dp.toPx(), ufoY + 2.dp.toPx())
                    beamPath.close()
                    drawPath(path = beamPath, color = Color(0xFF00FFCC).copy(alpha = 0.25f))

                    drawOval(
                        color = Color(0xFF78909C),
                        topLeft = androidx.compose.ui.geometry.Offset(ufoX - 12.dp.toPx(), ufoY - 2.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(24.dp.toPx(), 6.dp.toPx())
                    )
                    drawOval(
                        color = Color(0xFFE0F7FA),
                        topLeft = androidx.compose.ui.geometry.Offset(ufoX - 6.dp.toPx(), ufoY - 6.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(12.dp.toPx(), 6.dp.toPx())
                    )
                    drawCircle(
                        color = Color(0xFF00FFCC),
                        center = androidx.compose.ui.geometry.Offset(ufoX, ufoY + 1.dp.toPx()),
                        radius = 2.dp.toPx()
                    )
                }
            }
        }
    }
}

