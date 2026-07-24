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
    val videoFormat: String = MimeTypes.VIDEO_H265, // HEVC, AVC, VP9, AV1
    val targetBitrateBps: Int = 4_000_000,           // 4 Mbps
    val bitrateMode: Int = 1,                        // 1=VBR, 2=CBR, 0=CQ
    val targetWidth: Int = 1920,                     // 1080p
    val targetHeight: Int = 1080,                    // 1080p
    val scaleMode: Int = Presentation.LAYOUT_SCALE_TO_FIT,
    val frameRate: Int = 30,                         // 30 FPS
    val iFrameIntervalSec: Float = 2.0f,
    val audioFormat: String = MimeTypes.AUDIO_AAC,   // AAC, Opus, AMR_WB
    val audioBitrateBps: Int = 128_000,              // 128 kbps
    val isAudioMuted: Boolean = false,                // Remove audio
    val rotationDegrees: Float = 0.0f                 // 0, 90, 180, 270
)

class VideoEncodingEngine(private val context: Context) {

    fun buildTransformer(
        config: EncodingConfig,
        onCompleted: () -> Unit,
        onError: (ExportException) -> Unit
    ): Transformer {

        // 1. Configure Hardware Video Encoder Settings
        val videoEncoderSettings = VideoEncoderSettings.Builder()
            .setBitrate(config.targetBitrateBps)
            .build()

        val customEncoderFactory = DefaultEncoderFactory.Builder(context)
            .setRequestedVideoEncoderSettings(videoEncoderSettings)
            .build()

        // 2. Build Media3 Transformer with Video & Audio MimeTypes
        val builder = Transformer.Builder(context)
            .setVideoMimeType(config.videoFormat)
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

        if (!config.isAudioMuted) {
            builder.setAudioMimeType(config.audioFormat)
        }

        return builder.build()
    }

    fun createEditedMediaItem(inputUri: Uri, config: EncodingConfig): EditedMediaItem {
        val videoEffects = mutableListOf<androidx.media3.common.Effect>()

        // Scaling effect
        val scaleEffect = Presentation.createForWidthAndHeight(
            config.targetWidth, config.targetHeight, config.scaleMode
        )
        videoEffects.add(scaleEffect)

        // Rotation effect if set
        if (config.rotationDegrees != 0.0f) {
            val rotationEffect = ScaleAndRotateTransformation.Builder()
                .setRotationDegrees(config.rotationDegrees)
                .build()
            videoEffects.add(rotationEffect)
        }

        val builder = EditedMediaItem.Builder(MediaItem.fromUri(inputUri))
            .setEffects(Effects(emptyList(), videoEffects))

        if (config.isAudioMuted) {
            builder.setRemoveAudio(true)
        }

        return builder.build()
    }
}
