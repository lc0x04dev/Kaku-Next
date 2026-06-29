package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.ui.theme.*
import com.example.viewmodel.MusicViewModel

@Composable
fun Modifier.scaleOnPress(interactionSource: MutableInteractionSource): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale_on_press"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

// Custom Squircle/Star shape that matches the "Por defecto" setting shape in Image 7 preview art!
val SquircleStarShape: Shape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val numPoints = 8
    val outerRadius = w / 2f
    val innerRadius = outerRadius * 0.82f
    val centerX = w / 2f
    val centerY = h / 2f
    
    moveTo(centerX + outerRadius, centerY)
    for (i in 1 until numPoints * 2) {
        val angle = i * Math.PI / numPoints
        val r = if (i % 2 == 0) outerRadius else innerRadius
        lineTo(
            (centerX + r * Math.cos(angle)).toFloat(),
            (centerY + r * Math.sin(angle)).toFloat()
        )
    }
    close()
}

@Composable
fun SettingsScreen(
    viewModel: MusicViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioHiFi by viewModel.audioHiFi.collectAsState()
    val isAmoled by viewModel.amoledScreen.collectAsState()
    
    val NeonCyan = MaterialTheme.colorScheme.primary
    val DeepDark = MaterialTheme.colorScheme.background
    val CardDark = MaterialTheme.colorScheme.primaryContainer
    val SurfaceDark = MaterialTheme.colorScheme.surface
    val bgThemeColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val cardColor = MaterialTheme.colorScheme.primaryContainer

    var showAboutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(64.dp)
                    .background(bgThemeColor)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.testTag("settings_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Configuración",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        containerColor = bgThemeColor,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp)
        ) {
            // CATEGORY: GENERAL
            item {
                Text(
                    text = "General",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardColor)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                ) {
                    // Item: Audio Hi-Fi with Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setAudioHiFi(!audioHiFi) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.05f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Audio Hi-Fi",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Activar detección e insignias Hi-Fi",
                                    color = TextGray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                        Switch(
                            checked = audioHiFi,
                            onCheckedChange = { viewModel.setAudioHiFi(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeonCyan,
                                uncheckedThumbColor = TextGray,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    // Item: Personalización Navigation button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("customization") }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Personalización",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Personaliza el estilo de la app, el título y los colores",
                                color = TextGray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    // Item: Idioma
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                Toast.makeText(context, "Idioma sistema guardado correctamente", Toast.LENGTH_SHORT).show()
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Idioma",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Sistema",
                                color = TextGray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            // CATEGORY: SEGURIDAD
            item {
                Text(
                    text = "Seguridad",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardColor)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("permissions") }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Permisos",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Información sobre los permisos utilizados",
                                color = TextGray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            // CATEGORY: ACERCA DE
            item {
                Text(
                    text = "Acerca de",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardColor)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAboutDialog = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Acerca de",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // ABOUT DIALOG
    if (showAboutDialog) {
        Dialog(onDismissRequest = { showAboutDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardColor)
                    .border(1.dp, NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // App Logo Real Asset (Wavy Badge)
                    Box(
                        modifier = Modifier
                            .size(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_kaku_logo_light),
                            contentDescription = "Logo Kaku Next",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Kaku Next",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "Versión v2.7.8-dev.1-test",
                        color = TextGray,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Un reproductor moderno y de alta fidelidad con efectos de inmersión completos y ecualizador holográfico interactivo.",
                        color = TextWhite,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showAboutDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = DeepDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Cerrar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionsDetailScreen(
    viewModel: MusicViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val isAmoled by viewModel.amoledScreen.collectAsState()
    val NeonCyan = MaterialTheme.colorScheme.primary
    val DeepDark = MaterialTheme.colorScheme.background
    val CardDark = MaterialTheme.colorScheme.primaryContainer
    val SurfaceDark = MaterialTheme.colorScheme.surface
    val bgThemeColor = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.primaryContainer

    val permissionsList = listOf(
        PermissionItem(
            title = "Grabar Audio",
            description = "Necesario para que el visualizador de audio procese y muestre datos de frecuencia en tiempo real.",
            icon = Icons.Default.Mic
        ),
        PermissionItem(
            title = "Bluetooth",
            description = "Requerido para detectar y gestionar conexiones con dispositivos de audio bluetooth.",
            icon = Icons.Default.Bluetooth
        ),
        PermissionItem(
            title = "Notificaciones",
            description = "Requerido para mostrar controles de reproducción e información en la barra de notificaciones.",
            icon = Icons.Default.Notifications
        ),
        PermissionItem(
            title = "Acceso al Almacenamiento",
            description = "Necesario para escanear archivos de música y gestionar carpetas en tu dispositivo.",
            icon = Icons.Default.Folder
        ),
        PermissionItem(
            title = "Acceso a la carpeta de música",
            description = "Selecciona tu carpeta de música para permitir que Lune organice y gestione tu biblioteca de forma segura.",
            icon = Icons.Default.FolderOpen
        )
    )

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(64.dp)
                    .background(bgThemeColor)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Permisos",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        containerColor = bgThemeColor,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp)
        ) {
            items(permissionsList) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardColor)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.description,
                            color = TextGray,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

data class PermissionItem(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun PersonalizationScreen(
    viewModel: MusicViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    val currentTitle by viewModel.customTitle.collectAsState()
    val customSections by viewModel.customSections.collectAsState()
    val useCustomColors by viewModel.useCustomColors.collectAsState()
    val selectedColorVal by viewModel.selectedCustomColor.collectAsState()
    val isAmoled by viewModel.amoledScreen.collectAsState()
    
    val opticalVibration by viewModel.opticalVibration.collectAsState()
    val songInfo by viewModel.songInfo.collectAsState()
    val cinematicPlayer by viewModel.cinematicPlayer.collectAsState()
    val progressBarStyle by viewModel.progressBarStyle.collectAsState()
    val playerBackgroundStyle by viewModel.playerBackgroundStyle.collectAsState()

    val NeonCyan = MaterialTheme.colorScheme.primary
    val DeepDark = MaterialTheme.colorScheme.background
    val CardDark = MaterialTheme.colorScheme.primaryContainer
    val SurfaceDark = MaterialTheme.colorScheme.surface
    val bgThemeColor = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.primaryContainer

    var showEditTitleDialog by remember { mutableStateOf(false) }
    var tempTitle by remember { mutableStateOf(currentTitle) }

    val presetColors = listOf(
        0xFF8A2BE2, // Purple
        0xFFFF5722, // Coral
        0xFF4CAF50, // Green
        0xFF03A9F4, // Sky Blue
        0xFF9C27B0, // Violet
        0xFFFFC107  // Amber
    )

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(64.dp)
                    .background(bgThemeColor)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Personalización",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        containerColor = bgThemeColor,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp)
        ) {
            // CATEGORY: GENERAL
            item {
                Text(
                    text = "General",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardColor)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                ) {
                    // Título personalizado
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                tempTitle = currentTitle
                                showEditTitleDialog = true
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Título personalizado",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = currentTitle,
                                color = TextGray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    // Personalizar secciones
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setCustomSections(!customSections) 
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.05f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ViewAgenda,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Personalizar secciones",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Elige qué pestañas de sección mostrar",
                                    color = TextGray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                        Switch(
                            checked = customSections,
                            onCheckedChange = { 
                                if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setCustomSections(it) 
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeonCyan,
                                uncheckedThumbColor = TextGray,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    // Usar colores personalizados
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setUseCustomColors(!useCustomColors) 
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.05f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ColorLens,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Usar colores personalizados",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Sobrescribe el color dinámico con colores preestablecidos",
                                    color = TextGray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                        Switch(
                            checked = useCustomColors,
                            onCheckedChange = { 
                                if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setUseCustomColors(it) 
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeonCyan,
                                uncheckedThumbColor = TextGray,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                    }

                    // Paleta de colores (expands if true)
                    AnimatedVisibility(
                        visible = useCustomColors,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.02f))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Paleta de colores",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextGray,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                presetColors.forEach { colorVal ->
                                    val color = Color(colorVal)
                                    val isPicked = selectedColorVal == colorVal
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .clickable {
                                                if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                viewModel.setSelectedCustomColor(colorVal)
                                            }
                                            .border(
                                                width = if (isPicked) 3.dp else 0.dp,
                                                color = Color.White,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isPicked) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    // Pantalla AMOLED (Negro puro)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setAmoledScreen(!isAmoled) 
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.05f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BrightnessLow,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Pantalla AMOLED (Negro puro)",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Usa fondo negro puro en el modo oscuro",
                                    color = TextGray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                        Switch(
                            checked = isAmoled,
                            onCheckedChange = { 
                                if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setAmoledScreen(it) 
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeonCyan,
                                uncheckedThumbColor = TextGray,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }

            // CATEGORY: REPRODUCTOR MULTIMEDIA
            item {
                Text(
                    text = "Reproductor multimedia",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardColor)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                ) {
                    // Vibración óptica
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                if (!opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setOpticalVibration(!opticalVibration) 
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.05f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Vibration,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Vibración óptica",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Vibración ligera al usar los controles del reproductor",
                                    color = TextGray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                        Switch(
                            checked = opticalVibration,
                            onCheckedChange = { 
                                if (it) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setOpticalVibration(it) 
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeonCyan,
                                uncheckedThumbColor = TextGray,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    // Información de Canción
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setSongInfo(!songInfo) 
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.05f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Audiotrack,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Información de Canción",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Muestra el formato y la tasa de bits en el reproductor y la lista",
                                    color = TextGray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                        Switch(
                            checked = songInfo,
                            onCheckedChange = { 
                                if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setSongInfo(it) 
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeonCyan,
                                uncheckedThumbColor = TextGray,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    // Reproductor Cinemático
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setCinematicPlayer(!cinematicPlayer) 
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.05f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MovieFilter,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Reproductor Cinemático",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Habilita una interfaz inmersiva con carátula en movimiento y degradado suave.",
                                    color = TextGray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                        Switch(
                            checked = cinematicPlayer,
                            onCheckedChange = { 
                                if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setCinematicPlayer(it) 
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeonCyan,
                                uncheckedThumbColor = TextGray,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    // Reproductor de carátula button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("reproductor_caratula") }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RoundedCorner,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Reproductor de carátula",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Personaliza la forma, tamaño y rotación de la carátula",
                                color = TextGray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    // Control del reproductor button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("control_reproductor") }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircleOutline,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Control del reproductor",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Personaliza los botones anterior y siguiente",
                                color = TextGray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            // CATEGORY: ESTILO DEL REPRODUCTOR
            item {
                Text(
                    text = "Estilo del reproductor",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardColor)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                ) {
                    // Diseño del reproductor
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                Toast.makeText(context, "Diseño establecido a Nuevo diseño", Toast.LENGTH_SHORT).show()
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Diseño del reproductor",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Nuevo diseño",
                                color = TextGray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    // Estilo de barra de progreso
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("estilo_barra_progreso") }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Estilo de barra de progreso",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = progressBarStyle,
                                color = TextGray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    // Estilo de fondo
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("fondo_reproductor") }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Estilo de fondo",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (playerBackgroundStyle == "Apagado") "Apagado" else playerBackgroundStyle,
                                color = TextGray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            // CATEGORY: OTROS
            item {
                Text(
                    text = "Otros",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardColor)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                ) {
                    // Gestos
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                Toast.makeText(context, "Configuración de gestos por defecto", Toast.LENGTH_SHORT).show()
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gesture,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Gestos",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Configuracion de gestos",
                                color = TextGray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    // Desenfoque
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                Toast.makeText(context, "Configurar el efecto de desenfoque de fondo", Toast.LENGTH_SHORT).show()
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BlurOn,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Desenfoque",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Configurar el efecto de desenfoque de fondo",
                                color = TextGray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // DIALOG: EDIT TITLE
    if (showEditTitleDialog) {
        Dialog(onDismissRequest = { showEditTitleDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(cardColor)
                    .border(1.dp, NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "Título personalizado",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = tempTitle,
                        onValueChange = { tempTitle = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedContainerColor = bgThemeColor,
                            unfocusedContainerColor = bgThemeColor
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showEditTitleDialog = false }) {
                            Text("Cancelar", color = TextGray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.setCustomTitle(tempTitle)
                                showEditTitleDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan,
                                contentColor = DeepDark
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Guardar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CoverShapeScreen(
    viewModel: MusicViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isAmoled by viewModel.amoledScreen.collectAsState()
    val opticalVibration by viewModel.opticalVibration.collectAsState()

    val coverShape by viewModel.coverShape.collectAsState()
    val coverSize by viewModel.coverSize.collectAsState()
    val coverDesign by viewModel.coverDesign.collectAsState()
    val coverAnimation by viewModel.coverAnimation.collectAsState()
    val coverColorMode by viewModel.coverColorMode.collectAsState()
    val selectedCustomColor by viewModel.selectedCustomColor.collectAsState()

    val NeonCyan = MaterialTheme.colorScheme.primary
    val NeonMagenta = Color(0xFFFF4081)
    val DeepDark = MaterialTheme.colorScheme.background
    val CardDark = MaterialTheme.colorScheme.primaryContainer
    val SurfaceDark = MaterialTheme.colorScheme.surface
    val bgThemeColor = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.primaryContainer
    
    val customColor = Color(selectedCustomColor)

    val finalPercent = when (coverSize) {
        "70%" -> 0.7f
        "85%" -> 0.85f
        else -> 1f
    }

    val artworkShape = when (coverShape) {
        "Cuadrada" -> RoundedCornerShape(0.dp)
        "Circular" -> CircleShape
        else -> SquircleStarShape
    }

    // Dynamic cover colors computation
    val coverDesignColors = when (coverColorMode) {
        "Cian Eléctrico" -> listOf(Color(0xFF00E5FF), Color(0xFF00B0FF))
        "Verde Neón" -> listOf(Color(0xFF39FF14), Color(0xFF00FF87))
        "Magenta Vibrante" -> listOf(Color(0xFFE0115F), Color(0xFFFF4081))
        "Personalizado" -> listOf(customColor, customColor.copy(alpha = 0.6f))
        "RGB Animado (Arcoíris)" -> {
            val transition = rememberInfiniteTransition(label = "rgb_cov_preview")
            val animatedHue by transition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(6000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "hue"
            )
            listOf(
                Color.hsv(animatedHue, 0.85f, 1f),
                Color.hsv((animatedHue + 120f) % 360f, 0.85f, 1f),
                Color.hsv((animatedHue + 240f) % 360f, 0.85f, 1f)
            )
        }
        else -> listOf(NeonCyan, Color(0xFFFF007F))
    }

    // Cover Animation setup for preview
    val transition = rememberInfiniteTransition(label = "cover_preview_anim")
    val rotationAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val pulseScale by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val finalScale = when (coverAnimation) {
        "Efecto Pulso (Beats)" -> pulseScale
        "Escalar al Reproducir" -> 1.05f
        else -> 1f
    }
    
    val finalRotation = when (coverAnimation) {
        "Rotación de Vinilo" -> rotationAngle
        else -> 0f
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(64.dp)
                    .background(bgThemeColor)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reproductor de carátula",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        containerColor = bgThemeColor,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            // PREVIEW BOX
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Vista previa del reproductor",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(cardColor)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Artwork Layout based on coverDesign
                            Box(
                                modifier = Modifier
                                    .size(170.dp)
                                    .graphicsLayer {
                                        scaleX = finalScale
                                        scaleY = finalScale
                                        rotationZ = finalRotation
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (coverDesign == "Vinilo Retro") {
                                    // Protruding Vinyl record background
                                    Box(
                                        modifier = Modifier
                                            .size(150.dp)
                                            .offset(x = 25.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black)
                                            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Vinyl grooves
                                        Box(
                                            modifier = Modifier
                                                .size(110.dp)
                                                .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(70.dp)
                                                .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                                        )
                                    }
                                }

                                // Main artwork frame
                                Box(
                                    modifier = Modifier
                                        .size(150.dp * finalPercent)
                                        .clip(artworkShape)
                                        .background(Brush.linearGradient(colors = coverDesignColors))
                                        .then(
                                            if (coverDesign == "Borde Brillante (Neón)") {
                                                Modifier.padding(4.dp)
                                            } else {
                                                Modifier.padding(1.dp)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(artworkShape)
                                            .background(
                                                if (coverDesign == "Tarjeta Minimalista") {
                                                    cardColor.copy(alpha = 0.95f)
                                                } else {
                                                    cardColor.copy(alpha = 0.8f)
                                                }
                                            )
                                            .then(
                                                if (coverDesign == "Tarjeta Minimalista") {
                                                    Modifier.border(2.dp, Color.White.copy(alpha = 0.1f), artworkShape)
                                                } else {
                                                    Modifier
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = coverDesignColors.first(),
                                            modifier = Modifier.size(56.dp * finalPercent)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Below info card row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.02f))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Starlight Sonata",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Lune MrDemonc",
                                        color = TextGray,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                    ,
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = DeepDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SHAPE SELECTION
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Forma de la carátula",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val shapes = listOf(
                            ShapeOption("Por defecto", SquircleStarShape),
                            ShapeOption("Cuadrada", RoundedCornerShape(10.dp)),
                            ShapeOption("Circular", CircleShape)
                        )

                        shapes.forEach { option ->
                            val selected = coverShape == option.name
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (selected) coverDesignColors.first().copy(alpha = 0.1f) else cardColor)
                                    .border(
                                        width = 1.dp,
                                        color = if (selected) coverDesignColors.first() else Color.White.copy(alpha = 0.05f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.setCoverShape(option.name)
                                    }
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(option.shape)
                                            .background(if (selected) coverDesignColors.first() else TextGray)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = option.name,
                                        color = if (selected) coverDesignColors.first() else Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SIZE SELECTION
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Tamaño de la carátula",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(cardColor)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                            .padding(4.dp)
                    ) {
                        val sizes = listOf("70%", "85%", "100%")
                        sizes.forEach { sizeOption ->
                            val selected = coverSize == sizeOption
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) Color.White else Color.Transparent)
                                    .clickable {
                                        if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.setCoverSize(sizeOption)
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sizeOption,
                                    color = if (selected) DeepDark else Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // NEW: COVER DESIGNS SELECTION
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Diseño de la carátula",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(cardColor)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    ) {
                        val designs = listOf(
                            "Estándar" to "Marco clásico de alta definición",
                            "Borde Brillante (Neón)" to "Aura brillante que resalta en bordes",
                            "Vinilo Retro" to "Disco de vinilo saliendo del empaque",
                            "Tarjeta Minimalista" to "Diseño limpio con bordes finos plateados"
                        )

                        designs.forEachIndexed { idx, (designName, designDesc) ->
                            val selected = coverDesign == designName
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.setCoverDesign(designName)
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = designName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) coverDesignColors.first() else Color.White
                                    )
                                    Text(
                                        text = designDesc,
                                        fontSize = 11.sp,
                                        color = TextGray
                                    )
                                }
                                RadioButton(
                                    selected = selected,
                                    onClick = {
                                        if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.setCoverDesign(designName)
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = coverDesignColors.first(),
                                        unselectedColor = TextGray
                                    )
                                )
                            }
                            if (idx < designs.size - 1) {
                                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                            }
                        }
                    }
                }
            }

            // NEW: COVER ANIMATIONS SELECTION
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Animación al reproducir",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(cardColor)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    ) {
                        val animations = listOf(
                            "Ninguna" to "Estática sin movimiento",
                            "Rotación de Vinilo" to "Gira continuamente como un tocadiscos",
                            "Efecto Pulso (Beats)" to "Crece y se encoge rítmicamente",
                            "Escalar al Reproducir" to "Se expande suavemente al dar Play"
                        )

                        animations.forEachIndexed { idx, (animName, animDesc) ->
                            val selected = coverAnimation == animName
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.setCoverAnimation(animName)
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = animName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) coverDesignColors.first() else Color.White
                                    )
                                    Text(
                                        text = animDesc,
                                        fontSize = 11.sp,
                                        color = TextGray
                                    )
                                }
                                RadioButton(
                                    selected = selected,
                                    onClick = {
                                        if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.setCoverAnimation(animName)
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = coverDesignColors.first(),
                                        unselectedColor = TextGray
                                    )
                                )
                            }
                            if (idx < animations.size - 1) {
                                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                            }
                        }
                    }
                }
            }

            // NEW: COVER COLOR CUSTOMIZATION
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Gama de colores de la carátula",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(cardColor)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    ) {
                        val colorOptions = listOf(
                            "Por defecto (Gradiente)" to "Cian y Magenta integrados de fábrica",
                            "Cian Eléctrico" to "Color cian sólido de alta energía",
                            "Verde Neón" to "Verde fluorescente refrescante",
                            "Magenta Vibrante" to "Magenta profundo estético",
                            "RGB Animado (Arcoíris)" to "Transición infinita de colores RGB",
                            "Personalizado" to "Usa el color de tu tema de personalización"
                        )

                        colorOptions.forEachIndexed { idx, (colorName, colorDesc) ->
                            val selected = coverColorMode == colorName
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.setCoverColorMode(colorName)
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = colorName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) NeonCyan else Color.White
                                    )
                                    Text(
                                        text = colorDesc,
                                        fontSize = 11.sp,
                                        color = TextGray
                                    )
                                }
                                RadioButton(
                                    selected = selected,
                                    onClick = {
                                        if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.setCoverColorMode(colorName)
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = NeonCyan,
                                        unselectedColor = TextGray
                                    )
                                )
                            }
                            if (idx < colorOptions.size - 1) {
                                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                            }
                        }
                    }
                }
            }
        }
    }
}

data class ShapeOption(val name: String, val shape: Shape)

@Composable
fun PlayerControlStyleScreen(
    viewModel: MusicViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isAmoled by viewModel.amoledScreen.collectAsState()
    val opticalVibration by viewModel.opticalVibration.collectAsState()

    val buttonStyle by viewModel.buttonStyle.collectAsState()
    val filledIcons by viewModel.filledIcons.collectAsState()
    val customControlsColor by viewModel.customControlsColor.collectAsState()

    val NeonCyan = MaterialTheme.colorScheme.primary
    val DeepDark = MaterialTheme.colorScheme.background
    val CardDark = MaterialTheme.colorScheme.primaryContainer
    val SurfaceDark = MaterialTheme.colorScheme.surface
    val bgThemeColor = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.primaryContainer

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(64.dp)
                    .background(bgThemeColor)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Control del reproductor",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        containerColor = bgThemeColor,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            // PREVIEW BOX
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Vista previa de controles",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(cardColor)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Interaction sources for simulation click scaling animation
                    val prevInteraction = remember { MutableInteractionSource() }
                    val playInteraction = remember { MutableInteractionSource() }
                    val nextInteraction = remember { MutableInteractionSource() }

                    // Playback Controls Row simulation
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = if (buttonStyle == "Play 5") {
                            Modifier
                                .clip(RoundedCornerShape(32.dp))
                                .background(Color.White.copy(alpha = 0.04f))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(32.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        } else {
                            Modifier
                        }
                    ) {
                        // PREV BUTTON
                        IconButton(
                            onClick = { if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                            interactionSource = prevInteraction,
                            modifier = Modifier
                                .size(48.dp)
                                .scaleOnPress(prevInteraction)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = null,
                                tint = if (customControlsColor) NeonCyan else Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // PLAY BUTTON / STYLES
                        val playIcon = Icons.Default.PlayArrow
                        when (buttonStyle) {
                            "Play 2" -> {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .scaleOnPress(playInteraction)
                                        .clickable(
                                            interactionSource = playInteraction,
                                            indication = null
                                        ) {
                                            if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (customControlsColor) NeonCyan else Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = playIcon,
                                        contentDescription = null,
                                        tint = DeepDark,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                            "Play 3" -> {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .scaleOnPress(playInteraction)
                                        .clickable(
                                            interactionSource = playInteraction,
                                            indication = null
                                        ) {
                                            if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                        .border(2.dp, if (customControlsColor) NeonCyan else Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = playIcon,
                                        contentDescription = null,
                                        tint = if (customControlsColor) NeonCyan else Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                            "Play 4" -> {
                                // Squircle/Star design
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .scaleOnPress(playInteraction)
                                        .clickable(
                                            interactionSource = playInteraction,
                                            indication = null
                                        ) {
                                            if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                        .clip(SquircleStarShape)
                                        .background(if (customControlsColor) NeonCyan else Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = playIcon,
                                        contentDescription = null,
                                        tint = DeepDark,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                            "Play 5" -> {
                                // Capsule integrated mini style
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .scaleOnPress(playInteraction)
                                        .clickable(
                                            interactionSource = playInteraction,
                                            indication = null
                                        ) {
                                            if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    if (customControlsColor) NeonCyan else Color(0xFF00E5FF),
                                                    if (customControlsColor) NeonCyan.copy(alpha = 0.6f) else Color(0xFF00B0FF)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = playIcon,
                                        contentDescription = null,
                                        tint = DeepDark,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            "Play 6" -> {
                                // Glow outline
                                val infiniteTransition = rememberInfiniteTransition(label = "glow_btn")
                                val animatedGlowRadius by infiniteTransition.animateFloat(
                                    initialValue = 0f,
                                    targetValue = 6f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1500, easing = EaseInOutSine),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "glow"
                                )
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .scaleOnPress(playInteraction)
                                        .clickable(
                                            interactionSource = playInteraction,
                                            indication = null
                                        ) {
                                            if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                        .border(
                                            width = 1.dp + animatedGlowRadius.dp / 3,
                                            color = (if (customControlsColor) NeonCyan else Color(0xFFFF007F)).copy(
                                                alpha = 0.5f + (6f - animatedGlowRadius) / 12f
                                            ),
                                            shape = CircleShape
                                        )
                                        .padding(4.dp)
                                        .clip(CircleShape)
                                        .background(if (customControlsColor) NeonCyan else Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = playIcon,
                                        contentDescription = null,
                                        tint = DeepDark,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            else -> {
                                // Default circular filled
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .scaleOnPress(playInteraction)
                                        .clickable(
                                            interactionSource = playInteraction,
                                            indication = null
                                        ) {
                                            if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                        .clip(CircleShape)
                                        .background(if (customControlsColor) NeonCyan else Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = playIcon,
                                        contentDescription = null,
                                        tint = DeepDark,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        }

                        // NEXT BUTTON
                        IconButton(
                            onClick = { if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                            interactionSource = nextInteraction,
                            modifier = Modifier
                                .size(48.dp)
                                .scaleOnPress(nextInteraction)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = null,
                                tint = if (customControlsColor) NeonCyan else Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            // BUTTON STYLE SELECTOR
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Estilo de botones",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardColor)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                ) {
                    val styles = listOf(
                        "Por defecto" to "Círculo relleno clásico de alta visibilidad",
                        "Play 2" to "Cuadrado redondeado elegante",
                        "Play 3" to "Círculo minimalista con borde transparente",
                        "Play 4" to "Forma geométrica de estrella/esquírculo futurista",
                        "Play 5" to "Diseño de barra de cápsula integrada de neón",
                        "Play 6" to "Glow de neón respirante con halo flotante"
                    )

                    styles.forEachIndexed { idx, (styleOption, styleDesc) ->
                        val selected = buttonStyle == styleOption
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.setButtonStyle(styleOption)
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = styleOption,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) NeonCyan else Color.White
                                )
                                Text(
                                    text = styleDesc,
                                    fontSize = 11.sp,
                                    color = TextGray
                                )
                            }
                            RadioButton(
                                selected = selected,
                                onClick = {
                                    if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.setButtonStyle(styleOption)
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = NeonCyan,
                                    unselectedColor = TextGray
                                )
                            )
                        }
                        if (idx < styles.size - 1) {
                            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                        }
                    }
                }
            }

            // EXTRA OPTIONS (CATEGORÍAS)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Categorías",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardColor)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                ) {
                    // Iconos con relleno
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setFilledIcons(!filledIcons)
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                    text = "Iconos con relleno",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                            )
                            Text(
                                    text = "Usa iconos con relleno sólido",
                                    color = TextGray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Switch(
                            checked = filledIcons,
                            onCheckedChange = {
                                if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setFilledIcons(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeonCyan,
                                uncheckedThumbColor = TextGray,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    // Color personalizado de controles
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setCustomControlsColor(!customControlsColor)
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                    text = "Color personalizado de controles",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                            )
                            Text(
                                    text = "Usa el color dinámico de Material You o de la paleta personalizada",
                                    color = TextGray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Switch(
                            checked = customControlsColor,
                            onCheckedChange = {
                                if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setCustomControlsColor(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeonCyan,
                                uncheckedThumbColor = TextGray,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressBarStyleScreen(
    viewModel: MusicViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isAmoled by viewModel.amoledScreen.collectAsState()
    val opticalVibration by viewModel.opticalVibration.collectAsState()
    val progressBarStyle by viewModel.progressBarStyle.collectAsState()
    val progressBarCustomColorEnabled by viewModel.progressBarCustomColorEnabled.collectAsState()
    val progressBarCustomColor by viewModel.progressBarCustomColor.collectAsState()

    val bgThemeColor = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.primaryContainer
    val NeonCyan = MaterialTheme.colorScheme.primary

    val styles = listOf(
        "Por defecto" to "Barra deslizadora estándar del sistema",
        "Estilo 1" to "Barra plana sólida con indicador rectangular",
        "Estilo 2" to "Burbuja elástica de onda siri",
        "Estilo 3" to "Tubo de neón holográfico",
        "Estilo 4" to "Onda sinusoidal continua",
        "Barco de papel" to "Un barquito flotando sobre una onda pacífica",
        "Estilo 6 (Nave OVNI)" to "Línea elegante con platillo volador OVNI"
    )

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(64.dp)
                    .background(bgThemeColor)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Estilo de barra de progreso",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        containerColor = bgThemeColor,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(cardColor)
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Personalizar Color",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Si está desactivado se usará el color blanco por defecto",
                                    fontSize = 11.sp,
                                    color = TextGray
                                )
                            }
                            Switch(
                                checked = progressBarCustomColorEnabled,
                                onCheckedChange = { checked ->
                                    if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.setProgressBarCustomColorEnabled(checked)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NeonCyan,
                                    checkedTrackColor = NeonCyan.copy(alpha = 0.3f)
                                )
                            )
                        }
                        
                        if (progressBarCustomColorEnabled) {
                            Text(
                                text = "Elige un color para tu barra de progreso:",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            val paletteColors = listOf(
                                Color(0xFF00FFCC), // Neon Turquoise
                                Color(0xFFFF007F), // Neon Pink
                                Color(0xFFFFCC00), // Vibrant Amber
                                Color(0xFF9D4EDD), // Deep Violet
                                Color(0xFFFF5722), // Deep Orange
                                Color(0xFF00E5FF), // Cyan Accent
                                Color(0xFF2ECC71), // Smooth Green
                                Color(0xFFE74C3C)  // Smooth Red
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                paletteColors.forEach { color ->
                                    val isSelected = progressBarCustomColor == color.toArgb().toLong()
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(
                                                width = if (isSelected) 3.dp else 0.dp,
                                                color = Color.White,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                viewModel.setProgressBarCustomColor(color.toArgb().toLong())
                                            }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            items(styles) { (styleName, styleDesc) ->
                val isSelected = progressBarStyle == styleName
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(cardColor)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) NeonCyan else Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable {
                            if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.setProgressBarStyle(styleName)
                        }
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = styleName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = styleDesc,
                                    fontSize = 11.sp,
                                    color = TextGray
                                )
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.setProgressBarStyle(styleName)
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = NeonCyan,
                                    unselectedColor = TextGray
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.2f))
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ProgressBarStylesPreview(styleName = styleName)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressBarStylesPreview(styleName: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "prog_preview")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = Modifier.fillMaxWidth().height(40.dp)) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val activeWidth = width * 0.6f

        when (styleName) {
            "Por defecto" -> {
                drawLine(
                    color = Color.White.copy(alpha = 0.1f),
                    start = androidx.compose.ui.geometry.Offset(0f, centerY),
                    end = androidx.compose.ui.geometry.Offset(width, centerY),
                    strokeWidth = 4.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                drawLine(
                    color = Color(0xFF00E5FF),
                    start = androidx.compose.ui.geometry.Offset(0f, centerY),
                    end = androidx.compose.ui.geometry.Offset(activeWidth, centerY),
                    strokeWidth = 4.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                drawCircle(
                    color = Color(0xFF00E5FF),
                    center = androidx.compose.ui.geometry.Offset(activeWidth, centerY),
                    radius = 8.dp.toPx()
                )
            }
            "Estilo 1" -> {
                drawLine(
                    color = Color.White.copy(alpha = 0.1f),
                    start = androidx.compose.ui.geometry.Offset(0f, centerY),
                    end = androidx.compose.ui.geometry.Offset(width, centerY),
                    strokeWidth = 6.dp.toPx()
                )
                drawLine(
                    color = Color(0xFFFF5722),
                    start = androidx.compose.ui.geometry.Offset(0f, centerY),
                    end = androidx.compose.ui.geometry.Offset(activeWidth, centerY),
                    strokeWidth = 6.dp.toPx()
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
                    val envelope = if (progressRatio < 0.6f) {
                        progressRatio / 0.6f
                    } else {
                        (1f - progressRatio) / 0.4f
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
                val ufoY = centerY - 14.dp.toPx() + ufoHover

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

@Composable
fun PlayerBackgroundStyleScreen(
    viewModel: MusicViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isAmoled by viewModel.amoledScreen.collectAsState()
    val opticalVibration by viewModel.opticalVibration.collectAsState()
    val playerBackgroundStyle by viewModel.playerBackgroundStyle.collectAsState()
    val playerBackgroundPreset by viewModel.playerBackgroundPreset.collectAsState()

    val bgThemeColor = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.primaryContainer
    val NeonCyan = MaterialTheme.colorScheme.primary

    val options = listOf(
        "Apagado" to "Pure theme color",
        "Static Colors" to "Minimalist background",
        "Fondo animado" to "Se adapta a la reproducción de música"
    )

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(64.dp)
                    .background(bgThemeColor)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Fondo del reproductor",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        containerColor = bgThemeColor,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(cardColor)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                ) {
                    options.forEachIndexed { index, (styleName, styleDesc) ->
                        val isSelected = playerBackgroundStyle == styleName
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.setPlayerBackgroundStyle(styleName)
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = styleName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = styleDesc,
                                    fontSize = 11.sp,
                                    color = TextGray
                                )
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.setPlayerBackgroundStyle(styleName)
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = NeonCyan,
                                    unselectedColor = TextGray
                                )
                            )
                        }
                        if (index < options.size - 1) {
                            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Ajustes predefinidos",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PresetBackgroundCard(
                            name = "Programar 1",
                            modifier = Modifier.weight(1f),
                            selectedPreset = playerBackgroundPreset,
                            onClick = {
                                if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setPlayerBackgroundPreset("Programar 1")
                                viewModel.setPlayerBackgroundStyle("Static Colors")
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.verticalGradient(listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))))
                            )
                        }

                        PresetBackgroundCard(
                            name = "Programar 2",
                            modifier = Modifier.weight(1f),
                            selectedPreset = playerBackgroundPreset,
                            onClick = {
                                if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setPlayerBackgroundPreset("Programar 2")
                                viewModel.setPlayerBackgroundStyle("Static Colors")
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.verticalGradient(listOf(Color(0xFF3A6073), Color(0xFF16222F))))
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PresetBackgroundCard(
                            name = "Gradiente 3",
                            modifier = Modifier.weight(1f),
                            selectedPreset = playerBackgroundPreset,
                            onClick = {
                                if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setPlayerBackgroundPreset("Gradiente 3")
                                viewModel.setPlayerBackgroundStyle("Static Colors")
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.verticalGradient(listOf(Color(0xFF2D1B4E), Color(0xFF160E2A))))
                            )
                        }

                        PresetBackgroundCard(
                            name = "Fondo Paisaje",
                            modifier = Modifier.weight(1f),
                            selectedPreset = playerBackgroundPreset,
                            onClick = {
                                if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setPlayerBackgroundPreset("Fondo Paisaje")
                                viewModel.setPlayerBackgroundStyle("Static Colors")
                            }
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawRect(color = Color(0xFF1A237E))
                                drawCircle(color = Color.White.copy(alpha = 0.5f), center = androidx.compose.ui.geometry.Offset(20.dp.toPx(), 20.dp.toPx()), radius = 1.dp.toPx())
                                drawCircle(color = Color.White.copy(alpha = 0.8f), center = androidx.compose.ui.geometry.Offset(80.dp.toPx(), 40.dp.toPx()), radius = 1.5.dp.toPx())
                                drawCircle(color = Color.White.copy(alpha = 0.4f), center = androidx.compose.ui.geometry.Offset(130.dp.toPx(), 25.dp.toPx()), radius = 1.dp.toPx())
                                
                                drawCircle(color = Color(0xFFFFF59D), center = androidx.compose.ui.geometry.Offset(45.dp.toPx(), 35.dp.toPx()), radius = 10.dp.toPx())
                                drawCircle(color = Color(0xFF1A237E), center = androidx.compose.ui.geometry.Offset(39.dp.toPx(), 35.dp.toPx()), radius = 10.dp.toPx())

                                val mount1 = androidx.compose.ui.graphics.Path()
                                mount1.moveTo(0f, size.height)
                                mount1.lineTo(size.width * 0.3f, size.height * 0.4f)
                                mount1.lineTo(size.width * 0.7f, size.height * 0.7f)
                                mount1.lineTo(size.width, size.height * 0.3f)
                                mount1.lineTo(size.width, size.height)
                                mount1.close()
                                drawPath(path = mount1, color = Color(0xFF283593))

                                val mount2 = androidx.compose.ui.graphics.Path()
                                mount2.moveTo(0f, size.height)
                                mount2.lineTo(size.width * 0.5f, size.height * 0.55f)
                                mount2.lineTo(size.width, size.height * 0.45f)
                                mount2.lineTo(size.width, size.height)
                                mount2.close()
                                drawPath(path = mount2, color = Color(0xFF303F9F))

                                drawRect(
                                    color = Color(0xFF1E2746).copy(alpha = 0.9f),
                                    topLeft = androidx.compose.ui.geometry.Offset(0f, size.height * 0.82f),
                                    size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.18f)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PresetBackgroundCard(
                            name = "Místico Aleatorio",
                            modifier = Modifier.weight(1f),
                            selectedPreset = playerBackgroundPreset,
                            onClick = {
                                if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setPlayerBackgroundPreset("Místico Aleatorio")
                                viewModel.setPlayerBackgroundStyle("Static Colors")
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color(0xFF8E2DE2),
                                                Color(0xFF4A00E0)
                                            )
                                        )
                                    )
                            ) {
                                Text(
                                    text = "✦",
                                    color = Color.White.copy(alpha = 0.3f),
                                    fontSize = 40.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }

                        PresetBackgroundCard(
                            name = "Nebulosa Cósmica",
                            modifier = Modifier.weight(1f),
                            selectedPreset = playerBackgroundPreset,
                            onClick = {
                                if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setPlayerBackgroundPreset("Nebulosa Cósmica")
                                viewModel.setPlayerBackgroundStyle("Static Colors")
                            }
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawRect(color = Color(0xFF03001e))
                                drawCircle(
                                    color = Color(0xFFec38bc).copy(alpha = 0.4f),
                                    center = androidx.compose.ui.geometry.Offset(size.width * 0.3f, size.height * 0.4f),
                                    radius = size.width * 0.6f
                                )
                                drawCircle(
                                    color = Color(0xFF7303c0).copy(alpha = 0.3f),
                                    center = androidx.compose.ui.geometry.Offset(size.width * 0.7f, size.height * 0.6f),
                                    radius = size.width * 0.5f
                                )
                                drawCircle(
                                    color = Color(0xFF03001e).copy(alpha = 0.8f),
                                    center = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.5f),
                                    radius = 20f
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PresetBackgroundCard(
                            name = "Cyberpunk Retro",
                            modifier = Modifier.weight(1f),
                            selectedPreset = playerBackgroundPreset,
                            onClick = {
                                if (opticalVibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setPlayerBackgroundPreset("Cyberpunk Retro")
                                viewModel.setPlayerBackgroundStyle("Static Colors")
                            }
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawRect(color = Color(0xFF0D0D11))
                                // Draw synthwave grid line
                                drawCircle(
                                    color = Color(0xFFFF007F).copy(alpha = 0.15f),
                                    center = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.45f),
                                    radius = size.width * 0.4f
                                )
                                // Draw retro horizon lines
                                val horizonY = size.height * 0.65f
                                drawLine(
                                    color = Color(0xFF00FFCC).copy(alpha = 0.5f),
                                    start = androidx.compose.ui.geometry.Offset(0f, horizonY),
                                    end = androidx.compose.ui.geometry.Offset(size.width, horizonY),
                                    strokeWidth = 2f
                                )
                                for (i in 1..4) {
                                    val y = horizonY + i * (size.height - horizonY) / 5
                                    drawLine(
                                        color = Color(0xFF00FFCC).copy(alpha = 0.15f * (i.toFloat() / 4f)),
                                        start = androidx.compose.ui.geometry.Offset(0f, y),
                                        end = androidx.compose.ui.geometry.Offset(size.width, y),
                                        strokeWidth = 1f
                                    )
                                }
                            }
                        }

                        // Spacer placeholder box to maintain beautiful 2-column alignment
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun PresetBackgroundCard(
    name: String,
    modifier: Modifier = Modifier,
    selectedPreset: String,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val isSelected = selectedPreset == name
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.6f)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            content()
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.background,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
    }
}
