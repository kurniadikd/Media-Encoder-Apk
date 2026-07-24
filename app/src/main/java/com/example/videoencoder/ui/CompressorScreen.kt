package com.example.videoencoder.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.media3.common.MimeTypes
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Official Material Design 3 Expressive System Guidelines Implementation

@Composable
fun M3ExpressiveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    pressedShape: Shape = RoundedCornerShape(12.dp),
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "expressive_button_scale"
    )

    val currentShape = if (isPressed) pressedShape else shape

    Button(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = animatedScale
            scaleY = animatedScale
        },
        enabled = enabled,
        shape = currentShape,
        colors = colors,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
fun M3ExpressiveFilledTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    pressedShape: Shape = RoundedCornerShape(12.dp),
    colors: ButtonColors = ButtonDefaults.filledTonalButtonColors(),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "expressive_tonal_scale"
    )

    val currentShape = if (isPressed) pressedShape else shape

    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = animatedScale
            scaleY = animatedScale
        },
        enabled = enabled,
        shape = currentShape,
        colors = colors,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
fun M3ExpressiveFilledTonalIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    pressedShape: Shape = RoundedCornerShape(12.dp),
    colors: IconButtonColors = IconButtonDefaults.filledTonalIconButtonColors(),
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "expressive_tonal_icon_scale"
    )

    val currentShape = if (isPressed) pressedShape else shape

    FilledTonalIconButton(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = animatedScale
            scaleY = animatedScale
        },
        enabled = enabled,
        shape = currentShape,
        colors = colors,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
fun M3ExpressiveIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "expressive_icon_scale"
    )

    IconButton(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = animatedScale
            scaleY = animatedScale
        },
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
        content = content
    )
}

/**
 * Official Material Design 3 Expressive FAB Menu Items
 * Full pill shape with spring press scale physics & shape morphing
 */
@Composable
fun M3ExpressiveFabMenuItem(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "fab_item_scale"
    )

    val currentShape = if (isPressed) RoundedCornerShape(16.dp) else CircleShape

    Surface(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = animatedScale
            scaleY = animatedScale
        },
        shape = currentShape,
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = 4.dp,
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

/**
 * Official Material Design 3 Expressive FAB Button with Contrasting Close Button Transformation
 */
@Composable
fun M3ExpressiveLargeFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    shape: Shape = CircleShape,
    pressedShape: Shape = RoundedCornerShape(18.dp),
    collapsedContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    collapsedContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    expandedContainerColor: Color = MaterialTheme.colorScheme.tertiary,
    expandedContentColor: Color = MaterialTheme.colorScheme.onTertiary
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "expressive_fab_scale"
    )

    val currentContainerColor by animateColorAsState(
        targetValue = if (isExpanded) expandedContainerColor else collapsedContainerColor,
        animationSpec = tween(durationMillis = 200),
        label = "fab_container_color"
    )

    val currentContentColor by animateColorAsState(
        targetValue = if (isExpanded) expandedContentColor else collapsedContentColor,
        animationSpec = tween(durationMillis = 200),
        label = "fab_content_color"
    )

    val currentShape = if (isPressed) pressedShape else shape

    LargeFloatingActionButton(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = animatedScale
            scaleY = animatedScale
        },
        shape = currentShape,
        containerColor = currentContainerColor,
        contentColor = currentContentColor,
        interactionSource = interactionSource
    ) {
        AnimatedContent(
            targetState = isExpanded,
            transitionSpec = {
                (fadeIn(tween(150)) + scaleIn(initialScale = 0.7f)).togetherWith(
                    fadeOut(tween(100)) + scaleOut(targetScale = 0.7f)
                )
            },
            label = "fab_icon_crossfade"
        ) { expanded ->
            if (expanded) {
                Icon(Icons.Default.Close, contentDescription = "Tutup Menu", modifier = Modifier.size(32.dp))
            } else {
                Icon(Icons.Default.Add, contentDescription = "Tambah Media", modifier = Modifier.size(36.dp))
            }
        }
    }
}

