package com.example.videoencoder.engine

import android.content.Context
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

data class EncodingConfig(
    val videoFormat: String = "DEFAULT",            // "DEFAULT" or specific MIME type
    val targetBitrateBps: Int = 0,                  // 0 = Auto / Default Encoder Bitrate
    val bitrateMode: Int = 1,                       // 1=VBR, 2=CBR, 0=CQ
    val targetWidth: Int = 0,                       // 0 = Original Resolution (No Scaling Effect)
    val targetHeight: Int = 0,                      // 0 = Original Resolution
    val scaleMode: Int = Presentation.LAYOUT_SCALE_TO_FIT,
    val frameRate: Int = 0,                         // 0 = Original FPS
    val iFrameIntervalSec: Float = 0.0f,            // 0 = Auto Keyframe
    val audioFormat: String = "DEFAULT",            // "DEFAULT" or specific Audio MIME type
    val audioBitrateBps: Int = 0,                   // 0 = Auto Audio Bitrate
    val isAudioMuted: Boolean = false,               // Remove audio
    val rotationDegrees: Float = 0.0f                // 0, 90, 180, 270
)

class VideoEncodingEngine(private val context: Context) {

    fun buildTransformer(
        config: EncodingConfig,
        onCompleted: () -> Unit,
        onError: (ExportException) -> Unit
    ): Transformer {

        // 1. Configure Hardware Video Encoder Settings
        val videoEncoderSettingsBuilder = VideoEncoderSettings.Builder()
        if (config.targetBitrateBps > 0) {
            videoEncoderSettingsBuilder.setBitrate(config.targetBitrateBps)
        }
        val videoEncoderSettings = videoEncoderSettingsBuilder.build()

        val customEncoderFactory = DefaultEncoderFactory.Builder(context)
            .setRequestedVideoEncoderSettings(videoEncoderSettings)
            .build()

        // 2. Build Media3 Transformer with Video & Audio MimeTypes
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

        // Apply Scaling effect only if target width/height are specified (> 0)
        if (config.targetWidth > 0 && config.targetHeight > 0) {
            val scaleEffect = Presentation.createForWidthAndHeight(
                config.targetWidth, config.targetHeight, config.scaleMode
            )
            videoEffects.add(scaleEffect)
        }

        // Apply Rotation effect if specified (> 0°)
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
}
