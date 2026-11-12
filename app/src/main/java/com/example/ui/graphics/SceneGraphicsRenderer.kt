package com.example.ui.graphics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.ColorPalettePreset
import com.example.model.GraphicStyle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SceneGraphicCanvas(
    graphicStyle: GraphicStyle,
    palette: ColorPalettePreset,
    sceneProgress: Float,
    elapsedMs: Long,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(palette.primary)
    val secondaryColor = Color(palette.secondary)
    val accentColor = Color(palette.accent)

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        when (graphicStyle) {
            GraphicStyle.CONCENTRIC_RINGS -> {
                drawConcentricRings(cx, cy, primaryColor, secondaryColor, accentColor, sceneProgress, elapsedMs)
            }
            GraphicStyle.DYNAMIC_WAVE -> {
                drawDynamicWaves(w, h, primaryColor, secondaryColor, accentColor, sceneProgress, elapsedMs)
            }
            GraphicStyle.TECH_GRID -> {
                drawTechGrid(w, h, primaryColor, secondaryColor, accentColor, sceneProgress, elapsedMs)
            }
            GraphicStyle.AURORA_GRADIENT -> {
                drawAuroraBlooms(cx, cy, w, h, primaryColor, secondaryColor, accentColor, sceneProgress, elapsedMs)
            }
            GraphicStyle.CELEBRATION_BURST -> {
                drawCelebrationBurst(cx, cy, primaryColor, secondaryColor, accentColor, sceneProgress, elapsedMs)
            }
            GraphicStyle.METRICS_CHART -> {
                drawMetricsChart(w, h, primaryColor, secondaryColor, accentColor, sceneProgress)
            }
            GraphicStyle.ABSTRACT_SPHERE -> {
                drawAbstractSphere(cx, cy, primaryColor, secondaryColor, accentColor, sceneProgress, elapsedMs)
            }
            GraphicStyle.NEON_HEXAGONS -> {
                drawNeonHexagons(w, h, primaryColor, secondaryColor, accentColor, sceneProgress, elapsedMs)
            }
            GraphicStyle.DANCING_DOG -> {
                drawDancingDog(cx, cy, w, h, primaryColor, secondaryColor, accentColor, sceneProgress, elapsedMs)
            }
            GraphicStyle.RETRO_SYNTHWAVE -> {
                drawRetroSynthwave(w, h, cx, cy, primaryColor, secondaryColor, accentColor, sceneProgress, elapsedMs)
            }
            GraphicStyle.CINEMATIC_LETTERBOX -> {
                drawCinematicFlare(w, h, cx, cy, primaryColor, secondaryColor, accentColor, sceneProgress, elapsedMs)
            }
            GraphicStyle.CARTOON_SPEED_LINES -> {
                drawCartoonSpeedLines(w, h, cx, cy, primaryColor, secondaryColor, accentColor, sceneProgress, elapsedMs)
            }
            GraphicStyle.LUXURY_MINIMAL -> {
                drawLuxuryMinimal(cx, cy, primaryColor, secondaryColor, accentColor, sceneProgress, elapsedMs)
            }
            GraphicStyle.CYBER_HUD_TARGET -> {
                drawCyberHudTarget(cx, cy, primaryColor, secondaryColor, accentColor, sceneProgress, elapsedMs)
            }
            GraphicStyle.AUDIO_SPECTRUM -> {
                drawAudioSpectrum(w, h, cx, cy, primaryColor, secondaryColor, accentColor, sceneProgress, elapsedMs)
            }
            GraphicStyle.NATURE_HORIZON -> {
                drawNatureHorizon(w, h, cx, cy, primaryColor, secondaryColor, accentColor, sceneProgress, elapsedMs)
            }
        }
    }
}

private fun DrawScope.drawConcentricRings(
    cx: Float,
    cy: Float,
    primary: Color,
    secondary: Color,
    accent: Color,
    progress: Float,
    elapsedMs: Long
) {
    val baseRadius = (size.minDimension / 4f)
    val rot = (elapsedMs / 40f) % 360f

    for (i in 1..4) {
        val r = baseRadius * (i * 0.55f) * (0.95f + 0.08f * sin(progress * PI.toFloat() * 2f + i))
        val strokeWidth = 2.5f * (5 - i)
        val alpha = (0.2f + 0.15f * i).coerceIn(0.1f, 0.8f)

        drawCircle(
            color = if (i % 2 == 0) primary.copy(alpha = alpha) else secondary.copy(alpha = alpha),
            radius = r,
            center = Offset(cx, cy),
            style = Stroke(width = strokeWidth)
        )

        // Draw orbital dot
        val dotAngle = ((rot * (if (i % 2 == 0) 1 else -1) + i * 72f) * (PI / 180f)).toFloat()
        val dotX = cx + r * cos(dotAngle)
        val dotY = cy + r * sin(dotAngle)
        drawCircle(
            color = if (i == 2) accent else primary,
            radius = 5f + i * 1.5f,
            center = Offset(dotX, dotY)
        )
    }
}

