package com.example.videoencoder.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import java.io.File
import androidx.core.app.NotificationCompat
import androidx.media3.common.MimeTypes
import androidx.media3.effect.Presentation
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import com.example.videoencoder.MainActivity
import com.example.videoencoder.engine.EncoderEngineType
import com.example.videoencoder.engine.EncodingConfig
import com.example.videoencoder.engine.FFmpegEngine
import com.example.videoencoder.engine.VideoEncodingEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EncodingService : Service() {

    enum class EncodingStatus {
        IDLE, RUNNING, COMPLETED, ERROR, CANCELLED
    }

    data class ServiceProgressState(
        val status: EncodingStatus = EncodingStatus.IDLE,
        val progressPercent: Int = 0,
        val errorMessage: String? = null,
        val outputPath: String? = null
    )

    inner class LocalBinder : Binder() {
        fun getService(): EncodingService = this@EncodingService
    }

    private val binder = LocalBinder()
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private var activeTransformer: Transformer? = null
    private var ffmpegEngine: FFmpegEngine? = null
    private var progressJob: Job? = null
    private var wakeLock: android.os.PowerManager.WakeLock? = null

    private val _progressState = MutableStateFlow(ServiceProgressState())
    val progressState: StateFlow<ServiceProgressState> = _progressState.asStateFlow()

    private var stopSelfJob: Job? = null

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val inputUriStr = intent.getStringExtra(EXTRA_INPUT_URI)
                val outputPath = intent.getStringExtra(EXTRA_OUTPUT_PATH)
                val videoFormat = intent.getStringExtra(EXTRA_VIDEO_FORMAT) ?: MimeTypes.VIDEO_H265
                val bitrate = intent.getIntExtra(EXTRA_BITRATE, 4_000_000)
                val bitrateMode = intent.getIntExtra(EXTRA_BITRATE_MODE, 1)
                val width = intent.getIntExtra(EXTRA_WIDTH, 1920)
                val height = intent.getIntExtra(EXTRA_HEIGHT, 1080)
                val scaleMode = intent.getIntExtra(EXTRA_SCALE_MODE, Presentation.LAYOUT_SCALE_TO_FIT)
                val fps = intent.getIntExtra(EXTRA_FPS, 30)
                val iFrameSec = intent.getFloatExtra(EXTRA_IFRAME_SEC, 2.0f)
                val audioFormat = intent.getStringExtra(EXTRA_AUDIO_FORMAT) ?: MimeTypes.AUDIO_AAC
                val audioBitrate = intent.getIntExtra(EXTRA_AUDIO_BITRATE, 128_000)
                val isAudioMuted = intent.getBooleanExtra(EXTRA_AUDIO_MUTED, false)
                val rotation = intent.getFloatExtra(EXTRA_ROTATION, 0.0f)
                val engineTypeStr = intent.getStringExtra(EXTRA_ENCODER_ENGINE) ?: "HARDWARE"
                val encoderEngine = if (engineTypeStr.equals("SOFTWARE", ignoreCase = true)) EncoderEngineType.SOFTWARE else EncoderEngineType.HARDWARE
                val preset = intent.getStringExtra(EXTRA_PRESET) ?: "8"
                val crf = intent.getIntExtra(EXTRA_CRF, 28)

                if (inputUriStr != null && outputPath != null) {
                    val config = EncodingConfig(
                        encoderEngine = encoderEngine,
                        videoFormat = videoFormat,
                        preset = preset,
                        crf = crf,
                        targetBitrateBps = bitrate,
                        bitrateMode = bitrateMode,
                        targetWidth = width,
                        targetHeight = height,
                        scaleMode = scaleMode,
                        frameRate = fps,
                        iFrameIntervalSec = iFrameSec,
                        audioFormat = audioFormat,
                        audioBitrateBps = audioBitrate,
                        isAudioMuted = isAudioMuted,
                        rotationDegrees = rotation
                    )
                    startEncoding(Uri.parse(inputUriStr), outputPath, config)
                }
            }
            ACTION_CANCEL -> {
                cancelEncoding()
            }
        }
        return START_NOT_STICKY
    }

    fun startEncoding(inputUri: Uri, outputPath: String, config: EncodingConfig) {
        stopSelfJob?.cancel()
        stopSelfJob = null

        if (config.encoderEngine == EncoderEngineType.SOFTWARE) {
            startSoftwareEncoding(inputUri, outputPath, config)
            return
        }

        val notification = createNotification(0, "Memulai Pengodean Hardware...", isFinished = false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROCESSING
                } else {
                    0
                }
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            wakeLock = powerManager.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "MediaEncoder:EncodingWakeLock").apply {
                acquire(3 * 60 * 60 * 1000L)
            }
        } catch (_: Exception) {}

        _progressState.value = ServiceProgressState(status = EncodingStatus.RUNNING, progressPercent = 0)

        val modeText = when (config.bitrateMode) {
            1 -> "VBR"
            2 -> "CBR"
            0 -> "CQ"
            else -> "DEFAULT"
        }
        val bitrateText = if (config.targetBitrateBps > 0) String.format("%.2f Mbps", config.targetBitrateBps / 1_000_000.0) else "Auto"
        val resText = if (config.targetWidth > 0 && config.targetHeight > 0) "${config.targetWidth}x${config.targetHeight}" else "Bawaan File"
        val iframeText = if (config.iFrameIntervalSec > 0) "${config.iFrameIntervalSec}s" else "Auto"

        val paramSummary = "[PARAMS] Codec: ${config.videoFormat} | Bitrate: $bitrateText ($modeText) | Resolusi: $resText | Keyframe: $iframeText | Rotasi: ${config.rotationDegrees}°"
        android.util.Log.i("EncodingService", paramSummary)

        scope.launch(Dispatchers.IO) {
            val engine = VideoEncodingEngine(applicationContext)
            val editedItem = engine.createEditedMediaItem(inputUri, config)

            val targetFile = File(outputPath)
            targetFile.parentFile?.mkdirs()
            if (targetFile.exists()) {
                try { targetFile.delete() } catch (_: Exception) {}
            }

            withContext(Dispatchers.Main) {
                val transformer = engine.buildTransformer(
                    config = config,
                    onCompleted = {
                        progressJob?.cancel()
                        scope.launch(Dispatchers.IO) {
                            try {
                                val resolver = applicationContext.contentResolver
                                val contentValues = android.content.ContentValues().apply {
                                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, targetFile.name)
                                    put(android.provider.MediaStore.MediaColumns.SIZE, targetFile.length())
                                    put(android.provider.MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000)
                                    put(android.provider.MediaStore.MediaColumns.DATE_MODIFIED, System.currentTimeMillis() / 1000)
                                    val mime = if (config.videoFormat != "DEFAULT" && config.videoFormat.isNotBlank()) config.videoFormat else "video/mp4"
                                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, mime)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "Media Encoder")
                                        put(android.provider.MediaStore.MediaColumns.IS_PENDING, 0)
                                    }
                                }
                                val contentUri = if (config.videoFormat.startsWith("audio/")) {
                                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                                } else {
                                    android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                                }
                                resolver.insert(contentUri, contentValues)

                                android.media.MediaScannerConnection.scanFile(
                                    applicationContext,
                                    arrayOf(targetFile.absolutePath),
                                    null,
                                    null
                                )
                            } catch (_: Exception) {}

                            withContext(Dispatchers.Main) {
                                _progressState.value = ServiceProgressState(
                                    status = EncodingStatus.COMPLETED,
                                    progressPercent = 100,
                                    outputPath = targetFile.absolutePath
                                )
                                updateNotification(100, "Pengodean Selesai! 🎉", isFinished = true)
                                stopSelfWithDelay()
                            }
                        }
                    },
                    onError = { exception ->
                        progressJob?.cancel()
                        val errorCodeName = exception.errorCodeName
                        val errorCode = exception.errorCode
                        val causeMsg = exception.cause?.message ?: exception.localizedMessage ?: "Hardware Muxer Error"

                        val diagnosticNote = if (errorCode == 7002 || errorCodeName.contains("TIMEOUT")) {
                            " (Penyebab: Hardware Codec HP menghentikan sinyal frame output karena bitrate terlalu rendah. Harap naikkan bitrate ke min. 0.3Mbps - 1.0Mbps atau gunakan Auto Bitrate)"
                        } else ""

                        val detailedLogText = "Pengodean Gagal [$errorCodeName ($errorCode)]: $causeMsg$diagnosticNote"

                        _progressState.value = ServiceProgressState(
                            status = EncodingStatus.ERROR,
                            errorMessage = detailedLogText
                        )
                        updateNotification(0, "Gagal: $errorCodeName", isFinished = true)
                        stopSelfWithDelay()
                    }
                )

                try {
                    activeTransformer = transformer
                    transformer.start(editedItem, targetFile.absolutePath)
                } catch (e: Throwable) {
                    val causeMsg = e.localizedMessage ?: "Mode Bitrate (CQP) / Kodek Tidak Didukung Hardware HP"
                    val errorLogText = "Pengodean Gagal [CodecInitError]: $causeMsg. (Petunjuk: Sebagian besar hardware SoC Qualcomm tidak mendukung CQP. Harap gunakan VBR atau Auto Bitrate)"
                    android.util.Log.e("EncodingService", errorLogText, e)
                    _progressState.value = ServiceProgressState(
                        status = EncodingStatus.ERROR,
                        errorMessage = errorLogText
                    )
                    updateNotification(0, "Gagal: Mode CQP Tidak Didukung", isFinished = true)
                    stopSelfWithDelay()
                    return@withContext
                }

                val progressHolder = ProgressHolder()
                progressJob?.cancel()
                progressJob = scope.launch {
                    var simulatedProgress = 5.0f
                    while (true) {
                        delay(50)
                        activeTransformer?.let { trans ->
                            val state = trans.getProgress(progressHolder)
                            val currentProgress = if (state == Transformer.PROGRESS_STATE_AVAILABLE) {
                                progressHolder.progress
                            } else {
                                if (simulatedProgress < 95.0f) {
                                    simulatedProgress += 0.25f
                                }
                                simulatedProgress.toInt()
                            }
                            if (_progressState.value.progressPercent != currentProgress) {
                                _progressState.value = _progressState.value.copy(
                                    status = EncodingStatus.RUNNING,
                                    progressPercent = currentProgress,
                                    outputPath = outputPath
                                )
                                updateNotification(currentProgress, "Proses Pengodean: $currentProgress%", isFinished = false)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startSoftwareEncoding(inputUri: Uri, outputPath: String, config: EncodingConfig) {
        val targetFile = File(outputPath)
        targetFile.parentFile?.mkdirs()
        if (targetFile.exists()) {
            try { targetFile.delete() } catch (_: Exception) {}
        }

        _progressState.value = ServiceProgressState(
            status = EncodingStatus.RUNNING,
            progressPercent = 0,
            outputPath = targetFile.absolutePath
        )

        val inputPath = getRealPathFromUri(applicationContext, inputUri) ?: inputUri.toString()
        val totalDurationMs = getMediaDurationMs(applicationContext, inputUri)

        val ffmpeg = FFmpegEngine(applicationContext)
        ffmpegEngine = ffmpeg

        val notification = createNotification(0, "Memulai Pengodean Software (FFmpeg)...", isFinished = false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROCESSING
                } else {
                    0
                }
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        scope.launch(Dispatchers.IO) {
            ffmpeg.executeEncoding(
                inputPath = inputPath,
                outputPath = outputPath,
                config = config,
                totalDurationMs = totalDurationMs,
                onProgress = { percent, _, _, statusText ->
                    _progressState.value = ServiceProgressState(
                        status = EncodingStatus.RUNNING,
                        progressPercent = percent,
                        outputPath = targetFile.absolutePath
                    )
                    updateNotification(percent, "Software: $statusText", isFinished = false)
                },
                onCompleted = {
                    try {
                        val resolver = applicationContext.contentResolver
                        val contentValues = android.content.ContentValues().apply {
                            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, targetFile.name)
                            put(android.provider.MediaStore.MediaColumns.SIZE, targetFile.length())
                            put(android.provider.MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000)
                            put(android.provider.MediaStore.MediaColumns.DATE_MODIFIED, System.currentTimeMillis() / 1000)
                            val mime = if (config.videoFormat.contains("av1", ignoreCase = true)) "video/mp4" else "video/mp4"
                            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, mime)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "Media Encoder")
                                put(android.provider.MediaStore.MediaColumns.IS_PENDING, 0)
                            }
                        }
                        val contentUri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        resolver.insert(contentUri, contentValues)

                        android.media.MediaScannerConnection.scanFile(
                            applicationContext,
                            arrayOf(targetFile.absolutePath),
                            null,
                            null
                        )
                    } catch (_: Exception) {}

                    scope.launch(Dispatchers.Main) {
                        _progressState.value = ServiceProgressState(
                            status = EncodingStatus.COMPLETED,
                            progressPercent = 100,
                            outputPath = targetFile.absolutePath
                        )
                        updateNotification(100, "Pengodean Software Selesai! 🎉", isFinished = true)
                        stopSelfWithDelay()
                    }
                },
                onError = { errorMsg ->
                    scope.launch(Dispatchers.Main) {
                        _progressState.value = ServiceProgressState(
                            status = EncodingStatus.ERROR,
                            progressPercent = 0,
                            errorMessage = errorMsg
                        )
                        updateNotification(0, "Pengodean Software Gagal", isFinished = true)
                        stopSelfWithDelay()
                    }
                }
            )
        }
    }

    private fun getRealPathFromUri(context: Context, uri: Uri): String? {
        if (uri.scheme == "file") return uri.path
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, arrayOf(android.provider.MediaStore.MediaColumns.DATA), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idx = cursor.getColumnIndex(android.provider.MediaStore.MediaColumns.DATA)
                        if (idx != -1) {
                            val path = cursor.getString(idx)
                            if (!path.isNullOrBlank() && File(path).exists()) return path
                        }
                    }
                }
            } catch (_: Exception) {}

            try {
                val tempFile = File(context.cacheDir, "ffmpeg_input_temp_${System.currentTimeMillis()}.mp4")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    java.io.FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                if (tempFile.exists() && tempFile.length() > 0) return tempFile.absolutePath
            } catch (_: Exception) {}
        }
        return uri.path
    }

    private fun getMediaDurationMs(context: Context, uri: Uri): Long {
        return try {
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val durationStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            durationStr?.toLongOrNull() ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    fun cancelEncoding() {
        ffmpegEngine?.cancelEncoding()
        ffmpegEngine = null
        activeTransformer?.cancel()
        progressJob?.cancel()

        // Delete the partially-written output file
        val outputPath = _progressState.value.outputPath
        if (outputPath != null) {
            try {
                val partialFile = File(outputPath)
                if (partialFile.exists()) {
                    partialFile.delete()
                }
            } catch (_: Exception) {}
        }

        _progressState.value = ServiceProgressState(status = EncodingStatus.CANCELLED)
        clearNotificationAndStop()
    }

    private fun stopSelfWithDelay() {
        stopSelfJob?.cancel()
        stopSelfJob = scope.launch {
            delay(2500)
            clearNotificationAndStop()
        }
    }

    private fun clearNotificationAndStop() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
            wakeLock = null
        } catch (_: Exception) {}

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(NOTIFICATION_ID)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Media Encoder Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifikasi status pengodean media"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(progress: Int, statusText: String, isFinished: Boolean) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Media Encoder")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setProgress(100, progress, progress == 0 && !isFinished)
            .setOngoing(!isFinished)
            .setAutoCancel(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .apply {
                if (!isFinished) {
                    addAction(
                        android.R.drawable.ic_menu_close_clear_cancel,
                        "Batal",
                        PendingIntent.getService(
                            this@EncodingService,
                            1,
                            Intent(this@EncodingService, EncodingService::class.java).apply { action = ACTION_CANCEL },
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                }
            }
            .build()

    private fun updateNotification(progress: Int, statusText: String, isFinished: Boolean) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, createNotification(progress, statusText, isFinished))
    }

    override fun onDestroy() {
        progressJob?.cancel()
        activeTransformer?.cancel()
        ffmpegEngine?.cancelEncoding()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "encoding_service_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.videoencoder.START_ENCODING"
        const val ACTION_CANCEL = "com.example.videoencoder.CANCEL_ENCODING"

        const val EXTRA_INPUT_URI = "extra_input_uri"
        const val EXTRA_OUTPUT_PATH = "extra_output_path"
        const val EXTRA_VIDEO_FORMAT = "extra_video_format"
        const val EXTRA_BITRATE = "extra_bitrate"
        const val EXTRA_BITRATE_MODE = "extra_bitrate_mode"
        const val EXTRA_WIDTH = "extra_width"
        const val EXTRA_HEIGHT = "extra_height"
        const val EXTRA_SCALE_MODE = "extra_scale_mode"
        const val EXTRA_FPS = "extra_fps"
        const val EXTRA_IFRAME_SEC = "extra_iframe_sec"
        const val EXTRA_AUDIO_FORMAT = "extra_audio_format"
        const val EXTRA_AUDIO_BITRATE = "extra_audio_bitrate"
        const val EXTRA_AUDIO_MUTED = "extra_audio_muted"
        const val EXTRA_ROTATION = "extra_rotation"
        const val EXTRA_ENCODER_ENGINE = "extra_encoder_engine"
        const val EXTRA_PRESET = "extra_preset"
        const val EXTRA_CRF = "extra_crf"
    }
}
