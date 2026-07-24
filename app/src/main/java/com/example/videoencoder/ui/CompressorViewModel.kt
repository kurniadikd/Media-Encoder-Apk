package com.example.videoencoder.ui

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MimeTypes
import androidx.media3.effect.Presentation
import com.example.videoencoder.engine.EncodingConfig
import com.example.videoencoder.service.EncodingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

enum class ResolutionPreset(val label: String, val width: Int, val height: Int) {
    ORIGINAL("Original", 0, 0),
    RES_4K("4K (2160p)", 3840, 2160),
    FHD_1080P("1080p (FHD)", 1920, 1080),
    HD_720P("720p (HD)", 1280, 720),
    SD_480P("480p (SD)", 854, 480),
    SD_360P("360p (Low)", 640, 360)
}

enum class BitrateModeOption(val label: String, val modeValue: Int) {
    VBR("VBR (Variable)", 1),
    CBR("CBR (Constant)", 2),
    CQ("CQ (Constant Quality)", 0)
}

enum class ScaleModeOption(val label: String, val modeValue: Int) {
    FIT("Fit Inside", Presentation.LAYOUT_SCALE_TO_FIT),
    CROP("Crop to Fit", Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP),
    STRETCH("Stretch", Presentation.LAYOUT_STRETCH_TO_FIT)
}

enum class StorageLocationOption(val label: String, val subtitle: String) {
    EXTERNAL_MOVIES("Eksternal / Movies", "Tersimpan di Galeri & Folder Movies HP"),
    INTERNAL_APP("Internal App", "Penyimpanan Privat Aplikasi")
}

data class SelectedVideoMetadata(
    val uri: Uri,
    val fileName: String,
    val sizeBytes: Long,
    val durationSec: Long,
    val width: Int,
    val height: Int
)

data class EncodedFileItem(
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val lastModifiedMs: Long
)

data class CompressorUiState(
    val selectedVideo: SelectedVideoMetadata? = null,
    
    // Video Codec & Hardware Parameters
    val outputFormat: String = MimeTypes.VIDEO_H265,      // H.265 (HEVC), H.264 (AVC), VP9, AV1
    val targetBitrateMbps: Float = 4.0f,                    // 4 Mbps default
    val bitrateModeOption: BitrateModeOption = BitrateModeOption.VBR,
    val resolutionPreset: ResolutionPreset = ResolutionPreset.FHD_1080P,
    val scaleModeOption: ScaleModeOption = ScaleModeOption.FIT,
    val frameRate: Int = 30,                                // 15, 24, 30, 50, 60 FPS
    val iFrameIntervalSec: Float = 2.0f,                    // Keyframe interval
    val rotationDegrees: Float = 0.0f,                      // 0, 90, 180, 270 degrees

    // Audio Parameters
    val audioFormat: String = MimeTypes.AUDIO_AAC,         // AAC, Opus, AMR_WB
    val audioBitrateKbps: Int = 128,                       // 64, 96, 128, 192, 256, 320 kbps
    val isAudioMuted: Boolean = false,                      // Mute Audio

    // Storage Destination
    val storageLocationOption: StorageLocationOption = StorageLocationOption.EXTERNAL_MOVIES,

    // Available Hardware Encoders
    val availableVideoEncoders: List<Pair<String, String>> = emptyList(),

    // Encoding & UI State
    val estimatedSizeMb: Float = 0.0f,
    val isEncoding: Boolean = false,
    val encodingProgress: Int = 0,
    val encodingStatusText: String = "",
    val completedOutputPath: String? = null,
    val errorMessage: String? = null,
    val encodedHistory: List<EncodedFileItem> = emptyList()
)

class CompressorViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CompressorUiState())
    val uiState: StateFlow<CompressorUiState> = _uiState.asStateFlow()

    private var encodingService: EncodingService? = null
    private var isServiceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as EncodingService.LocalBinder
            encodingService = binder.getService()
            isServiceBound = true

            // Observe service progress
            viewModelScope.launch {
                encodingService?.progressState?.collect { serviceState ->
                    when (serviceState.status) {
                        EncodingService.EncodingStatus.RUNNING -> {
                            _uiState.update {
                                it.copy(
                                    isEncoding = true,
                                    encodingProgress = serviceState.progressPercent,
                                    encodingStatusText = "Encoding... ${serviceState.progressPercent}%",
                                    errorMessage = null
                                )
                            }
                        }
                        EncodingService.EncodingStatus.COMPLETED -> {
                            _uiState.update {
                                it.copy(
                                    isEncoding = false,
                                    encodingProgress = 100,
                                    encodingStatusText = "Done!",
                                    completedOutputPath = serviceState.outputPath,
                                    errorMessage = null
                                )
                            }
                            refreshEncodedHistory()
                        }
                        EncodingService.EncodingStatus.ERROR -> {
                            _uiState.update {
                                it.copy(
                                    isEncoding = false,
                                    encodingProgress = 0,
                                    encodingStatusText = "Error",
                                    errorMessage = serviceState.errorMessage
                                )
                            }
                        }
                        EncodingService.EncodingStatus.CANCELLED -> {
                            _uiState.update {
                                it.copy(
                                    isEncoding = false,
                                    encodingProgress = 0,
                                    encodingStatusText = "Cancelled"
                                )
                            }
                        }
                        EncodingService.EncodingStatus.IDLE -> {}
                    }
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            encodingService = null
            isServiceBound = false
        }
    }

    init {
        // Bind to EncodingService
        val intent = Intent(application, EncodingService::class.java)
        application.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        discoverHardwareEncoders()
        refreshEncodedHistory()
    }

    private fun discoverHardwareEncoders() {
        val result = mutableListOf<Pair<String, String>>()
        try {
            val codecList = android.media.MediaCodecList(android.media.MediaCodecList.REGULAR_CODECS)
            for (info in codecList.codecInfos) {
                if (!info.isEncoder) continue
                for (type in info.supportedTypes) {
                    if (type.startsWith("video/")) {
                        val label = when (type) {
                            MimeTypes.VIDEO_H265 -> "HEVC (H.265)"
                            MimeTypes.VIDEO_H264 -> "AVC (H.264)"
                            MimeTypes.VIDEO_VP9 -> "VP9"
                            MimeTypes.VIDEO_AV1 -> "AV1"
                            MimeTypes.VIDEO_VP8 -> "VP8"
                            MimeTypes.VIDEO_H263 -> "H.263"
                            MimeTypes.VIDEO_MP4V -> "MPEG-4"
                            else -> type.removePrefix("video/").uppercase()
                        }
                        val nameClean = info.name.replace("OMX.", "").replace("c2.", "")
                        if (!result.any { it.first == type }) {
                            result.add(type to "$label ($nameClean)")
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        if (result.isEmpty()) {
            result.add(MimeTypes.VIDEO_H265 to "HEVC (H.265)")
            result.add(MimeTypes.VIDEO_H264 to "AVC (H.264)")
            result.add(MimeTypes.VIDEO_VP9 to "VP9")
            result.add(MimeTypes.VIDEO_AV1 to "AV1")
        }

        _uiState.update { it.copy(availableVideoEncoders = result) }
    }

    fun refreshEncodedHistory() {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            val directoriesToScan = mutableListOf<File>()
            
            // External Movies / DCIM
            val externalPublicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            if (externalPublicDir != null && externalPublicDir.exists()) {
                directoriesToScan.add(externalPublicDir)
            }

            // External App Files Movies
            context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)?.let {
                if (it.exists()) directoriesToScan.add(it)
            }

            // Internal Files
            directoriesToScan.add(context.filesDir)

            val fileMap = mutableMapOf<String, EncodedFileItem>()
            directoriesToScan.forEach { dir ->
                dir.listFiles()
                    ?.filter { it.isFile && (it.name.endsWith(".mp4") || it.name.endsWith(".mkv") || it.name.endsWith(".webm")) }
                    ?.forEach { file ->
                        fileMap[file.absolutePath] = EncodedFileItem(
                            name = file.name,
                            path = file.absolutePath,
                            sizeBytes = file.length(),
                            lastModifiedMs = file.lastModified()
                        )
                    }
            }

            val sortedFiles = fileMap.values.sortedByDescending { it.lastModifiedMs }
            _uiState.update { it.copy(encodedHistory = sortedFiles) }
        }
    }

    fun deleteHistoryItem(path: String) {
        viewModelScope.launch {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
            refreshEncodedHistory()
        }
    }

    fun onVideoSelected(uri: Uri) {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
                val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 1920
                val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 1080
                
                var fileSize = 0L
                context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { afd ->
                    fileSize = afd.length
                }

                val fileName = uri.lastPathSegment ?: "Selected Video"
                val metadata = SelectedVideoMetadata(
                    uri = uri,
                    fileName = fileName,
                    sizeBytes = fileSize,
                    durationSec = durationMs / 1000,
                    width = width,
                    height = height
                )

                _uiState.update { current ->
                    val updated = current.copy(
                        selectedVideo = metadata,
                        completedOutputPath = null,
                        errorMessage = null
                    )
                    updated.copy(estimatedSizeMb = calculateEstimatedSize(updated))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to parse video metadata: ${e.localizedMessage}") }
            } finally {
                try { retriever.release() } catch (_: Exception) {}
            }
        }
    }

    fun setCodecFormat(format: String) {
        _uiState.update { current ->
            val updated = current.copy(outputFormat = format)
            updated.copy(estimatedSizeMb = calculateEstimatedSize(updated))
        }
    }

    fun setTargetBitrate(bitrateMbps: Float) {
        _uiState.update { current ->
            val updated = current.copy(targetBitrateMbps = bitrateMbps)
            updated.copy(estimatedSizeMb = calculateEstimatedSize(updated))
        }
    }

    fun setBitrateModeOption(option: BitrateModeOption) {
        _uiState.update { it.copy(bitrateModeOption = option) }
    }

    fun setResolutionPreset(preset: ResolutionPreset) {
        _uiState.update { current ->
            val updated = current.copy(resolutionPreset = preset)
            updated.copy(estimatedSizeMb = calculateEstimatedSize(updated))
        }
    }

    fun setScaleModeOption(option: ScaleModeOption) {
        _uiState.update { it.copy(scaleModeOption = option) }
    }

    fun setFrameRate(fps: Int) {
        _uiState.update { it.copy(frameRate = fps) }
    }

    fun setIFrameInterval(seconds: Float) {
        _uiState.update { it.copy(iFrameIntervalSec = seconds) }
    }

    fun setRotationDegrees(degrees: Float) {
        _uiState.update { it.copy(rotationDegrees = degrees) }
    }

    fun setAudioFormat(format: String) {
        _uiState.update { it.copy(audioFormat = format) }
    }

    fun setAudioBitrate(kbps: Int) {
        _uiState.update { current ->
            val updated = current.copy(audioBitrateKbps = kbps)
            updated.copy(estimatedSizeMb = calculateEstimatedSize(updated))
        }
    }

    fun setAudioMuted(muted: Boolean) {
        _uiState.update { current ->
            val updated = current.copy(isAudioMuted = muted)
            updated.copy(estimatedSizeMb = calculateEstimatedSize(updated))
        }
    }

    fun setStorageLocationOption(option: StorageLocationOption) {
        _uiState.update { it.copy(storageLocationOption = option) }
    }

    fun startCompression() {
        val state = _uiState.value
        val video = state.selectedVideo ?: return

        val context = getApplication<Application>().applicationContext

        val outputDir = if (state.storageLocationOption == StorageLocationOption.EXTERNAL_MOVIES) {
            val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            if (publicDir != null && !publicDir.exists()) {
                publicDir.mkdirs()
            }
            publicDir ?: (context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir)
        } else {
            context.filesDir
        }

        val extension = "mp4"
        val outputFile = File(outputDir, "compressed_${System.currentTimeMillis()}.$extension")

        val targetWidth = if (state.resolutionPreset == ResolutionPreset.ORIGINAL) video.width else state.resolutionPreset.width
        val targetHeight = if (state.resolutionPreset == ResolutionPreset.ORIGINAL) video.height else state.resolutionPreset.height

        val bitrateBps = (state.targetBitrateMbps * 1_000_000).toInt()
        val audioBitrateBps = state.audioBitrateKbps * 1000

        val serviceIntent = Intent(context, EncodingService::class.java).apply {
            action = EncodingService.ACTION_START
            putExtra(EncodingService.EXTRA_INPUT_URI, video.uri.toString())
            putExtra(EncodingService.EXTRA_OUTPUT_PATH, outputFile.absolutePath)
            putExtra(EncodingService.EXTRA_VIDEO_FORMAT, state.outputFormat)
            putExtra(EncodingService.EXTRA_BITRATE, bitrateBps)
            putExtra(EncodingService.EXTRA_BITRATE_MODE, state.bitrateModeOption.modeValue)
            putExtra(EncodingService.EXTRA_WIDTH, targetWidth)
            putExtra(EncodingService.EXTRA_HEIGHT, targetHeight)
            putExtra(EncodingService.EXTRA_SCALE_MODE, state.scaleModeOption.modeValue)
            putExtra(EncodingService.EXTRA_FPS, state.frameRate)
            putExtra(EncodingService.EXTRA_IFRAME_SEC, state.iFrameIntervalSec)
            putExtra(EncodingService.EXTRA_AUDIO_FORMAT, state.audioFormat)
            putExtra(EncodingService.EXTRA_AUDIO_BITRATE, audioBitrateBps)
            putExtra(EncodingService.EXTRA_AUDIO_MUTED, state.isAudioMuted)
            putExtra(EncodingService.EXTRA_ROTATION, state.rotationDegrees)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    fun cancelCompression() {
        val context = getApplication<Application>().applicationContext
        val serviceIntent = Intent(context, EncodingService::class.java).apply {
            action = EncodingService.ACTION_CANCEL
        }
        context.startService(serviceIntent)
    }

    private fun calculateEstimatedSize(state: CompressorUiState): Float {
        val video = state.selectedVideo ?: return 0.0f
        val videoBitrateBps = state.targetBitrateMbps * 1_000_000f
        val audioBitrateBps = if (state.isAudioMuted) 0f else state.audioBitrateKbps * 1000f
        val totalBitrateBps = videoBitrateBps + audioBitrateBps
        // Est Size (MB) = (Total Bitrate (bps) / 8 * Duration (s)) / 1,000,000
        return (totalBitrateBps / 8.0f * video.durationSec) / 1_000_000f
    }

    override fun onCleared() {
        if (isServiceBound) {
            getApplication<Application>().unbindService(serviceConnection)
            isServiceBound = false
        }
        super.onCleared()
    }
}
