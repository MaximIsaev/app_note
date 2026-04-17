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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.regex.Pattern

// Функция для преобразования текста: возвращает текст без символов # и оригинальные строки
fun transformMarkdownText(text: String): Pair<String, List<String>> {
    val lines = text.split("\n")
    val resultLines = mutableListOf<String>()
    val originalLines = mutableListOf<String>()
    
    for (line in lines) {
        originalLines.add(line)
        when {
            line.startsWith("### ") -> resultLines.add(line.substring(4))
            line.startsWith("## ") -> resultLines.add(line.substring(3))
            line.startsWith("# ") -> resultLines.add(line.substring(2))
            else -> resultLines.add(line)
        }
    }
    
    return Pair(resultLines.joinToString("\n"), originalLines)
}

// Функция для создания AnnotatedString со стилями заголовков
fun buildAnnotatedStringWithStyles(displayText: String, originalLines: List<String>): AnnotatedString {
    val builder = AnnotatedString.Builder()
    val displayLines = displayText.split("\n")
    
    for (i in displayLines.indices) {
        val originalLine = if (i < originalLines.size) originalLines[i] else ""
        val displayLine = if (i < displayLines.size) displayLines[i] else ""
        
        when {
            originalLine.startsWith("### ") -> {
                builder.pushStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold))
                builder.append(displayLine)
                builder.pop()
            }
            originalLine.startsWith("## ") -> {
                builder.pushStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold))
                builder.append(displayLine)
                builder.pop()
            }
            originalLine.startsWith("# ") -> {
                builder.pushStyle(SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
                builder.append(displayLine)
                builder.pop()
            }
            else -> {
                builder.pushStyle(SpanStyle(fontSize = 16.sp))
                builder.append(displayLine)
                builder.pop()
            }
        }
        
        if (i < displayLines.size - 1) {
            builder.append("\n")
        }
    }
    
    return builder.toAnnotatedString()
}

// Функция для создания визуальной трансформации, скрывающей символы форматирования
fun createMarkdownVisualTransformation(): VisualTransformation {
    return VisualTransformation { text ->
        val transformedResult = transformMarkdownText(text.text)
        val transformedString = transformedResult.first
        val originalLines = transformedResult.second
        
        TransformedText(
            text = buildAnnotatedStringWithStyles(transformedString, originalLines),
            offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    if (offset == 0) return 0
                    
                    val originalText = text.text
                    var originalIndex = 0
                    var transformedIndex = 0
                    
                    while (originalIndex < offset && originalIndex < originalText.length) {
                        // Проверяем начало строки
                        val isStartOfLine = (originalIndex == 0 || originalText[originalIndex - 1] == '\n')
                        
                        if (isStartOfLine) {
                            val remaining = originalText.substring(originalIndex)
                            when {
                                remaining.startsWith("### ") -> {
                                    if (originalIndex + 4 <= offset) {
                                        originalIndex += 4
                                        transformedIndex += 0
                                        continue
                                    } else {
                                        // Курсор внутри скрытой части
                                        return transformedIndex
                                    }
                                }
                                remaining.startsWith("## ") -> {
                                    if (originalIndex + 3 <= offset) {
                                        originalIndex += 3
                                        transformedIndex += 0
                                        continue
                                    } else {
                                        return transformedIndex
                                    }
                                }
                                remaining.startsWith("# ") -> {
                                    if (originalIndex + 2 <= offset) {
                                        originalIndex += 2
                                        transformedIndex += 0
                                        continue
                                    } else {
                                        return transformedIndex
                                    }
                                }
                            }
                        }
                        
                        if (originalText[originalIndex] == '\n') {
                            transformedIndex++
                            originalIndex++
                        } else {
                            transformedIndex++
                            originalIndex++
                        }
                    }
                    
                    return transformedIndex
                }

                override fun transformedToOriginal(offset: Int): Int {
                    if (offset == 0) return 0
                    
                    val originalText = text.text
                    var originalIndex = 0
                    var transformedIndex = 0
                    
                    while (transformedIndex < offset && originalIndex < originalText.length) {
                        // Проверяем начало строки
                        val isStartOfLine = (originalIndex == 0 || originalText[originalIndex - 1] == '\n')
                        
                        if (isStartOfLine) {
                            val remaining = originalText.substring(originalIndex)
                            when {
                                remaining.startsWith("### ") -> {
                                    originalIndex += 4
                                    // transformedIndex не увеличивается
                                    continue
                                }
                                remaining.startsWith("## ") -> {
                                    originalIndex += 3
                                    continue
                                }
                                remaining.startsWith("# ") -> {
                                    originalIndex += 2
                                    continue
                                }
                            }
                        }
                        
                        if (originalIndex < originalText.length) {
                            if (originalText[originalIndex] == '\n') {
                                transformedIndex++
                                originalIndex++
                            } else {
                                transformedIndex++
                                originalIndex++
                            }
                        }
                    }
                    
                    return originalIndex
                }
            }
        )
    }
}

