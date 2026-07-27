package com.example.videoencoder.engine

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.Locale
import java.util.regex.Pattern

/**
 * Custom FFmpeg Software Encoding Engine
 * Executes standalone FFmpeg ARM64 binary compiled with SVT-AV1, FDK-AAC, x264, x265, Opus & MediaCodec.
 */
class FFmpegEngine(private val context: Context) {

    private var activeProcess: Process? = null

    /**
     * Resolves the executable path to libffmpeg.so (or ffmpeg)
     */
    fun getFFmpegBinaryFile(): File {
        // 1. Primary path: Android nativeLibraryDir (extracted automatically when extractNativeLibs="true")
        val nativeLibDir = File(context.applicationInfo.nativeLibraryDir)
        val nativeFmpeg = File(nativeLibDir, "libffmpeg.so")

        if (nativeFmpeg.exists() && nativeFmpeg.length() > 0) {
            try {
                Runtime.getRuntime().exec("chmod 755 ${nativeFmpeg.absolutePath}").waitFor()
            } catch (_: Exception) {}
            Log.i("FFmpegEngine", "Using nativeLibraryDir FFmpeg binary: ${nativeFmpeg.absolutePath}")
            return nativeFmpeg
        }

        // 2. Fallback path: context.filesDir/ffmpeg
        val filesFmpeg = File(context.filesDir, "ffmpeg")
        if (filesFmpeg.exists() && filesFmpeg.length() > 0) {
            try {
                Runtime.getRuntime().exec("chmod 755 ${filesFmpeg.absolutePath}").waitFor()
            } catch (_: Exception) {}
            return filesFmpeg
        }

        // 3. Fail-safe Fallback: Extract libffmpeg.so directly from installed APK zip file
        try {
            val apkPath = context.applicationInfo.sourceDir
            val zipFile = java.util.zip.ZipFile(apkPath)
            val entry = zipFile.getEntry("lib/arm64-v8a/libffmpeg.so")
                ?: zipFile.getEntry("lib/arm64/libffmpeg.so")
                ?: zipFile.entries().asSequence().firstOrNull { it.name.endsWith("libffmpeg.so") }

            if (entry != null) {
                zipFile.getInputStream(entry).use { input ->
                    FileOutputStream(filesFmpeg).use { output ->
                        input.copyTo(output)
                    }
                }
                zipFile.close()
                Runtime.getRuntime().exec("chmod 755 ${filesFmpeg.absolutePath}").waitFor()
                Log.i("FFmpegEngine", "Successfully extracted libffmpeg.so from APK zip to: ${filesFmpeg.absolutePath}")
                return filesFmpeg
            }
        } catch (e: Exception) {
            Log.e("FFmpegEngine", "Failed to extract libffmpeg.so from APK zip: ${e.message}", e)
        }

        return if (filesFmpeg.exists()) filesFmpeg else nativeFmpeg
    }

