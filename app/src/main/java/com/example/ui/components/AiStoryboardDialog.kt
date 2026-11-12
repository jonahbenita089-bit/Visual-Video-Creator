package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AiGenerationState
import com.example.model.TemplateCategory

@Composable
fun AiStoryboardDialog(
    aiState: AiGenerationState,
    onGenerate: (prompt: String, category: TemplateCategory) -> Unit,
    onDismiss: () -> Unit
) {
    var prompt by remember {
        mutableStateOf("Create a high energy social media product announcement for wireless headphones with deep bass and sleek design")
    }
    var selectedCategory by remember { mutableStateOf(TemplateCategory.SOCIAL_MEDIA) }

    val isGenerating = aiState is AiGenerationState.Generating

    val promptSuggestions = listOf(
        "Social media announcement for new wireless audio gear",
        "Executive presentation on artificial intelligence roadmap",
        "Heartfelt birthday greeting with vibrant celebration graphics",
        "Motivational daily reminder quote for entrepreneurs",
        "Wedding save-the-date announcement with romantic aura"
    )

    AlertDialog(
        onDismissRequest = { if (!isGenerating) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI Video Director", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Describe your video concept. Gemini drafts sequenced scenes (up to 700 scenes maximum, up to 30 seconds per scene) with custom graphics, timing, and animations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Category chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        TemplateCategory.SOCIAL_MEDIA,
                        TemplateCategory.PRESENTATIONS,
                        TemplateCategory.GREETINGS
                    ).forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.title, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    label = { Text("Video Concept Prompt") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("ai_prompt_input"),
                    maxLines = 4,
                    enabled = !isGenerating
                )

                Text(
                    text = "Quick Inspiration",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    promptSuggestions.take(3).forEach { suggestion ->
                        Surface(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp)
                        ) {
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                    .clickable {
                                        if (!isGenerating) prompt = suggestion
                                    }
                            )
                        }
                    }
                }

                if (aiState is AiGenerationState.Generating) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Text(
                            text = "Synthesizing storyboard scenes with Gemini...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (aiState is AiGenerationState.Error) {
                    Text(
                        text = "Error: ${aiState.message}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (aiState is AiGenerationState.Success) {
                    Text(
                        text = "Video storyboard created successfully! Loaded into player.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            if (aiState is AiGenerationState.Success) {
                Button(onClick = onDismiss, modifier = Modifier.testTag("ai_done_btn")) {
                    Text("View in Player")
                }
            } else {
                Button(
                    onClick = { onGenerate(prompt, selectedCategory) },
                    enabled = prompt.isNotBlank() && !isGenerating,
                    modifier = Modifier.testTag("submit_ai_generation_btn")
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Generate Video")
                }
            }
        },
        dismissButton = {
            if (!isGenerating) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
