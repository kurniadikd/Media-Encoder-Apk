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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.HighQuality
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

    // Intercept native Back button when video is selected
    BackHandler(enabled = uiState.selectedVideo != null && !uiState.isEncoding) {
        viewModel.clearSelectedVideo()
    }

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.onVideoSelected(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (uiState.selectedVideo != null) {
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
                            text = "Hardware Video Encoder",
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
            if (uiState.selectedVideo != null) {
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
                            if (uiState.isEncoding) "Cancel Encoding" else "Start Hardware Compression",
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
            // 1. Hero Header / Media Card (Flat Solid Style)
            HeroMediaCard(
                uiState = uiState,
                onPickVideoClick = {
                    mediaPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                }
            )

            if (uiState.selectedVideo != null) {
                // 2. Expressive Video Codec Selector
                CodecSelectorSection(
                    uiState = uiState,
                    onFormatSelected = { viewModel.setCodecFormat(it) },
                    enabled = !uiState.isEncoding
                )

                // 3. Resolution & Scale Mode Section
                ResolutionAndScaleSection(
                    uiState = uiState,
                    onResolutionSelected = { viewModel.setResolutionPreset(it) },
                    onScaleModeSelected = { viewModel.setScaleModeOption(it) },
                    enabled = !uiState.isEncoding
                )

                // 4. Bitrate & Bitrate Control Mode
                BitrateSection(
                    uiState = uiState,
                    viewModel = viewModel,
                    onBitrateChanged = { viewModel.setTargetBitrate(it) },
                    onBitrateModeSelected = { viewModel.setBitrateModeOption(it) },
                    enabled = !uiState.isEncoding
                )

                // 5. Storage Location Selector Section (Internal vs External/Movies)
                StorageLocationSection(
                    selectedOption = uiState.storageLocationOption,
                    onOptionSelected = { viewModel.setStorageLocationOption(it) },
                    enabled = !uiState.isEncoding
                )

                // 6. Expandable Advanced Hardware Encoder Parameters
                AdvancedParametersSection(
                    uiState = uiState,
                    isExpanded = isAdvancedExpanded,
                    onExpandToggle = { isAdvancedExpanded = !isAdvancedExpanded },
                    viewModel = viewModel,
                    enabled = !uiState.isEncoding
                )

                // 7. File Size Estimate Card (Flat Solid)
                FileSizeEstimateCard(uiState = uiState)

                // 8. Encoding Progress & Status Card
                if (uiState.isEncoding || uiState.completedOutputPath != null || uiState.errorMessage != null) {
                    EncodingStatusCard(uiState = uiState)
                }
            }

            // 9. Encoded Videos History List Section
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
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "video/*")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            val chooser = Intent.createChooser(intent, "Putar Video Dengan")
                            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(chooser)
                        } else {
                            android.widget.Toast.makeText(context, "File tidak ditemukan: ${item.name}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "Gagal membuka video: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
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
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "video/*"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            val chooser = Intent.createChooser(intent, "Bagikan Video")
                            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(chooser)
                        } else {
                            android.widget.Toast.makeText(context, "File tidak ditemukan: ${item.name}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "Gagal membagikan video: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            )

            Spacer(modifier = Modifier.height(64.dp)) // Extra padding for FAB
        }
    }
}