@Composable
fun M3ExpressiveFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(14.dp),
    pressedShape: Shape = RoundedCornerShape(8.dp),
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primary,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
    )
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "expressive_chip_scale"
    )

    val currentShape = if (isPressed) pressedShape else shape

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier.graphicsLayer {
            scaleX = animatedScale
            scaleY = animatedScale
        },
        shape = currentShape,
        colors = colors,
        border = null,
        interactionSource = interactionSource
    )
}

@Composable
fun M3ExpressiveCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(20.dp),
    pressedShape: Shape = RoundedCornerShape(14.dp),
    colors: CardColors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.97f else 1.0f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "expressive_card_scale"
    )

    val currentShape = if (isPressed && onClick != null) pressedShape else shape

    if (onClick != null) {
        Card(
            modifier = modifier
                .graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            shape = currentShape,
            colors = colors,
            border = null,
            content = content
        )
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = colors,
            border = null,
            content = content
        )
    }
}

@Composable
fun CompressorScreen(
    viewModel: CompressorViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    AnimatedContent(
        targetState = uiState.currentScreen,
        transitionSpec = {
            if (targetState == AppScreen.PREPROCESS || targetState == AppScreen.LOGS) {
                (slideInHorizontally(initialOffsetX = { it }, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn()).togetherWith(
                    slideOutHorizontally(targetOffsetX = { -it / 3 }) + fadeOut()
                )
            } else {
                (slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn()).togetherWith(
                    slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
                )
            }
        },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            AppScreen.MAIN -> MainScreenView(viewModel = viewModel, uiState = uiState)
            AppScreen.PREPROCESS -> PreprocessScreenView(viewModel = viewModel, uiState = uiState)
            AppScreen.LOGS -> LogsScreenView(viewModel = viewModel, uiState = uiState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenView(
    viewModel: CompressorViewModel,
    uiState: CompressorUiState
) {
    val context = LocalContext.current
    var isFabMenuExpanded by remember { mutableStateOf(false) }

    BackHandler(enabled = isFabMenuExpanded) {
        isFabMenuExpanded = false
    }

    // Native SAF Document Pickers for Video, Image, and Audio
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.onMediaSelected(it, MediaType.VIDEO) }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.onMediaSelected(it, MediaType.IMAGE) }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.onMediaSelected(it, MediaType.AUDIO) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Scaffold & List Content
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Media Encoder",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    },
                    actions = {
                        // Native M3 Expressive Log Action Button
                        M3ExpressiveFilledTonalIconButton(
                            onClick = { viewModel.navigateToLogsScreen() },
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.Terminal, contentDescription = "Log Sistem & Enkoder")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Unified Encoded File List (Contains active encoding items AND finished history)
                UnifiedMediaListSection(
                    uiState = uiState,
                    onCancelEncoding = { viewModel.cancelCompression() },
                    onDelete = { viewModel.deleteHistoryItem(it.path) },
                    onPlay = { item ->
                        try {
                            val file = File(item.path)
                            if (file.exists()) {
                                try {
                                    android.media.MediaScannerConnection.scanFile(
                                        context.applicationContext,
                                        arrayOf(file.absolutePath),
                                        null,
                                        null
                                    )
                                } catch (_: Exception) {}

                                val uri: Uri = try {
                                    FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                } catch (e: Exception) {
                                    Uri.fromFile(file)
                                }

                                val mime = when (item.mediaType) {
                                    MediaType.IMAGE -> "image/*"
                                    MediaType.AUDIO -> "audio/*"
                                    else -> "video/*"
                                }

                                val targetIntent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, mime)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                }

                                val resInfoList = context.packageManager.queryIntentActivities(targetIntent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
                                for (resolveInfo in resInfoList) {
                                    val packageName = resolveInfo.activityInfo.packageName
                                    try {
                                        context.grantUriPermission(
                                            packageName,
                                            uri,
                                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        )
                                    } catch (_: Exception) {}
                                }

                                val chooser = Intent.createChooser(targetIntent, "Buka Berkas Dengan")
                                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(chooser)
                            } else {
                                Toast.makeText(context, "Berkas tidak ditemukan: ${file.name}", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Gagal membuka media: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    },
                    onShare = { item ->
                        try {
                            val file = File(item.path)
                            if (file.exists()) {
                                val uri: Uri = try {
                                    FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                } catch (e: Exception) {
                                    Uri.fromFile(file)
                                }

                                val mime = when (item.mediaType) {
                                    MediaType.IMAGE -> "image/*"
                                    MediaType.AUDIO -> "audio/*"
                                    else -> "video/*"
                                }

                                val targetIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = mime
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }

                                val resInfoList = context.packageManager.queryIntentActivities(targetIntent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
                                for (resolveInfo in resInfoList) {
                                    val packageName = resolveInfo.activityInfo.packageName
                                    try {
                                        context.grantUriPermission(
                                            packageName,
                                            uri,
                                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        )
                                    } catch (_: Exception) {}
                                }

                                val chooser = Intent.createChooser(targetIntent, "Bagikan Berkas Media")
                                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(chooser)
                            } else {
                                Toast.makeText(context, "Berkas tidak ditemukan: ${file.name}", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Gagal membagikan media: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(130.dp))
            }
        }

        // 2. Native Material Dimmed Backdrop Scrim Overlay (Below FAB overlay, above content)
        AnimatedVisibility(
            visible = isFabMenuExpanded,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(150))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isFabMenuExpanded = false }
            )
        }

        // 3. Official Material Design 3 Expressive FAB Menu Implementation
        val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val fabBottomPadding = if (navBarBottom > 0.dp) navBarBottom + 20.dp else 36.dp

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = fabBottomPadding, end = 16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                AnimatedVisibility(
                    visible = isFabMenuExpanded,
                    enter = fadeIn(tween(180)) + slideInVertically(initialOffsetY = { it / 2 }, animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy)) + scaleIn(initialScale = 0.75f),
                    exit = fadeOut(tween(120)) + slideOutVertically(targetOffsetY = { it / 2 }) + scaleOut(targetScale = 0.75f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 1. Video SAF Item
                        M3ExpressiveFabMenuItem(
                            onClick = {
                                isFabMenuExpanded = false
                                videoPickerLauncher.launch(arrayOf("video/*", "application/octet-stream", "video/x-msvideo", "video/x-ms-wmv", "video/x-matroska", "video/avi", "*/*"))
                            },
                            icon = { Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(24.dp)) },
                            label = "Video"
                        )

                        // 2. Gambar SAF Item
                        M3ExpressiveFabMenuItem(
                            onClick = {
                                isFabMenuExpanded = false
                                imagePickerLauncher.launch(arrayOf("image/*"))
                            },
                            icon = { Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(24.dp)) },
                            label = "Gambar"
                        )

                        // 3. Audio SAF Item
                        M3ExpressiveFabMenuItem(
                            onClick = {
                                isFabMenuExpanded = false
                                audioPickerLauncher.launch(arrayOf("audio/*"))
                            },
                            icon = { Icon(Icons.Default.Audiotrack, contentDescription = null, modifier = Modifier.size(24.dp)) },
                            label = "Audio"
                        )
                    }
                }

                // Official M3 Expressive Main Toggle FAB with Contrasting Close Button Transformation
                M3ExpressiveLargeFAB(
                    onClick = { isFabMenuExpanded = !isFabMenuExpanded },
                    isExpanded = isFabMenuExpanded,
                    collapsedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    collapsedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    expandedContainerColor = MaterialTheme.colorScheme.tertiary,
                    expandedContentColor = MaterialTheme.colorScheme.onTertiary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreprocessScreenView(
    viewModel: CompressorViewModel,
    uiState: CompressorUiState
) {
    val media = uiState.selectedMedia ?: return

    BackHandler {
        viewModel.navigateToMainScreen()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    M3ExpressiveIconButton(onClick = { viewModel.navigateToMainScreen() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                title = {
                    Text(
                        text = "Pengaturan Enkoder (${media.mediaType.label})",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    M3ExpressiveFilledTonalIconButton(
                        onClick = { viewModel.navigateToLogsScreen() },
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Terminal, contentDescription = "Log Sistem & Enkoder")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            val bottomPadding = if (navBarBottom > 0.dp) navBarBottom + 16.dp else 24.dp

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = bottomPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Estimasi Ukuran", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = String.format(Locale.US, "%.2f MB", uiState.estimatedSizeMb),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Native M3 Expressive Action Button with Shape Morphing & Spring Physics
                    M3ExpressiveButton(
                        onClick = { viewModel.startCompression() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mulai Pengodean Hardware", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selected Media Preview Expressive Card
            M3ExpressiveCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when (media.mediaType) {
                                    MediaType.IMAGE -> Icons.Default.Image
                                    MediaType.AUDIO -> Icons.Default.Audiotrack
                                    else -> Icons.Default.Movie
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(media.fileName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        val sizeStr = String.format(Locale.US, "%.1f MB", media.sizeBytes / 1_000_000f)
                        val extraDetails = buildString {
                            if (media.width > 0) append("${media.width}x${media.height}") else append("${media.durationSec}s")
                            if (media.fps > 0f) append(" • ${media.fps.toInt()} FPS")
                            if (media.audioChannels > 0) append(" • ${if (media.audioChannels == 2) "Stereo" else if (media.audioChannels == 1) "Mono" else "${media.audioChannels} Ch"}")
                            if (!media.colorStandard.isNullOrBlank()) append(" • ${media.colorStandard}")
                        }
                        Text(
                            text = "$sizeStr • $extraDetails",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Text("Parameter Pengodean Hardware (Dropdown)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

            when (media.mediaType) {
                MediaType.VIDEO -> {
                    // 1. Video Codec Dropdown
                    val codecOptions = if (uiState.availableVideoEncoders.isNotEmpty()) uiState.availableVideoEncoders else listOf(
                        "DEFAULT" to "Default (Bawaan System)",
                        MimeTypes.VIDEO_H265 to "HEVC (H.265)",
                        MimeTypes.VIDEO_H264 to "AVC (H.264)",
                        MimeTypes.VIDEO_VP9 to "VP9",
                        MimeTypes.VIDEO_AV1 to "AV1"
                    )
                    M3DropdownSelector(
                        label = "Video Codec Encoder",
                        selectedLabel = codecOptions.firstOrNull { it.first == uiState.outputFormat }?.second ?: "Default (Bawaan System)",
                        options = codecOptions.map { it.second },
                        onOptionSelected = { index -> viewModel.setCodecFormat(codecOptions[index].first) }
                    )

                    // 2. Target Resolution Dropdown
                    val resOptions = ResolutionPreset.values()
                    M3DropdownSelector(
                        label = "Target Resolusi Video",
                        selectedLabel = uiState.resolutionPreset.label,
                        options = resOptions.map { it.label },
                        onOptionSelected = { index -> viewModel.setResolutionPreset(resOptions[index]) }
                    )

                    // 3. Target Bitrate Slider & Auto Option
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Target Video Bitrate", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = if (uiState.useAutoBitrate) "Default (Auto System)" else String.format(Locale.US, "%.1f Mbps", uiState.targetBitrateMbps),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            M3ExpressiveFilterChip(
                                selected = uiState.useAutoBitrate,
                                onClick = { viewModel.setUseAutoBitrate(true) },
                                label = { Text("Default (Auto System)") }
                            )

                            M3ExpressiveFilterChip(
                                selected = !uiState.useAutoBitrate,
                                onClick = { viewModel.setUseAutoBitrate(false) },
                                label = { Text("Custom Bitrate Slider") }
                            )
                        }

                        if (!uiState.useAutoBitrate) {
                            Slider(
                                value = uiState.targetBitrateMbps,
                                onValueChange = { viewModel.setTargetBitrate(it) },
                                valueRange = 1.0f..30.0f,
                                steps = 57,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }

                    // 4. Scale Mode Dropdown
                    val scaleOptions = ScaleModeOption.values()
                    M3DropdownSelector(
                        label = "Modus Skala (Scaling Mode)",
                        selectedLabel = uiState.scaleModeOption.label,
                        options = scaleOptions.map { it.label },
                        onOptionSelected = { index -> viewModel.setScaleModeOption(scaleOptions[index]) }
                    )

                    // 5. Bitrate Control Mode Dropdown
                    val bitrateModeOptions = BitrateModeOption.values()
                    M3DropdownSelector(
                        label = "Bitrate Control Mode",
                        selectedLabel = uiState.bitrateModeOption.label,
                        options = bitrateModeOptions.map { it.label },
                        onOptionSelected = { index -> viewModel.setBitrateModeOption(bitrateModeOptions[index]) }
                    )

                    // 6. Frame Rate (FPS) Dropdown
                    val fpsList = listOf(0 to "Default (FPS Asli Video)", 60 to "60 FPS", 50 to "50 FPS", 30 to "30 FPS", 24 to "24 FPS", 15 to "15 FPS")
                    M3DropdownSelector(
                        label = "Target Frame Rate (FPS)",
                        selectedLabel = fpsList.firstOrNull { it.first == uiState.frameRate }?.second ?: "Default (FPS Asli Video)",
                        options = fpsList.map { it.second },
                        onOptionSelected = { index -> viewModel.setFrameRate(fpsList[index].first) }
                    )

                    // 7. Keyframe Interval Dropdown
                    val keyframeList = listOf(0.0f to "Default (Auto Keyframe)", 1.0f to "1.0 Second", 2.0f to "2.0 Seconds", 3.0f to "3.0 Seconds", 5.0f to "5.0 Seconds")
                    M3DropdownSelector(
                        label = "Keyframe Interval (I-Frame)",
                        selectedLabel = keyframeList.firstOrNull { it.first == uiState.iFrameIntervalSec }?.second ?: "Default (Auto Keyframe)",
                        options = keyframeList.map { it.second },
                        onOptionSelected = { index -> viewModel.setIFrameInterval(keyframeList[index].first) }
                    )

                    // 8. Rotation Dropdown
                    val rotationList = listOf(0.0f to "0° (Normal)", 90.0f to "90° Clockwise", 180.0f to "180° Inverted", 270.0f to "270° Counter-Clockwise")
                    M3DropdownSelector(
                        label = "Rotasi Video",
                        selectedLabel = rotationList.firstOrNull { it.first == uiState.rotationDegrees }?.second ?: "0° (Normal)",
                        options = rotationList.map { it.second },
                        onOptionSelected = { index -> viewModel.setRotationDegrees(rotationList[index].first) }
                    )
                }
                MediaType.IMAGE -> {
                    // Image Format Dropdown
                    val imgFormats = listOf("WEBP" to "WEBP (Next-Gen)", "JPEG" to "JPEG", "PNG" to "PNG (Lossless)")
                    M3DropdownSelector(
                        label = "Format Gambar Output",
                        selectedLabel = imgFormats.firstOrNull { it.first == uiState.imageFormat }?.second ?: "WEBP (Next-Gen)",
                        options = imgFormats.map { it.second },
                        onOptionSelected = { index -> viewModel.setImageFormat(imgFormats[index].first) }
                    )

                    // Image Quality Slider
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Kualitas Kompresi Gambar", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                            Text(text = "${uiState.imageQuality}%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = uiState.imageQuality.toFloat(),
                            onValueChange = { viewModel.setImageQuality(it.toInt()) },
                            valueRange = 10f..100f,
                            steps = 17,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }

                    // Image Resizing Scale Dropdown
                    val scaleList = listOf(100 to "Original (100%)", 75 to "75% Skala", 50 to "50% Skala", 25 to "25% Skala")
                    M3DropdownSelector(
                        label = "Dimensi Gambar (Resizing)",
                        selectedLabel = scaleList.firstOrNull { it.first == uiState.imageScalePercent }?.second ?: "Original (100%)",
                        options = scaleList.map { it.second },
                        onOptionSelected = { index -> viewModel.setImageScalePercent(scaleList[index].first) }
                    )
                }
                MediaType.AUDIO -> {
                    // Audio Format Dropdown
                    val audioFormats = listOf("DEFAULT" to "Default (AAC)", MimeTypes.AUDIO_AAC to "AAC", MimeTypes.AUDIO_OPUS to "Opus", MimeTypes.AUDIO_AMR_WB to "AMR-WB")
                    M3DropdownSelector(
                        label = "Audio Codec Target",
                        selectedLabel = audioFormats.firstOrNull { it.first == uiState.audioFormat }?.second ?: "Default (AAC)",
                        options = audioFormats.map { it.second },
                        onOptionSelected = { index -> viewModel.setAudioFormat(audioFormats[index].first) }
                    )

                    // Audio Bitrate Dropdown
                    val audioBitrates = listOf(0 to "Default (Auto Bitrate)", 320 to "320 kbps (High Quality)", 256 to "256 kbps", 192 to "192 kbps", 128 to "128 kbps (Standard)", 64 to "64 kbps (Low)")
                    M3DropdownSelector(
                        label = "Target Audio Bitrate",
                        selectedLabel = audioBitrates.firstOrNull { it.first == uiState.audioBitrateKbps }?.second ?: "Default (Auto Bitrate)",
                        options = audioBitrates.map { it.second },
                        onOptionSelected = { index -> viewModel.setAudioBitrate(audioBitrates[index].first) }
                    )
                }
            }

            // Storage Location Dropdown
            val storageOptions = StorageLocationOption.values()
            M3DropdownSelector(
                label = "Lokasi Penyimpanan Output",
                selectedLabel = uiState.storageLocationOption.label,
                options = storageOptions.map { "${it.label} (${it.subtitle})" },
                onOptionSelected = { index -> viewModel.setStorageLocationOption(storageOptions[index]) }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Dedicated Native Material 3 Expressive Logs Screen View
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreenView(
    viewModel: CompressorViewModel,
    uiState: CompressorUiState
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    BackHandler {
        viewModel.navigateToMainScreen()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    M3ExpressiveIconButton(onClick = { viewModel.navigateToMainScreen() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                title = {
                    Text(
                        text = "Log Sistem & Enkoder",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    // Native M3 Expressive Copy Logs Button
                    M3ExpressiveFilledTonalButton(
                        onClick = {
                            val allLogsText = uiState.logList.joinToString("\n") { log ->
                                "[${log.timestamp}] [${log.level}] [${log.tag}] ${log.message}"
                            }
                            clipboardManager.setText(AnnotatedString(allLogsText))
                            Toast.makeText(context, "Seluruh log disalin ke klipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salin Log", fontWeight = FontWeight.Bold)
                    }

                    // Native M3 Expressive Clear Logs Button
                    M3ExpressiveIconButton(onClick = { viewModel.clearLogs() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Bersihkan Log")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val bottomPadding = if (navBarBottom > 0.dp) navBarBottom + 16.dp else 24.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = bottomPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (uiState.logList.isEmpty()) {
                M3ExpressiveCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Belum ada log sistem tercatat.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                uiState.logList.forEach { log ->
                    LogCardItem(log = log)
                }
            }
        }
    }
}

@Composable
fun LogCardItem(log: LogEntry) {
    val (badgeBgColor, badgeTextColor) = when (log.level) {
        LogLevel.SUCCESS -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        LogLevel.WARNING -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        LogLevel.ERROR -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    }

    M3ExpressiveCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeBgColor
                ) {
                    Text(
                        text = "[${log.level}] ${log.tag}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = badgeTextColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = log.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = log.message,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3DropdownSelector(
    label: String,
    selectedLabel: String,
    options: List<String>,
    onOptionSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed || expanded) 0.98f else 1.0f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "dropdown_scale"
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedLabel,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = if (expanded || isPressed) RoundedCornerShape(10.dp) else RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                    }
                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEachIndexed { index, option ->
                    DropdownMenuItem(
                        text = { Text(option, fontWeight = if (option == selectedLabel) FontWeight.Bold else FontWeight.Normal) },
                        onClick = {
                            onOptionSelected(index)
                            expanded = false
                        },
                        leadingIcon = if (option == selectedLabel) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }
        }
    }
}

@Composable
fun UnifiedMediaListSection(
    uiState: CompressorUiState,
    onCancelEncoding: () -> Unit,
    onDelete: (EncodedFileItem) -> Unit,
    onPlay: (EncodedFileItem) -> Unit,
    onShare: (EncodedFileItem) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FolderZip, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Daftar File & Proses Media", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            }

            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                val totalCount = uiState.encodedHistory.size + (if (uiState.isEncoding) 1 else 0)
                Text(
                    text = "$totalCount File",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // 1. Render Active Encoding Item Card with Circular Wavy Progress Bar
        if (uiState.isEncoding) {
            ActiveEncodingCardItem(
                fileName = uiState.activeEncodingFileName ?: "Berkas Media",
                progressPercent = uiState.encodingProgress,
                statusText = uiState.encodingStatusText,
                mediaType = uiState.selectedMedia?.mediaType ?: MediaType.VIDEO,
                onCancel = onCancelEncoding
            )
        }

        // 2. Render Completed History Items
        if (uiState.encodedHistory.isEmpty() && !uiState.isEncoding) {
            M3ExpressiveCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Belum ada file media yang dikompresi.\nTekan tombol FAB di kanan bawah untuk mengodekan berkas.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                uiState.encodedHistory.forEach { item ->
                    EncodedHistoryCardItem(
                        item = item,
                        onDelete = { onDelete(item) },
                        onPlay = { onPlay(item) },
                        onShare = { onShare(item) }
                    )
                }
            }
        }
    }
}

/**
 * Active Encoding Card - Fully Synergistic with EncodedHistoryCardItem
 * Uses M3 Expressive Circular Wavy Progress Indicator encircling the media icon!
 */
@Composable
fun ActiveEncodingCardItem(
    fileName: String,
    progressPercent: Int,
    statusText: String,
    mediaType: MediaType,
    onCancel: () -> Unit
) {
    M3ExpressiveCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Media Icon surrounded by M3 Expressive Circular Wavy Progress Indicator
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(52.dp)
                ) {
                    M3ExpressiveCircularWavyProgressIndicator(
                        progress = progressPercent / 100f,
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )

                    // Unfilled icon container during progress with icon color matching progress wavy!
                    Surface(
                        shape = CircleShape,
                        color = Color.Transparent,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when (mediaType) {
                                    MediaType.IMAGE -> Icons.Default.Image
                                    MediaType.AUDIO -> Icons.Default.Audiotrack
                                    else -> Icons.Default.Movie
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = fileName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$progressPercent%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("•", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            M3ExpressiveFilledTonalIconButton(
                onClick = onCancel,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(Icons.Default.Stop, contentDescription = "Batal")
            }
        }
    }
}

/**
 * Official Material 3 Expressive Circular Wavy Progress Indicator
 * - Both active segment AND inactive segment follow a continuous 360-degree wavy track!
 * - Native Completion Flattening Animation: Ramps wave amplitude down from 1.8dp -> 0.0dp when progress reaches 100%!
 */
@Composable
fun M3ExpressiveCircularWavyProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0.01f, 1.0f),
        animationSpec = tween(durationMillis = 200, easing = LinearEasing),
        label = "granular_progress"
    )

    // Native M3 Wave Flattening Completion Animation (flattens amplitude to 0.0dp when progress >= 1.0f / completed!)
    val animatedWaveAmplitudeDp by animateFloatAsState(
        targetValue = if (progress >= 1.0f) 0.0f else 1.8f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "wave_flattening"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "m3_expressive_circular_wave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    Canvas(modifier = modifier) {
        val diameter = minOf(size.width, size.height)
        val strokeWidth = 3.5.dp.toPx()
        val radius = (diameter - strokeWidth * 2) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        val cyclesPerDegree = 8.0 / 360.0
        val waveAmplitude = animatedWaveAmplitudeDp.dp.toPx()
        val activeSweepAngle = 360f * animatedProgress

        // 1. Draw INACTIVE WAVY TRACK (the unreached path from activeSweepAngle to 360 degrees)
        if (activeSweepAngle < 358f) {
            val inactiveSweepAngle = 360f - activeSweepAngle
            val numInactivePoints = (inactiveSweepAngle * 0.6f).toInt().coerceAtLeast(10)
            val inactivePath = Path()

            for (i in 0..numInactivePoints) {
                val fraction = i.toFloat() / numInactivePoints
                val angleDegrees = activeSweepAngle + fraction * inactiveSweepAngle
                val currentAngleDegrees = -90f + angleDegrees
                val currentAngleRads = Math.toRadians(currentAngleDegrees.toDouble())

                val waveOffset = waveAmplitude * sin(angleDegrees * cyclesPerDegree * 2 * PI + wavePhase)
                val currentRadius = radius + waveOffset

                val x = (center.x + currentRadius * cos(currentAngleRads)).toFloat()
                val y = (center.y + currentRadius * sin(currentAngleRads)).toFloat()

                if (i == 0) {
                    inactivePath.moveTo(x, y)
                } else {
                    inactivePath.lineTo(x, y)
                }
            }

            drawPath(
                path = inactivePath,
                color = trackColor,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // 2. Draw ACTIVE WAVY TRACK (the reached path from 0 to activeSweepAngle)
        val numActivePoints = (activeSweepAngle * 0.6f).toInt().coerceAtLeast(10)
        val activePath = Path()

        for (i in 0..numActivePoints) {
            val fraction = i.toFloat() / numActivePoints
            val angleDegrees = fraction * activeSweepAngle
            val currentAngleDegrees = -90f + angleDegrees
            val currentAngleRads = Math.toRadians(currentAngleDegrees.toDouble())

            val waveOffset = waveAmplitude * sin(angleDegrees * cyclesPerDegree * 2 * PI + wavePhase)
            val currentRadius = radius + waveOffset

            val x = (center.x + currentRadius * cos(currentAngleRads)).toFloat()
            val y = (center.y + currentRadius * sin(currentAngleRads)).toFloat()

            if (i == 0) {
                activePath.moveTo(x, y)
            } else {
                activePath.lineTo(x, y)
            }
        }

        drawPath(
            path = activePath,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun EncodedHistoryCardItem(
    item: EncodedFileItem,
    onDelete: () -> Unit,
    onPlay: () -> Unit,
    onShare: () -> Unit
) {
    val sizeMb = String.format(Locale.US, "%.1f MB", item.sizeBytes / 1_000_000f)
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    val dateStr = dateFormat.format(Date(item.lastModifiedMs))

    M3ExpressiveCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onPlay,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (item.mediaType) {
                                MediaType.IMAGE -> Icons.Default.Image
                                MediaType.AUDIO -> Icons.Default.Audiotrack
                                else -> Icons.Default.Movie
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(item.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(sizeMb, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.primary)
                        Text("•", style = MaterialTheme.typography.labelSmall)
                        Text(dateStr, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                M3ExpressiveFilledTonalIconButton(onClick = onPlay) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play/View")
                }
                M3ExpressiveFilledTonalIconButton(onClick = onShare) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
                M3ExpressiveFilledTonalIconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}