// Функция для создания стиля текста (теперь @Composable, так как использует MaterialTheme)
@Composable
fun buildTextStyle(): TextStyle {
    return TextStyle(
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 16.sp
    )
}

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
    
    // Объявляем textFieldValue здесь, чтобы он был доступен в кнопках
    var textFieldValue by remember { mutableStateOf(TextFieldValue(initialContent)) }

    LaunchedEffect(Unit) {
        isExpanded = true
    }
    
    // Синхронизируем textFieldValue при изменении content извне
    LaunchedEffect(content) {
        if (textFieldValue.text != content) {
            textFieldValue = TextFieldValue(content, TextRange(content.length))
        }
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
                    .padding(paddingValues),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Поле контента без рамок и линий с поддержкой нумерованных списков
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
                        textStyle = buildTextStyle(),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        maxLines = Int.MAX_VALUE,
                        visualTransformation = createMarkdownVisualTransformation(),
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
                
                // Панель инструментов с кнопками форматирования внизу экрана
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Кнопка H1
                        IconButton(
                            onClick = {
                                val prefix = "# "
                                val currentText = textFieldValue.text
                                val selectionStart = textFieldValue.selection.start
                                val selectionEnd = textFieldValue.selection.end
                                val lines = currentText.substring(0, selectionStart).split("\n")
                                val currentLineStart = if (lines.size > 1) {
                                    currentText.substring(0, selectionStart).lastIndexOf('\n') + 1
                                } else 0
                                
                                val newText = currentText.replaceRange(
                                    currentLineStart,
                                    currentLineStart,
                                    prefix
                                )
                                val newCursorPos = if (selectionStart == selectionEnd) {
                                    selectionStart + prefix.length
                                } else {
                                    selectionEnd + prefix.length
                                }
                                textFieldValue = TextFieldValue(
                                    newText,
                                    TextRange(newCursorPos)
                                )
                                content = newText
                            },
                            modifier = Modifier.size(48.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text("H1", style = MaterialTheme.typography.labelMedium)
                        }
                        
                        // Кнопка H2
                        IconButton(
                            onClick = {
                                val prefix = "## "
                                val currentText = textFieldValue.text
                                val selectionStart = textFieldValue.selection.start
                                val selectionEnd = textFieldValue.selection.end
                                val lines = currentText.substring(0, selectionStart).split("\n")
                                val currentLineStart = if (lines.size > 1) {
                                    currentText.substring(0, selectionStart).lastIndexOf('\n') + 1
                                } else 0
                                
                                val newText = currentText.replaceRange(
                                    currentLineStart,
                                    currentLineStart,
                                    prefix
                                )
                                val newCursorPos = if (selectionStart == selectionEnd) {
                                    selectionStart + prefix.length
                                } else {
                                    selectionEnd + prefix.length
                                }
                                textFieldValue = TextFieldValue(
                                    newText,
                                    TextRange(newCursorPos)
                                )
                                content = newText
                            },
                            modifier = Modifier.size(48.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text("H2", style = MaterialTheme.typography.labelMedium)
                        }
                        
                        // Кнопка H3
                        IconButton(
                            onClick = {
                                val prefix = "### "
                                val currentText = textFieldValue.text
                                val selectionStart = textFieldValue.selection.start
                                val selectionEnd = textFieldValue.selection.end
                                val lines = currentText.substring(0, selectionStart).split("\n")
                                val currentLineStart = if (lines.size > 1) {
                                    currentText.substring(0, selectionStart).lastIndexOf('\n') + 1
                                } else 0
                                
                                val newText = currentText.replaceRange(
                                    currentLineStart,
                                    currentLineStart,
                                    prefix
                                )
                                val newCursorPos = if (selectionStart == selectionEnd) {
                                    selectionStart + prefix.length
                                } else {
                                    selectionEnd + prefix.length
                                }
                                textFieldValue = TextFieldValue(
                                    newText,
                                    TextRange(newCursorPos)
                                )
                                content = newText
                            },
                            modifier = Modifier.size(48.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text("H3", style = MaterialTheme.typography.labelMedium)
                        }
                        
                        // Кнопка Bold
                        IconButton(
                            onClick = {
                                val currentText = textFieldValue.text
                                val selection = textFieldValue.selection
                                val start = minOf(selection.start, selection.end)
                                val end = maxOf(selection.start, selection.end)
                                
                                if (start != end) {
                                    val selectedText = currentText.substring(start, end)
                                    val newText = currentText.replaceRange(
                                        start,
                                        end,
                                        "**$selectedText**"
                                    )
                                    textFieldValue = TextFieldValue(
                                        newText,
                                        TextRange(start + 2, end + 2)
                                    )
                                    content = newText
                                } else {
                                    val prefix = "****"
                                    val newText = currentText.replaceRange(
                                        start,
                                        start,
                                        prefix
                                    )
                                    textFieldValue = TextFieldValue(
                                        newText,
                                        TextRange(start + 2)
                                    )
                                    content = newText
                                }
                            },
                            modifier = Modifier.size(48.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Icon(Icons.Default.FormatBold, contentDescription = "Жирный")
                        }
                        
                        // Кнопка Italic
                        IconButton(
                            onClick = {
                                val currentText = textFieldValue.text
                                val selection = textFieldValue.selection
                                val start = minOf(selection.start, selection.end)
                                val end = maxOf(selection.start, selection.end)
                                
                                if (start != end) {
                                    val selectedText = currentText.substring(start, end)
                                    val newText = currentText.replaceRange(
                                        start,
                                        end,
                                        "*$selectedText*"
                                    )
                                    textFieldValue = TextFieldValue(
                                        newText,
                                        TextRange(start + 1, end + 1)
                                    )
                                    content = newText
                                } else {
                                    val prefix = "**"
                                    val newText = currentText.replaceRange(
                                        start,
                                        start,
                                        prefix
                                    )
                                    textFieldValue = TextFieldValue(
                                        newText,
                                        TextRange(start + 1)
                                    )
                                    content = newText
                                }
                            },
                            modifier = Modifier.size(48.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Icon(Icons.Default.FormatItalic, contentDescription = "Курсив")
                        }
                    }
                }
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

