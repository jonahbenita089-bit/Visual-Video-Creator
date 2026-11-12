package com.example.video

import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import android.view.Surface
import com.example.model.AnimationStyle
import com.example.model.GraphicStyle
import com.example.model.VideoAspectRatio
import com.example.model.VideoTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val TAG = "RealVideoEncoder"

data class VideoExportResult(
    val file: File,
    val durationSeconds: Float,
    val width: Int,
    val height: Int,
    val fileSizeFormatted: String
)

object RealVideoEncoder {

    /**
     * Encodes a complete playable MP4 video file on-device using Android MediaCodec and MediaMuxer.
     * Renders each frame of each scene at 30 fps with custom typography, procedural graphics, and animations.
     */
    suspend fun renderMp4Video(
        context: Context,
        template: VideoTemplate,
        aspectRatio: VideoAspectRatio,
        onProgress: (Float) -> Unit
    ): VideoExportResult = withContext(Dispatchers.Default) {
        val (width, height) = when (aspectRatio) {
            VideoAspectRatio.PORTRAIT_9_16 -> Pair(720, 1280)
            VideoAspectRatio.LANDSCAPE_16_9 -> Pair(1280, 720)
            VideoAspectRatio.SQUARE_1_1 -> Pair(720, 720)
        }

        val frameRate = 30
        val bitRate = 4_000_000 // 4 Mbps
        val iFrameInterval = 1 // 1 sec between keyframes

        val outputDir = File(context.cacheDir, "rendered_videos").apply { mkdirs() }
        val outputFile = File(outputDir, "video_${System.currentTimeMillis()}.mp4")

        val totalDurationSeconds = template.scenes.sumOf { it.durationSeconds.toDouble() }.toFloat()
        val totalFrames = (totalDurationSeconds * frameRate).toInt().coerceAtLeast(30)

        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iFrameInterval)
        }

        val codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val inputSurface: Surface = codec.createInputSurface()
        codec.start()

        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var trackIndex = -1
        var muxerStarted = false

        val bufferInfo = MediaCodec.BufferInfo()
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
        }
        val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG)

        try {
            var currentGlobalFrame = 0
            val scenes = template.scenes
            val bgStart = template.palette.bgStart.toInt()
            val bgEnd = template.palette.bgEnd.toInt()
            val primary = template.palette.primary.toInt()
            val secondary = template.palette.secondary.toInt()
            val textPrimary = template.palette.textPrimary.toInt()
            val accent = template.palette.accent.toInt()

            for (sceneIdx in scenes.indices) {
                val scene = scenes[sceneIdx]
                val sceneFrames = (scene.durationSeconds * frameRate).toInt().coerceAtLeast(10)

                for (frame in 0 until sceneFrames) {
                    val sceneProgress = (frame.toFloat() / sceneFrames.toFloat()).coerceIn(0f, 1f)
                    val ptsUs = (currentGlobalFrame * 1_000_000L) / frameRate

                    // Draw frame onto the MediaCodec input surface
                    val canvas = inputSurface.lockCanvas(null)
                    try {
                        // 1. Background gradient
                        val bgShader = LinearGradient(
                            0f, 0f, 0f, height.toFloat(),
                            bgStart, bgEnd,
                            Shader.TileMode.CLAMP
                        )
                        shapePaint.shader = bgShader
                        shapePaint.style = Paint.Style.FILL
                        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), shapePaint)
                        shapePaint.shader = null

                        // 2. Procedural Scene Graphics
                        drawSceneGraphicsOnCanvas(
                            canvas = canvas,
                            width = width.toFloat(),
                            height = height.toFloat(),
                            graphicStyle = scene.graphicStyle,
                            primary = primary,
                            secondary = secondary,
                            accent = accent,
                            sceneProgress = sceneProgress,
                            globalFrame = currentGlobalFrame,
                            shapePaint = shapePaint
                        )

                        // 3. Animated Typography and Content
                        drawSceneTypographyOnCanvas(
                            canvas = canvas,
                            width = width.toFloat(),
                            height = height.toFloat(),
                            scene = scene,
                            primary = primary,
                            accent = accent,
                            textPrimary = textPrimary,
                            sceneProgress = sceneProgress,
                            textPaint = textPaint,
                            shapePaint = shapePaint
                        )
                    } finally {
                        inputSurface.unlockCanvasAndPost(canvas)
                    }

                    // Drain output buffers
                    drainEncoder(codec, muxer, bufferInfo, false) {
                        trackIndex = it
                        muxerStarted = true
                    }

                    currentGlobalFrame++
                    val overallProgress = (currentGlobalFrame.toFloat() / totalFrames.toFloat()).coerceIn(0f, 1f)
                    onProgress(overallProgress)
                }
            }

            // Signal End of Stream
            codec.signalEndOfInputStream()
            drainEncoder(codec, muxer, bufferInfo, true) {
                trackIndex = it
                muxerStarted = true
            }

        } finally {
            try {
                codec.stop()
                codec.release()
            } catch (e: Exception) {
                Log.w(TAG, "Codec stop exception", e)
            }

            try {
                if (muxerStarted) {
                    muxer.stop()
                }
                muxer.release()
            } catch (e: Exception) {
                Log.w(TAG, "Muxer stop exception", e)
            }
        }

        val sizeBytes = outputFile.length()
        val sizeFormatted = String.format("%.2f MB", sizeBytes / (1024f * 1024f))

        VideoExportResult(
            file = outputFile,
            durationSeconds = totalDurationSeconds,
            width = width,
            height = height,
            fileSizeFormatted = sizeFormatted
        )
    }

    private fun drainEncoder(
        codec: MediaCodec,
        muxer: MediaMuxer,
        bufferInfo: MediaCodec.BufferInfo,
        endOfStream: Boolean,
        onMuxerStart: (Int) -> Unit
    ) {
        val timeoutUs = 10_000L
        while (true) {
            val status = codec.dequeueOutputBuffer(bufferInfo, timeoutUs)
            if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) break
            } else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                val newFormat = codec.outputFormat
                val trackIndex = muxer.addTrack(newFormat)
                muxer.start()
                onMuxerStart(trackIndex)
            } else if (status >= 0) {
                val encodedData = codec.getOutputBuffer(status)
                if (encodedData != null) {
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        bufferInfo.size = 0
                    }
                    if (bufferInfo.size != 0) {
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(0, encodedData, bufferInfo)
                    }
                    codec.releaseOutputBuffer(status, false)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        break
                    }
                }
            }
        }
    }

    private fun drawSceneGraphicsOnCanvas(
        canvas: android.graphics.Canvas,
        width: Float,
        height: Float,
        graphicStyle: GraphicStyle,
        primary: Int,
        secondary: Int,
        accent: Int,
        sceneProgress: Float,
        globalFrame: Int,
        shapePaint: Paint
    ) {
        val cx = width / 2f
        val cy = height / 2f

        when (graphicStyle) {
            GraphicStyle.CONCENTRIC_RINGS -> {
                shapePaint.style = Paint.Style.STROKE
                for (i in 1..4) {
                    val r = (width * 0.18f * i) * (0.96f + 0.05f * sin((sceneProgress * 4f + i).toDouble()).toFloat())
                    shapePaint.color = if (i % 2 == 0) primary else secondary
                    shapePaint.strokeWidth = 4f
                    canvas.drawCircle(cx, cy, r, shapePaint)
                }
            }
            GraphicStyle.DYNAMIC_WAVE -> {
                shapePaint.style = Paint.Style.STROKE
                shapePaint.strokeWidth = 5f
                val baseY = height * 0.65f
                val path = Path()
                for (x in 0..width.toInt() step 15) {
                    val y = baseY + sin((x * 0.015f + globalFrame * 0.1f).toDouble()).toFloat() * 35f
                    if (x == 0) path.moveTo(x.toFloat(), y) else path.lineTo(x.toFloat(), y)
                }
                shapePaint.color = primary
                canvas.drawPath(path, shapePaint)
            }
            GraphicStyle.TECH_GRID -> {
                shapePaint.style = Paint.Style.STROKE
                shapePaint.strokeWidth = 1.5f
                shapePaint.color = Color.argb(40, Color.red(primary), Color.green(primary), Color.blue(primary))
                val step = 40f
                var x = 0f
                while (x <= width) {
                    canvas.drawLine(x, 0f, x, height, shapePaint)
                    x += step
                }
                var y = 0f
                while (y <= height) {
                    canvas.drawLine(0f, y, width, y, shapePaint)
                    y += step
                }
            }
            GraphicStyle.CELEBRATION_BURST -> {
                shapePaint.style = Paint.Style.STROKE
                shapePaint.strokeWidth = 3f
                val rayCount = 12
                val rot = (globalFrame * 2f) % 360f
                for (i in 0 until rayCount) {
                    val angle = ((rot + i * (360f / rayCount)) * (PI / 180f)).toFloat()
                    val r1 = 60f
                    val r2 = 180f + 20f * sin((sceneProgress * 6f).toDouble()).toFloat()
                    shapePaint.color = if (i % 2 == 0) primary else accent
                    canvas.drawLine(
                        cx + r1 * cos(angle), cy + r1 * sin(angle),
                        cx + r2 * cos(angle), cy + r2 * sin(angle),
                        shapePaint
                    )
                }
            }
            GraphicStyle.DANCING_DOG -> {
                val dogScale = (width / 500f).coerceIn(0.8f, 1.4f)
                val beatTime = globalFrame * 0.35f
                val bounceY = sin(beatTime.toDouble()).toFloat() * 14f * dogScale
                val swayX = sin((beatTime * 0.5f).toDouble()).toFloat() * 10f * dogScale
                val dogCenterX = cx + swayX
                val dogCenterY = cy + 20f * dogScale + bounceY

                // Disco beat floor
                shapePaint.style = Paint.Style.STROKE
                shapePaint.strokeWidth = 3f
                shapePaint.color = Color.argb(80, Color.red(primary), Color.green(primary), Color.blue(primary))
                val floorY = cy + 120f * dogScale
                for (i in 1..3) {
                    val waveR = (width * 0.3f + i * 30f)
                    canvas.drawOval(
                        dogCenterX - waveR, floorY - 15f * dogScale,
                        dogCenterX + waveR, floorY + 15f * dogScale,
                        shapePaint
                    )
                }

                // Wagging tail
                val tailSway = sin((beatTime * 1.4f).toDouble()).toFloat() * 35f
                shapePaint.style = Paint.Style.STROKE
                shapePaint.strokeWidth = 14f * dogScale
                shapePaint.strokeCap = Paint.Cap.ROUND
                shapePaint.color = Color.rgb(212, 154, 88)
                val tailPath = Path().apply {
                    moveTo(dogCenterX + 45f * dogScale, dogCenterY + 40f * dogScale)
                    quadTo(
                        dogCenterX + 80f * dogScale,
                        dogCenterY + 25f * dogScale + tailSway,
                        dogCenterX + 100f * dogScale,
                        dogCenterY - 15f * dogScale + tailSway * 1.2f
                    )
                }
                canvas.drawPath(tailPath, shapePaint)

                // Body
                shapePaint.style = Paint.Style.FILL
                shapePaint.color = Color.rgb(226, 167, 101)
                val bodyW = 90f * dogScale
                val bodyH = 80f * dogScale
                canvas.drawOval(
                    dogCenterX - bodyW / 2f, dogCenterY + 5f * dogScale,
                    dogCenterX + bodyW / 2f, dogCenterY + 5f * dogScale + bodyH,
                    shapePaint
                )
                // Chest fluff
                shapePaint.color = Color.rgb(255, 241, 214)
                canvas.drawOval(
                    dogCenterX - bodyW * 0.28f, dogCenterY + 12f * dogScale,
                    dogCenterX + bodyW * 0.28f, dogCenterY + 12f * dogScale + bodyH * 0.65f,
                    shapePaint
                )

                // Dancing tapping paws
                val leftPawTap = sin(beatTime.toDouble()).toFloat() * 16f * dogScale
                val rightPawTap = cos(beatTime.toDouble()).toFloat() * 16f * dogScale
                val pawRadius = 16f * dogScale
                shapePaint.color = Color.rgb(255, 241, 214)
                canvas.drawCircle(dogCenterX - 35f * dogScale, dogCenterY + 68f * dogScale + leftPawTap, pawRadius, shapePaint)
                canvas.drawCircle(dogCenterX + 35f * dogScale, dogCenterY + 68f * dogScale + rightPawTap, pawRadius, shapePaint)

                // Collar & Bowtie
                shapePaint.color = accent
                canvas.drawRoundRect(
                    dogCenterX - 28f * dogScale, dogCenterY - 2f * dogScale,
                    dogCenterX + 28f * dogScale, dogCenterY + 10f * dogScale,
                    6f, 6f, shapePaint
                )
                shapePaint.color = primary
                canvas.drawCircle(dogCenterX, dogCenterY + 4f * dogScale, 8f * dogScale, shapePaint)

                // Head
                val headRadius = 48f * dogScale
                val headCenterY = dogCenterY - 35f * dogScale
                val earFlap = sin((beatTime * 1.1f).toDouble()).toFloat() * 18f

                // Ears
                shapePaint.color = Color.rgb(184, 120, 56)
                val leftEar = Path().apply {
                    moveTo(dogCenterX - 30f * dogScale, headCenterY - 20f * dogScale)
                    quadTo(dogCenterX - 65f * dogScale + earFlap, headCenterY + 10f * dogScale, dogCenterX - 45f * dogScale, headCenterY + 45f * dogScale)
                    quadTo(dogCenterX - 25f * dogScale, headCenterY + 20f * dogScale, dogCenterX - 20f * dogScale, headCenterY - 5f * dogScale)
                }
                canvas.drawPath(leftEar, shapePaint)

                val rightEar = Path().apply {
                    moveTo(dogCenterX + 30f * dogScale, headCenterY - 20f * dogScale)
                    quadTo(dogCenterX + 65f * dogScale - earFlap, headCenterY + 10f * dogScale, dogCenterX + 45f * dogScale, headCenterY + 45f * dogScale)
                    quadTo(dogCenterX + 25f * dogScale, headCenterY + 20f * dogScale, dogCenterX + 20f * dogScale, headCenterY - 5f * dogScale)
                }
                canvas.drawPath(rightEar, shapePaint)

                // Head circle
                shapePaint.color = Color.rgb(226, 167, 101)
                canvas.drawCircle(dogCenterX, headCenterY, headRadius, shapePaint)

                // Snout
                shapePaint.color = Color.rgb(255, 241, 214)
                val snoutW = 46f * dogScale
                val snoutH = 34f * dogScale
                val snoutY = headCenterY + 12f * dogScale
                canvas.drawOval(
                    dogCenterX - snoutW / 2f, snoutY - snoutH / 2f,
                    dogCenterX + snoutW / 2f, snoutY + snoutH / 2f,
                    shapePaint
                )

                // Nose
                shapePaint.color = Color.rgb(30, 30, 30)
                canvas.drawCircle(dogCenterX, snoutY - 4f * dogScale, 8f * dogScale, shapePaint)

                // Tongue
                shapePaint.color = Color.rgb(255, 107, 129)
                canvas.drawOval(
                    dogCenterX - 7f * dogScale, snoutY + 6f * dogScale,
                    dogCenterX + 7f * dogScale, snoutY + 18f * dogScale,
                    shapePaint
                )

                // Eyes
                shapePaint.color = Color.rgb(30, 30, 30)
                canvas.drawCircle(dogCenterX - 18f * dogScale, headCenterY - 10f * dogScale, 7f * dogScale, shapePaint)
                canvas.drawCircle(dogCenterX + 18f * dogScale, headCenterY - 10f * dogScale, 7f * dogScale, shapePaint)
                shapePaint.color = Color.WHITE
                canvas.drawCircle(dogCenterX - 20f * dogScale, headCenterY - 12f * dogScale, 2.5f * dogScale, shapePaint)
                canvas.drawCircle(dogCenterX + 16f * dogScale, headCenterY - 12f * dogScale, 2.5f * dogScale, shapePaint)
            }
            else -> {
                // Aurora bloom default
                shapePaint.style = Paint.Style.FILL
                shapePaint.color = Color.argb(60, Color.red(primary), Color.green(primary), Color.blue(primary))
                canvas.drawCircle(cx, cy - height * 0.1f, width * 0.35f, shapePaint)
                shapePaint.color = Color.argb(50, Color.red(secondary), Color.green(secondary), Color.blue(secondary))
                canvas.drawCircle(cx, cy + height * 0.1f, width * 0.4f, shapePaint)
            }
        }
    }

    private fun drawSceneTypographyOnCanvas(
        canvas: android.graphics.Canvas,
        width: Float,
        height: Float,
        scene: com.example.model.VideoScene,
        primary: Int,
        accent: Int,
        textPrimary: Int,
        sceneProgress: Float,
        textPaint: Paint,
        shapePaint: Paint
    ) {
        val cx = width / 2f
        val cy = height / 2f

        // Motion translation based on animation
        val introProgress = (sceneProgress / 0.2f).coerceIn(0f, 1f)
        val alphaInt = (introProgress * 255).toInt()
        val textYOffset = when (scene.animation) {
            AnimationStyle.SLIDE_UP -> (1f - introProgress) * 40f
            else -> 0f
        }

        // 1. Badge Pill
        if (scene.badge.isNotBlank()) {
            shapePaint.style = Paint.Style.FILL
            shapePaint.color = accent
            val badgeW = 180f
            val badgeH = 40f
            val badgeY = cy - 120f + textYOffset
            canvas.drawRoundRect(
                cx - badgeW / 2f, badgeY - badgeH / 2f,
                cx + badgeW / 2f, badgeY + badgeH / 2f,
                20f, 20f, shapePaint
            )

            textPaint.color = Color.BLACK
            textPaint.textSize = 18f
            textPaint.isFakeBoldText = true
            canvas.drawText(scene.badge.uppercase(), cx, badgeY + 6f, textPaint)
        }

        // 2. Headline Title
        textPaint.color = textPrimary
        textPaint.textSize = if (width > 800) 48f else 38f
        textPaint.isFakeBoldText = true
        textPaint.alpha = alphaInt
        canvas.drawText(scene.title, cx, cy - 30f + textYOffset, textPaint)

        // 3. Subtitle
        if (scene.subtitle.isNotBlank()) {
            textPaint.textSize = if (width > 800) 24f else 20f
            textPaint.isFakeBoldText = false
            textPaint.alpha = (alphaInt * 0.85f).toInt()
            canvas.drawText(scene.subtitle, cx, cy + 25f + textYOffset, textPaint)
        }

        // 4. Bottom Caption Tag
        if (scene.caption.isNotBlank()) {
            shapePaint.style = Paint.Style.FILL
            shapePaint.color = Color.argb(160, 0, 0, 0)
            val capW = width * 0.7f
            val capH = 44f
            val capY = height - 80f
            canvas.drawRoundRect(
                cx - capW / 2f, capY - capH / 2f,
                cx + capW / 2f, capY + capH / 2f,
                16f, 16f, shapePaint
            )

            textPaint.color = primary
            textPaint.textSize = 18f
            textPaint.isFakeBoldText = true
            textPaint.alpha = 255
            canvas.drawText(scene.caption, cx, capY + 6f, textPaint)
        }
    }
}
