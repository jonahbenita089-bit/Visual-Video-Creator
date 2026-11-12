package com.example

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.TemplateRepository
import com.example.model.AnimationStyle
import com.example.model.ColorPalettePreset
import com.example.model.GraphicStyle
import com.example.model.TemplateCategory
import com.example.model.VideoAspectRatio
import com.example.model.VideoScene
import com.example.model.VideoTemplate
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.util.UUID

private const val TAG = "VideoCreatorViewModel"

sealed interface AiGenerationState {
    data object Idle : AiGenerationState
    data object Generating : AiGenerationState
    data class Success(val generatedTemplate: VideoTemplate) : AiGenerationState
    data class Error(val message: String) : AiGenerationState
}

class VideoCreatorViewModel : ViewModel() {

    // Models lazily initialized
    private val textModel by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    // Template catalog
    private val _templates = MutableStateFlow<List<VideoTemplate>>(TemplateRepository.templates)
    val templates: StateFlow<List<VideoTemplate>> = _templates.asStateFlow()

    // Current category filter
    private val _selectedCategory = MutableStateFlow(TemplateCategory.ALL)
    val selectedCategory: StateFlow<TemplateCategory> = _selectedCategory.asStateFlow()

    // Active selected template
    private val _activeTemplate = MutableStateFlow(TemplateRepository.templates.first())
    val activeTemplate: StateFlow<VideoTemplate> = _activeTemplate.asStateFlow()

    // Active scene index
    private val _activeSceneIndex = MutableStateFlow(0)
    val activeSceneIndex: StateFlow<Int> = _activeSceneIndex.asStateFlow()

    // Playback state
    private val _isPlaying = MutableStateFlow(true)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _aspectRatio = MutableStateFlow(VideoAspectRatio.PORTRAIT_9_16)
    val aspectRatio: StateFlow<VideoAspectRatio> = _aspectRatio.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _isLooping = MutableStateFlow(true)
    val isLooping: StateFlow<Boolean> = _isLooping.asStateFlow()

    // AI Generation state
    private val _aiGenState = MutableStateFlow<AiGenerationState>(AiGenerationState.Idle)
    val aiGenState: StateFlow<AiGenerationState> = _aiGenState.asStateFlow()

    fun selectCategory(category: TemplateCategory) {
        _selectedCategory.value = category
    }

