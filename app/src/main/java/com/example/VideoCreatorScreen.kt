package com.example

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun VideoCreatorScreen(viewModel: VideoCreatorViewModel = viewModel(), modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsState()
    var prompt by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        TextField(
            value = prompt,
            onValueChange = { prompt = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Enter video graphic prompt") }
        )
        Button(
            onClick = { viewModel.generateVideoGraphic(prompt) },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Generate Graphic")
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (val state = uiState) {
                is VideoGenUiState.Loading -> CircularProgressIndicator()
                is VideoGenUiState.Success -> {
                    Image(
                        bitmap = state.bitmap.asImageBitmap(),
                        contentDescription = "Generated Video Graphic",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Fit
                    )
                }
                is VideoGenUiState.Error -> Text(
                    text = "Error: ${state.message}",
                    color = MaterialTheme.colorScheme.error
                )
                is VideoGenUiState.Idle -> Text("Enter a prompt to start")
            }
        }
    }
}
