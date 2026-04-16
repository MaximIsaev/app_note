package com.notesapp.markdownnotes.ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.noties.markwon.Markwon
import io.noties.markwon.core.CorePlugin

@Composable
fun MarkdownPreview(
    markdown: String,
    modifier: Modifier = Modifier
) {
    // Simple markdown rendering placeholder
    // In a real app, you would use a proper markdown renderer with Android WebView or custom Compose component
    val processedText = processMarkdown(markdown)
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        processedText.forEach { line ->
            when {
                line.startsWith("# ") -> {
                    androidx.compose.material3.Text(
                        text = line.removePrefix("# "),
                        style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                line.startsWith("## ") -> {
                    androidx.compose.material3.Text(
                        text = line.removePrefix("## "),
                        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                line.startsWith("### ") -> {
                    androidx.compose.material3.Text(
                        text = line.removePrefix("### "),
                        style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                line.startsWith("- ") || line.startsWith("* ") -> {
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        androidx.compose.material3.Text("• ", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                        androidx.compose.material3.Text(
                            text = line.removePrefix("- ").removePrefix("* "),
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                line.startsWith("> ") -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.LightGray.copy(alpha = 0.3f))
                            .padding(8.dp)
                    ) {
                        androidx.compose.material3.Text(
                            text = line.removePrefix("> "),
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        )
                    }
                }
                line.startsWith("```") -> {
                    // Code block handling (simplified)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF2D2D2D))
                            .padding(8.dp)
                    ) {
                        androidx.compose.material3.Text(
                            text = line,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
                else -> {
                    if (line.isNotBlank()) {
                        androidx.compose.material3.Text(
                            text = line,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun processMarkdown(text: String): List<String> {
    return text.lines()
}