private fun DrawScope.drawDynamicWaves(
    w: Float,
    h: Float,
    primary: Color,
    secondary: Color,
    accent: Color,
    progress: Float,
    elapsedMs: Long
) {
    val phase = (elapsedMs / 600f)
    val baseY = h * 0.65f

    for (layer in 0..2) {
        val path = Path()
        path.moveTo(0f, baseY)
        val waveColor = when (layer) {
            0 -> primary.copy(alpha = 0.35f)
            1 -> secondary.copy(alpha = 0.45f)
            else -> accent.copy(alpha = 0.6f)
        }
        val amplitude = 25f + layer * 15f
        val freq = 0.015f - layer * 0.003f
        val layerPhase = phase + layer * 1.2f

        for (x in 0..w.toInt() step 12) {
            val y = baseY + sin(x * freq + layerPhase) * amplitude + cos(x * 0.008f + layerPhase * 0.7f) * 12f
            if (x == 0) path.moveTo(x.toFloat(), y) else path.lineTo(x.toFloat(), y)
        }

        drawPath(
            path = path,
            color = waveColor,
            style = Stroke(width = 3.5f - layer * 0.5f, cap = StrokeCap.Round)
        )
    }
}

private fun DrawScope.drawTechGrid(
    w: Float,
    h: Float,
    primary: Color,
    secondary: Color,
    accent: Color,
    progress: Float,
    elapsedMs: Long
) {
    val step = 36f
    val gridAlpha = 0.14f

    // Vertical lines
    var x = 0f
    while (x <= w) {
        drawLine(
            color = primary.copy(alpha = gridAlpha),
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = 1f
        )
        x += step
    }

    // Horizontal lines
    var y = 0f
    while (y <= h) {
        drawLine(
            color = primary.copy(alpha = gridAlpha),
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 1f
        )
        y += step
    }

    // Glowing coordinate markers
    val markerCount = 5
    for (i in 0 until markerCount) {
        val pulse = (sin(elapsedMs / 300f + i) + 1f) / 2f
        val mx = (w * (0.2f + 0.15f * i)).toInt() / step.toInt() * step
        val my = (h * (0.3f + 0.1f * ((i * 3) % 4))).toInt() / step.toInt() * step

        drawCircle(
            color = accent.copy(alpha = 0.7f * pulse),
            radius = 4f + 3f * pulse,
            center = Offset(mx, my)
        )
        drawCircle(
            color = secondary.copy(alpha = 0.4f * pulse),
            radius = 12f + 6f * pulse,
            center = Offset(mx, my),
            style = Stroke(width = 1.5f)
        )
    }
}

private fun DrawScope.drawAuroraBlooms(
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    primary: Color,
    secondary: Color,
    accent: Color,
    progress: Float,
    elapsedMs: Long
) {
    val t = elapsedMs / 1200f
    val r1 = (w * 0.35f) * (0.85f + 0.15f * sin(t))
    val r2 = (w * 0.4f) * (0.85f + 0.15f * cos(t * 0.8f))

    val o1 = Offset(cx + cos(t) * (w * 0.12f), cy - h * 0.1f + sin(t) * (h * 0.08f))
    val o2 = Offset(cx - cos(t * 0.7f) * (w * 0.15f), cy + h * 0.12f + cos(t * 0.9f) * (h * 0.06f))

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(primary.copy(alpha = 0.4f), Color.Transparent),
            center = o1,
            radius = r1
        ),
        radius = r1,
        center = o1
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(secondary.copy(alpha = 0.35f), Color.Transparent),
            center = o2,
            radius = r2
        ),
        radius = r2,
        center = o2
    )
}

