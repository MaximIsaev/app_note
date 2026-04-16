package com.notesapp.markdownnotes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.notesapp.markdownnotes.data.local.NoteDatabase
import com.notesapp.markdownnotes.data.repository.NoteRepository
import com.notesapp.markdownnotes.domain.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteRepository
    val allNotes: Flow<List<Note>>

    init {
        val noteDao = NoteDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        allNotes = repository.allNotes
    }

    fun addNote(title: String, content: String) = viewModelScope.launch {
        val note = Note(title = title, content = content)
        repository.insertNote(note)
    }

    fun updateNote(note: Note) = viewModelScope.launch {
        val updatedNote = note.copy(lastModified = System.currentTimeMillis())
        repository.updateNote(updatedNote)
    }

    fun deleteNote(note: Note) = viewModelScope.launch {
        repository.deleteNote(note)
    }

    suspend fun getNoteById(noteId: Long): Note? = repository.getNoteById(noteId)
}
