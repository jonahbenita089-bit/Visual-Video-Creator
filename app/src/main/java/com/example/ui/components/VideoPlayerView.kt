package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MotionPhotosOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AnimationStyle
import com.example.model.VideoAspectRatio
import com.example.model.VideoScene
import com.example.model.VideoTemplate
import com.example.ui.graphics.SceneGraphicCanvas
import kotlinx.coroutines.delay
import kotlin.math.sin

@Composable
fun VideoPlayerView(
    template: VideoTemplate,
    currentSceneIndex: Int,
    isPlaying: Boolean,
    aspectRatio: VideoAspectRatio,
    playbackSpeed: Float,
    isLooping: Boolean,
    onPlayPauseToggle: () -> Unit,
    onSceneSelect: (Int) -> Unit,
    onNextScene: () -> Unit,
    onPrevScene: () -> Unit,
    onRestart: () -> Unit,
    onSpeedToggle: () -> Unit,
    onAspectRatioToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scenes = template.scenes
    if (scenes.isEmpty()) return

    val safeIndex = currentSceneIndex.coerceIn(0, scenes.size - 1)
    val currentScene = scenes[safeIndex]

    // Time tracking within scene
    var sceneElapsedSec by remember(safeIndex, isPlaying) { mutableFloatStateOf(0f) }
    var globalClockMs by remember { mutableLongStateOf(0L) }

    // Ticking loop when playing
    LaunchedEffect(isPlaying, safeIndex, playbackSpeed, isLooping) {
        if (!isPlaying) return@LaunchedEffect
        val tickIntervalMs = 33L // ~30 fps update
        while (isPlaying) {
            delay(tickIntervalMs)
            globalClockMs += (tickIntervalMs * playbackSpeed).toLong()
            val delta = (tickIntervalMs / 1000f) * playbackSpeed
            sceneElapsedSec += delta

            if (sceneElapsedSec >= currentScene.durationSeconds) {
                if (safeIndex < scenes.size - 1) {
                    onNextScene()
                    sceneElapsedSec = 0f
                } else if (isLooping) {
                    onRestart()
                    sceneElapsedSec = 0f
                } else {
                    onPlayPauseToggle() // stop playback at end
                    break
                }
            }
        }
    }

    val sceneProgress = (sceneElapsedSec / currentScene.durationSeconds).coerceIn(0f, 1f)
    val bgStartColor = Color(template.palette.bgStart)
    val bgEndColor = Color(template.palette.bgEnd)
    val primaryColor = Color(template.palette.primary)
    val secondaryColor = Color(template.palette.secondary)
    val textPrimaryColor = Color(template.palette.textPrimary)
    val accentColor = Color(template.palette.accent)

    // Calculate motion parameters from animation style
    val (contentScale, contentAlpha, contentOffsetY, contentOffsetX) = calculateSceneMotion(
        animationStyle = currentScene.animation,
        progress = sceneProgress
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("video_player_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top player header with template title and ratio badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Scene ${safeIndex + 1} of ${scenes.size} • ${currentScene.animation.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.clickable { onAspectRatioToggle() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitScreen,
                            contentDescription = "Aspect Ratio",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = aspectRatio.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Aspect Ratio Screen Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                // Inner video frame bounded by aspect ratio
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(aspectRatio.ratio)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(bgStartColor, bgEndColor)
                            )
                        )
                        .border(1.dp, primaryColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                ) {
                    // 1. Procedural Scene Graphic Canvas
                    SceneGraphicCanvas(
                        graphicStyle = currentScene.graphicStyle,
                        palette = template.palette,
                        sceneProgress = sceneProgress,
                        elapsedMs = globalClockMs
                    )

                    // 2. Animated Scene Typography & Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 20.dp)
                            .graphicsLayer {
                                scaleX = contentScale
                                scaleY = contentScale
                                alpha = contentAlpha
                                translationY = contentOffsetY
                                translationX = contentOffsetX
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (currentScene.badge.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = accentColor.copy(alpha = 0.9f),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    Text(
                                        text = currentScene.badge.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            Text(
                                text = currentScene.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = textPrimaryColor,
                                textAlign = TextAlign.Center,
                                lineHeight = 26.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp)
                            )

                            if (currentScene.subtitle.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = currentScene.subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textPrimaryColor.copy(alpha = 0.88f),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp)
                                )
                            }
                        }

                        // Bottom caption callout
                        if (currentScene.caption.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.Black.copy(alpha = 0.65f),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, primaryColor.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 6.dp)
                            ) {
                                Text(
                                    text = currentScene.caption,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = primaryColor,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // Top Scene Meta Chip (Graphic Style & Animation)
                    Surface(
                        shape = RoundedCornerShape(topStart = 0.dp, bottomEnd = 12.dp),
                        color = Color.Black.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = secondaryColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentScene.graphicStyle.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = textPrimaryColor.copy(alpha = 0.9f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Multi-segment Scene Progress Bar for small scene counts, or continuous timeline for up to 700 scenes
            val totalVideoSeconds = remember(scenes) { scenes.sumOf { it.durationSeconds.toDouble() }.toFloat() }
            val elapsedBeforeCurrent = remember(scenes, safeIndex) {
                scenes.take(safeIndex).sumOf { it.durationSeconds.toDouble() }.toFloat()
            }
            val overallElapsedSeconds = elapsedBeforeCurrent + sceneProgress * currentScene.durationSeconds
            val overallProgress = if (totalVideoSeconds > 0f) (overallElapsedSeconds / totalVideoSeconds).coerceIn(0f, 1f) else 0f

            if (scenes.size <= 30) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    scenes.forEachIndexed { idx, _ ->
                        val segmentProgress = when {
                            idx < safeIndex -> 1f
                            idx == safeIndex -> sceneProgress
                            else -> 0f
                        }
                        LinearProgressIndicator(
                            progress = { segmentProgress },
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (idx == safeIndex) primaryColor else secondaryColor,
                            trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { overallProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = primaryColor,
                        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = com.example.model.formatVideoDuration(overallElapsedSeconds),
                            style = MaterialTheme.typography.labelSmall,
                            color = primaryColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Scene ${safeIndex + 1}/${scenes.size} (${currentScene.durationSeconds}s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = com.example.model.formatVideoDuration(totalVideoSeconds),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Playback Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speed control button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.clickable { onSpeedToggle() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Playback Speed",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${playbackSpeed}x",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Center navigation and play buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onPrevScene,
                        modifier = Modifier.testTag("prev_scene_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "Previous Scene"
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(52.dp)
                    ) {
                        IconButton(
                            onClick = onPlayPauseToggle,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause Video" else "Play Video",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onNextScene,
                        modifier = Modifier.testTag("next_scene_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Next Scene"
                        )
                    }
                }

                // Restart button
                IconButton(
                    onClick = onRestart,
                    modifier = Modifier.testTag("restart_video_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "Restart Video",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Placeholder Graphic details card for the current scene
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MotionPhotosOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Visual Cue: ${currentScene.placeholderGraphicDesc}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private data class SceneMotionValues(
    val scale: Float,
    val alpha: Float,
    val offsetY: Float,
    val offsetX: Float
)

private fun calculateSceneMotion(
    animationStyle: AnimationStyle,
    progress: Float
): SceneMotionValues {
    // Settle in during first 20% of scene duration, stay during 20%-85%, soft transition in last 15%
    val introProgress = (progress / 0.25f).coerceIn(0f, 1f)
    val outroProgress = ((progress - 0.85f) / 0.15f).coerceIn(0f, 1f)
    val baseAlpha = (introProgress * (1f - outroProgress * 0.4f)).coerceIn(0f, 1f)

    return when (animationStyle) {
        AnimationStyle.ZOOM_KEN_BURNS -> {
            val scale = 0.94f + 0.14f * progress
            SceneMotionValues(scale = scale, alpha = baseAlpha, offsetY = 0f, offsetX = 0f)
        }
        AnimationStyle.SLIDE_UP -> {
            val offsetY = (1f - introProgress) * 45f
            SceneMotionValues(scale = 1f, alpha = baseAlpha, offsetY = offsetY, offsetX = 0f)
        }
        AnimationStyle.PAN_SIDEWAYS -> {
            val offsetX = -20f + 40f * progress
            SceneMotionValues(scale = 1.02f, alpha = baseAlpha, offsetY = 0f, offsetX = offsetX)
        }
        AnimationStyle.PULSE_SCALE -> {
            val pulse = 1f + 0.05f * sin(progress * 3.14159f * 4f)
            SceneMotionValues(scale = pulse, alpha = baseAlpha, offsetY = 0f, offsetX = 0f)
        }
        AnimationStyle.KINETIC_BOUNCE -> {
            val bounce = if (introProgress < 0.7f) {
                0.7f + 0.45f * (introProgress / 0.7f)
            } else {
                1.15f - 0.15f * ((introProgress - 0.7f) / 0.3f)
            }
            SceneMotionValues(scale = bounce, alpha = baseAlpha, offsetY = 0f, offsetX = 0f)
        }
        AnimationStyle.GLITCH_SHIFT -> {
            val jitterX = if (progress < 0.2f && (progress * 100).toInt() % 2 == 0) 6f else 0f
            SceneMotionValues(scale = 1f, alpha = baseAlpha, offsetY = 0f, offsetX = jitterX)
        }
    }
}