    /**
     * Executes FFmpeg encoding asynchronously with live progress parsing
     */
    suspend fun executeEncoding(
        inputPath: String,
        outputPath: String,
        config: EncodingConfig,
        totalDurationMs: Long,
        onProgress: (progressPercent: Int, fps: Float, speedStr: String, statusText: String) -> Unit,
        onCompleted: () -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val ffmpegBinary = getFFmpegBinaryFile()
        if (!ffmpegBinary.exists()) {
            onError("Binary FFmpeg tidak ditemukan pada sistem!")
            return@withContext
        }

        // Ensure binary is executable
        try {
            Runtime.getRuntime().exec("chmod 755 ${ffmpegBinary.absolutePath}").waitFor()
        } catch (_: Exception) {}

        val cmdList = mutableListOf<String>().apply {
            add(ffmpegBinary.absolutePath)
            add("-y") // Overwrite output file without asking

            // Input File
            add("-i")
            add(inputPath)

            // Video Codec & Preset
            val vCodec = when (config.videoFormat.lowercase()) {
                "svt-av1", "av1", "libsvtav1" -> "libsvtav1"
                "h264", "x264", "libx264" -> "libx264"
                "h265", "hevc", "x265", "libx265" -> "libx265"
                "vp9", "libvpx-vp9" -> "libvpx-vp9"
                else -> "libsvtav1" // Default software encoder is SVT-AV1!
            }
            add("-c:v")
            add(vCodec)

            // Explicit Pixel Format for maximum device compatibility
            add("-pix_fmt")
            add("yuv420p")

            // Preset & Rate Control (CRF vs Bitrate)
            if (vCodec == "libsvtav1") {
                val presetVal = config.preset.ifEmpty { "8" }
                add("-preset")
                add(presetVal)
                if (config.targetBitrateBps > 0) {
                    add("-b:v")
                    add("${config.targetBitrateBps}")
                } else {
                    add("-crf")
                    add(config.crf.coerceIn(15, 50).toString())
                }
            } else if (vCodec == "libx264" || vCodec == "libx265") {
                val presetVal = config.preset.ifEmpty { "medium" }
                add("-preset")
                add(presetVal)
                if (config.targetBitrateBps > 0) {
                    add("-b:v")
                    add("${config.targetBitrateBps}")
                } else {
                    add("-crf")
                    add(config.crf.coerceIn(15, 50).toString())
                }
            } else if (config.targetBitrateBps > 0) {
                add("-b:v")
                add("${config.targetBitrateBps}")
            }

            // Frame Rate
            if (config.frameRate > 0) {
                add("-r")
                add(config.frameRate.toString())
            }

            // Resolution / Filters
            val filters = mutableListOf<String>()
            if (config.targetWidth > 0 && config.targetHeight > 0) {
                filters.add("scale=${config.targetWidth}:${config.targetHeight}:force_original_aspect_ratio=decrease,pad=${config.targetWidth}:${config.targetHeight}:(ow-iw)/2:(oh-ih)/2")
            }
            if (config.rotationDegrees > 0) {
                when (config.rotationDegrees.toInt()) {
                    90 -> filters.add("transpose=1")
                    180 -> filters.add("transpose=2,transpose=2")
                    270 -> filters.add("transpose=2")
                }
            }
            if (filters.isNotEmpty()) {
                add("-vf")
                add(filters.joinToString(","))
            }

            // Audio Configuration
            if (config.isAudioMuted) {
                add("-an")
            } else {
                val aCodec = when (config.audioFormat.lowercase()) {
                    "fdk-aac", "libfdk_aac", "fdk_aac" -> "libfdk_aac"
                    "opus", "libopus" -> "libopus"
                    "mp3", "libmp3lame" -> "libmp3lame"
                    "copy" -> "copy"
                    else -> "aac" // Safe universal native AAC encoder
                }
                add("-c:a")
                add(aCodec)

                if (aCodec != "copy") {
                    val aBitrate = if (config.audioBitrateBps > 0) "${config.audioBitrateBps}" else "128k"
                    add("-b:a")
                    add(aBitrate)
                }
            }

            // Multi-threading optimization for ARM64
            add("-threads")
            add("0")

            // Real-time progress output format
            add("-progress")
            add("pipe:1")

            // Output File
            add(outputPath)
        }

        val fullCmdStr = cmdList.joinToString(" ")
        Log.i("FFmpegEngine", "Running FFmpeg Command: $fullCmdStr")

        val recentLogLines = mutableListOf<String>()

        try {
            val processBuilder = ProcessBuilder(cmdList)
            processBuilder.redirectErrorStream(true)
            val process = processBuilder.start()
            activeProcess = process

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?

            var currentFps = 0.0f
            var currentSpeedStr = "1.0x"
            var currentTimeMs = 0L

            val timePattern = Pattern.compile("out_time_ms=(\\d+)")
            val fpsPattern = Pattern.compile("fps=([0-9.]+)")
            val speedPattern = Pattern.compile("speed=\\s*([0-9.]+x)")

            while (reader.readLine().also { line = it } != null) {
                val currentLine = line ?: continue
                if (recentLogLines.size > 15) recentLogLines.removeAt(0)
                recentLogLines.add(currentLine)

                if (currentLine.startsWith("out_time_ms=")) {
                    val matcher = timePattern.matcher(currentLine)
                    if (matcher.find()) {
                        currentTimeMs = (matcher.group(1)?.toLongOrNull() ?: 0L) / 1000L
                    }
                } else if (currentLine.startsWith("fps=")) {
                    val matcher = fpsPattern.matcher(currentLine)
                    if (matcher.find()) {
                        currentFps = matcher.group(1)?.toFloatOrNull() ?: 0.0f
                    }
                } else if (currentLine.startsWith("speed=")) {
                    val matcher = speedPattern.matcher(currentLine)
                    if (matcher.find()) {
                        currentSpeedStr = matcher.group(1) ?: "1.0x"
                    }
                } else if (currentLine.startsWith("progress=end")) {
                    onProgress(100, currentFps, currentSpeedStr, "100% • Selesai!")
                }

                if (totalDurationMs > 0 && currentTimeMs > 0) {
                    val percent = ((currentTimeMs.toDouble() / totalDurationMs.toDouble()) * 100.0).toInt().coerceIn(0, 99)
                    val statusText = String.format(Locale.US, "%d%% • %.1f fps • %s", percent, currentFps, currentSpeedStr)
                    onProgress(percent, currentFps, currentSpeedStr, statusText)
                }
            }

            val exitCode = process.waitFor()
            activeProcess = null

            if (exitCode == 0 && File(outputPath).exists() && File(outputPath).length() > 0) {
                Log.i("FFmpegEngine", "FFmpeg software encoding completed successfully!")
                onCompleted()
            } else {
                val errReason = recentLogLines.filter { it.isNotBlank() && !it.startsWith("frame=") && !it.startsWith("fps=") && !it.startsWith("progress=") }.takeLast(2).joinToString(" | ")
                val fullErrMsg = if (errReason.isNotBlank()) "FFmpeg Error ($exitCode): $errReason" else "FFmpeg keluar dengan kode error: $exitCode"
                Log.e("FFmpegEngine", fullErrMsg)
                onError(fullErrMsg)
            }

        } catch (e: Exception) {
            activeProcess = null
            Log.e("FFmpegEngine", "Execution exception: ${e.message}", e)
            onError("FFmpeg Error: ${e.message}")
        }
    }

    fun cancelEncoding() {
        try {
            activeProcess?.destroyForcibly()
            activeProcess = null
            Log.i("FFmpegEngine", "FFmpeg process destroyed by user request.")
        } catch (e: Exception) {
            Log.e("FFmpegEngine", "Failed to kill FFmpeg process: ${e.message}", e)
        }
    }
}