private fun DrawScope.drawCelebrationBurst(
    cx: Float,
    cy: Float,
    primary: Color,
    secondary: Color,
    accent: Color,
    progress: Float,
    elapsedMs: Long
) {
    val rayCount = 12
    val rot = (elapsedMs / 60f) % 360f

    for (i in 0 until rayCount) {
        val angle = ((rot + i * (360f / rayCount)) * (PI / 180f)).toFloat()
        val lenInner = 50f + 10f * sin(progress * PI.toFloat() * 3f + i)
        val lenOuter = 160f + 25f * cos(progress * PI.toFloat() * 2f + i)

        val x1 = cx + lenInner * cos(angle)
        val y1 = cy + lenInner * sin(angle)
        val x2 = cx + lenOuter * cos(angle)
        val y2 = cy + lenOuter * sin(angle)

        val rayColor = when (i % 3) {
            0 -> primary
            1 -> secondary
            else -> accent
        }.copy(alpha = 0.6f)

        drawLine(
            color = rayColor,
            start = Offset(x1, y1),
            end = Offset(x2, y2),
            strokeWidth = if (i % 2 == 0) 3f else 1.5f,
            cap = StrokeCap.Round
        )

        // Floating diamond confetti
        val particleDist = lenOuter + 30f + (i * 7) % 40
        val px = cx + particleDist * cos(angle + 0.1f)
        val py = cy + particleDist * sin(angle + 0.1f)
        drawCircle(
            color = accent.copy(alpha = 0.8f),
            radius = 3.5f,
            center = Offset(px, py)
        )
    }
}

private fun DrawScope.drawMetricsChart(
    w: Float,
    h: Float,
    primary: Color,
    secondary: Color,
    accent: Color,
    progress: Float
) {
    val barCount = 5
    val barWidth = 28f
    val startX = w * 0.18f
    val spacing = (w * 0.64f) / barCount
    val baseY = h * 0.72f
    val maxHeight = h * 0.32f

    val heights = listOf(0.4f, 0.62f, 0.55f, 0.82f, 0.98f)

    val trendPoints = mutableListOf<Offset>()

    for (i in 0 until barCount) {
        val targetH = maxHeight * heights[i]
        // Animate entrance based on scene progress
        val barProgress = ((progress * 1.4f) - (i * 0.12f)).coerceIn(0f, 1f)
        val currentH = targetH * barProgress
        val bx = startX + i * spacing
        val by = baseY - currentH

        drawRect(
            color = if (i == barCount - 1) accent.copy(alpha = 0.85f) else primary.copy(alpha = 0.45f + i * 0.08f),
            topLeft = Offset(bx, by),
            size = Size(barWidth, currentH)
        )

        trendPoints.add(Offset(bx + barWidth / 2f, by))
    }

    // Trend line
    if (trendPoints.size > 1) {
        val linePath = Path()
        linePath.moveTo(trendPoints.first().x, trendPoints.first().y)
        for (idx in 1 until trendPoints.size) {
            linePath.lineTo(trendPoints[idx].x, trendPoints[idx].y)
        }
        drawPath(
            path = linePath,
            color = secondary,
            style = Stroke(width = 3f, cap = StrokeCap.Round)
        )
        // Trend line head
        drawCircle(
            color = accent,
            radius = 6f,
            center = trendPoints.last()
        )
    }
}

private fun DrawScope.drawAbstractSphere(
    cx: Float,
    cy: Float,
    primary: Color,
    secondary: Color,
    accent: Color,
    progress: Float,
    elapsedMs: Long
) {
    val sphereRadius = (size.minDimension * 0.3f)
    val rot = (elapsedMs / 50f)

    for (step in 0..4) {
        val factor = cos((rot + step * 36f) * (PI / 180f)).toFloat()
        val ellipseWidth = sphereRadius * factor
        val color = if (step % 2 == 0) primary else secondary

        drawOval(
            color = color.copy(alpha = 0.55f),
            topLeft = Offset(cx - ellipseWidth, cy - sphereRadius),
            size = Size(ellipseWidth * 2f, sphereRadius * 2f),
            style = Stroke(width = 2f)
        )
    }

    // Outer equatorial ring
    drawCircle(
        color = accent.copy(alpha = 0.7f),
        radius = sphereRadius * 1.15f,
        center = Offset(cx, cy),
        style = Stroke(width = 2.5f)
    )
}

