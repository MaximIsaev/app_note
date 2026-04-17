package com.notesapp.markdownnotes.data.repository

import android.content.Context
import com.notesapp.markdownnotes.data.local.dao.NoteFileDao
import com.notesapp.markdownnotes.domain.model.Note
import java.io.File
import java.util.zip.ZipOutputStream
import java.io.FileInputStream
import java.io.BufferedInputStream
import java.util.zip.ZipEntry

class NoteRepository(context: Context) {
    
    private val dao: NoteFileDao
    private val notesDir: File
    
    init {
        notesDir = File(context.filesDir, "notes")
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
    
    fun exportAllNotesToZip(context: Context): Result<String> {
        return try {
            val notes = getAllNotes()
            if (notes.isEmpty()) {
                return Result.failure(Exception("Нет заметок для экспорта"))
            }
            
            // Создаем ZIP файл в папке загрузок
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_DOWNLOADS
            )
            val timestamp = System.currentTimeMillis()
            val zipFileName = "notes_export_$timestamp.zip"
            val zipFile = File(downloadsDir, zipFileName)
            
            ZipOutputStream(zipFile.outputStream()).use { zos ->
                notes.forEach { note ->
                    val noteFile = File(notesDir, "${note.id}.md")
                    if (noteFile.exists()) {
                        val entry = ZipEntry("${note.id}.md")
                        zos.putNextEntry(entry)
                        
                        FileInputStream(noteFile).use { fis ->
                            BufferedInputStream(fis).use { bis ->
                                bis.copyTo(zos)
                            }
                        }
                        zos.closeEntry()
                    }
                }
            }
            
            Result.success(zipFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
