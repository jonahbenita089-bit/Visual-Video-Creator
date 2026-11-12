package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TemplateRepository
import com.example.model.AnimationStyle
import com.example.model.ColorPalettePreset
import com.example.model.GraphicStyle
import com.example.model.VideoGenre
import com.example.model.VideoLimits
import com.example.model.VideoScene
import com.example.model.VideoTemplate
import com.example.model.formatVideoDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SceneEditorSheet(
    template: VideoTemplate,
    activeSceneIndex: Int,
    onSceneSelect: (Int) -> Unit,
    onUpdateScene: (Int, VideoScene) -> Unit,
    onAddScene: () -> Unit,
    onDuplicateScene: (Int) -> Unit,
    onDeleteScene: (Int) -> Unit,
    onBatchGenerateScenes: (count: Int, baseTitle: String, duration: Float) -> Unit,
    onUpdatePalette: (ColorPalettePreset) -> Unit,
    onApplyVideoGenre: (VideoGenre) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scenes = template.scenes
    if (scenes.isEmpty()) return

    val safeIndex = activeSceneIndex.coerceIn(0, scenes.size - 1)
    val currentScene = scenes[safeIndex]

    var title by remember(currentScene) { mutableStateOf(currentScene.title) }
    var subtitle by remember(currentScene) { mutableStateOf(currentScene.subtitle) }
    var badge by remember(currentScene) { mutableStateOf(currentScene.badge) }
    var caption by remember(currentScene) { mutableStateOf(currentScene.caption) }
    var duration by remember(currentScene) { mutableFloatStateOf(currentScene.durationSeconds) }
    var selectedAnimation by remember(currentScene) { mutableStateOf(currentScene.animation) }
    var selectedGraphicStyle by remember(currentScene) { mutableStateOf(currentScene.graphicStyle) }

    var showBatchDialog by remember { mutableStateOf(false) }
    var batchCount by remember { mutableIntStateOf(10) }
    var batchDuration by remember { mutableFloatStateOf(5.0f) }
    var batchBaseTitle by remember { mutableStateOf("Segment") }

    val lazyListState = rememberLazyListState()

    LaunchedEffect(safeIndex) {
        lazyListState.animateScrollToItem(safeIndex)
    }

    fun commitChanges() {
        val clampedDuration = duration.coerceIn(
            VideoLimits.MIN_SCENE_DURATION_SECONDS,
            VideoLimits.MAX_SCENE_DURATION_SECONDS
        )
        onUpdateScene(
            safeIndex,
            currentScene.copy(
                title = title,
                subtitle = subtitle,
                badge = badge,
                caption = caption,
                durationSeconds = clampedDuration,
                animation = selectedAnimation,
                graphicStyle = selectedGraphicStyle
            )
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("scene_editor_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Title, Capacity Badge & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Scene Studio Editor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${scenes.size} / ${VideoLimits.MAX_SCENES} Scenes (Total: ${formatVideoDuration(template.scenes.sumOf { it.durationSeconds.toDouble() }.toFloat())})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Batch generator button
                    IconButton(
                        onClick = { showBatchDialog = !showBatchDialog },
                        modifier = Modifier.size(36.dp).testTag("batch_scene_dialog_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LibraryAdd,
                            contentDescription = "Bulk Generate Scenes",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { onDuplicateScene(safeIndex) },
                        enabled = scenes.size < VideoLimits.MAX_SCENES,
                        modifier = Modifier.size(36.dp).testTag("duplicate_scene_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Duplicate Scene",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (scenes.size > 1) {
                        IconButton(
                            onClick = { onDeleteScene(safeIndex) },
                            modifier = Modifier.size(36.dp).testTag("delete_scene_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Scene",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onAddScene,
                        enabled = scenes.size < VideoLimits.MAX_SCENES,
                        modifier = Modifier.size(36.dp).testTag("add_scene_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add New Scene",
                            tint = if (scenes.size < VideoLimits.MAX_SCENES) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Expandable Batch Scene Generator Card (Allows creating up to 700 scenes!)
            if (showBatchDialog) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth().testTag("batch_generator_panel")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Batch Scene Generator (Up to 700 Scenes)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Add multiple sequenced scenes in one click. Remaining capacity: ${VideoLimits.MAX_SCENES - scenes.size} scenes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Count Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val available = VideoLimits.MAX_SCENES - scenes.size
                            listOf(5, 10, 50, 100).forEach { cnt ->
                                FilterChip(
                                    selected = batchCount == cnt,
                                    onClick = { batchCount = cnt.coerceAtMost(available) },
                                    label = { Text("+$cnt", fontSize = 11.sp) },
                                    enabled = cnt <= available
                                )
                            }
                            if (available > 0) {
                                FilterChip(
                                    selected = batchCount == available,
                                    onClick = { batchCount = available },
                                    label = { Text("Max ($available)", fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Batch duration selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Duration Per Scene", style = MaterialTheme.typography.labelMedium)
                            Text(text = "${batchDuration}s (Max 30s)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(3.0f, 5.0f, 10.0f, 15.0f, 30.0f).forEach { d ->
                                FilterChip(
                                    selected = batchDuration == d,
                                    onClick = { batchDuration = d },
                                    label = { Text("${d.toInt()}s", fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                onBatchGenerateScenes(batchCount, batchBaseTitle, batchDuration)
                                showBatchDialog = false
                            },
                            modifier = Modifier.fillMaxWidth().testTag("execute_batch_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create $batchCount Scenes (${batchDuration}s each)")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Fast Jump Navigation Bar (especially handy when scenes > 20)
            if (scenes.size > 10) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedButton(
                            onClick = { onSceneSelect((safeIndex - 50).coerceAtLeast(0)) },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("-50", fontSize = 10.sp)
                        }
                        OutlinedButton(
                            onClick = { onSceneSelect((safeIndex - 10).coerceAtLeast(0)) },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("-10", fontSize = 10.sp)
                        }
                    }

                    Text(
                        text = "Viewing Scene ${safeIndex + 1} of ${scenes.size}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedButton(
                            onClick = { onSceneSelect((safeIndex + 10).coerceAtMost(scenes.size - 1)) },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("+10", fontSize = 10.sp)
                        }
                        OutlinedButton(
                            onClick = { onSceneSelect((safeIndex + 50).coerceAtMost(scenes.size - 1)) },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("+50", fontSize = 10.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Virtualized LazyRow for Scene Pills (Handles up to 700 scenes with zero lag!)
            LazyRow(
                state = lazyListState,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(scenes, key = { _, sc -> sc.id }) { idx, sc ->
                    val isCurrent = idx == safeIndex
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .clickable {
                                commitChanges()
                                onSceneSelect(idx)
                            }
                            .testTag("scene_tab_$idx")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Scene ${idx + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${sc.durationSeconds}s",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isCurrent) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Text Inputs
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    commitChanges()
                },
                label = { Text("Scene Headline / Title") },
                modifier = Modifier.fillMaxWidth().testTag("input_scene_title"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = subtitle,
                onValueChange = {
                    subtitle = it
                    commitChanges()
                },
                label = { Text("Subtitle / Explainer Text") },
                modifier = Modifier.fillMaxWidth().testTag("input_scene_subtitle"),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = badge,
                    onValueChange = {
                        badge = it
                        commitChanges()
                    },
                    label = { Text("Top Badge Tag") },
                    modifier = Modifier.weight(1f).testTag("input_scene_badge"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = caption,
                    onValueChange = {
                        caption = it
                        commitChanges()
                    },
                    label = { Text("Bottom Caption") },
                    modifier = Modifier.weight(1f).testTag("input_scene_caption"),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // DURATION SLIDER (Configured for up to 30.0s maximum per user requirement!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Scene Duration (Max 30s)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = String.format("%.1f seconds", duration),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Slider(
                value = duration,
                onValueChange = {
                    duration = it
                    commitChanges()
                },
                valueRange = VideoLimits.MIN_SCENE_DURATION_SECONDS..VideoLimits.MAX_SCENE_DURATION_SECONDS,
                steps = 28, // 1s increments from 1 to 30
                modifier = Modifier.fillMaxWidth().testTag("duration_slider")
            )

            // Quick preset duration chips up to 30s
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(1.0f, 3.0f, 5.0f, 10.0f, 15.0f, 20.0f, 30.0f).forEach { presetSec ->
                    val isPreset = (duration == presetSec)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isPreset) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isPreset) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .clickable {
                                duration = presetSec
                                commitChanges()
                            }
                            .testTag("preset_dur_${presetSec.toInt()}s")
                    ) {
                        Text(
                            text = "${presetSec.toInt()}s",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isPreset) FontWeight.Bold else FontWeight.Normal,
                            color = if (isPreset) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Animation Style Picker
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Animation,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Suggested Animation",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AnimationStyle.values().forEach { anim ->
                    val isAnimSelected = anim == selectedAnimation
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isAnimSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isAnimSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .clickable {
                                selectedAnimation = anim
                                commitChanges()
                            }
                            .testTag("anim_${anim.name}")
                    ) {
                        Text(
                            text = anim.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isAnimSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isAnimSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Graphic Style Picker
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Placeholder Graphic Theme",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GraphicStyle.values().forEach { gStyle ->
                    val isGSelected = gStyle == selectedGraphicStyle
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isGSelected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isGSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .clickable {
                                selectedGraphicStyle = gStyle
                                commitChanges()
                            }
                            .testTag("graphic_${gStyle.name}")
                    ) {
                        Text(
                            text = gStyle.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isGSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isGSelected) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1-Tap Video Style Transformation (Any Genre)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Transform Video Style (Any Genre)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Apply cohesive aesthetic, graphics, and palette to all scenes in 1 tap:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VideoGenre.values().forEach { genre ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .clickable { onApplyVideoGenre(genre) }
                            .testTag("genre_preset_${genre.name}")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = genre.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = genre.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Palette Theme Switcher
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ColorLens,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Color Theme Palette",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TemplateRepository.palettes.forEach { pal ->
                    val isPalSelected = pal.id == template.palette.id
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            if (isPalSelected) 2.dp else 1.dp,
                            if (isPalSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .clickable { onUpdatePalette(pal) }
                            .testTag("palette_${pal.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(pal.primary)))
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(pal.secondary)))
                            Text(
                                text = pal.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isPalSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}