private fun DrawScope.drawNeonHexagons(
    w: Float,
    h: Float,
    primary: Color,
    secondary: Color,
    accent: Color,
    progress: Float,
    elapsedMs: Long
) {
    val hexRadius = 45f
    val cx = w / 2f
    val cy = h * 0.5f
    val pulse = (sin(elapsedMs / 400f) + 1f) / 2f

    val centers = listOf(
        Offset(cx, cy),
        Offset(cx - hexRadius * 1.6f, cy - hexRadius * 0.9f),
        Offset(cx + hexRadius * 1.6f, cy - hexRadius * 0.9f),
        Offset(cx - hexRadius * 1.6f, cy + hexRadius * 0.9f),
        Offset(cx + hexRadius * 1.6f, cy + hexRadius * 0.9f),
        Offset(cx, cy - hexRadius * 1.8f),
        Offset(cx, cy + hexRadius * 1.8f)
    )

    centers.forEachIndexed { index, center ->
        val path = Path()
        for (i in 0 until 6) {
            val angle = (i * 60f + 30f) * (PI / 180f).toFloat()
            val px = center.x + hexRadius * cos(angle)
            val py = center.y + hexRadius * sin(angle)
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()

        val hexColor = if (index == 0) accent.copy(alpha = 0.6f + 0.3f * pulse)
        else if (index % 2 == 0) primary.copy(alpha = 0.4f)
        else secondary.copy(alpha = 0.35f)

        drawPath(
            path = path,
            color = hexColor,
            style = Stroke(width = if (index == 0) 3f else 1.8f)
        )
    }
}

private fun DrawScope.drawDancingDog(
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    primary: Color,
    secondary: Color,
    accent: Color,
    progress: Float,
    elapsedMs: Long
) {
    val dogScale = (size.minDimension / 480f).coerceIn(0.7f, 1.3f)
    val beatTime = elapsedMs / 100f
    val bounceY = sin(beatTime * 0.8) * 12f * dogScale
    val swayX = sin(beatTime * 0.4) * 8f * dogScale
    val tailSway = sin(beatTime * 1.2) * 35f

    val dogCenterY = cy + 20f * dogScale + bounceY.toFloat()
    val dogCenterX = cx + swayX.toFloat()

    // 1. Dance Floor Beat Waves
    val floorY = cy + 120f * dogScale
    for (i in 1..3) {
        val waveR = (w * 0.28f + i * 28f) * (0.95f + 0.08f * sin(beatTime * 0.6 + i).toFloat())
        drawOval(
            color = primary.copy(alpha = 0.25f / i),
            topLeft = Offset(cx - waveR, floorY - 14f * dogScale),
            size = Size(waveR * 2f, 28f * dogScale),
            style = Stroke(width = 2.5f)
        )
    }

    // 2. Floating Musical / Disco Notes
    for (i in 0 until 5) {
        val notePhase = (elapsedMs / 800f + i * 1.3f) % 3f
        val noteY = cy - 40f * dogScale - notePhase * 50f
        val noteX = cx + (if (i % 2 == 0) -1 else 1) * (70f + (i * 25f)) * dogScale + sin(notePhase * 2f) * 15f
        val noteAlpha = (1f - (notePhase / 3f)).coerceIn(0f, 0.8f)

        drawCircle(
            color = accent.copy(alpha = noteAlpha),
            radius = 5f * dogScale,
            center = Offset(noteX, noteY)
        )
        drawLine(
            color = accent.copy(alpha = noteAlpha),
            start = Offset(noteX + 4f * dogScale, noteY),
            end = Offset(noteX + 4f * dogScale, noteY - 16f * dogScale),
            strokeWidth = 2f * dogScale
        )
    }

    // 3. Wagging Tail (drawn behind body)
    val tailStartX = dogCenterX + 45f * dogScale
    val tailStartY = dogCenterY + 40f * dogScale
    val tailPath = Path()
    tailPath.moveTo(tailStartX, tailStartY)
    tailPath.quadraticTo(
        tailStartX + 35f * dogScale,
        tailStartY - 10f * dogScale + tailSway.toFloat(),
        tailStartX + 50f * dogScale,
        tailStartY - 40f * dogScale + tailSway.toFloat() * 1.3f
    )
    drawPath(
        path = tailPath,
        color = Color(0xFFD49A58),
        style = Stroke(width = 14f * dogScale, cap = StrokeCap.Round)
    )

    // 4. Dog Body
    val bodyW = 90f * dogScale
    val bodyH = 80f * dogScale
    drawOval(
        color = Color(0xFFE2A765),
        topLeft = Offset(dogCenterX - bodyW / 2f, dogCenterY + 5f * dogScale),
        size = Size(bodyW, bodyH)
    )
    // Chest fluff
    drawOval(
        color = Color(0xFFFFF1D6),
        topLeft = Offset(dogCenterX - bodyW * 0.28f, dogCenterY + 12f * dogScale),
        size = Size(bodyW * 0.56f, bodyH * 0.65f)
    )

    // 5. Dancing Tapping Paws
    val leftPawTap = sin(beatTime).toFloat() * 15f * dogScale
    val rightPawTap = cos(beatTime).toFloat() * 15f * dogScale
    val pawRadius = 16f * dogScale

    // Left paw
    drawCircle(
        color = Color(0xFFFFF1D6),
        radius = pawRadius,
        center = Offset(dogCenterX - 35f * dogScale, dogCenterY + 68f * dogScale + leftPawTap)
    )
    drawCircle(
        color = Color(0xFFC88746),
        radius = pawRadius,
        center = Offset(dogCenterX - 35f * dogScale, dogCenterY + 68f * dogScale + leftPawTap),
        style = Stroke(width = 2f)
    )

    // Right paw
    drawCircle(
        color = Color(0xFFFFF1D6),
        radius = pawRadius,
        center = Offset(dogCenterX + 35f * dogScale, dogCenterY + 68f * dogScale + rightPawTap)
    )
    drawCircle(
        color = Color(0xFFC88746),
        radius = pawRadius,
        center = Offset(dogCenterX + 35f * dogScale, dogCenterY + 68f * dogScale + rightPawTap),
        style = Stroke(width = 2f)
    )

    // 6. Dance Collar with Bowtie
    val collarY = dogCenterY + 5f * dogScale
    drawRoundRect(
        color = accent,
        topLeft = Offset(dogCenterX - 28f * dogScale, collarY - 5f * dogScale),
        size = Size(56f * dogScale, 10f * dogScale),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )
    // Bowtie knot
    drawCircle(
        color = primary,
        radius = 7f * dogScale,
        center = Offset(dogCenterX, collarY)
    )

    // 7. Dog Head (bobs slightly faster)
    val headRadius = 46f * dogScale
    val headCenterY = dogCenterY - 35f * dogScale
    val headCenterX = dogCenterX

    // Floppy dancing ears
    val earFlapL = sin(beatTime * 0.9).toFloat() * 18f
    val earFlapR = -sin(beatTime * 0.9).toFloat() * 18f

    // Left ear
    val leftEarPath = Path()
    leftEarPath.moveTo(headCenterX - 28f * dogScale, headCenterY - 20f * dogScale)
    leftEarPath.quadraticTo(
        headCenterX - 60f * dogScale + earFlapL,
        headCenterY + 10f * dogScale,
        headCenterX - 45f * dogScale,
        headCenterY + 45f * dogScale + earFlapL * 0.5f
    )
    leftEarPath.quadraticTo(
        headCenterX - 25f * dogScale,
        headCenterY + 25f * dogScale,
        headCenterX - 20f * dogScale,
        headCenterY - 5f * dogScale
    )
    drawPath(path = leftEarPath, color = Color(0xFFB87838))

    // Right ear
    val rightEarPath = Path()
    rightEarPath.moveTo(headCenterX + 28f * dogScale, headCenterY - 20f * dogScale)
    rightEarPath.quadraticTo(
        headCenterX + 60f * dogScale + earFlapR,
        headCenterY + 10f * dogScale,
        headCenterX + 45f * dogScale,
        headCenterY + 45f * dogScale + earFlapR * 0.5f
    )
    rightEarPath.quadraticTo(
        headCenterX + 25f * dogScale,
        headCenterY + 25f * dogScale,
        headCenterX + 20f * dogScale,
        headCenterY - 5f * dogScale
    )
    drawPath(path = rightEarPath, color = Color(0xFFB87838))

    // Main head circle
    drawCircle(
        color = Color(0xFFE2A765),
        radius = headRadius,
        center = Offset(headCenterX, headCenterY)
    )

    // Snout
    val snoutW = 46f * dogScale
    val snoutH = 34f * dogScale
    val snoutY = headCenterY + 12f * dogScale
    drawOval(
        color = Color(0xFFFFF1D6),
        topLeft = Offset(headCenterX - snoutW / 2f, snoutY - snoutH / 2f),
        size = Size(snoutW, snoutH)
    )

    // Little cute nose
    val noseRadius = 7f * dogScale
    drawCircle(
        color = Color(0xFF1E1E1E),
        radius = noseRadius,
        center = Offset(headCenterX, snoutY - 4f * dogScale)
    )

    // Happy smiling mouth
    val mouthPath = Path()
    mouthPath.moveTo(headCenterX - 10f * dogScale, snoutY + 4f * dogScale)
    mouthPath.quadraticTo(headCenterX, snoutY + 12f * dogScale, headCenterX + 10f * dogScale, snoutY + 4f * dogScale)
    drawPath(path = mouthPath, color = Color(0xFF2B1810), style = Stroke(width = 2.5f * dogScale, cap = StrokeCap.Round))

    // Happy pink tongue
    val tongueW = 12f * dogScale
    val tongueH = 10f * dogScale
    drawOval(
        color = Color(0xFFFF6B81),
        topLeft = Offset(headCenterX - tongueW / 2f, snoutY + 8f * dogScale),
        size = Size(tongueW, tongueH)
    )

    // Happy eyes
    val eyeOffsetX = 16f * dogScale
    val eyeY = headCenterY - 10f * dogScale
    // Left eye
    drawCircle(color = Color(0xFF1E1E1E), radius = 6.5f * dogScale, center = Offset(headCenterX - eyeOffsetX, eyeY))
    drawCircle(color = Color.White, radius = 2f * dogScale, center = Offset(headCenterX - eyeOffsetX - 1.5f, eyeY - 2f))
    // Right eye
    drawCircle(color = Color(0xFF1E1E1E), radius = 6.5f * dogScale, center = Offset(headCenterX + eyeOffsetX, eyeY))
    drawCircle(color = Color.White, radius = 2f * dogScale, center = Offset(headCenterX + eyeOffsetX - 1.5f, eyeY - 2f))

    // Eyebrows showing groove expression
    drawLine(
        color = Color(0xFF7A4A20),
        start = Offset(headCenterX - eyeOffsetX - 6f * dogScale, eyeY - 11f * dogScale),
        end = Offset(headCenterX - eyeOffsetX + 6f * dogScale, eyeY - 9f * dogScale),
        strokeWidth = 2.5f * dogScale,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFF7A4A20),
        start = Offset(headCenterX + eyeOffsetX - 6f * dogScale, eyeY - 9f * dogScale),
        end = Offset(headCenterX + eyeOffsetX + 6f * dogScale, eyeY - 11f * dogScale),
        strokeWidth = 2.5f * dogScale,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawRetroSynthwave(
    w: Float,
    h: Float,
    cx: Float,
    cy: Float,
    primary: Color,
    secondary: Color,
    accent: Color,
    progress: Float,
    elapsedMs: Long
) {
    val horizonY = h * 0.58f

    // 1. Synthwave Horizon Sun with horizontal segments
    val sunRadius = w * 0.22f
    val sunCenter = Offset(cx, horizonY - 15f)
    drawCircle(
        brush = Brush.verticalGradient(
            colors = listOf(accent, Color(0xFFFFD166)),
            startY = sunCenter.y - sunRadius,
            endY = sunCenter.y + sunRadius
        ),
        radius = sunRadius,
        center = sunCenter
    )
    // Slices in sun
    for (i in 1..5) {
        val sliceY = sunCenter.y + (i * 12f)
        val sliceH = 2f + i * 1.5f
        drawRect(
            color = Color(0xFF161F38),
            topLeft = Offset(sunCenter.x - sunRadius, sliceY),
            size = Size(sunRadius * 2f, sliceH)
        )
    }

    // 2. Perspective Ground Grid
    val vanishX = cx
    val vanishY = horizonY
    val lineCount = 10
    for (i in -lineCount..lineCount) {
        val bottomX = cx + (i * w * 0.12f)
        drawLine(
            color = primary.copy(alpha = 0.5f),
            start = Offset(vanishX, vanishY),
            end = Offset(bottomX, h),
            strokeWidth = 1.8f
        )
    }
    // Horizontal moving grid lines
    val gridStep = 45f
    val offset = (elapsedMs / 25f) % gridStep
    var y = horizonY + 10f
    var step = 8f
    while (y <= h) {
        val alpha = ((y - horizonY) / (h - horizonY)).coerceIn(0.1f, 0.7f)
        drawLine(
            color = secondary.copy(alpha = alpha),
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 2f
        )
        step *= 1.35f
        y += step + offset * 0.1f
    }
}

private fun DrawScope.drawCinematicFlare(
    w: Float,
    h: Float,
    cx: Float,
    cy: Float,
    primary: Color,
    secondary: Color,
    accent: Color,
    progress: Float,
    elapsedMs: Long
) {
    val flarePulse = (sin(elapsedMs / 400f) + 1f) / 2f
    val flareY = cy - 20f

    // Horizontal anamorphic lens flare beam
    drawLine(
        brush = Brush.horizontalGradient(
            colors = listOf(Color.Transparent, primary.copy(alpha = 0.8f), Color.White, primary.copy(alpha = 0.8f), Color.Transparent),
            startX = 0f,
            endX = w
        ),
        start = Offset(0f, flareY),
        end = Offset(w, flareY),
        strokeWidth = 3f + 3f * flarePulse
    )

    // Central flare halo
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.9f), primary.copy(alpha = 0.4f), Color.Transparent),
            center = Offset(cx, flareY),
            radius = 60f + 25f * flarePulse
        ),
        radius = 60f + 25f * flarePulse,
        center = Offset(cx, flareY)
    )

    // Floating cinematic dust motes
    for (i in 0 until 8) {
        val motePhase = (elapsedMs / 1000f + i * 1.5f) % 4f
        val my = (h * (0.2f + (i * 0.08f))) + sin(motePhase + i) * 20f
        val mx = (w * (0.15f + (i * 0.09f))) + cos(motePhase * 0.8f) * 15f
        drawCircle(
            color = Color.White.copy(alpha = 0.4f),
            radius = 2.5f,
            center = Offset(mx, my)
        )
    }
}

