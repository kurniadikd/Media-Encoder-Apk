package com.example.videoencoder.ui

import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.runtime.key
import androidx.compose.ui.unit.IntSize
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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

import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity

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
 * Expressive Dropdown Menu sesuai pedoman Material M3 Expressive Reference
 */
@Composable
fun M3ExpressiveDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.padding(horizontal = 4.dp, vertical = 2.dp),
        shape = RoundedCornerShape(24.dp), // Corner melengkung khas M3 Expressive
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 6.dp,
        content = content
    )
}

/**
 * Expressive Dropdown Menu Item dengan gaya Pill Shape & Highlight
 */
@Composable
fun M3ExpressiveDropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    textColor: Color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
    selectedContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    icon: (@Composable () -> Unit)? = null
) {
    val itemShape = RoundedCornerShape(16.dp)
    val backgroundColor = if (isSelected) selectedContainerColor else Color.Transparent

    DropdownMenuItem(
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = textColor
                )
            )
        },
        leadingIcon = icon,
        onClick = onClick,
        colors = MenuDefaults.itemColors(
            textColor = textColor,
            leadingIconColor = textColor
        ),
        modifier = modifier
            .padding(horizontal = 6.dp, vertical = 2.dp) // Margin antar item dalam popup
            .background(color = backgroundColor, shape = itemShape)
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
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
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
fun <T> M3ExpressiveSegmentedButtonGroup(
    options: List<Pair<T, String>>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEachIndexed { index, (option, label) ->
            val isSelected = option == selectedOption
            val isFirst = index == 0
            val isLast = index == options.size - 1

            val startCorner by animateDpAsState(
                targetValue = if (isFirst || isSelected) 24.dp else 6.dp,
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioMediumBouncy
                ),
                label = "start_corner"
            )

            val endCorner by animateDpAsState(
                targetValue = if (isLast || isSelected) 24.dp else 6.dp,
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioMediumBouncy
                ),
                label = "end_corner"
            )

            val shape = RoundedCornerShape(
                topStart = startCorner,
                bottomStart = startCorner,
                topEnd = endCorner,
                bottomEnd = endCorner
            )

            val containerColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                animationSpec = tween(durationMillis = 180),
                label = "segmented_bg"
            )

            val contentColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(durationMillis = 180),
                label = "segmented_fg"
            )

            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()

            val animatedScale by animateFloatAsState(
                targetValue = if (isPressed) 0.94f else 1.0f,
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioMediumBouncy
                ),
                label = "segmented_scale"
            )

            Surface(
                onClick = { onOptionSelected(option) },
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                    },
                shape = shape,
                color = containerColor,
                contentColor = contentColor,
                interactionSource = interactionSource
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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

    // Native Media Pickers
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.onMediaSelected(it, MediaType.VIDEO) }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.onMediaSelected(it, MediaType.IMAGE) }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.onMediaSelected(it, MediaType.AUDIO) }
    }

    val lazyListState = rememberLazyListState()
    val isScrollAtTop by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset == 0 }
    }

    val pullToRefreshState = rememberPullToRefreshState()
    val density = LocalDensity.current

    // 1. Jarak Tarikan Tanpa Batas Mentok (Sinergis 1:1 dengan Gestur Jari)
    val pullDistanceDp = (pullToRefreshState.distanceFraction * 140).coerceAtLeast(0f).dp
    val targetContentOffsetDp = if (uiState.isRefreshing) {
        88.dp // Posisi istirahat saat aktif memuat ulang (refreshing)
    } else {
        pullDistanceDp
    }

    // 2. Single Source Spring Physics untuk Pergeseran Daftar & Indikator
    val animatedContentOffsetDp by animateDpAsState(
        targetValue = targetContentOffsetDp,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "pull_spring_sync"
    )

    // ROOT CONTAINER (Layer Paling Luar)
    Box(modifier = Modifier.fillMaxSize()) {

        // LAYER 1: Scaffold, TopAppBar & Content Daftar File
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
                        M3ExpressiveFilledTonalIconButton(
                            onClick = { viewModel.refreshEncodedHistory(showPullToRefreshSpinner = true) },
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh Daftar File")
                        }
                        Spacer(modifier = Modifier.width(4.dp))
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
            val topBarHeightDp = innerPadding.calculateTopPadding()
            val indicatorSizeDp = 48.dp

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refreshEncodedHistory(showPullToRefreshSpinner = true) },
                state = pullToRefreshState,
                indicator = {}, // Disosongkan karena ditaruh pada Layer Teratas di luar Scaffold
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            // Menerapkan efek pegas pada pergeseran daftar
                            translationY = with(density) { animatedContentOffsetDp.toPx() }
                        }
                        .padding(horizontal = 16.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 20.dp, bottom = 150.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (uiState.encodedHistory.isEmpty()) {
                        item(key = "empty_state") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 120.dp, horizontal = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Untuk memulai, pilih file dengan klik tombol +",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    ),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        item(key = "list_header") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.FolderZip,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Daftar File & Proses Media",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                                    AnimatedContent(
                                        targetState = uiState.encodedHistory.size,
                                        transitionSpec = {
                                            (fadeIn(tween(200)) + scaleIn(initialScale = 0.8f))
                                                .togetherWith(fadeOut(tween(150)) + scaleOut(targetScale = 0.8f))
                                        },
                                        label = "file_count"
                                    ) { count ->
                                        Text(
                                            text = "$count File",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        items(
                            items = uiState.encodedHistory,
                            key = { it.id }
                        ) { item ->
                            UnifiedMediaCardItem(
                                modifier = Modifier.animateItem(
                                    fadeInSpec = tween(durationMillis = 250),
                                    fadeOutSpec = tween(durationMillis = 220),
                                    placementSpec = spring(
                                        stiffness = Spring.StiffnessMediumLow,
                                        dampingRatio = Spring.DampingRatioNoBouncy
                                    )
                                ),
                                item = item,
                                onCancel = { viewModel.requestCancelCompression() },
                                onDelete = { viewModel.requestDeleteHistoryItem(item) },
                                onPlay = {
                                    try {
                                        val file = File(item.path)
                                        if (file.exists()) {
                                            val exactMime = when {
                                                file.name.endsWith(".mp4", ignoreCase = true) -> "video/mp4"
                                                file.name.endsWith(".mkv", ignoreCase = true) -> "video/x-matroska"
                                                file.name.endsWith(".webm", ignoreCase = true) -> "video/webm"
                                                file.name.endsWith(".avi", ignoreCase = true) -> "video/avi"
                                                file.name.endsWith(".jpg", ignoreCase = true) || file.name.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
                                                file.name.endsWith(".png", ignoreCase = true) -> "image/png"
                                                file.name.endsWith(".webp", ignoreCase = true) -> "image/webp"
                                                file.name.endsWith(".m4a", ignoreCase = true) || file.name.endsWith(".aac", ignoreCase = true) -> "audio/aac"
                                                file.name.endsWith(".mp3", ignoreCase = true) -> "audio/mpeg"
                                                else -> when (item.mediaType) {
                                                    MediaType.IMAGE -> "image/*"
                                                    MediaType.AUDIO -> "audio/*"
                                                    else -> "video/*"
                                                }
                                            }
                                            val fileProviderUri = try {
                                                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                            } catch (_: Exception) { Uri.fromFile(file) }
                                            val getMediaStoreUri: () -> Uri? = {
                                                try {
                                                    val contentUri = when (item.mediaType) {
                                                        MediaType.IMAGE -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                                                        MediaType.AUDIO -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                                                        else -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                                                    }
                                                    context.contentResolver.query(
                                                        contentUri,
                                                        arrayOf(MediaStore.MediaColumns._ID),
                                                        "${MediaStore.MediaColumns.DATA} = ?",
                                                        arrayOf(file.absolutePath), null
                                                    )?.use { cursor ->
                                                        if (cursor.moveToFirst()) {
                                                            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                                                            ContentUris.withAppendedId(contentUri, id)
                                                        } else null
                                                    }
                                                } catch (_: Exception) { null }
                                            }
                                            val launchIntent: (Uri) -> Unit = { targetUri ->
                                                val targetIntent = Intent(Intent.ACTION_VIEW).apply {
                                                    setDataAndType(targetUri, exactMime)
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                                val resInfoList = context.packageManager.queryIntentActivities(targetIntent, PackageManager.MATCH_DEFAULT_ONLY)
                                                for (resolveInfo in resInfoList) {
                                                    try { context.grantUriPermission(resolveInfo.activityInfo.packageName, targetUri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
                                                }
                                                try {
                                                    context.startActivity(Intent.createChooser(targetIntent, "Buka Berkas Dengan").apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                                                } catch (_: Exception) {
                                                    Toast.makeText(context, "Tidak ada aplikasi penampil media yang cocok.", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            val existingUri = getMediaStoreUri()
                                            if (existingUri != null) {
                                                launchIntent(existingUri)
                                            } else {
                                                android.media.MediaScannerConnection.scanFile(
                                                    context.applicationContext, arrayOf(file.absolutePath), arrayOf(exactMime)
                                                ) { _, scannedUri ->
                                                    val uriToUse = scannedUri ?: getMediaStoreUri() ?: fileProviderUri
                                                    android.os.Handler(android.os.Looper.getMainLooper()).post { launchIntent(uriToUse) }
                                                }
                                            }
                                        } else {
                                            Toast.makeText(context, "Berkas tidak ditemukan: ${file.name}", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Gagal membuka media: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                    }
                                },
                                onShare = {
                                    try {
                                        val file = File(item.path)
                                        if (file.exists()) {
                                            val exactMime = when {
                                                file.name.endsWith(".mp4", ignoreCase = true) -> "video/mp4"
                                                file.name.endsWith(".jpg", ignoreCase = true) || file.name.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
                                                file.name.endsWith(".png", ignoreCase = true) -> "image/png"
                                                file.name.endsWith(".webp", ignoreCase = true) -> "image/webp"
                                                file.name.endsWith(".m4a", ignoreCase = true) || file.name.endsWith(".aac", ignoreCase = true) -> "audio/aac"
                                                else -> "video/mp4"
                                            }
                                            val fileProviderUri = try {
                                                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                            } catch (_: Exception) { Uri.fromFile(file) }
                                            val launchShare: (Uri) -> Unit = { targetUri ->
                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = exactMime
                                                    putExtra(Intent.EXTRA_STREAM, targetUri)
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                                val resInfoList = context.packageManager.queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY)
                                                for (ri in resInfoList) {
                                                    try { context.grantUriPermission(ri.activityInfo.packageName, targetUri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
                                                }
                                                context.startActivity(Intent.createChooser(shareIntent, "Bagikan Berkas").apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                                            }
                                            android.media.MediaScannerConnection.scanFile(
                                                context.applicationContext, arrayOf(file.absolutePath), arrayOf(exactMime)
                                            ) { _, scannedUri -> launchShare(scannedUri ?: fileProviderUri) }
                                        } else {
                                            Toast.makeText(context, "Berkas tidak ditemukan: ${item.path}", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Gagal membagikan media: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // LAYER 2 (LAYER ATAS): Loading Indicator Dihitung TEPAT di Tengah Area Kosong (Stretch Gap)
            if (animatedContentOffsetDp > 2.dp || uiState.isRefreshing) {
                // Formula Posisi Tengah Geometris Presisi
                val indicatorY = topBarHeightDp + ((animatedContentOffsetDp - indicatorSizeDp) / 2)

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = indicatorY)
                        .graphicsLayer {
                            val fraction = pullToRefreshState.distanceFraction
                            alpha = if (uiState.isRefreshing) 1f else fraction.coerceIn(0f, 1f)
                            val scale = if (uiState.isRefreshing) 1f else fraction.coerceIn(0.4f, 1f)
                            scaleX = scale
                            scaleY = scale
                        }
                ) {
                    if (uiState.isRefreshing) {
                        ContainedLoadingIndicator(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            indicatorColor = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        ContainedLoadingIndicator(
                            progress = { pullToRefreshState.distanceFraction },
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            indicatorColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // LAYER 3: Scrim Backdrop Overlay
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

            // LAYER 4: Floating Action Button Menu
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
                            M3ExpressiveFabMenuItem(
                                onClick = {
                                    isFabMenuExpanded = false
                                    videoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                                },
                                icon = { Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(24.dp)) },
                                label = "Video"
                            )
                            M3ExpressiveFabMenuItem(
                                onClick = {
                                    isFabMenuExpanded = false
                                    imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                                icon = { Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(24.dp)) },
                                label = "Gambar"
                            )
                            M3ExpressiveFabMenuItem(
                                onClick = {
                                    isFabMenuExpanded = false
                                    audioPickerLauncher.launch("audio/*")
                                },
                                icon = { Icon(Icons.Default.Audiotrack, contentDescription = null, modifier = Modifier.size(24.dp)) },
                                label = "Audio"
                            )
                        }
                    }

                    ExtendedFloatingActionButton(
                        text = {
                            AnimatedContent(
                                targetState = isFabMenuExpanded,
                                transitionSpec = {
                                    (fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 4 }).togetherWith(
                                        fadeOut(tween(150)) + slideOutVertically(tween(150)) { -it / 4 }
                                    )
                                },
                                label = "fab_text"
                            ) { expanded ->
                                Text(
                                    text = if (expanded) "Tutup" else "Input File",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        },
                        icon = {
                            AnimatedContent(
                                targetState = isFabMenuExpanded,
                                transitionSpec = {
                                    (fadeIn(tween(200)) + scaleIn(initialScale = 0.8f, animationSpec = tween(200))).togetherWith(
                                        fadeOut(tween(150)) + scaleOut(targetScale = 0.8f, animationSpec = tween(150))
                                    )
                                },
                                label = "fab_icon"
                            ) { expanded ->
                                Icon(
                                    imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        onClick = { isFabMenuExpanded = !isFabMenuExpanded },
                        expanded = isFabMenuExpanded || isScrollAtTop,
                        containerColor = if (isFabMenuExpanded) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        contentColor = if (isFabMenuExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(20.dp),
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                    )
                }
            }
        }
    }

    // Modal Dialog Konfirmasi Tindakan (Hentikan Encoding / Hapus Antrean / Hapus File)
    val pendingAction = uiState.pendingConfirmAction
    if (pendingAction != null) {
        data class ConfirmDialogData(
            val title: String,
            val text: String,
            val confirmText: String,
            val icon: androidx.compose.ui.graphics.vector.ImageVector
        )

        val dialogData = when (pendingAction.type) {
            PendingActionType.STOP_ENCODING -> ConfirmDialogData(
                "Hentikan Pengodean?",
                "Proses pengodean untuk file \"${pendingAction.item.name}\" sedang berlangsung. Apakah Anda yakin ingin menghentikannya?",
                "Hentikan",
                Icons.Default.Stop
            )
            PendingActionType.REMOVE_FROM_QUEUE -> ConfirmDialogData(
                "Hapus dari Antrean?",
                "Apakah Anda yakin ingin menghapus file \"${pendingAction.item.name}\" dari antrean pengodean?",
                "Hapus Antrean",
                Icons.Default.Close
            )
            PendingActionType.DELETE_FILE -> ConfirmDialogData(
                "Hapus File Media?",
                "File \"${pendingAction.item.name}\" akan dihapus secara permanen dari penyimpanan perangkat Anda.",
                "Hapus Permanent",
                Icons.Default.Delete
            )
        }

        AlertDialog(
            onDismissRequest = { viewModel.dismissConfirmDialog() },
            icon = { Icon(dialogData.icon, contentDescription = null, tint = if (pendingAction.type == PendingActionType.STOP_ENCODING || pendingAction.type == PendingActionType.DELETE_FILE) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) },
            title = { Text(text = dialogData.title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
            text = { Text(text = dialogData.text, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                M3ExpressiveButton(
                    onClick = { viewModel.confirmPendingAction() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (pendingAction.type == PendingActionType.STOP_ENCODING || pendingAction.type == PendingActionType.DELETE_FILE) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(dialogData.confirmText, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                M3ExpressiveFilledTonalButton(
                    onClick = { viewModel.dismissConfirmDialog() }
                ) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    } else if (uiState.showCancelEncodingDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCancelDialog() },
            icon = { Icon(Icons.Default.Stop, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Hentikan Pengodean?", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
            text = { Text("Apakah Anda yakin ingin menghentikan proses pengodean media yang sedang berlangsung?") },
            confirmButton = {
                M3ExpressiveButton(
                    onClick = { viewModel.confirmCancelCompression() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hentikan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                M3ExpressiveFilledTonalButton(
                    onClick = { viewModel.dismissCancelDialog() }
                ) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
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
                        text = "Pengaturan Enkoder",
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
        bottomBar = {}
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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

            Text("Parameter Pengodean Hardware", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

            when (media.mediaType) {
                MediaType.VIDEO -> {
                    // 1. Video Codec Dropdown
                    val fileCodecLabel = when {
                        media.videoMime?.contains("hevc", ignoreCase = true) == true || media.videoMime?.contains("h265", ignoreCase = true) == true -> "HEVC (H.265)"
                        media.videoMime?.contains("avc", ignoreCase = true) == true || media.videoMime?.contains("h264", ignoreCase = true) == true -> "AVC (H.264)"
                        media.videoMime?.contains("vp9", ignoreCase = true) == true -> "VP9"
                        media.videoMime?.contains("av01", ignoreCase = true) == true || media.videoMime?.contains("av1", ignoreCase = true) == true -> "AV1"
                        else -> "AVC (H.264)"
                    }

                    val codecOptions = if (uiState.selectedMedia?.mediaType == MediaType.AUDIO) {
                        listOf(
                            "DEFAULT" to fileCodecLabel,
                            "libfdk_aac" to "🧪 FDK-AAC (Software AAC)",
                            "libopus" to "🧪 Opus (Software Audio)",
                            "libmp3lame" to "🧪 MP3 (Software Audio)",
                            MimeTypes.AUDIO_AAC to "⚡ AAC (Hardware)",
                            MimeTypes.AUDIO_AMR_NB to "⚡ AMR-NB (Hardware)"
                        )
                    } else listOf(
                        "DEFAULT" to fileCodecLabel,
                        "libsvtav1" to "🧪 SVT-AV1 (Software AV1)",
                        "libx264" to "🧪 x264 (Software H.264)",
                        "libx265" to "🧪 x265 (Software H.265)",
                        MimeTypes.VIDEO_H265 to "⚡ HEVC (H.265 Hardware)",
                        MimeTypes.VIDEO_H264 to "⚡ AVC (H.264 Hardware)",
                        MimeTypes.VIDEO_VP9 to "⚡ VP9 (Hardware)",
                        MimeTypes.VIDEO_AV1 to "⚡ AV1 (Hardware)"
                    )

                    M3DropdownSelector(
                        label = "Video Codec Encoder",
                        selectedLabel = codecOptions.firstOrNull { it.first == uiState.outputFormat }?.second ?: fileCodecLabel,
                        options = codecOptions.map { it.second },
                        onOptionSelected = { index -> viewModel.setCodecFormat(codecOptions[index].first) }
                    )

                    // 2. Bitrate Control Mode Connected Segmented ButtonGroup
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Bitrate Control Mode",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        val effectiveOption = if (uiState.bitrateModeOption == BitrateModeOption.DEFAULT) BitrateModeOption.VBR else uiState.bitrateModeOption
                        M3ExpressiveSegmentedButtonGroup(
                            options = listOf(
                                BitrateModeOption.VBR to "VBR",
                                BitrateModeOption.CBR to "CBR"
                            ),
                            selectedOption = effectiveOption,
                            onOptionSelected = { option -> viewModel.setBitrateModeOption(option) }
                        )
                    }

                    // 3. Adaptive Bitrate Slider Direct Display
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Target Video Bitrate",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (uiState.targetBitrateMbps < 1.0f) {
                                    String.format(Locale.US, "%.1f Mbps (%d kbps)", uiState.targetBitrateMbps, (uiState.targetBitrateMbps * 1000).toInt())
                                } else {
                                    String.format(Locale.US, "%.1f Mbps", uiState.targetBitrateMbps)
                                },
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Slider(
                            value = uiState.targetBitrateMbps,
                            onValueChange = { viewModel.setTargetBitrate(it) },
                            valueRange = 0.1f..50.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }

                    // 4. Target Resolution Dropdown
                    val fileResLabel = if (media.width > 0 && media.height > 0) "${media.width}x${media.height}" else "Asli Video"
                    val resOptions = ResolutionPreset.values()
                    val resOptionsLabels = resOptions.map { preset ->
                        if (preset == ResolutionPreset.DEFAULT) fileResLabel else preset.label
                    }

                    M3DropdownSelector(
                        label = "Target Resolusi Video",
                        selectedLabel = if (uiState.resolutionPreset == ResolutionPreset.DEFAULT) fileResLabel else uiState.resolutionPreset.label,
                        options = resOptionsLabels,
                        onOptionSelected = { index -> viewModel.setResolutionPreset(resOptions[index]) }
                    )

                    // 5. Scale Mode Dropdown
                    val scaleOptions = ScaleModeOption.values()
                    M3DropdownSelector(
                        label = "Modus Skala (Scaling Mode)",
                        selectedLabel = uiState.scaleModeOption.label,
                        options = scaleOptions.map { it.label },
                        onOptionSelected = { index -> viewModel.setScaleModeOption(scaleOptions[index]) }
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

                    // 8. Rotasi Video Connected Segmented ButtonGroup
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Rotasi Video",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        M3ExpressiveSegmentedButtonGroup(
                            options = listOf(
                                0.0f to "0°",
                                90.0f to "90°",
                                180.0f to "180°",
                                270.0f to "270°"
                            ),
                            selectedOption = uiState.rotationDegrees,
                            onOptionSelected = { deg -> viewModel.setRotationDegrees(deg) }
                        )
                    }
                }
                MediaType.IMAGE -> {
                    // Image Encoder Engine Selector
                    val engineOptions = listOf("LIBVIPS" to "⚡ libvips (Ultra High-Performance Native Engine)", "BITMAP" to "📱 Android Bitmap API (Software Standard)")
                    M3DropdownSelector(
                        label = "Engine Pengodean Gambar",
                        selectedLabel = engineOptions.firstOrNull { it.first == uiState.imageEncoderEngine }?.second ?: "⚡ libvips (Ultra High-Performance Native Engine)",
                        options = engineOptions.map { it.second },
                        onOptionSelected = { index -> viewModel.setImageEncoderEngine(engineOptions[index].first) }
                    )

                    // Image Format Dropdown
                    val imgFormats = listOf("WEBP" to "WEBP", "JPEG" to "JPEG", "PNG" to "PNG")
                    M3DropdownSelector(
                        label = "Format Gambar Output",
                        selectedLabel = imgFormats.firstOrNull { it.first == uiState.imageFormat }?.second ?: "WEBP",
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
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }

                    // Image Resizing Slider with Output Dimensions Display
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val targetW = if (media.width > 0) (media.width * uiState.imageScalePercent / 100) else 0
                        val targetH = if (media.height > 0) (media.height * uiState.imageScalePercent / 100) else 0
                        val sizeText = if (targetW > 0 && targetH > 0) {
                            "${targetW}x${targetH} (${uiState.imageScalePercent}%)"
                        } else {
                            "${uiState.imageScalePercent}%"
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Ukuran Hasil", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = sizeText,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = uiState.imageScalePercent.toFloat(),
                            onValueChange = { viewModel.setImageScalePercent(it.toInt()) },
                            valueRange = 10f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
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



            Spacer(modifier = Modifier.height(120.dp))
        }

        val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val fabBottomPadding = if (navBarBottom > 0.dp) navBarBottom + 20.dp else 36.dp

        ExtendedFloatingActionButton(
            text = {
                Text(
                    text = "Mulai",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            onClick = { viewModel.startCompression() },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(20.dp),
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = fabBottomPadding)
        )
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

/**
 * Integrated Expressive Motion & Animation Design Tokens System
 * Provides standardized, spring-based native animations for list entry, exit, bounds morphing, and card state transitions.
 */
object ExpressiveMotion {
    // Standard spring specs for container bounds morphing
    val boundsSpring = spring<IntSize>(
        stiffness = Spring.StiffnessMediumLow,
        dampingRatio = Spring.DampingRatioNoBouncy
    )

    // Standard state transition specs for morphing between Queued -> Encoding -> Finished states
    val stateTransitionSpec = (fadeIn(tween(260, easing = FastOutSlowInEasing)) + scaleIn(initialScale = 0.85f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy)))
        .togetherWith(fadeOut(tween(180, easing = FastOutSlowInEasing)) + scaleOut(targetScale = 0.85f, animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioNoBouncy)))

    // Badge Icon Inward Enter / Outward Exit Transition Spec
    val badgeTransitionSpec = (
        fadeIn(tween(260, easing = FastOutSlowInEasing)) + scaleIn(
            initialScale = 0.5f,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy)
        )
    ).togetherWith(
        fadeOut(tween(200, easing = FastOutSlowInEasing)) + scaleOut(
            targetScale = 1.4f,
            animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioNoBouncy)
        )
    )

    // List Item Addition Enter Transition (Expand + FadeIn + ScaleIn)
    val itemEnterTransition = expandVertically(
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy)
    ) + fadeIn(
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioNoBouncy)
    ) + scaleIn(
        initialScale = 0.88f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    // List Item Removal Exit Transition (Shrink + FadeOut + ScaleOut)
    val itemExitTransition = shrinkVertically(
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy)
    ) + fadeOut(
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
    ) + scaleOut(
        targetScale = 0.88f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioNoBouncy)
    )
}

@Composable
fun UnifiedMediaListSection(
    uiState: CompressorUiState,
    onCancelEncoding: () -> Unit,
    onDelete: (EncodedFileItem) -> Unit,
    onPlay: (EncodedFileItem) -> Unit,
    onShare: (EncodedFileItem) -> Unit
) {
    if (uiState.encodedHistory.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 120.dp, horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Untuk memulai, pilih file dengan klik tombol +",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    } else {
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
                    val totalCount = uiState.encodedHistory.size
                    Text(
                        text = "$totalCount File",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Render Unified Media Items smoothly
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                uiState.encodedHistory.forEach { item ->
                    key(item.id) {
                        UnifiedMediaCardItem(
                            item = item,
                            onCancel = onCancelEncoding,
                            onPlay = { onPlay(item) },
                            onShare = { onShare(item) },
                            onDelete = { onDelete(item) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Unified Media Card Item
 * - Uses 1 SINGLE M3ExpressiveCard container for both Active Progress & Finished File
 * - Smoothly morphs badge, subtext details, and action buttons using native Compose AnimatedContent & ExpressiveMotion system!
 */
@Composable
fun UnifiedMediaCardItem(
    item: EncodedFileItem,
    modifier: Modifier = Modifier,
    onCancel: () -> Unit,
    onPlay: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    M3ExpressiveCard(
        onClick = {
            if (!item.isEncodingActive && !item.isQueued && item.queueStatus != QueueStatus.QUEUED) {
                onPlay()
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = ExpressiveMotion.boundsSpring
            ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val cardStateKey = when {
                item.isEncodingActive -> "ENCODING"
                item.isQueued || item.queueStatus == QueueStatus.QUEUED -> "QUEUED"
                else -> "FINISHED"
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // 1. Left Badge Icon
                AnimatedContent(
                    targetState = cardStateKey,
                    transitionSpec = { ExpressiveMotion.badgeTransitionSpec },
                    label = "badge_animation"
                ) { state ->
                    when (state) {
                        "ENCODING" -> {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(52.dp)
                            ) {
                                M3ExpressiveCircularWavyProgressIndicator(
                                    progress = item.progressPercent / 100f,
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.35f)
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = Color.Transparent,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = when (item.mediaType) {
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
                        }
                        "QUEUED" -> {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(52.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.35f),
                                    strokeWidth = 3.dp
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = Color.Transparent,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = when (item.mediaType) {
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
                        }
                        else -> {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(52.dp)
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
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 2. Middle Text Details
                Column {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    AnimatedContent(
                        targetState = cardStateKey,
                        transitionSpec = { ExpressiveMotion.stateTransitionSpec },
                        label = "subtext_animation"
                    ) { state ->
                        when (state) {
                            "ENCODING" -> {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${item.progressPercent.coerceIn(0, 100)}%",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text("•", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = item.statusText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            "QUEUED" -> {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Dalam Antrean",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text("•", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = "Menunggu giliran...",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            else -> {
                                val formattedSize = remember(item.sizeBytes) {
                                    if (item.sizeBytes > 0) String.format(Locale.US, "%.1f MB", item.sizeBytes / 1_000_000f) else ""
                                }
                                val formattedDate = remember(item.lastModifiedMs) {
                                    if (item.lastModifiedMs > 0) {
                                        SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(Date(item.lastModifiedMs))
                                    } else "Baru saja"
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (item.queueStatus == QueueStatus.FAILED) {
                                        Text(
                                            text = "Gagal",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    } else if (item.queueStatus == QueueStatus.CANCELLED) {
                                        Text(
                                            text = "Dibatalkan",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    } else {
                                        if (formattedSize.isNotEmpty()) {
                                            Text(
                                                text = formattedSize,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text("•", style = MaterialTheme.typography.labelSmall)
                                        }
                                        Text(
                                            text = formattedDate,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 3. Single Action Button: Three Dots (MoreVert) with DropdownMenu
            AnimatedContent(
                targetState = cardStateKey,
                transitionSpec = { ExpressiveMotion.stateTransitionSpec },
                label = "actions_animation"
            ) { state ->
                when (state) {
                    "ENCODING" -> {
                        M3ExpressiveFilledTonalIconButton(
                            onClick = onCancel,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Hentikan Encoding")
                        }
                    }
                    "QUEUED" -> {
                        M3ExpressiveFilledTonalIconButton(
                            onClick = onDelete,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Hapus Antrean")
                        }
                    }
                    else -> {
                        Box {
                            M3ExpressiveIconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Opsi Lainnya")
                            }

                            M3ExpressiveDropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                if (item.queueStatus == QueueStatus.COMPLETED) {
                                    M3ExpressiveDropdownMenuItem(
                                        text = "Bagikan",
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            onShare()
                                        }
                                    )
                                }

                                M3ExpressiveDropdownMenuItem(
                                    text = "Hapus",
                                    textColor = MaterialTheme.colorScheme.error,
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onDelete()
                                    }
                                )
                            }
                        }
                    }
                }
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
    trackColor: Color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.35f)
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0.0f, 1.0f),
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
        val strokeWidth = 3.dp.toPx()
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
