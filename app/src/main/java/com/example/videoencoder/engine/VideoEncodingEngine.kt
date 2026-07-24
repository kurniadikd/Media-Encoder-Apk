package com.example.videoencoder.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.effect.Presentation
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.transformer.DefaultEncoderFactory
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.VideoEncoderSettings
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

data class EncodingConfig(
    val videoFormat: String = "DEFAULT",            // "DEFAULT" or specific MIME type
    val targetBitrateBps: Int = 0,                  // 0 = Auto / Default Encoder Bitrate
    val bitrateMode: Int = 1,                       // 1=VBR, 2=CBR, 0=CQ
    val targetWidth: Int = 0,                       // 0 = Original Resolution
    val targetHeight: Int = 0,                      // 0 = Original Resolution
    val scaleMode: Int = Presentation.LAYOUT_SCALE_TO_FIT,
    val frameRate: Int = 0,                         // 0 = Original FPS
    val iFrameIntervalSec: Float = 0.0f,            // 0 = Auto Keyframe
    val audioFormat: String = "DEFAULT",            // "DEFAULT" or specific Audio MIME type
    val audioBitrateBps: Int = 0,                   // 0 = Auto Audio Bitrate
    val isAudioMuted: Boolean = false,               // Remove audio
    val rotationDegrees: Float = 0.0f,               // 0, 90, 180, 270
    val quantizationParameterQP: Int = 23           // 1 to 51
)

class VideoEncodingEngine(private val context: Context) {

    fun buildTransformer(
        config: EncodingConfig,
        onCompleted: () -> Unit,
        onError: (ExportException) -> Unit
    ): Transformer {

        val videoEncoderSettingsBuilder = VideoEncoderSettings.Builder()
        if (config.targetBitrateBps > 0) {
            // Hardware safety floor: clamp minimum custom bitrate to 300_000 bps (0.3 Mbps) to prevent Qualcomm MediaCodec starvation
            val safeBitrateBps = config.targetBitrateBps.coerceAtLeast(300_000)
            videoEncoderSettingsBuilder.setBitrate(safeBitrateBps)
        }
        val videoEncoderSettings = videoEncoderSettingsBuilder.build()

        val customEncoderFactory = DefaultEncoderFactory.Builder(context)
            .setRequestedVideoEncoderSettings(videoEncoderSettings)
            .setEnableFallback(true)
            .build()

        val builder = Transformer.Builder(context)
            .setEncoderFactory(customEncoderFactory)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    onCompleted()
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    onError(exportException)
                }
            })

        if (config.videoFormat != "DEFAULT" && config.videoFormat.isNotBlank()) {
            builder.setVideoMimeType(config.videoFormat)
        }

        if (!config.isAudioMuted && config.audioFormat != "DEFAULT" && config.audioFormat.isNotBlank()) {
            builder.setAudioMimeType(config.audioFormat)
        }

        return builder.build()
    }

    fun prepareInputUri(inputUri: Uri): Uri {
        if (inputUri.scheme == "file") return inputUri

        return try {
            val cacheDir = context.cacheDir
            cacheDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("input_media_temp_")) {
                    try { file.delete() } catch (_: Exception) {}
                }
            }

            val cacheFile = File(cacheDir, "input_media_temp_${System.currentTimeMillis()}.tmp")
            context.contentResolver.openInputStream(inputUri)?.use { input ->
                cacheFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            if (cacheFile.exists() && cacheFile.length() > 0) {
                Uri.fromFile(cacheFile)
            } else {
                inputUri
            }
        } catch (_: Exception) {
            inputUri
        }
    }

    fun createEditedMediaItem(inputUri: Uri, config: EncodingConfig): EditedMediaItem {
        val seekableUri = prepareInputUri(inputUri)
        val videoEffects = mutableListOf<androidx.media3.common.Effect>()

        if (config.targetWidth > 0 && config.targetHeight > 0) {
            val scaleEffect = Presentation.createForWidthAndHeight(
                config.targetWidth, config.targetHeight, config.scaleMode
            )
            videoEffects.add(scaleEffect)
        }

        if (config.rotationDegrees > 0.0f) {
            val rotationEffect = ScaleAndRotateTransformation.Builder()
                .setRotationDegrees(config.rotationDegrees)
                .build()
            videoEffects.add(rotationEffect)
        }

        val builder = EditedMediaItem.Builder(MediaItem.fromUri(seekableUri))

        if (videoEffects.isNotEmpty()) {
            builder.setEffects(Effects(emptyList(), videoEffects))
        }

        if (config.isAudioMuted) {
            builder.setRemoveAudio(true)
        }

        return builder.build()
    }

    /**
     * Encodes and compresses Image to JPEG, PNG, or WEBP with scaling & quality options
     */
    fun encodeImage(
        inputUri: Uri,
        outputFile: File,
        format: String,
        quality: Int,
        targetWidth: Int,
        targetHeight: Int
    ): Boolean {
        return try {
            val seekableUri = prepareInputUri(inputUri)
            val bitmap = if (seekableUri.scheme == "file" && seekableUri.path != null) {
                BitmapFactory.decodeFile(seekableUri.path)
            } else {
                context.contentResolver.openInputStream(seekableUri)?.use { input ->
                    BitmapFactory.decodeStream(input)
                }
            } ?: return false

            val finalBitmap = if (targetWidth > 0 && targetHeight > 0 && (bitmap.width != targetWidth || bitmap.height != targetHeight)) {
                Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
            } else {
                bitmap
            }

            val compressFormat = when (format.uppercase()) {
                "PNG" -> Bitmap.CompressFormat.PNG
                "WEBP" -> if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
                else -> Bitmap.CompressFormat.JPEG
            }

            FileOutputStream(outputFile).use { out ->
                finalBitmap.compress(compressFormat, quality.coerceIn(1, 100), out)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
