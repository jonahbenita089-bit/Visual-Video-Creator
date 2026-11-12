package com.example

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.TemplateCategory
import com.example.ui.components.AiStoryboardDialog
import com.example.ui.components.RealExportModal
import com.example.ui.components.SceneEditorSheet
import com.example.ui.components.TemplateCard
import com.example.ui.components.VideoPlayerView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoCreatorScreen(
    viewModel: VideoCreatorViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val templates by viewModel.templates.collectAsState()
    val activeTemplate by viewModel.activeTemplate.collectAsState()
    val activeSceneIndex by viewModel.activeSceneIndex.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val aspectRatio by viewModel.aspectRatio.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val isLooping by viewModel.isLooping.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val aiGenState by viewModel.aiGenState.collectAsState()

    var currentTab by remember { mutableIntStateOf(0) } // 0 = Templates, 1 = Scene Studio
    var showAiDialog by remember { mutableStateOf(false) }
    var showExportModal by remember { mutableStateOf(false) }

    val filteredTemplates = remember(templates, selectedCategory) {
        if (selectedCategory == TemplateCategory.ALL) templates
        else templates.filter { it.category == selectedCategory }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Video Studio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    // AI Storyboard button
                    OutlinedButton(
                        onClick = {
                            viewModel.resetAiState()
                            showAiDialog = true
                        },
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("open_ai_director_btn"),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Storyboard",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AI Director", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Real MP4 Export button
                    Button(
                        onClick = { showExportModal = true },
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("export_real_video_btn"),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Export MP4",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export MP4", fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Live Interactive Video Player (always pinned at top)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                VideoPlayerView(
                    template = activeTemplate,
                    currentSceneIndex = activeSceneIndex,
                    isPlaying = isPlaying,
                    aspectRatio = aspectRatio,
                    playbackSpeed = playbackSpeed,
                    isLooping = isLooping,
                    onPlayPauseToggle = { viewModel.togglePlayPause() },
                    onSceneSelect = { viewModel.selectScene(it) },
                    onNextScene = { viewModel.nextScene() },
                    onPrevScene = { viewModel.prevScene() },
                    onRestart = { viewModel.restartVideo() },
                    onSpeedToggle = { viewModel.toggleSpeed() },
                    onAspectRatioToggle = { viewModel.toggleAspectRatio() }
                )
            }

            // Tab Selector: Template Catalog vs Scene Studio Customizer
            PrimaryTabRow(
                selectedTabIndex = currentTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.VideoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Template Library (${filteredTemplates.size})")
                        }
                    },
                    modifier = Modifier.testTag("tab_templates")
                )
                Tab(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scene Studio")
                        }
                    },
                    modifier = Modifier.testTag("tab_scene_studio")
                )
            }

            when (currentTab) {
                0 -> {
                    // TAB 0: TEMPLATES CATALOG
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Category Filter Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TemplateCategory.values().forEach { cat ->
                                val isSelected = cat == selectedCategory
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.selectCategory(cat) },
                                    label = { Text(cat.title, fontSize = 12.sp) },
                                    modifier = Modifier.testTag("category_chip_${cat.name}")
                                )
                            }
                        }

                        // List of templates
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(filteredTemplates, key = { it.id }) { tpl ->
                                TemplateCard(
                                    template = tpl,
                                    isSelected = tpl.id == activeTemplate.id,
                                    onSelect = {
                                        viewModel.selectTemplate(tpl)
                                    },
                                    onPreview = {
                                        viewModel.selectTemplate(tpl)
                                        viewModel.restartVideo()
                                    }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // TAB 1: SCENE STUDIO CUSTOMIZER
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        item {
                            SceneEditorSheet(
                                template = activeTemplate,
                                activeSceneIndex = activeSceneIndex,
                                onSceneSelect = { viewModel.selectScene(it) },
                                onUpdateScene = { idx, scene -> viewModel.updateScene(idx, scene) },
                                onAddScene = { viewModel.addScene() },
                                onDuplicateScene = { idx -> viewModel.duplicateScene(idx) },
                                onDeleteScene = { idx -> viewModel.deleteScene(idx) },
                                onBatchGenerateScenes = { count, baseTitle, dur -> viewModel.batchGenerateScenes(count, baseTitle, dur) },
                                onUpdatePalette = { pal -> viewModel.updatePalette(pal) },
                                onApplyVideoGenre = { genre -> viewModel.applyVideoGenre(genre) }
                            )
                        }
                    }
                }
            }
        }
    }

    // AI Storyboard Generator Modal
    if (showAiDialog) {
        AiStoryboardDialog(
            aiState = aiGenState,
            onGenerate = { prompt, category ->
                viewModel.generateAiVideoTemplate(prompt, category)
            },
            onDismiss = {
                showAiDialog = false
                viewModel.resetAiState()
            }
        )
    }

    // Real MP4 Video Export Modal
    if (showExportModal) {
        RealExportModal(
            template = activeTemplate,
            aspectRatio = aspectRatio,
            onDismiss = { showExportModal = false }
        )
    }
}
