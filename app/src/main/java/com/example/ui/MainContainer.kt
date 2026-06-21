package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.MusicViewModel

@Composable
fun MainContainer(
    viewModel: MusicViewModel = viewModel()
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    // Configuration / Customization states from Settings
    val customTitle by viewModel.customTitle.collectAsState()
    val useCustomColors by viewModel.useCustomColors.collectAsState()
    val selectedColorVal by viewModel.selectedCustomColor.collectAsState()
    val isAmoled by viewModel.amoledScreen.collectAsState()

    val accentColor = if (useCustomColors) Color(selectedColorVal) else NeonCyan
    val bgThemeColor = MaterialTheme.colorScheme.background
    val bottomBarBgColor = if (isAmoled) Color.Black else ContainerDark

    // Define standard main routes that show bottom and top bars
    val mainRoutes = listOf("home", "songs", "playlists", "search")
    val isMainRoute = currentRoute in mainRoutes

    // Control visibility of MiniPlayer: active song, but NOT on full Player screen and ONLY on main routes
    val showMiniPlayer = currentSong != null && currentRoute != "player" && isMainRoute

    Scaffold(
        topBar = {
            // Immersive Top Header (shows ONLY on main routes so settings/player have full bleed)
            if (isMainRoute) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(64.dp)
                        .background(bgThemeColor)
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Project Title Name Info
                    Column {
                        Text(
                            text = "PROJECT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextGray,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = if (customTitle.isNotEmpty()) customTitle else "Kaku Next",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    // Clicking that badge opens the Configuración (Settings) screen as requested!
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = if (useCustomColors) listOf(accentColor, accentColor) else listOf(NeonCyan, NeonMagenta)
                                )
                            )
                            .clickable {
                                navController.navigate("settings")
                            }
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(bgThemeColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (customTitle.isNotEmpty()) customTitle.take(2).uppercase() else "KN",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = accentColor,
                                letterSpacing = (-1).sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (isMainRoute) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                ) {
                    // FLOATING MINI PLAYER
                    AnimatedVisibility(
                        visible = showMiniPlayer,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(300)
                        ) + fadeIn(),
                        exit = slideOutVertically(
                            targetOffsetY = { it },
                            animationSpec = tween(300)
                        ) + fadeOut()
                    ) {
                        currentSong?.let { song ->
                            val songAccentColor = Color(song.coverColor)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(bottomBarBgColor)
                                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                                    .clickable {
                                        navController.navigate("player") {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .testTag("mini_player_bar")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Cover art thumb
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(songAccentColor.copy(alpha = 0.2f))
                                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = songAccentColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Song Info
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "Siguiente: " + song.title,
                                            color = TextWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = song.artist,
                                            color = TextGray,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    // Interactive Mini Play/Pause button and Next button
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        // Play/Pause
                                        IconButton(
                                            onClick = { viewModel.togglePlayPause() },
                                            modifier = Modifier
                                                .size(40.dp)
                                                .testTag("mini_play_pause")
                                        ) {
                                            Icon(
                                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                                                tint = TextWhite,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        // Next track fast-action button
                                        IconButton(
                                            onClick = { viewModel.playNextSong() },
                                            modifier = Modifier
                                                .size(40.dp)
                                                .testTag("mini_next")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.SkipNext,
                                                contentDescription = "Siguiente",
                                                tint = TextWhite,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Thin white border line at the top
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.05f),
                        thickness = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // BOTTOM NAVIGATION BAR (Only visible on mainRoutes)
                    NavigationBar(
                        containerColor = bottomBarBgColor,
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bottom_nav_bar")
                    ) {
                        // Home Screen item
                        NavigationBarItem(
                            selected = currentRoute == "home",
                            onClick = {
                                navController.navigate("home") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Inicio"
                                )
                            },
                            label = { Text("Inicio") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = accentColor,
                                selectedTextColor = accentColor,
                                unselectedIconColor = TextGray,
                                unselectedTextColor = TextGray,
                                indicatorColor = accentColor.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("nav_item_home")
                        )

                        // Player Screen item (Canción) -> Shows the list of songs
                        NavigationBarItem(
                            selected = currentRoute == "songs",
                            onClick = {
                                navController.navigate("songs") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.PlayCircle,
                                    contentDescription = "Canción"
                                )
                            },
                            label = { Text("Canción") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = accentColor,
                                selectedTextColor = accentColor,
                                unselectedIconColor = TextGray,
                                unselectedTextColor = TextGray,
                                indicatorColor = accentColor.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("nav_item_player")
                        )

                        // Playlist Screen item (Lista)
                        NavigationBarItem(
                            selected = currentRoute == "playlists",
                            onClick = {
                                navController.navigate("playlists") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.QueueMusic,
                                    contentDescription = "Lista"
                                )
                            },
                            label = { Text("Lista") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = accentColor,
                                selectedTextColor = accentColor,
                                unselectedIconColor = TextGray,
                                unselectedTextColor = TextGray,
                                indicatorColor = accentColor.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("nav_item_playlists")
                        )

                        // Search Screen item (Búsqueda)
                        NavigationBarItem(
                            selected = currentRoute == "search",
                            onClick = {
                                navController.navigate("search") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Búsqueda"
                                )
                            },
                            label = { Text("Búsqueda") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = accentColor,
                                selectedTextColor = accentColor,
                                unselectedIconColor = TextGray,
                                unselectedTextColor = TextGray,
                                indicatorColor = accentColor.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("nav_item_search")
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.fillMaxSize()
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                ) { song ->
                    viewModel.selectSong(song)
                    navController.navigate("player") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
            composable("player") {
                PlayerScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            composable("songs") {
                SongsScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                ) { song ->
                    viewModel.selectSong(song)
                    navController.navigate("player") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
            composable("playlists") {
                PlaylistsScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                ) { song ->
                    viewModel.selectSong(song)
                    navController.navigate("player") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
            composable("search") {
                SearchScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                ) { song ->
                    viewModel.selectSong(song)
                    navController.navigate("player") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }

            // SETTINGS / CONFIGURACIÓN AND SUB-SCREENS DESTINATIONS
            composable("settings") {
                SettingsScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }
            composable("permissions") {
                PermissionsDetailScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }
            composable("customization") {
                PersonalizationScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }
            composable("reproductor_caratula") {
                CoverShapeScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }
            composable("control_reproductor") {
                PlayerControlStyleScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }
            composable("estilo_barra_progreso") {
                ProgressBarStyleScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }
            composable("fondo_reproductor") {
                PlayerBackgroundStyleScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }
        }
    }
}

