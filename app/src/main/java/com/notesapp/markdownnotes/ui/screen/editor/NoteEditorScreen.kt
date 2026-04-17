package com.notesapp.markdownnotes.ui.screen.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import java.util.regex.Pattern

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    initialTitle: String,
    initialContent: String,
    isEditMode: Boolean = false,
    onNavigateBack: (String, String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var title by remember { mutableStateOf(initialTitle) }
    var content by remember { mutableStateOf(initialContent) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isExpanded = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack(title, content) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateBack(title, content) }) {
                        Icon(Icons.Default.Check, contentDescription = "Сохранить")
                    }
                    if (isEditMode && onDelete != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        AnimatedVisibility(
            visible = isExpanded,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Панель инструментов с кнопками форматирования
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val prefix = "# "
                            val currentText = textFieldValue.text
                            val selectionStart = textFieldValue.selection.start
                            val lines = currentText.substring(0, selectionStart).split("\n")
                            val currentLineStart = if (lines.size > 1) {
                                currentText.substring(0, selectionStart).lastIndexOf('\n') + 1
                            } else 0
                            
                            val newText = currentText.replaceRange(
                                currentLineStart,
                                currentLineStart,
                                prefix
                            )
                            textFieldValue = TextFieldValue(
                                newText,
                                TextRange(selectionStart + prefix.length)
                            )
                            content = newText
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text("H1")
                    }
                    
                    Button(
                        onClick = {
                            val prefix = "## "
                            val currentText = textFieldValue.text
                            val selectionStart = textFieldValue.selection.start
                            val lines = currentText.substring(0, selectionStart).split("\n")
                            val currentLineStart = if (lines.size > 1) {
                                currentText.substring(0, selectionStart).lastIndexOf('\n') + 1
                            } else 0
                            
                            val newText = currentText.replaceRange(
                                currentLineStart,
                                currentLineStart,
                                prefix
                            )
                            textFieldValue = TextFieldValue(
                                newText,
                                TextRange(selectionStart + prefix.length)
                            )
                            content = newText
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text("H2")
                    }
                    
                    Button(
                        onClick = {
                            val prefix = "### "
                            val currentText = textFieldValue.text
                            val selectionStart = textFieldValue.selection.start
                            val lines = currentText.substring(0, selectionStart).split("\n")
                            val currentLineStart = if (lines.size > 1) {
                                currentText.substring(0, selectionStart).lastIndexOf('\n') + 1
                            } else 0
                            
                            val newText = currentText.replaceRange(
                                currentLineStart,
                                currentLineStart,
                                prefix
                            )
                            textFieldValue = TextFieldValue(
                                newText,
                                TextRange(selectionStart + prefix.length)
                            )
                            content = newText
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text("H3")
                    }
                }
                
                // Поле контента без рамок и линий с поддержкой нумерованных списков
                var textFieldValue by remember { mutableStateOf(TextFieldValue(initialContent)) }
                
                LaunchedEffect(content) {
                    if (textFieldValue.text != content) {
                        textFieldValue = TextFieldValue(content, TextRange(content.length))
                    }
                }
                
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        val lastText = textFieldValue.text
                        val currentText = newValue.text
                        
                        // Проверяем, была ли нажата клавиша Enter (добавлен символ новой строки)
                        if (currentText.length > lastText.length && 
                            currentText[lastText.length] == '\n') {
                            
                            val lines = lastText.split("\n")
                            if (lines.isNotEmpty()) {
                                val lastLine = lines.last()
                                
                                // Паттерн для поиска нумерованного списка: цифра(и), точка, пробел
                                val numberedListPattern = Pattern.compile("^(\\d+)\\.\\s(.*)$")
                                val matcher = numberedListPattern.matcher(lastLine)
                                
                                if (matcher.matches()) {
                                    val currentNumber = matcher.group(1)?.toIntOrNull() ?: 0
                                    val listItemText = matcher.group(2) ?: ""
                                    
                                    // Если текст элемента списка не пустой, продолжаем нумерацию
                                    if (listItemText.isNotEmpty()) {
                                        val nextNumber = currentNumber + 1
                                        val newText = "$lastText\n$nextNumber. "
                                        val newSelection = TextRange(newText.length)
                                        textFieldValue = TextFieldValue(newText, newSelection)
                                        content = newText
                                        return@BasicTextField
                                    }
                                    // Если текст пустой, просто добавляем новую строку без нумерации
                                }
                            }
                        }
                        
                        textFieldValue = newValue
                        content = newValue.text
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 400.dp),
                    textStyle = LocalTextStyle.current.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    maxLines = Int.MAX_VALUE,
                    decorationBox = { innerTextField ->
                        Box {
                            if (textFieldValue.text.isEmpty()) {
                                Text(
                                    text = "Текст",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
    }

    if (showDeleteDialog && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Подтверждение") },
            text = { Text("Вы действительно хотите удалить заметку?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("ДА")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("НЕТ")
                }
            }
        )
    }
}

