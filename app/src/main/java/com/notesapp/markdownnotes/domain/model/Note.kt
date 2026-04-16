package com.notesapp.markdownnotes.domain.model

data class Note(
    val id: String,
    val title: String,
    val content: String,
    val lastModified: Long
)
