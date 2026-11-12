package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.model.VideoAspectRatio
import com.example.model.VideoTemplate
import com.example.video.RealVideoEncoder
import com.example.video.VideoExportResult
import kotlinx.coroutines.launch

@Composable
fun RealExportModal(
    template: VideoTemplate,
    aspectRatio: VideoAspectRatio,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isEncoding by remember { mutableStateOf(false) }
    var encodeProgress by remember { mutableFloatStateOf(0f) }
    var exportResult by remember { mutableStateOf<VideoExportResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var sceneRangeLimit by remember(template) {
        mutableStateOf(if (template.scenes.size > 25) 25 else template.scenes.size)
    }

    val scenesToExport = remember(template, sceneRangeLimit) {
        if (sceneRangeLimit >= template.scenes.size) template.scenes
        else template.scenes.take(sceneRangeLimit)
    }

    val templateToExport = remember(template, scenesToExport) {
        template.copy(scenes = scenesToExport)
    }

    val totalDurationSeconds = remember(scenesToExport) {
        scenesToExport.sumOf { it.durationSeconds.toDouble() }.toFloat()
    }

    fun startRealVideoEncoding() {
        isEncoding = true
        encodeProgress = 0f
        errorMessage = null

        scope.launch {
            try {
                val result = RealVideoEncoder.renderMp4Video(
                    context = context,
                    template = templateToExport,
                    aspectRatio = aspectRatio,
                    onProgress = { p -> encodeProgress = p }
                )
                exportResult = result
                isEncoding = false
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Failed to encode video."
                isEncoding = false
            }
        }
    }

    fun openVideoFile(result: VideoExportResult) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                result.file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "video/mp4")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Play MP4 Video"))
        } catch (e: Exception) {
            Toast.makeText(context, "No external video player found", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareVideoFile(result: VideoExportResult) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                result.file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "video/mp4"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share MP4 Video"))
        } catch (e: Exception) {
            Toast.makeText(context, "Could not share video file", Toast.LENGTH_SHORT).show()
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isEncoding) onDismiss() },
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
                    text = if (exportResult != null) "MP4 Video Created" else "Export Real MP4 Video",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Project: ${template.title}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Format: MP4 (H.264 Video Encoder) • Aspect: ${aspectRatio.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Exporting: ${scenesToExport.size} of ${template.scenes.size} Scenes (Total: ${com.example.model.formatVideoDuration(totalDurationSeconds)})",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (!isEncoding && exportResult == null && template.scenes.size > 20) {
                    Text(
                        text = "Scene Export Range",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(10, 25, 50, template.scenes.size).distinct().forEach { count ->
                            androidx.compose.material3.FilterChip(
                                selected = sceneRangeLimit == count,
                                onClick = { sceneRangeLimit = count },
                                label = {
                                    Text(
                                        text = if (count == template.scenes.size) "All (${template.scenes.size})" else "First $count",
                                        fontSize = 11.sp
                                    )
                                }
                            )
                        }
                    }
                }

                if (isEncoding) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Encoding frames at 30 fps with MediaCodec...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    LinearProgressIndicator(
                        progress = { encodeProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${(encodeProgress * 100).toInt()}% rendered",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.End),
                        fontWeight = FontWeight.Bold
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = "Error: $errorMessage",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                exportResult?.let { res ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Real MP4 File Created on Device",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Resolution: ${res.width} x ${res.height} @ 30fps",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Duration: ${String.format("%.1f", res.durationSeconds)} seconds",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "File Size: ${res.fileSizeFormatted}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "File Name: ${res.file.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { openVideoFile(res) },
                                    modifier = Modifier.weight(1f).testTag("play_mp4_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Play MP4", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { shareVideoFile(res) },
                                    modifier = Modifier.weight(1f).testTag("share_mp4_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isEncoding && exportResult == null) {
                Button(
                    onClick = { startRealVideoEncoding() },
                    modifier = Modifier.testTag("start_mp4_encoding_btn")
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Render Real MP4")
                }
            } else if (exportResult != null) {
                Button(onClick = onDismiss) {
                    Text("Done")
                }
            }
        },
        dismissButton = {
            if (!isEncoding) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}