private fun DrawScope.drawCartoonSpeedLines(
    w: Float,
    h: Float,
    cx: Float,
    cy: Float,
    primary: Color,
    secondary: Color,
    accent: Color,
    progress: Float,
    elapsedMs: Long
) {
    val lineCount = 20
    val rot = (elapsedMs / 30f) % 360f

    for (i in 0 until lineCount) {
        val angle = ((rot + i * (360f / lineCount)) * (PI / 180f)).toFloat()
        val jitter = (sin(elapsedMs / 50f + i) * 15f).toFloat()
        val innerR = w * 0.22f + jitter
        val outerR = w * 0.65f

        val x1 = cx + innerR * cos(angle)
        val y1 = cy + innerR * sin(angle)
        val x2 = cx + outerR * cos(angle)
        val y2 = cy + outerR * sin(angle)

        val stroke = if (i % 3 == 0) 4.5f else 2f
        val color = if (i % 2 == 0) primary else accent

        drawLine(
            color = color.copy(alpha = 0.6f),
            start = Offset(x1, y1),
            end = Offset(x2, y2),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }

    // Comic center impact ring
    drawCircle(
        color = accent.copy(alpha = 0.5f),
        radius = w * 0.18f,
        center = Offset(cx, cy),
        style = Stroke(width = 3f)
    )
}

private fun DrawScope.drawLuxuryMinimal(
    cx: Float,
    cy: Float,
    primary: Color,
    secondary: Color,
    accent: Color,
    progress: Float,
    elapsedMs: Long
) {
    val rot = (elapsedMs / 80f) % 360f
    val r = 90f

    // Rotating gold diamond wireframe
    for (layer in 0..1) {
        val lRot = (rot * (if (layer == 0) 1f else -0.7f)) * (PI / 180f).toFloat()
        val path = Path()
        for (i in 0 until 4) {
            val angle = lRot + (i * 90f) * (PI / 180f).toFloat()
            val px = cx + (r * (1f - layer * 0.3f)) * cos(angle)
            val py = cy + (r * (1f - layer * 0.3f)) * sin(angle)
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()

        drawPath(
            path = path,
            color = if (layer == 0) accent.copy(alpha = 0.7f) else primary.copy(alpha = 0.5f),
            style = Stroke(width = 1.8f)
        )
    }

    // Outer refined accent ring
    drawCircle(
        color = secondary.copy(alpha = 0.4f),
        radius = r * 1.3f,
        center = Offset(cx, cy),
        style = Stroke(width = 1f)
    )
}

private fun DrawScope.drawCyberHudTarget(
    cx: Float,
    cy: Float,
    primary: Color,
    secondary: Color,
    accent: Color,
    progress: Float,
    elapsedMs: Long
) {
    val rot = (elapsedMs / 60f) % 360f
    val targetR = 110f

    // Segmented rotating outer dial
    val segCount = 8
    for (i in 0 until segCount) {
        val startAngle = rot + i * (360f / segCount)
        drawArc(
            color = primary.copy(alpha = 0.7f),
            startAngle = startAngle,
            sweepAngle = 25f,
            useCenter = false,
            topLeft = Offset(cx - targetR, cy - targetR),
            size = Size(targetR * 2f, targetR * 2f),
            style = Stroke(width = 3f)
        )
    }

    // Inner crosshair target
    drawLine(
        color = accent,
        start = Offset(cx - 30f, cy),
        end = Offset(cx + 30f, cy),
        strokeWidth = 2f
    )
    drawLine(
        color = accent,
        start = Offset(cx, cy - 30f),
        end = Offset(cx, cy + 30f),
        strokeWidth = 2f
    )
    drawCircle(
        color = accent.copy(alpha = 0.6f),
        radius = 24f,
        center = Offset(cx, cy),
        style = Stroke(width = 1.5f)
    )

    // Corner HUD brackets
    val bracketSize = 25f
    val bw = targetR * 1.25f
    val bh = targetR * 1.25f
    // Top-Left
    drawLine(color = secondary, start = Offset(cx - bw, cy - bh), end = Offset(cx - bw + bracketSize, cy - bh), strokeWidth = 2f)
    drawLine(color = secondary, start = Offset(cx - bw, cy - bh), end = Offset(cx - bw, cy - bh + bracketSize), strokeWidth = 2f)
    // Top-Right
    drawLine(color = secondary, start = Offset(cx + bw, cy - bh), end = Offset(cx + bw - bracketSize, cy - bh), strokeWidth = 2f)
    drawLine(color = secondary, start = Offset(cx + bw, cy - bh), end = Offset(cx + bw, cy - bh + bracketSize), strokeWidth = 2f)
    // Bottom-Left
    drawLine(color = secondary, start = Offset(cx - bw, cy + bh), end = Offset(cx - bw + bracketSize, cy + bh), strokeWidth = 2f)
    drawLine(color = secondary, start = Offset(cx - bw, cy + bh), end = Offset(cx - bw, cy + bh - bracketSize), strokeWidth = 2f)
    // Bottom-Right
    drawLine(color = secondary, start = Offset(cx + bw, cy + bh), end = Offset(cx + bw - bracketSize, cy + bh), strokeWidth = 2f)
    drawLine(color = secondary, start = Offset(cx + bw, cy + bh), end = Offset(cx + bw, cy + bh - bracketSize), strokeWidth = 2f)
}

private fun DrawScope.drawAudioSpectrum(
    w: Float,
    h: Float,
    cx: Float,
    cy: Float,
    primary: Color,
    secondary: Color,
    accent: Color,
    progress: Float,
    elapsedMs: Long
) {
    val barCount = 14
    val barWidth = 14f
    val spacing = 6f
    val totalW = (barWidth + spacing) * barCount
    val startX = cx - totalW / 2f
    val baseY = cy + 40f

    for (i in 0 until barCount) {
        val freq = sin(elapsedMs / 80f + i * 0.7f).toFloat().coerceIn(0.1f, 1f)
        val barH = (60f + 70f * freq)
        val bx = startX + i * (barWidth + spacing)

        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(accent, primary),
                startY = baseY - barH,
                endY = baseY
            ),
            topLeft = Offset(bx, baseY - barH),
            size = Size(barWidth, barH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
    }

    // Central pulsing audio halo
    val pulse = (sin(elapsedMs / 120f) + 1f) / 2f
    drawCircle(
        color = secondary.copy(alpha = 0.35f + 0.2f * pulse),
        radius = 70f + 20f * pulse,
        center = Offset(cx, cy - 25f),
        style = Stroke(width = 2.5f)
    )
}

private fun DrawScope.drawNatureHorizon(
    w: Float,
    h: Float,
    cx: Float,
    cy: Float,
    primary: Color,
    secondary: Color,
    accent: Color,
    progress: Float,
    elapsedMs: Long
) {
    val horizonY = h * 0.62f

    // Soft rising sun
    val sunY = horizonY - 40f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFF4D6), Color(0xFFFFD166).copy(alpha = 0.7f), Color.Transparent),
            center = Offset(cx, sunY),
            radius = w * 0.28f
        ),
        radius = w * 0.28f,
        center = Offset(cx, sunY)
    )

    // Mountain silhouettes Layer 1 (back)
    val backPath = Path()
    backPath.moveTo(0f, horizonY + 20f)
    backPath.lineTo(w * 0.25f, horizonY - 45f)
    backPath.lineTo(w * 0.55f, horizonY - 10f)
    backPath.lineTo(w * 0.8f, horizonY - 60f)
    backPath.lineTo(w, horizonY + 15f)
    backPath.lineTo(w, h)
    backPath.lineTo(0f, h)
    backPath.close()
    drawPath(path = backPath, color = secondary.copy(alpha = 0.45f))

    // Mountain silhouettes Layer 2 (front)
    val frontPath = Path()
    frontPath.moveTo(0f, horizonY + 50f)
    frontPath.lineTo(w * 0.35f, horizonY + 10f)
    frontPath.lineTo(w * 0.7f, horizonY + 25f)
    frontPath.lineTo(w, horizonY + 35f)
    frontPath.lineTo(w, h)
    frontPath.lineTo(0f, h)
    frontPath.close()
    drawPath(path = frontPath, color = primary.copy(alpha = 0.8f))
}


