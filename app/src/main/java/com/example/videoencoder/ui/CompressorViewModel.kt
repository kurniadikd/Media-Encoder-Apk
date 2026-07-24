package com.example.videoencoder.ui

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.os.IBinder
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MimeTypes
import androidx.media3.effect.Presentation
import com.example.videoencoder.engine.EncodingConfig
import com.example.videoencoder.engine.VideoEncodingEngine
import com.example.videoencoder.service.EncodingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppScreen {
    MAIN, PREPROCESS, LOGS
}

enum class LogLevel {
    INFO, SUCCESS, WARNING, ERROR
}

data class LogEntry(
    val timestamp: String,
    val level: LogLevel,
    val message: String,
    val tag: String = "SYSTEM"
)

enum class MediaType(val label: String) {
    VIDEO("Video"),
    IMAGE("Gambar"),
    AUDIO("Audio")
}

enum class ResolutionPreset(val label: String, val width: Int, val height: Int) {
    DEFAULT("Default (Bawaan Video)", 0, 0),
    RES_4K("4K (2160p)", 3840, 2160),
    FHD_1080P("1080p (FHD)", 1920, 1080),
    HD_720P("720p (HD)", 1280, 720),
    SD_480P("480p (SD)", 854, 480),
    SD_360P("360p (Low)", 640, 360)
}

enum class BitrateModeOption(val label: String, val modeValue: Int) {
    DEFAULT("Default (Auto VBR)", 1),
    VBR("VBR (Variable Bitrate)", 1),
    CBR("CBR (Constant Bitrate)", 2),
    CQ("CQ (Constant Quality)", 0)
}

enum class ScaleModeOption(val label: String, val modeValue: Int) {
    DEFAULT("Default (Bawaan Video)", Presentation.LAYOUT_SCALE_TO_FIT),
    FIT("Fit Inside", Presentation.LAYOUT_SCALE_TO_FIT),
    CROP("Crop to Fit", Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP),
    STRETCH("Stretch", Presentation.LAYOUT_STRETCH_TO_FIT)
}

enum class StorageLocationOption(val label: String, val subtitle: String) {
    EXTERNAL_MOVIES("Eksternal / Movies", "Tersimpan di Galeri & Folder Movies HP"),
    INTERNAL_APP("Internal App", "Penyimpanan Privat Aplikasi")
}

data class SelectedMediaItem(
    val uri: Uri,
    val mediaType: MediaType,
    val fileName: String,
    val sizeBytes: Long,
    val durationSec: Long = 0,
    val width: Int = 0,
    val height: Int = 0
)

data class EncodedFileItem(
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val lastModifiedMs: Long,
    val mediaType: MediaType,
    val isEncodingActive: Boolean = false,
    val progressPercent: Int = 100,
    val statusText: String = ""
)

data class CompressorUiState(
    val currentScreen: AppScreen = AppScreen.MAIN,
    val selectedMedia: SelectedMediaItem? = null,
    
    // Video Codec & Hardware Parameters
    val outputFormat: String = "DEFAULT",
    val useAutoBitrate: Boolean = true,
    val targetBitrateMbps: Float = 4.0f,
    val bitrateModeOption: BitrateModeOption = BitrateModeOption.DEFAULT,
    val resolutionPreset: ResolutionPreset = ResolutionPreset.DEFAULT,
    val scaleModeOption: ScaleModeOption = ScaleModeOption.DEFAULT,
    val frameRate: Int = 0,
    val iFrameIntervalSec: Float = 0.0f,
    val rotationDegrees: Float = 0.0f,

    // Audio Parameters
    val audioFormat: String = "DEFAULT",
    val audioBitrateKbps: Int = 0,
    val isAudioMuted: Boolean = false,

    // Image Encoding Parameters
    val imageFormat: String = "WEBP",
    val imageQuality: Int = 80,
    val imageScalePercent: Int = 100,

    // Storage Destination
    val storageLocationOption: StorageLocationOption = StorageLocationOption.EXTERNAL_MOVIES,

    // Available Hardware Encoders
    val availableVideoEncoders: List<Pair<String, String>> = emptyList(),

    // Encoding & UI State
    val estimatedSizeMb: Float = 0.0f,
    val isEncoding: Boolean = false,
    val encodingProgress: Int = 0,
    val encodingStatusText: String = "",
    val activeEncodingFileName: String? = null,
    val completedOutputPath: String? = null,
    val errorMessage: String? = null,
    val encodedHistory: List<EncodedFileItem> = emptyList(),
    val logList: List<LogEntry> = emptyList()
)

class CompressorViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CompressorUiState())
    val uiState: StateFlow<CompressorUiState> = _uiState.asStateFlow()

    private var encodingService: EncodingService? = null
    private var isServiceBound = false
    private var encodingStartTimeMs: Long = 0L

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as EncodingService.LocalBinder
            encodingService = binder.getService()
            isServiceBound = true
            addLog("Service Pengodean terhubung.", LogLevel.INFO, "SERVICE")

            viewModelScope.launch {
                encodingService?.progressState?.collect { serviceState ->
                    when (serviceState.status) {
                        EncodingService.EncodingStatus.RUNNING -> {
                            val currentProgress = serviceState.progressPercent
                            val totalEstMb = _uiState.value.estimatedSizeMb
                            val currentMb = totalEstMb * (currentProgress / 100.0f)
                            val sizeText = String.format(Locale.US, "%.1f MB / %.1f MB", currentMb, totalEstMb)

                            val elapsedMs = System.currentTimeMillis() - encodingStartTimeMs
                            val timeText = if (currentProgress > 3 && elapsedMs > 500) {
                                val totalEstMs = (elapsedMs.toFloat() / (currentProgress / 100.0f)).toLong()
                                val remainingMs = (totalEstMs - elapsedMs).coerceAtLeast(0L)
                                val remainingSec = Math.round(remainingMs / 1000.0f)
                                "Sisa ~${remainingSec}s"
                            } else {
                                "Menghitung sisa waktu..."
                            }

                            _uiState.update {
                                it.copy(
                                    isEncoding = true,
                                    encodingProgress = currentProgress,
                                    encodingStatusText = "$sizeText • $timeText",
                                    errorMessage = null
                                )
                            }
                        }
                        EncodingService.EncodingStatus.COMPLETED -> {
                            addLog("Pengodean Hardware Selesai! File output: ${serviceState.outputPath}", LogLevel.SUCCESS, "HARDWARE")
                            _uiState.update {
                                it.copy(
                                    isEncoding = false,
                                    encodingProgress = 100,
                                    encodingStatusText = "Selesai!",
                                    completedOutputPath = serviceState.outputPath,
                                    errorMessage = null,
                                    activeEncodingFileName = null
                                )
                            }
                            refreshEncodedHistory()
                        }
                        EncodingService.EncodingStatus.ERROR -> {
                            addLog("Pengodean Hardware Gagal: ${serviceState.errorMessage}", LogLevel.ERROR, "HARDWARE")
                            _uiState.update {
                                it.copy(
                                    isEncoding = false,
                                    encodingProgress = 0,
                                    encodingStatusText = "Gagal",
                                    errorMessage = serviceState.errorMessage,
                                    activeEncodingFileName = null
                                )
                            }
                        }
                        EncodingService.EncodingStatus.CANCELLED -> {
                            addLog("Proses Pengodean dibatalkan oleh pengguna.", LogLevel.WARNING, "USER")
                            _uiState.update {
                                it.copy(
                                    isEncoding = false,
                                    encodingProgress = 0,
                                    encodingStatusText = "Dibatalkan",
                                    activeEncodingFileName = null
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
            addLog("Service Pengodean terputus.", LogLevel.WARNING, "SERVICE")
        }
    }

    init {
        addLog("Aplikasi Media Encoder diinisialisasi.", LogLevel.INFO, "SYSTEM")
        val intent = Intent(application, EncodingService::class.java)
        application.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        discoverHardwareEncoders()
        refreshEncodedHistory()
    }

    fun addLog(message: String, level: LogLevel = LogLevel.INFO, tag: String = "SYSTEM") {
        val timeStr = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val entry = LogEntry(timestamp = timeStr, level = level, message = message, tag = tag)
        _uiState.update { current ->
            val updatedLogs = current.logList + entry
            current.copy(logList = updatedLogs)
        }
    }

    fun clearLogs() {
        _uiState.update { it.copy(logList = emptyList()) }
        addLog("Riwayat log telah dibersihkan.", LogLevel.INFO, "SYSTEM")
    }

    fun navigateToLogsScreen() {
        _uiState.update { it.copy(currentScreen = AppScreen.LOGS) }
    }

    private fun discoverHardwareEncoders() {
        val result = mutableListOf<Pair<String, String>>()
        result.add("DEFAULT" to "Default (Bawaan System)")

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

        if (result.size <= 1) {
            result.add(MimeTypes.VIDEO_H265 to "HEVC (H.265)")
            result.add(MimeTypes.VIDEO_H264 to "AVC (H.264)")
            result.add(MimeTypes.VIDEO_VP9 to "VP9")
            result.add(MimeTypes.VIDEO_AV1 to "AV1")
        }

        addLog("Penemuan Hardware Encoders Perangkat: ${result.size - 1} encoder terdeteksi.", LogLevel.INFO, "HARDWARE")
        _uiState.update { it.copy(availableVideoEncoders = result) }
    }

    fun refreshEncodedHistory() {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            val directoriesToScan = mutableListOf<File>()
            
            val externalPublicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            if (externalPublicDir != null && externalPublicDir.exists()) {
                directoriesToScan.add(externalPublicDir)
            }

            context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)?.let {
                if (it.exists()) directoriesToScan.add(it)
            }

            directoriesToScan.add(context.filesDir)

            val fileMap = mutableMapOf<String, EncodedFileItem>()
            directoriesToScan.forEach { dir ->
                dir.listFiles()
                    ?.filter { file ->
                        file.isFile && (
                            file.name.endsWith(".mp4") || file.name.endsWith(".mkv") || file.name.endsWith(".webm") || file.name.endsWith(".avi") || file.name.endsWith(".wmv") ||
                            file.name.endsWith(".jpg") || file.name.endsWith(".jpeg") || file.name.endsWith(".png") || file.name.endsWith(".webp") ||
                            file.name.endsWith(".m4a") || file.name.endsWith(".aac") || file.name.endsWith(".opus") || file.name.endsWith(".mp3")
                        )
                    }
                    ?.forEach { file ->
                        val mediaType = when {
                            file.name.endsWith(".jpg") || file.name.endsWith(".jpeg") || file.name.endsWith(".png") || file.name.endsWith(".webp") -> MediaType.IMAGE
                            file.name.endsWith(".m4a") || file.name.endsWith(".aac") || file.name.endsWith(".opus") || file.name.endsWith(".mp3") -> MediaType.AUDIO
                            else -> MediaType.VIDEO
                        }
                        fileMap[file.absolutePath] = EncodedFileItem(
                            name = file.name,
                            path = file.absolutePath,
                            sizeBytes = file.length(),
                            lastModifiedMs = file.lastModified(),
                            mediaType = mediaType,
                            isEncodingActive = false,
                            progressPercent = 100,
                            statusText = "Selesai"
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
                addLog("File dihapus: ${file.name}", LogLevel.INFO, "STORAGE")
            }
            refreshEncodedHistory()
        }
    }

    fun clearSelectedVideo() {
        _uiState.update {
            it.copy(
                currentScreen = AppScreen.MAIN,
                selectedMedia = null,
                completedOutputPath = null,
                errorMessage = null
            )
        }
    }

    fun navigateToMainScreen() {
        _uiState.update { it.copy(currentScreen = AppScreen.MAIN) }
    }

    fun navigateToPreprocessScreen() {
        if (_uiState.value.selectedMedia != null) {
            _uiState.update { it.copy(currentScreen = AppScreen.PREPROCESS) }
        }
    }

    fun onMediaSelected(uri: Uri, mediaType: MediaType) {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            var fileName = "Selected Media"
            var fileSize = 0L

            try {
                context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (nameIndex != -1) cursor.getString(nameIndex)?.let { fileName = it }
                        if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                    }
                }
            } catch (_: Exception) {}

            if (fileName == "Selected Media") {
                fileName = uri.lastPathSegment ?: "Media_Item"
            }

            if (fileSize == 0L) {
                try {
                    context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { afd ->
                        fileSize = afd.length
                    }
                } catch (_: Exception) {}
            }

            addLog("Media dipilih: $fileName (${mediaType.label}), Ukuran: ${String.format(Locale.US, "%.1f MB", fileSize / 1_000_000f)}", LogLevel.INFO, "INPUT")

            when (mediaType) {
                MediaType.VIDEO -> {
                    var durationSec = 0L
                    var width = 1920
                    var height = 1080

                    val retriever = MediaMetadataRetriever()
                    try {
                        retriever.setDataSource(context, uri)
                        val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                        val w = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
                        val h = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
                        if (durationMs > 0) durationSec = durationMs / 1000
                        if (w > 0) width = w
                        if (h > 0) height = h
                    } catch (_: Exception) {
                        addLog("Gagal membaca metadata parsial video (menggunakan fallback 1920x1080).", LogLevel.WARNING, "METADATA")
                    } finally {
                        try { retriever.release() } catch (_: Exception) {}
                    }

                    val metadata = SelectedMediaItem(
                        uri = uri,
                        mediaType = MediaType.VIDEO,
                        fileName = fileName,
                        sizeBytes = fileSize,
                        durationSec = durationSec,
                        width = width,
                        height = height
                    )
                    _uiState.update { current ->
                        val updated = current.copy(
                            currentScreen = AppScreen.PREPROCESS,
                            selectedMedia = metadata,
                            completedOutputPath = null,
                            errorMessage = null
                        )
                        updated.copy(estimatedSizeMb = calculateEstimatedSize(updated))
                    }
                }
                MediaType.IMAGE -> {
                    var imgWidth = 0
                    var imgHeight = 0
                    try {
                        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BitmapFactory.decodeStream(stream, null, options)
                        }
                        imgWidth = options.outWidth
                        imgHeight = options.outHeight
                    } catch (_: Exception) {}

                    val metadata = SelectedMediaItem(
                        uri = uri,
                        mediaType = MediaType.IMAGE,
                        fileName = fileName,
                        sizeBytes = fileSize,
                        width = imgWidth,
                        height = imgHeight
                    )
                    _uiState.update { current ->
                        val updated = current.copy(
                            currentScreen = AppScreen.PREPROCESS,
                            selectedMedia = metadata,
                            completedOutputPath = null,
                            errorMessage = null
                        )
                        updated.copy(estimatedSizeMb = calculateEstimatedSize(updated))
                    }
                }
                MediaType.AUDIO -> {
                    var durationSec = 0L
                    val retriever = MediaMetadataRetriever()
                    try {
                        retriever.setDataSource(context, uri)
                        val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                        if (durationMs > 0) durationSec = durationMs / 1000
                    } catch (_: Exception) {
                    } finally {
                        try { retriever.release() } catch (_: Exception) {}
                    }

                    val metadata = SelectedMediaItem(
                        uri = uri,
                        mediaType = MediaType.AUDIO,
                        fileName = fileName,
                        sizeBytes = fileSize,
                        durationSec = durationSec
                    )
                    _uiState.update { current ->
                        val updated = current.copy(
                            currentScreen = AppScreen.PREPROCESS,
                            selectedMedia = metadata,
                            completedOutputPath = null,
                            errorMessage = null
                        )
                        updated.copy(estimatedSizeMb = calculateEstimatedSize(updated))
                    }
                }
            }
        }
    }

    fun setImageFormat(format: String) {
        _uiState.update { current ->
            val updated = current.copy(imageFormat = format)
            updated.copy(estimatedSizeMb = calculateEstimatedSize(updated))
        }
    }

    fun setImageQuality(quality: Int) {
        _uiState.update { current ->
            val updated = current.copy(imageQuality = quality)
            updated.copy(estimatedSizeMb = calculateEstimatedSize(updated))
        }
    }

    fun setImageScalePercent(percent: Int) {
        _uiState.update { current ->
            val updated = current.copy(imageScalePercent = percent)
            updated.copy(estimatedSizeMb = calculateEstimatedSize(updated))
        }
    }

    fun setCodecFormat(format: String) {
        _uiState.update { current ->
            val updated = current.copy(outputFormat = format)
            updated.copy(estimatedSizeMb = calculateEstimatedSize(updated))
        }
    }

    fun setUseAutoBitrate(auto: Boolean) {
        _uiState.update { current ->
            val updated = current.copy(useAutoBitrate = auto)
            updated.copy(estimatedSizeMb = calculateEstimatedSize(updated))
        }
    }

    fun setTargetBitrate(bitrateMbps: Float) {
        _uiState.update { current ->
            val updated = current.copy(useAutoBitrate = false, targetBitrateMbps = bitrateMbps)
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
        val media = state.selectedMedia ?: return
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

        addLog("Memulai pengodean untuk berkas: ${media.fileName}", LogLevel.INFO, "ENCODER")
        encodingStartTimeMs = System.currentTimeMillis()

        val initialSizeText = String.format(Locale.US, "0.0 MB / %.1f MB", state.estimatedSizeMb)

        // Switch to MAIN screen immediately (0ms lag!)
        _uiState.update {
            it.copy(
                currentScreen = AppScreen.MAIN,
                isEncoding = true,
                encodingProgress = 0,
                activeEncodingFileName = media.fileName,
                encodingStatusText = "$initialSizeText • Menyiapkan..."
            )
        }

        if (media.mediaType == MediaType.IMAGE) {
            val extension = state.imageFormat.lowercase()
            val outputFile = File(outputDir, "encoded_${System.currentTimeMillis()}.$extension")

            viewModelScope.launch {
                val engine = VideoEncodingEngine(context)
                val targetW = if (state.imageScalePercent < 100 && media.width > 0) (media.width * state.imageScalePercent / 100) else 0
                val targetH = if (state.imageScalePercent < 100 && media.height > 0) (media.height * state.imageScalePercent / 100) else 0

                val success = withContext(Dispatchers.IO) {
                    engine.encodeImage(
                        inputUri = media.uri,
                        outputFile = outputFile,
                        format = state.imageFormat,
                        quality = state.imageQuality,
                        targetWidth = targetW,
                        targetHeight = targetH
                    )
                }

                if (success) {
                    addLog("Pengodean gambar selesai: ${outputFile.name}", LogLevel.SUCCESS, "IMAGE")
                    try {
                        android.media.MediaScannerConnection.scanFile(
                            context,
                            arrayOf(outputFile.absolutePath),
                            null,
                            null
                        )
                    } catch (_: Exception) {}

                    _uiState.update {
                        it.copy(
                            isEncoding = false,
                            encodingProgress = 100,
                            encodingStatusText = "Selesai!",
                            completedOutputPath = outputFile.absolutePath,
                            activeEncodingFileName = null
                        )
                    }
                    refreshEncodedHistory()
                } else {
                    addLog("Gagal memproses pengodean gambar.", LogLevel.ERROR, "IMAGE")
                    _uiState.update {
                        it.copy(
                            isEncoding = false,
                            encodingProgress = 0,
                            errorMessage = "Gagal memproses pengodean gambar.",
                            activeEncodingFileName = null
                        )
                    }
                }
            }
            return
        }

        val extension = if (media.mediaType == MediaType.AUDIO) {
            when (state.audioFormat) {
                MimeTypes.AUDIO_OPUS -> "opus"
                MimeTypes.AUDIO_AMR_WB -> "3gp"
                else -> "m4a"
            }
        } else "mp4"

        val outputFile = File(outputDir, "encoded_${System.currentTimeMillis()}.$extension")

        val targetWidth = if (state.resolutionPreset == ResolutionPreset.DEFAULT) 0 else state.resolutionPreset.width
        val targetHeight = if (state.resolutionPreset == ResolutionPreset.DEFAULT) 0 else state.resolutionPreset.height

        val bitrateBps = if (state.useAutoBitrate) 0 else (state.targetBitrateMbps * 1_000_000).toInt()
        val audioBitrateBps = state.audioBitrateKbps * 1000

        val serviceIntent = Intent(context, EncodingService::class.java).apply {
            action = EncodingService.ACTION_START
            putExtra(EncodingService.EXTRA_INPUT_URI, media.uri.toString())
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
        addLog("Mengirim perintah pembatalan encoding ke service.", LogLevel.WARNING, "USER")
        _uiState.update {
            it.copy(
                isEncoding = false,
                encodingProgress = 0,
                activeEncodingFileName = null
            )
        }
    }

    private fun calculateEstimatedSize(state: CompressorUiState): Float {
        val media = state.selectedMedia ?: return 0.0f
        if (media.mediaType == MediaType.IMAGE) {
            val scaleFactor = (state.imageScalePercent / 100f) * (state.imageScalePercent / 100f)
            val qualityFactor = state.imageQuality / 100f
            val rawEst = media.sizeBytes * scaleFactor * qualityFactor
            return (rawEst / 1_000_000f).coerceAtLeast(0.01f)
        }
        val videoBitrateBps = if (media.mediaType == MediaType.AUDIO) 0f else (if (state.useAutoBitrate) (media.sizeBytes * 8f / media.durationSec.coerceAtLeast(1)) * 0.7f else state.targetBitrateMbps * 1_000_000f)
        val audioBitrateBps = if (state.isAudioMuted) 0f else (if (state.audioBitrateKbps == 0) 128_000f else state.audioBitrateKbps * 1000f)
        val totalBitrateBps = videoBitrateBps + audioBitrateBps
        return (totalBitrateBps / 8.0f * media.durationSec) / 1_000_000f
    }

    override fun onCleared() {
        if (isServiceBound) {
            getApplication<Application>().unbindService(serviceConnection)
            isServiceBound = false
        }
        super.onCleared()
    }
}
