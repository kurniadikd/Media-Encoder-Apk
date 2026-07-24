package com.example.videoencoder.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
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

@Composable
fun CompressorScreen(
    viewModel: CompressorViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    AnimatedContent(
        targetState = uiState.currentScreen,
        transitionSpec = {
            if (targetState == AppScreen.PREPROCESS) {
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

    // Native M3 Spring Rotation Animation for FAB Icon (0 -> 135 deg)
    val fabIconRotation by animateFloatAsState(
        targetValue = if (isFabMenuExpanded) 135f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "fab_icon_rotation"
    )

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
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, mime)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                val chooser = Intent.createChooser(intent, "Buka Berkas Dengan")
                                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(chooser)
                            }
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Gagal membuka media: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
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
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = mime
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                val chooser = Intent.createChooser(intent, "Bagikan Berkas")
                                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(chooser)
                            }
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Gagal membagikan media: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(72.dp))
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

        // 3. Native M3 Speed Dial FAB Menu Overlay (At top layer so buttons receive clicks cleanly!)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp, end = 16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedVisibility(
                    visible = isFabMenuExpanded,
                    enter = fadeIn(tween(150)) + slideInVertically(initialOffsetY = { it / 3 }, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + scaleIn(initialScale = 0.8f),
                    exit = fadeOut(tween(100)) + slideOutVertically(targetOffsetY = { it / 3 }) + scaleOut(targetScale = 0.8f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 1. Video SAF (Supports MP4, AVI, WMV, MKV, FLV, MOV, TS, WebM, 3GP, etc.)
                        ExtendedFloatingActionButton(
                            onClick = {
                                isFabMenuExpanded = false
                                videoPickerLauncher.launch(arrayOf("video/*", "application/octet-stream", "video/x-msvideo", "video/x-ms-wmv", "video/x-matroska", "video/avi", "*/*"))
                            },
                            icon = { Icon(Icons.Default.Movie, contentDescription = null) },
                            text = { Text("Video", fontWeight = FontWeight.Bold) },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            shape = RoundedCornerShape(20.dp),
                            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
                        )

                        // 2. Gambar SAF
                        ExtendedFloatingActionButton(
                            onClick = {
                                isFabMenuExpanded = false
                                imagePickerLauncher.launch(arrayOf("image/*"))
                            },
                            icon = { Icon(Icons.Default.Image, contentDescription = null) },
                            text = { Text("Gambar", fontWeight = FontWeight.Bold) },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            shape = RoundedCornerShape(20.dp),
                            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
                        )

                        // 3. Audio SAF
                        ExtendedFloatingActionButton(
                            onClick = {
                                isFabMenuExpanded = false
                                audioPickerLauncher.launch(arrayOf("audio/*"))
                            },
                            icon = { Icon(Icons.Default.Audiotrack, contentDescription = null) },
                            text = { Text("Audio", fontWeight = FontWeight.Bold) },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            shape = RoundedCornerShape(20.dp),
                            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
                        )
                    }
                }

                // Main FAB Button with Native M3 Icon Morphing Rotation
                FloatingActionButton(
                    onClick = { isFabMenuExpanded = !isFabMenuExpanded },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Menu Input Media",
                        modifier = Modifier.graphicsLayer {
                            rotationZ = fabIconRotation
                        }
                    )
                }
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
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateToMainScreen() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                title = {
                    Text(
                        text = "Pengaturan Enkoder (${media.mediaType.label})",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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

                    Button(
                        onClick = { viewModel.startCompression() },
                        shape = RoundedCornerShape(20.dp),
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
            // Selected Media Preview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = null
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
                        Text(
                            text = if (media.width > 0) "$sizeStr • ${media.width}x${media.height}" else "$sizeStr • ${media.durationSec}s",
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
                            FilterChip(
                                selected = uiState.useAutoBitrate,
                                onClick = { viewModel.setUseAutoBitrate(true) },
                                label = { Text("Default (Auto System)") },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                border = null
                            )

                            FilterChip(
                                selected = !uiState.useAutoBitrate,
                                onClick = { viewModel.setUseAutoBitrate(false) },
                                label = { Text("Custom Bitrate Slider") },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                border = null
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3DropdownSelector(
    label: String,
    selectedLabel: String,
    options: List<String>,
    onOptionSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

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
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = null
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = null
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

            FilledTonalIconButton(
                onClick = onCancel,
                shape = CircleShape,
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
 * Clean & Simplified Material 3 Expressive Circular Wavy Progress Indicator
 */
@Composable
fun M3ExpressiveCircularWavyProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
    val infiniteTransition = rememberInfiniteTransition(label = "m3_expressive_circular_wave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    Canvas(modifier = modifier) {
        val diameter = minOf(size.width, size.height)
        val strokeWidth = 3.5.dp.toPx()
        val radius = (diameter - strokeWidth * 2) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // 1. Draw smooth background track circle
        drawCircle(
            color = trackColor,
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth)
        )

        // 2. Draw active wavy progress arc with 100% CONSISTENT wave frequency along arc length
        val clampedProgress = progress.coerceIn(0.02f, 1.0f)
        val sweepAngle = 360f * clampedProgress
        val numPoints = (sweepAngle * 0.8f).toInt().coerceAtLeast(20)

        val waveAmplitude = 1.8.dp.toPx()
        // Exactly 8 wave cycles per 360 degrees (1 cycle every 45 degrees of arc!)
        val cyclesPerDegree = 8.0 / 360.0

        val wavePath = Path()
        for (i in 0..numPoints) {
            val fraction = i.toFloat() / numPoints
            val angleDegrees = fraction * sweepAngle
            val currentAngleDegrees = -90f + angleDegrees
            val currentAngleRads = Math.toRadians(currentAngleDegrees.toDouble())

            // Absolute angle calculation ensures constant wavelength everywhere along the arc!
            val waveOffset = waveAmplitude * sin(angleDegrees * cyclesPerDegree * 2 * PI + wavePhase)
            val currentRadius = radius + waveOffset

            val x = (center.x + currentRadius * cos(currentAngleRads)).toFloat()
            val y = (center.y + currentRadius * sin(currentAngleRads)).toFloat()

            if (i == 0) {
                wavePath.moveTo(x, y)
            } else {
                wavePath.lineTo(x, y)
            }
        }

        drawPath(
            path = wavePath,
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = null
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
                FilledTonalIconButton(onClick = onPlay, shape = CircleShape) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play/View")
                }
                FilledTonalIconButton(onClick = onShare, shape = CircleShape) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
                FilledTonalIconButton(onClick = onDelete, shape = CircleShape) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}
