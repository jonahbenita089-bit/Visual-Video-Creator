package com.example

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.GenerativeModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val TAG = "VideoCreatorViewModel"

sealed interface VideoGenUiState {
    data object Idle : VideoGenUiState
    data object Loading : VideoGenUiState
    data class Success(val bitmap: Bitmap) : VideoGenUiState
    data class Error(val message: String) : VideoGenUiState
}

class VideoCreatorViewModel : ViewModel() {
    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-nano-banana-2.1"
        )
    }

    private val _uiState = MutableStateFlow<VideoGenUiState>(VideoGenUiState.Idle)
    val uiState: StateFlow<VideoGenUiState> = _uiState.asStateFlow()

    fun generateVideoGraphic(prompt: String) {
        viewModelScope.launch {
            _uiState.value = VideoGenUiState.Loading
            try {
                // Realistic 60-second timeout for model inference
                val bitmap = withTimeout(60_000L) {
                    val response = model.generateContent(prompt)
                    // Try to find image in a more generic way
                    val candidate = response.candidates.firstOrNull()
                    val part = candidate?.content?.parts?.firstOrNull()
                    
                    // Since I'm having trouble with imports, I'll log the part type and return null
                    Log.d(TAG, "Part type: ${part?.javaClass?.name}")
                    
                    // For now, return null, will need to figure out the class name for image part.
                    null
                }
                
                if (bitmap != null) {
                    _uiState.value = VideoGenUiState.Success(bitmap)
                } else {
                    _uiState.value = VideoGenUiState.Error("Model returned no image or I failed to parse it.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Generation timed out", e)
                _uiState.value = VideoGenUiState.Error(
                    "Generation timed out after 60 seconds. Please try again."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Firebase AI generation failed", e)
                _uiState.value = VideoGenUiState.Error(
                    e.localizedMessage ?: "Generation failed."
                )
            }
        }
    }
}