@Composable
fun HeroMediaCard(
    uiState: CompressorUiState,
    onPickVideoClick: () -> Unit
) {
    val video = uiState.selectedVideo

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
            if (video == null) {
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
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Text(
                        text = "Pilih Video untuk Kompresi Hardware",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "MediaCodec Hardware Acceleration • Flat Solid M3 Expressive UI",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )

                    Button(
                        onClick = onPickVideoClick,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        border = null
                    ) {
                        Icon(imageVector = Icons.Default.VideoFile, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pilih Berkas Video", fontWeight = FontWeight.Bold)
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
                                        imageVector = Icons.Default.VideoFile,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = video.fileName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }

                        FilledTonalButton(
                            onClick = onPickVideoClick,
                            shape = RoundedCornerShape(16.dp),
                            enabled = !uiState.isEncoding,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            border = null
                        ) {
                            Text("Ganti Video", fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = {},
                            label = { Text("${video.width}x${video.height}") },
                            leadingIcon = { Icon(Icons.Default.HighQuality, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = null
                        )

                        AssistChip(
                            onClick = {},
                            label = { Text("${video.durationSec}s") },
                            leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = null
                        )

                        val sizeMb = String.format(Locale.US, "%.1f MB", video.sizeBytes / 1_000_000f)
                        AssistChip(
                            onClick = {},
                            label = { Text(sizeMb) },
                            leadingIcon = { Icon(Icons.Default.SdStorage, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = null
                        )
                    }
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
            MimeTypes.VIDEO_AV1 to "AV1 (Next-Gen)"
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
        // Resolution Presets
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

        // Scale Mode
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Modus Skala (Scaling Mode)",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                enabled = enabled,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }

        // Bitrate Control Mode (VBR, CBR, CQ)
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
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            activeContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
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
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Advanced Encoder Parameters",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalIconButton(
                    onClick = onExpandToggle,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Advanced Settings"
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // 1. Frame Rate (FPS)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Target Frame Rate (FPS)",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
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

                    // 2. Keyframe / I-Frame Interval
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Keyframe Interval (I-Frame Seconds)",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
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

                    // 3. Video Rotation
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Rotasi Video (Video Rotation)",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
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

                    // 4. Audio Settings Section
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (uiState.isAudioMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pengaturan Audio Encoder",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (uiState.isAudioMuted) "Mute Audio" else "Sertakan Audio",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Switch(
                                    checked = uiState.isAudioMuted,
                                    onCheckedChange = { viewModel.setAudioMuted(it) },
                                    enabled = enabled,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.error,
                                        checkedTrackColor = MaterialTheme.colorScheme.errorContainer
                                    )
                                )
                            }
                        }

                        if (!uiState.isAudioMuted) {
                            // Audio Format
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(text = "Audio Format Codec", style = MaterialTheme.typography.labelMedium)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(
                                        "DEFAULT" to "Default (Audio Asli)",
                                        MimeTypes.AUDIO_AAC to "AAC",
                                        MimeTypes.AUDIO_OPUS to "Opus",
                                        MimeTypes.AUDIO_AMR_WB to "AMR-WB"
                                    ).forEach { (mime, label) ->
                                        FilterChip(
                                            selected = uiState.audioFormat == mime,
                                            onClick = { viewModel.setAudioFormat(mime) },
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

                            // Audio Bitrate
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(text = "Audio Bitrate", style = MaterialTheme.typography.labelMedium)
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
                }
            }
        }
    }
}

@Composable
fun FileSizeEstimateCard(uiState: CompressorUiState) {
    val video = uiState.selectedVideo ?: return
    val originalMb = video.sizeBytes / 1_000_000f
    val estMb = uiState.estimatedSizeMb
    val reductionPercent = if (originalMb > 0) ((originalMb - estMb) / originalMb * 100).toInt() else 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
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
                Text(
                    text = "Estimasi Ukuran Akhir (Video + Audio)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = String.format(Locale.US, "%.1f MB", estMb),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
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
                Text(
                    text = "Pengodean Hardware Sedang Berjalan",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                LinearProgressIndicator(
                    progress = { uiState.encodingProgress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface
                )
                Text(
                    text = uiState.encodingStatusText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            } else if (uiState.completedOutputPath != null) {
                Text(
                    text = "Kompresi Video Selesai! 🎉",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                Text(
                    text = "Tersimpan di: ${uiState.completedOutputPath}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            } else if (uiState.errorMessage != null) {
                Text(
                    text = "Kompresi Gagal",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                )
                Text(
                    text = uiState.errorMessage ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
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
                Icon(
                    imageVector = Icons.Default.FolderZip,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Daftar File Hasil Encode",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "${historyItems.size} File",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        if (historyItems.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = null
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada file video yang dikompresi.\nHasil kompresi akan muncul di daftar ini.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
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
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sizeMb,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilledTonalIconButton(
                    onClick = onPlay,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play Video")
                }

                FilledTonalIconButton(
                    onClick = onShare,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share Video")
                }

                FilledTonalIconButton(
                    onClick = onDelete,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete File")
                }
            }
        }
    }
}