    fun selectTemplate(template: VideoTemplate) {
        _activeTemplate.value = template
        _activeSceneIndex.value = 0
        _aspectRatio.value = template.defaultAspectRatio
        _isPlaying.value = false
    }

    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }

    fun setPlaying(playing: Boolean) {
        _isPlaying.value = playing
    }

    fun selectScene(index: Int) {
        val scenes = _activeTemplate.value.scenes
        if (scenes.isNotEmpty()) {
            _activeSceneIndex.value = index.coerceIn(0, scenes.size - 1)
        }
    }

    fun nextScene() {
        val scenes = _activeTemplate.value.scenes
        if (scenes.isNotEmpty()) {
            val next = _activeSceneIndex.value + 1
            if (next < scenes.size) {
                _activeSceneIndex.value = next
            } else if (_isLooping.value) {
                _activeSceneIndex.value = 0
            } else {
                _isPlaying.value = false
            }
        }
    }

    fun prevScene() {
        val scenes = _activeTemplate.value.scenes
        if (scenes.isNotEmpty()) {
            val prev = _activeSceneIndex.value - 1
            _activeSceneIndex.value = if (prev >= 0) prev else 0
        }
    }

    fun restartVideo() {
        _activeSceneIndex.value = 0
        _isPlaying.value = true
    }

    fun toggleSpeed() {
        _playbackSpeed.value = when (_playbackSpeed.value) {
            0.5f -> 1.0f
            1.0f -> 1.5f
            1.5f -> 2.0f
            else -> 0.5f
        }
    }

    fun toggleAspectRatio() {
        _aspectRatio.value = when (_aspectRatio.value) {
            VideoAspectRatio.PORTRAIT_9_16 -> VideoAspectRatio.LANDSCAPE_16_9
            VideoAspectRatio.LANDSCAPE_16_9 -> VideoAspectRatio.SQUARE_1_1
            VideoAspectRatio.SQUARE_1_1 -> VideoAspectRatio.PORTRAIT_9_16
        }
    }

    fun setAspectRatio(ratio: VideoAspectRatio) {
        _aspectRatio.value = ratio
    }

    // SCENE EDITING
    fun updateScene(sceneIndex: Int, updatedScene: VideoScene) {
        val current = _activeTemplate.value
        val updatedScenes = current.scenes.toMutableList()
        if (sceneIndex in updatedScenes.indices) {
            val clampedDuration = updatedScene.durationSeconds.coerceIn(
                com.example.model.VideoLimits.MIN_SCENE_DURATION_SECONDS,
                com.example.model.VideoLimits.MAX_SCENE_DURATION_SECONDS
            )
            updatedScenes[sceneIndex] = updatedScene.copy(durationSeconds = clampedDuration)
            val modifiedTemplate = current.copy(scenes = updatedScenes)
            _activeTemplate.value = modifiedTemplate
            updateTemplateInCatalog(modifiedTemplate)
        }
    }

    fun addScene() {
        val current = _activeTemplate.value
        if (current.scenes.size >= com.example.model.VideoLimits.MAX_SCENES) {
            return
        }
        val nextSceneNumber = current.scenes.size + 1
        val newScene = VideoScene(
            id = "scene_" + UUID.randomUUID().toString().take(6),
            title = "Scene $nextSceneNumber Highlight",
            subtitle = "Custom graphics and animation description.",
            badge = "Part $nextSceneNumber",
            caption = "Tap to learn more",
            durationSeconds = 5.0f.coerceIn(
                com.example.model.VideoLimits.MIN_SCENE_DURATION_SECONDS,
                com.example.model.VideoLimits.MAX_SCENE_DURATION_SECONDS
            ),
            animation = AnimationStyle.ZOOM_KEN_BURNS,
            graphicStyle = GraphicStyle.AURORA_GRADIENT,
            placeholderGraphicDesc = "Dynamic aura gradient centering on key message"
        )
        val updatedScenes = current.scenes + newScene
        val modifiedTemplate = current.copy(scenes = updatedScenes)
        _activeTemplate.value = modifiedTemplate
        _activeSceneIndex.value = updatedScenes.size - 1
        updateTemplateInCatalog(modifiedTemplate)
    }

    fun batchGenerateScenes(count: Int, baseTitle: String, durationSeconds: Float) {
        val current = _activeTemplate.value
        val availableSlots = com.example.model.VideoLimits.MAX_SCENES - current.scenes.size
        if (availableSlots <= 0) return

        val scenesToAddCount = count.coerceIn(1, availableSlots)
        val clampedDuration = durationSeconds.coerceIn(
            com.example.model.VideoLimits.MIN_SCENE_DURATION_SECONDS,
            com.example.model.VideoLimits.MAX_SCENE_DURATION_SECONDS
        )

        val graphicStyles = GraphicStyle.values()
        val animationStyles = AnimationStyle.values()

        val newScenes = (1..scenesToAddCount).map { i ->
            val sceneNumber = current.scenes.size + i
            val graphicStyle = graphicStyles[(sceneNumber) % graphicStyles.size]
            val animationStyle = animationStyles[(sceneNumber) % animationStyles.size]

            VideoScene(
                id = "scene_batch_${UUID.randomUUID().toString().take(6)}",
                title = if (baseTitle.isNotBlank()) "$baseTitle Part $sceneNumber" else "Scene $sceneNumber",
                subtitle = "Sequenced scene $sceneNumber of ${current.scenes.size + scenesToAddCount}.",
                badge = "Part $sceneNumber",
                caption = "Scene duration: ${clampedDuration}s",
                durationSeconds = clampedDuration,
                animation = animationStyle,
                graphicStyle = graphicStyle,
                placeholderGraphicDesc = "${graphicStyle.displayName} animation at ${clampedDuration}s duration"
            )
        }

        val updatedScenes = current.scenes + newScenes
        val modifiedTemplate = current.copy(scenes = updatedScenes)
        _activeTemplate.value = modifiedTemplate
        updateTemplateInCatalog(modifiedTemplate)
    }

    fun duplicateScene(sceneIndex: Int) {
        val current = _activeTemplate.value
        if (current.scenes.size >= com.example.model.VideoLimits.MAX_SCENES) {
            return
        }
        if (sceneIndex in current.scenes.indices) {
            val sceneToDup = current.scenes[sceneIndex]
            val duplicated = sceneToDup.copy(
                id = "scene_" + UUID.randomUUID().toString().take(6),
                title = "${sceneToDup.title} (Copy)"
            )
            val updatedScenes = current.scenes.toMutableList().apply {
                add(sceneIndex + 1, duplicated)
            }
            val modifiedTemplate = current.copy(scenes = updatedScenes)
            _activeTemplate.value = modifiedTemplate
            _activeSceneIndex.value = sceneIndex + 1
            updateTemplateInCatalog(modifiedTemplate)
        }
    }

    fun deleteScene(sceneIndex: Int) {
        val current = _activeTemplate.value
        if (current.scenes.size > 1 && sceneIndex in current.scenes.indices) {
            val updatedScenes = current.scenes.toMutableList().apply {
                removeAt(sceneIndex)
            }
            val modifiedTemplate = current.copy(scenes = updatedScenes)
            _activeTemplate.value = modifiedTemplate
            _activeSceneIndex.value = (sceneIndex - 1).coerceAtLeast(0)
            updateTemplateInCatalog(modifiedTemplate)
        }
    }

    fun clearAllScenesExceptFirst() {
        val current = _activeTemplate.value
        if (current.scenes.isNotEmpty()) {
            val singleScene = listOf(current.scenes.first())
            val modifiedTemplate = current.copy(scenes = singleScene)
            _activeTemplate.value = modifiedTemplate
            _activeSceneIndex.value = 0
            updateTemplateInCatalog(modifiedTemplate)
        }
    }

    fun updatePalette(palette: ColorPalettePreset) {
        val current = _activeTemplate.value
        val modifiedTemplate = current.copy(palette = palette)
        _activeTemplate.value = modifiedTemplate
        updateTemplateInCatalog(modifiedTemplate)
    }

    fun applyVideoGenre(genre: com.example.model.VideoGenre) {
        val current = _activeTemplate.value
        val newPalette = TemplateRepository.palettes.find { it.id == genre.paletteId } ?: current.palette
        val updatedScenes = current.scenes.map { sc ->
            sc.copy(
                graphicStyle = genre.defaultGraphicStyle,
                placeholderGraphicDesc = "${genre.displayName}: ${genre.defaultGraphicStyle.description}"
            )
        }
        val modifiedTemplate = current.copy(
            palette = newPalette,
            scenes = updatedScenes
        )
        _activeTemplate.value = modifiedTemplate
        updateTemplateInCatalog(modifiedTemplate)
    }

    private fun updateTemplateInCatalog(template: VideoTemplate) {
        val list = _templates.value.toMutableList()
        val idx = list.indexOfFirst { it.id == template.id }
        if (idx != -1) {
            list[idx] = template
        } else {
            list.add(0, template)
        }
        _templates.value = list
    }

    fun resetAiState() {
        _aiGenState.value = AiGenerationState.Idle
    }

    // AI STORYBOARD & VIDEO CREATION USING GEMINI 3.8 FLASH
    fun generateAiVideoTemplate(userPrompt: String, targetCategory: TemplateCategory) {
        viewModelScope.launch {
            _aiGenState.value = AiGenerationState.Generating
            try {
                val promptInstruction = """
You are a senior motion graphics director and video producer.
Create a complete video storyboard template based on this user request: "$userPrompt".
Target category: ${targetCategory.title}.

Return ONLY valid raw JSON with NO markdown code fences, NO formatting text, NO preamble, and NO emojis.
Schema:
{
  "title": "Short punchy video title",
  "description": "One sentence summary of this video format",
  "aspectRatio": "PORTRAIT_9_16" or "LANDSCAPE_16_9" or "SQUARE_1_1",
  "paletteId": "cyber_neon" or "sunset_gold" or "corporate_slate" or "emerald_nature" or "celebration_warm" or "monochrome_minimal",
  "musicMood": "Descriptive background music style",
  "scenes": [
    {
      "title": "Scene headline under 35 chars",
      "subtitle": "Scene subtitle explanation",
      "badge": "Short badge tag",
      "caption": "Bottom call to action",
      "durationSeconds": 3.5,
      "animation": "ZOOM_KEN_BURNS" or "SLIDE_UP" or "PAN_SIDEWAYS" or "PULSE_SCALE" or "KINETIC_BOUNCE" or "GLITCH_SHIFT",
      "graphicStyle": "CONCENTRIC_RINGS" or "DYNAMIC_WAVE" or "TECH_GRID" or "AURORA_GRADIENT" or "CELEBRATION_BURST" or "METRICS_CHART" or "ABSTRACT_SPHERE" or "NEON_HEXAGONS",
      "placeholderGraphicDesc": "Concise visual description of the procedural graphics"
    }
  ]
}
Generate between 3 to 4 distinct sequential scenes. Ensure NO emojis are used in the text.
"""
                val responseText = withTimeout(60_000L) {
                    val response = textModel.generateContent(promptInstruction)
                    response.text ?: throw IllegalStateException("Model returned empty text.")
                }

                // Clean string in case of accidental markdown fence wrapping
                val jsonString = responseText.trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                val json = JSONObject(jsonString)
                val title = json.optString("title", "AI Custom Video Project")
                val desc = json.optString("description", "AI crafted video template")
                val ratioStr = json.optString("aspectRatio", "PORTRAIT_9_16")
                val paletteId = json.optString("paletteId", "cyber_neon")
                val musicMood = json.optString("musicMood", "Dynamic Modern Electronic")

                val chosenRatio = when (ratioStr) {
                    "LANDSCAPE_16_9" -> VideoAspectRatio.LANDSCAPE_16_9
                    "SQUARE_1_1" -> VideoAspectRatio.SQUARE_1_1
                    else -> VideoAspectRatio.PORTRAIT_9_16
                }

                val chosenPalette = TemplateRepository.palettes.find { it.id == paletteId }
                    ?: TemplateRepository.palettes[0]

                val scenesArray = json.getJSONArray("scenes")
                val parsedScenes = mutableListOf<VideoScene>()

                for (i in 0 until scenesArray.length()) {
                    val scObj = scenesArray.getJSONObject(i)
                    val sTitle = scObj.optString("title", "Scene ${i + 1}")
                    val sSub = scObj.optString("subtitle", "")
                    val sBadge = scObj.optString("badge", "Highlight")
                    val sCaption = scObj.optString("caption", "")
                    val sDur = scObj.optDouble("durationSeconds", 3.5).toFloat()
                    val sAnimStr = scObj.optString("animation", "ZOOM_KEN_BURNS")
                    val sGraphicStr = scObj.optString("graphicStyle", "CONCENTRIC_RINGS")
                    val sGraphicDesc = scObj.optString("placeholderGraphicDesc", "Animated motion graphics")

                    val anim = try {
                        AnimationStyle.valueOf(sAnimStr)
                    } catch (e: Exception) {
                        AnimationStyle.ZOOM_KEN_BURNS
                    }

                    val gStyle = try {
                        GraphicStyle.valueOf(sGraphicStr)
                    } catch (e: Exception) {
                        GraphicStyle.AURORA_GRADIENT
                    }

                    parsedScenes.add(
                        VideoScene(
                            id = "ai_sc_" + UUID.randomUUID().toString().take(6),
                            title = sTitle,
                            subtitle = sSub,
                            badge = sBadge,
                            caption = sCaption,
                            durationSeconds = sDur.coerceIn(2.0f, 6.0f),
                            animation = anim,
                            graphicStyle = gStyle,
                            placeholderGraphicDesc = sGraphicDesc
                        )
                    )
                }

                val newTemplate = VideoTemplate(
                    id = "ai_template_" + UUID.randomUUID().toString().take(6),
                    title = title,
                    description = desc,
                    category = if (targetCategory == TemplateCategory.ALL) TemplateCategory.MARKETING else targetCategory,
                    defaultAspectRatio = chosenRatio,
                    palette = chosenPalette,
                    suggestedMusicMood = musicMood,
                    scenes = if (parsedScenes.isNotEmpty()) parsedScenes else listOf(
                        VideoScene(
                            id = "ai_sc_fallback",
                            title = title,
                            subtitle = desc,
                            badge = "Generated",
                            caption = "Watch to the end",
                            durationSeconds = 3.5f,
                            animation = AnimationStyle.ZOOM_KEN_BURNS,
                            graphicStyle = GraphicStyle.AURORA_GRADIENT,
                            placeholderGraphicDesc = "Dynamic procedural background"
                        )
                    )
                )

                // Add to template list and make active
                _templates.value = listOf(newTemplate) + _templates.value
                selectTemplate(newTemplate)
                _aiGenState.value = AiGenerationState.Success(newTemplate)

            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI Generation timed out", e)
                _aiGenState.value = AiGenerationState.Error("Generation timed out. Please try a shorter prompt.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI Generation failed", e)
                _aiGenState.value = AiGenerationState.Error(
                    e.localizedMessage ?: "Failed to generate video storyboard."
                )
            }
        }
    }
}
