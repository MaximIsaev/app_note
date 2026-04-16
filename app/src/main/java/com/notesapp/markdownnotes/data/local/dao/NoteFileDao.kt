package com.notesapp.markdownnotes.data.local.dao

import com.notesapp.markdownnotes.data.local.entity.NoteFile
import com.notesapp.markdownnotes.domain.model.Note
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class NoteFileDao(private val notesDirectory: File) {
    
    init {
        if (!notesDirectory.exists()) {
            notesDirectory.mkdirs()
        }
    }
    
    fun getAllNotes(): List<Note> {
        val files = notesDirectory.listFiles { file ->
            file.isFile && file.extension == "md"
        } ?: emptyArray()
        
        return files
            .map { NoteFile.fromFile(it) }
            .map { noteFile ->
                Note(
                    id = noteFile.id,
                    title = noteFile.title,
                    content = noteFile.content,
                    lastModified = noteFile.lastModified
                )
            }
            .sortedByDescending { it.lastModified }
    }
    
    fun getNoteById(id: String): Note? {
        val file = File(notesDirectory, "$id.md")
        return if (file.exists()) {
            val noteFile = NoteFile.fromFile(file)
            Note(
                id = noteFile.id,
                title = noteFile.title,
                content = noteFile.content,
                lastModified = noteFile.lastModified
            )
        } else {
            null
        }
    }
    
    fun saveNote(note: Note) {
        val file = File(notesDirectory, "${note.id}.md")
        file.writeText(note.content)
    }
    
    fun createNote(title: String, content: String): Note {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val id = "${timestamp}_${UUID.randomUUID().toString().take(8)}"
        val now = System.currentTimeMillis()
        
        val note = Note(
            id = id,
            title = title,
            content = content,
            lastModified = now
        )
        
        saveNote(note)
        return note
    }
    
    fun deleteNote(id: String): Boolean {
        val file = File(notesDirectory, "$id.md")
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }
}
