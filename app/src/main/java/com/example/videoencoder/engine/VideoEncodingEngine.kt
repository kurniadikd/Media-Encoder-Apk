package com.example.videoencoder.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.effect.Presentation
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.transformer.Composition
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

enum class EncoderEngineType {
    HARDWARE, SOFTWARE
}

data class EncodingConfig(
    val encoderEngine: EncoderEngineType = EncoderEngineType.HARDWARE,
    val videoFormat: String = "DEFAULT",            // "DEFAULT" or specific MIME type / "libsvtav1", "libx264", "libx265"
    val preset: String = "8",                       // SVT-AV1 preset (0-13) or x264/x265 preset
    val crf: Int = 28,                              // Constant Rate Factor (15-50)
    val targetBitrateBps: Int = 0,                  // 0 = Auto / Default Encoder Bitrate
    val bitrateMode: Int = 1,                       // 1=VBR, 2=CBR, 0=CQ
    val targetWidth: Int = 0,                       // 0 = Original Resolution
    val targetHeight: Int = 0,                      // 0 = Original Resolution
    val scaleMode: Int = Presentation.LAYOUT_SCALE_TO_FIT,
    val frameRate: Int = 0,                         // 0 = Original FPS
    val iFrameIntervalSec: Float = 0.0f,            // 0 = Auto Keyframe
    val audioFormat: String = "DEFAULT",            // "DEFAULT" or "libfdk_aac", "libopus", "libmp3lame"
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
        // Constant Quality (CQ / mode 0) does not use target bitrate. Only set bitrate for VBR (1), CBR (2), or Default.
        if (config.bitrateMode != 0 && config.targetBitrateBps > 0) {
            // Hardware safety floor: clamp minimum custom bitrate to 300_000 bps (0.3 Mbps) to prevent Qualcomm MediaCodec starvation
            val safeBitrateBps = config.targetBitrateBps.coerceAtLeast(300_000)
            videoEncoderSettingsBuilder.setBitrate(safeBitrateBps)
        }
        if (config.bitrateMode in listOf(0, 1, 2)) {
            videoEncoderSettingsBuilder.setBitrateMode(config.bitrateMode)
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

    fun createEditedMediaItem(inputUri: Uri, config: EncodingConfig): EditedMediaItem {
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

        val builder = EditedMediaItem.Builder(MediaItem.fromUri(inputUri))

        if (videoEffects.isNotEmpty()) {
            builder.setEffects(Effects(emptyList(), videoEffects))
        }

        if (config.isAudioMuted) {
            builder.setRemoveAudio(true)
        }

        return builder.build()
    }

    object VipsJni {
        external fun encodeVipsImage(
            inputPath: String,
            outputPath: String,
            format: String,
            quality: Int,
            targetWidth: Int,
            targetHeight: Int
        ): Boolean
    }

    companion object {
        private var isLibVipsLoaded = false
        init {
            try {
                System.loadLibrary("vips")
                System.loadLibrary("videoencoder_jni")
                isLibVipsLoaded = true
                android.util.Log.i("VideoEncodingEngine", "Native libvips.so + videoencoder_jni successfully loaded!")
            } catch (e: Throwable) {
                isLibVipsLoaded = false
                android.util.Log.w("VideoEncodingEngine", "Native vips JNI not loaded (falling back to Android Bitmap API): ${e.localizedMessage}")
            }
        }
    }

    /**
     * Encodes and compresses Image directly using libvips (or Android Bitmap API as fallback)
     */
    fun encodeImage(
        inputUri: Uri,
        outputFile: File,
        format: String,
        quality: Int,
        targetWidth: Int,
        targetHeight: Int,
        useLibVipsEngine: Boolean = true
    ): Boolean {
        var tempInputFile: File? = null
        val actualInputPath = if (inputUri.scheme == "file" && inputUri.path != null) {
            inputUri.path
        } else {
            try {
                val tempFile = File.createTempFile("vips_in_", ".tmp", context.cacheDir)
                context.contentResolver.openInputStream(inputUri)?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                tempInputFile = tempFile
                tempFile.absolutePath
            } catch (e: Exception) {
                null
            }
        }

        if (useLibVipsEngine && isLibVipsLoaded && actualInputPath != null) {
            android.util.Log.i("VideoEncodingEngine", "Encoding image '${outputFile.name}' via Native libvips Engine ($format, Q=$quality)...")
            val success = VipsJni.encodeVipsImage(
                inputPath = actualInputPath,
                outputPath = outputFile.absolutePath,
                format = format.uppercase(),
                quality = quality,
                targetWidth = targetWidth,
                targetHeight = targetHeight
            )
            tempInputFile?.delete()
            if (success) return true
            android.util.Log.e("VideoEncodingEngine", "libvips encoding returned false for format $format!")
            return false
        }

        return try {
            val bitmap = if (inputUri.scheme == "file" && inputUri.path != null) {
                BitmapFactory.decodeFile(inputUri.path)
            } else {
                context.contentResolver.openInputStream(inputUri)?.use { input ->
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
