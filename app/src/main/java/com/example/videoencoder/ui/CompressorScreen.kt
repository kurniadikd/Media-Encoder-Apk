package com.example.videoencoder.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.media3.common.MimeTypes
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CompressorScreen(
    viewModel: CompressorViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var isAdvancedExpanded by remember { mutableStateOf(false) }
    var showMediaPickerSheet by remember { mutableStateOf(false) }

    // Intercept native Back button when media is selected
    BackHandler(enabled = uiState.selectedMedia != null && !uiState.isEncoding) {
        viewModel.clearSelectedVideo()
    }

    // Launchers for Video, Image, and Audio
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

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (uiState.selectedMedia != null) {
                        IconButton(
                            onClick = {
                                if (!uiState.isEncoding) {
                                    viewModel.clearSelectedVideo()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali"
                            )
                        }
                    }
                },
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
                            text = "Hardware Media Encoder",
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
        },
        floatingActionButton = {
            if (uiState.selectedMedia == null) {
                // FAB to select Media (Video, Image, Audio)
                ExtendedFloatingActionButton(
                    onClick = { showMediaPickerSheet = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Pilih Berkas Media", fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(28.dp)
                )
            } else {
                // FAB to Start or Cancel Hardware Encoding
                ExtendedFloatingActionButton(
                    onClick = {
                        if (uiState.isEncoding) {
                            viewModel.cancelCompression()
                        } else {
                            viewModel.startCompression()
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (uiState.isEncoding) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(
                            if (uiState.isEncoding) "Cancel Encoding" else "Start Encoding (${uiState.selectedMedia?.mediaType?.label})",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    containerColor = if (uiState.isEncoding) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primary,
                    contentColor = if (uiState.isEncoding) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(28.dp)
                )
            }
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
            // 1. Hero Header / Media Card
            HeroMediaCard(
                uiState = uiState,
                onPickMediaClick = { showMediaPickerSheet = true }
            )

            val media = uiState.selectedMedia
            if (media != null) {
                when (media.mediaType) {
                    MediaType.VIDEO -> {
                        // Video Codec Selector
                        CodecSelectorSection(
                            uiState = uiState,
                            onFormatSelected = { viewModel.setCodecFormat(it) },
                            enabled = !uiState.isEncoding
                        )

                        // Resolution & Scale Mode Section
                        ResolutionAndScaleSection(
                            uiState = uiState,
                            onResolutionSelected = { viewModel.setResolutionPreset(it) },
                            onScaleModeSelected = { viewModel.setScaleModeOption(it) },
                            enabled = !uiState.isEncoding
                        )

                        // Bitrate & Control Mode
                        BitrateSection(
                            uiState = uiState,
                            viewModel = viewModel,
                            onBitrateChanged = { viewModel.setTargetBitrate(it) },
                            onBitrateModeSelected = { viewModel.setBitrateModeOption(it) },
                            enabled = !uiState.isEncoding
                        )

                        // Advanced Parameters
                        AdvancedParametersSection(
                            uiState = uiState,
                            isExpanded = isAdvancedExpanded,
                            onExpandToggle = { isAdvancedExpanded = !isAdvancedExpanded },
                            viewModel = viewModel,
                            enabled = !uiState.isEncoding
                        )
                    }
                    MediaType.IMAGE -> {
                        // Image Encoding Options
                        ImageEncodingSection(
                            uiState = uiState,
                            viewModel = viewModel,
                            enabled = !uiState.isEncoding
                        )
                    }
                    MediaType.AUDIO -> {
                        // Audio Encoding Options
                        AudioEncodingSection(
                            uiState = uiState,
                            viewModel = viewModel,
                            enabled = !uiState.isEncoding
                        )
                    }
                }

                // Storage Location Selector Section
                StorageLocationSection(
                    selectedOption = uiState.storageLocationOption,
                    onOptionSelected = { viewModel.setStorageLocationOption(it) },
                    enabled = !uiState.isEncoding
                )

                // File Size Estimate Card
                FileSizeEstimateCard(uiState = uiState)

                // Encoding Progress & Status Card
                if (uiState.isEncoding || uiState.completedOutputPath != null || uiState.errorMessage != null) {
                    EncodingStatusCard(uiState = uiState)
                }
            }

            // Encoded Videos/Media History List Section
            EncodedHistorySection(
                historyItems = uiState.encodedHistory,
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

        // Modal Bottom Sheet for Selecting Media Type (Video, Image, Audio)
        if (showMediaPickerSheet) {
            ModalBottomSheet(
                onDismissRequest = { showMediaPickerSheet = false },
                sheetState = rememberModalBottomSheetState(),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Pilih Jenis Media",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    // Video
                    Card(
                        onClick = {
                            showMediaPickerSheet = false
                            videoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Movie, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("🎥 Berkas Video", fontWeight = FontWeight.Bold)
                                Text("Kompresi Hardware MediaCodec (HEVC, AVC, VP9, AV1)", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    // Image
                    Card(
                        onClick = {
                            showMediaPickerSheet = false
                            imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("🖼️ Berkas Gambar", fontWeight = FontWeight.Bold)
                                Text("Encode & Kompresi WEBP, JPEG, PNG", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    // Audio
                    Card(
                        onClick = {
                            showMediaPickerSheet = false
                            audioPickerLauncher.launch("audio/*")
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Audiotrack, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("🎵 Berkas Audio", fontWeight = FontWeight.Bold)
                                Text("Encode Audio AAC, Opus, AMR-WB", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun HeroMediaCard(
    uiState: CompressorUiState,
    onPickMediaClick: () -> Unit
) {
    val media = uiState.selectedMedia

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            if (media == null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Text(
                        text = "Hardware Video, Gambar & Audio Encoder",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Kompresi Hardware MediaCodec • Dynamic M3 Expressive UI",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )

                    Button(
                        onClick = onPickMediaClick,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        border = null
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pilih Berkas Media (FAB)", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = when (media.mediaType) {
                                            MediaType.IMAGE -> Icons.Default.Image
                                            MediaType.AUDIO -> Icons.Default.Audiotrack
                                            else -> Icons.Default.VideoFile
                                        },
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = media.fileName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }

                        FilledTonalButton(
                            onClick = onPickMediaClick,
                            shape = RoundedCornerShape(16.dp),
                            enabled = !uiState.isEncoding,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            border = null
                        ) {
                            Text("Ganti Media", fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (media.width > 0 && media.height > 0) {
                            AssistChip(
                                onClick = {},
                                label = { Text("${media.width}x${media.height}") },
                                leadingIcon = { Icon(Icons.Default.HighQuality, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                border = null
                            )
                        }

                        if (media.durationSec > 0) {
                            AssistChip(
                                onClick = {},
                                label = { Text("${media.durationSec}s") },
                                leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                border = null
                            )
                        }

                        val sizeMb = String.format(Locale.US, "%.1f MB", media.sizeBytes / 1_000_000f)
                        AssistChip(
                            onClick = {},
                            label = { Text(sizeMb) },
                            leadingIcon = { Icon(Icons.Default.SdStorage, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            border = null
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ImageEncodingSection(
    uiState: CompressorUiState,
    viewModel: CompressorViewModel,
    enabled: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Pengaturan Pengodean Gambar",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        // Format
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Format Gambar Target", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("WEBP" to "WEBP (Next-Gen)", "JPEG" to "JPEG", "PNG" to "PNG (Lossless)").forEach { (fmt, label) ->
                    FilterChip(
                        selected = uiState.imageFormat == fmt,
                        onClick = { viewModel.setImageFormat(fmt) },
                        label = { Text(label, fontWeight = FontWeight.Bold) },
                        enabled = enabled,
                        shape = RoundedCornerShape(14.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        border = null
                    )
                }
            }
        }

        // Quality Slider
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Kualitas Kompresi", style = MaterialTheme.typography.labelLarge)
                Text(text = "${uiState.imageQuality}%", fontWeight = FontWeight.Bold)
            }
            Slider(
                value = uiState.imageQuality.toFloat(),
                onValueChange = { viewModel.setImageQuality(it.toInt()) },
                valueRange = 10f..100f,
                steps = 17,
                enabled = enabled
            )
        }

        // Resizing Scale Chips
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Ukuran Dimensi (Resizing)", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(100 to "Original (100%)", 75 to "75%", 50 to "50%", 25 to "25%").forEach { (scale, label) ->
                    FilterChip(
                        selected = uiState.imageScalePercent == scale,
                        onClick = { viewModel.setImageScalePercent(scale) },
                        label = { Text(label) },
                        enabled = enabled,
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        border = null
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AudioEncodingSection(
    uiState: CompressorUiState,
    viewModel: CompressorViewModel,
    enabled: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Pengaturan Pengodean Audio",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        // Audio Codec
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Audio Codec Target", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "DEFAULT" to "Default (AAC)",
                    MimeTypes.AUDIO_AAC to "AAC",
                    MimeTypes.AUDIO_OPUS to "Opus",
                    MimeTypes.AUDIO_AMR_WB to "AMR-WB"
                ).forEach { (mime, label) ->
                    FilterChip(
                        selected = uiState.audioFormat == mime,
                        onClick = { viewModel.setAudioFormat(mime) },
                        label = { Text(label, fontWeight = FontWeight.Bold) },
                        enabled = enabled,
                        shape = RoundedCornerShape(14.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        border = null
                    )
                }
            }
        }

        // Audio Bitrate
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Target Audio Bitrate", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0 to "Default (Auto)", 320 to "320 kbps", 256 to "256 kbps", 192 to "192 kbps", 128 to "128 kbps", 96 to "96 kbps", 64 to "64 kbps").forEach { (kbps, label) ->
                    FilterChip(
                        selected = uiState.audioBitrateKbps == kbps,
                        onClick = { viewModel.setAudioBitrate(kbps) },
                        label = { Text(label) },
                        enabled = enabled,
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        border = null
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CodecSelectorSection(
    uiState: CompressorUiState,
    onFormatSelected: (String) -> Unit,
    enabled: Boolean
) {
    val codecs = if (uiState.availableVideoEncoders.isNotEmpty()) {
        uiState.availableVideoEncoders
    } else {
        listOf(
            "DEFAULT" to "Default (Bawaan System)",
            MimeTypes.VIDEO_H265 to "HEVC (H.265)",
            MimeTypes.VIDEO_H264 to "AVC (H.264)",
            MimeTypes.VIDEO_VP9 to "VP9",
            MimeTypes.VIDEO_AV1 to "AV1"
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Video Codec Encoder",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            codecs.forEach { (mime, label) ->
                FilterChip(
                    selected = uiState.outputFormat == mime,
                    onClick = { onFormatSelected(mime) },
                    label = { Text(label, fontWeight = FontWeight.Bold) },
                    leadingIcon = if (uiState.outputFormat == mime) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    enabled = enabled,
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    border = null
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ResolutionAndScaleSection(
    uiState: CompressorUiState,
    onResolutionSelected: (ResolutionPreset) -> Unit,
    onScaleModeSelected: (ScaleModeOption) -> Unit,
    enabled: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Target Resolusi",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ResolutionPreset.values().forEach { preset ->
                    FilterChip(
                        selected = uiState.resolutionPreset == preset,
                        onClick = { onResolutionSelected(preset) },
                        label = { Text(preset.label, fontWeight = FontWeight.SemiBold) },
                        leadingIcon = if (uiState.resolutionPreset == preset) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        enabled = enabled,
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        border = null
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Modus Skala (Scaling Mode)",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ScaleModeOption.values().forEach { option ->
                    FilterChip(
                        selected = uiState.scaleModeOption == option,
                        onClick = { onScaleModeSelected(option) },
                        label = { Text(option.label) },
                        enabled = enabled,
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        border = null
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BitrateSection(
    uiState: CompressorUiState,
    viewModel: CompressorViewModel,
    onBitrateChanged: (Float) -> Unit,
    onBitrateModeSelected: (BitrateModeOption) -> Unit,
    enabled: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Target Video Bitrate",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (uiState.useAutoBitrate) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = if (uiState.useAutoBitrate) "Default (Auto)" else String.format(Locale.US, "%.1f Mbps", uiState.targetBitrateMbps),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (uiState.useAutoBitrate) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = uiState.useAutoBitrate,
                onClick = { viewModel.setUseAutoBitrate(true) },
                label = { Text("Default (Auto System)") },
                enabled = enabled,
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
                label = { Text("Custom Bitrate") },
                enabled = enabled,
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
                onValueChange = onBitrateChanged,
                valueRange = 1.0f..30.0f,
                steps = 57,
                enabled = enabled
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Bitrate Control Mode",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                BitrateModeOption.values().forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = uiState.bitrateModeOption == option,
                        onClick = { onBitrateModeSelected(option) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = BitrateModeOption.values().size),
                        enabled = enabled,
                        border = SegmentedButtonDefaults.borderStroke(color = Color.Transparent)
                    ) {
                        Text(option.label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageLocationSection(
    selectedOption: StorageLocationOption,
    onOptionSelected: (StorageLocationOption) -> Unit,
    enabled: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Lokasi Penyimpanan Output",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            StorageLocationOption.values().forEach { option ->
                val isSelected = selectedOption == option
                Card(
                    onClick = { if (enabled) onOptionSelected(option) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (option == StorageLocationOption.EXTERNAL_MOVIES) Icons.Default.Folder else Icons.Default.SdCard,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = option.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AdvancedParametersSection(
    uiState: CompressorUiState,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    viewModel: CompressorViewModel,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Advanced Hardware Parameters", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }

                FilledTonalIconButton(
                    onClick = onExpandToggle,
                    shape = CircleShape
                ) {
                    Icon(if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Frame Rate
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Target Frame Rate (FPS)", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(0 to "Default (FPS Asli)", 60 to "60 FPS", 50 to "50 FPS", 30 to "30 FPS", 24 to "24 FPS", 15 to "15 FPS").forEach { (fps, label) ->
                                FilterChip(
                                    selected = uiState.frameRate == fps,
                                    onClick = { viewModel.setFrameRate(fps) },
                                    label = { Text(label) },
                                    enabled = enabled,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    border = null
                                )
                            }
                        }
                    }

                    // Keyframe Interval
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Keyframe Interval (I-Frame Seconds)", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(0.0f to "Default (Auto)", 1.0f to "1.0s", 2.0f to "2.0s", 3.0f to "3.0s", 5.0f to "5.0s").forEach { (sec, label) ->
                                FilterChip(
                                    selected = uiState.iFrameIntervalSec == sec,
                                    onClick = { viewModel.setIFrameInterval(sec) },
                                    label = { Text(label) },
                                    enabled = enabled,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    border = null
                                )
                            }
                        }
                    }

                    // Rotation
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Rotasi Video", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(0.0f to "0° (Normal)", 90.0f to "90°", 180.0f to "180°", 270.0f to "270°").forEach { (deg, label) ->
                                FilterChip(
                                    selected = uiState.rotationDegrees == deg,
                                    onClick = { viewModel.setRotationDegrees(deg) },
                                    label = { Text(label) },
                                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    enabled = enabled,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    border = null
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FileSizeEstimateCard(uiState: CompressorUiState) {
    val media = uiState.selectedMedia ?: return
    val originalMb = media.sizeBytes / 1_000_000f
    val estMb = uiState.estimatedSizeMb
    val reductionPercent = if (originalMb > 0 && estMb < originalMb) ((originalMb - estMb) / originalMb * 100).toInt() else 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        border = null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Estimasi Ukuran Akhir", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = String.format(Locale.US, "%.2f MB", estMb),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (reductionPercent > 0) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "-$reductionPercent% Lebih Hemat",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EncodingStatusCard(uiState: CompressorUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                uiState.errorMessage != null -> MaterialTheme.colorScheme.errorContainer
                uiState.completedOutputPath != null -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (uiState.isEncoding) {
                Text("Pengodean Sedang Berjalan", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                LinearProgressIndicator(
                    progress = { uiState.encodingProgress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                )
                Text(uiState.encodingStatusText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            } else if (uiState.completedOutputPath != null) {
                Text("Pengodean Media Selesai! 🎉", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text("Tersimpan di: ${uiState.completedOutputPath}", style = MaterialTheme.typography.bodySmall)
            } else if (uiState.errorMessage != null) {
                Text("Pengodean Gagal", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(uiState.errorMessage ?: "", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun EncodedHistorySection(
    historyItems: List<EncodedFileItem>,
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
                Text("Daftar File Hasil Encode", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            }

            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Text(
                    text = "${historyItems.size} File",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        if (historyItems.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = null
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Belum ada file media yang dikompresi.\nHasil pengodean akan muncul di daftar ini.", style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                historyItems.forEach { item ->
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
