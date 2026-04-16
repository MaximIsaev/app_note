package com.notesapp.markdownnotes.data.local.entity

import java.io.File

data class NoteFile(
    val file: File,
    val id: String,
    val title: String,
    val content: String,
    val lastModified: Long
) {
    companion object {
        fun fromFile(file: File): NoteFile {
            val id = file.nameWithoutExtension
            val content = if (file.exists()) file.readText() else ""
            val title = extractTitle(content) ?: file.nameWithoutExtension
            val lastModified = file.lastModified()
            
            return NoteFile(
                file = file,
                id = id,
                title = title,
                content = content,
                lastModified = lastModified
            )
        }
        
        private fun extractTitle(content: String): String? {
            val lines = content.lines()
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.startsWith("# ")) {
                    return trimmed.substring(2).trim()
                }
                if (trimmed.isNotEmpty()) {
                    return trimmed.take(50)
                }
            }
            return null
        }
    }
}
