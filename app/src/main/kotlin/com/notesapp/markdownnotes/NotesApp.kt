package com.notesapp.markdownnotes

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.notesapp.markdownnotes.domain.model.Note
import com.notesapp.markdownnotes.ui.screens.NoteEditorScreen
import com.notesapp.markdownnotes.ui.screens.NotesListScreen
import com.notesapp.markdownnotes.viewmodel.NoteViewModel

@Composable
fun NotesApp(
    noteViewModel: NoteViewModel = viewModel()
) {
    val navController = rememberNavController()
    val notes by noteViewModel.allNotes.collectAsState(initial = emptyList())
    
    var currentNote by remember { mutableStateOf<Note?>(null) }
    var editedTitle by remember { mutableStateOf("") }
    var editedContent by remember { mutableStateOf("") }

    NavHost(navController = navController, startDestination = "notes") {
        composable("notes") {
            NotesListScreen(
                notes = notes,
                onNoteClick = { note ->
                    currentNote = note
                    editedTitle = note.title
                    editedContent = note.content
                    navController.navigate("edit/${note.id}")
                },
                onAddNoteClick = {
                    currentNote = null
                    editedTitle = ""
                    editedContent = ""
                    navController.navigate("edit/new")
                }
            )
        }
        
        composable(
            route = "edit/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            val isEditing = noteId != "new"
            
            LaunchedEffect(noteId) {
                if (isEditing) {
                    noteId.toLongOrNull()?.let { id ->
                        currentNote = noteViewModel.getNoteById(id)
                        currentNote?.let {
                            editedTitle = it.title
                            editedContent = it.content
                        }
                    }
                } else {
                    currentNote = null
                    editedTitle = ""
                    editedContent = ""
                }
            }
            
            NoteEditorScreen(
                note = currentNote,
                onTitleChange = { editedTitle = it },
                onContentChange = { editedContent = it },
                onSaveClick = {
                    if (isEditing && currentNote != null) {
                        noteViewModel.updateNote(
                            currentNote!!.copy(title = editedTitle, content = editedContent)
                        )
                    } else {
                        noteViewModel.addNote(editedTitle, editedContent)
                    }
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
