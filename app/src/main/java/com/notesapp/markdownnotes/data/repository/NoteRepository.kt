package com.notesapp.markdownnotes.data.repository

import android.content.Context
import com.notesapp.markdownnotes.data.local.dao.NoteFileDao
import com.notesapp.markdownnotes.domain.model.Note
import java.io.File

class NoteRepository(context: Context) {
    
    private val dao: NoteFileDao
    
    init {
        val notesDir = File(context.filesDir, "notes")
        dao = NoteFileDao(notesDir)
    }
    
    fun getAllNotes(): List<Note> {
        return dao.getAllNotes()
    }
    
    fun getNoteById(id: String): Note? {
        return dao.getNoteById(id)
    }
    
    fun saveNote(note: Note) {
        dao.saveNote(note)
    }
    
    fun createNote(title: String, content: String): Note {
        return dao.createNote(title, content)
    }
    
    fun deleteNote(id: String): Boolean {
        return dao.deleteNote(id)
    }
}
