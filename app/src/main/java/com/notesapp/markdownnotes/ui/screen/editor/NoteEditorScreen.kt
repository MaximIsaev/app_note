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

// Функция для удаления markdown-символов из строки (bold, italic)
fun removeMarkdownSymbols(line: String): String {
    var result = line
    // Удаляем ** для bold
    result = result.replace("**", "")
    // Удаляем * для italic (но не те, что были частью **)
    // После удаления ** остались только одиночные *
    result = result.replace("*", "")
    return result
}

// Функция для создания AnnotatedString со стилями заголовков, bold и italic
fun buildAnnotatedStringWithStyles(displayText: String, originalLines: List<String>): AnnotatedString {
    val builder = AnnotatedString.Builder()
    val displayLines = displayText.split("\n")
    
    for (i in displayLines.indices) {
        val originalLine = if (i < originalLines.size) originalLines[i] else ""
        val displayLine = if (i < displayLines.size) displayLines[i] else ""
        
        // Сначала применяем стиль заголовка если есть
        val baseStyle = when {
            originalLine.startsWith("### ") -> SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)
            originalLine.startsWith("## ") -> SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
            originalLine.startsWith("# ") -> SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
            else -> SpanStyle(fontSize = 16.sp)
        }
        
        // Теперь обрабатываем bold и italic внутри строки
        val contentLine = when {
            originalLine.startsWith("### ") -> originalLine.substring(4)
            originalLine.startsWith("## ") -> originalLine.substring(3)
            originalLine.startsWith("# ") -> originalLine.substring(2)
            else -> originalLine
        }
        
        builder.pushStyle(baseStyle)
        applyInlineStyles(builder, contentLine)
        builder.pop()
        
        if (i < displayLines.size - 1) {
            builder.append("\n")
        }
    }
    
    return builder.toAnnotatedString()
}

// Функция для применения inline стилей (bold, italic) внутри строки
fun applyInlineStyles(builder: AnnotatedString.Builder, text: String) {
    var index = 0
    while (index < text.length) {
        // Проверяем на bold (**text**)
        if (index + 1 < text.length && text[index] == '*' && text[index + 1] == '*') {
            val startIndex = index + 2
            val endIndex = text.indexOf("**", startIndex)
            if (endIndex != -1) {
                // Нашли закрывающий **
                builder.pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                builder.append(text.substring(startIndex, endIndex))
                builder.pop()
                index = endIndex + 2
                continue
            }
        }
        
        // Проверяем на italic (*text*)
        if (text[index] == '*') {
            val startIndex = index + 1
            val endIndex = text.indexOf('*', startIndex)
            if (endIndex != -1) {
                // Проверяем, что это не часть **
                if (!(startIndex > 0 && text[startIndex - 1] == '*') && 
                    !(endIndex + 1 < text.length && text[endIndex + 1] == '*')) {
                    builder.pushStyle(SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic))
                    builder.append(text.substring(startIndex, endIndex))
                    builder.pop()
                    index = endIndex + 1
                    continue
                }
            }
        }
        
        // Обычный символ
        builder.append(text[index])
        index++
    }
}

// Функция для создания визуальной трансформации, скрывающей символы форматирования
// Функция для создания визуальной трансформации, скрывающей символы форматирования
fun createMarkdownVisualTransformation(): VisualTransformation {
    return VisualTransformation { text ->
        val transformedResult = transformMarkdownText(text.text)
        val transformedString = transformedResult.first
        val originalLines = transformedResult.second
        
        // Создаём отображение позиций с учётом bold и italic
        val offsetMapping = object : OffsetMapping {
            // Кэш для хранения позиций markdown-символов
            private var cachedOriginalText: String = ""
            private var hiddenPositions: List<Int> = emptyList()
            
            private fun computeHiddenPositions(originalText: String) {
                if (cachedOriginalText == originalText) return
                
                cachedOriginalText = originalText
                val positions = mutableListOf<Int>()
                
                var i = 0
                while (i < originalText.length) {
                    // Проверяем начало строки для заголовков
                    val isStartOfLine = (i == 0 || originalText[i - 1] == '\n')
                    
                    if (isStartOfLine) {
                        when {
                            originalText.startsWith("### ", i) -> {
                                positions.addAll(listOf(i, i+1, i+2, i+3)) // ### и пробел
                                i += 4
                                continue
                            }
                            originalText.startsWith("## ", i) -> {
                                positions.addAll(listOf(i, i+1, i+2)) // ## и пробел
                                i += 3
                                continue
                            }
                            originalText.startsWith("# ", i) -> {
                                positions.addAll(listOf(i, i+1)) // # и пробел
                                i += 2
                                continue
                            }
                        }
                    }
                    
                    // Проверяем на bold (**text**)
                    if (i + 1 < originalText.length && originalText[i] == '*' && originalText[i + 1] == '*') {
                        val contentStart = i + 2
                        val contentEnd = originalText.indexOf("**", contentStart)
                        
                        if (contentEnd != -1) {
                            // Скрываем открывающие **
                            positions.addAll(listOf(i, i+1))
                            // Скрываем закрывающие **
                            positions.addAll(listOf(contentEnd, contentEnd+1))
                            i = contentEnd + 2
                            continue
                        }
                    }
                    
                    // Проверяем на italic (*text*)
                    if (originalText[i] == '*') {
                        // Проверяем, что это не часть **
                        val isPartOfBold = (i > 0 && originalText[i-1] == '*') || 
                                          (i + 1 < originalText.length && originalText[i+1] == '*')
                        
                        if (!isPartOfBold) {
                            val contentStart = i + 1
                            val contentEnd = originalText.indexOf('*', contentStart)
                            
                            if (contentEnd != -1) {
                                // Проверяем, что закрывающая * тоже не часть **
                                val isEndPartOfBold = (contentEnd + 1 < originalText.length && originalText[contentEnd+1] == '*')
                                
                                if (!isEndPartOfBold) {
                                    positions.add(i) // Скрываем открывающую *
                                    positions.add(contentEnd) // Скрываем закрывающую *
                                    i = contentEnd + 1
                                    continue
                                }
                            }
                        }
                    }
                    
                    i++
                }
                
                hiddenPositions = positions.sorted()
            }
            
            override fun originalToTransformed(offset: Int): Int {
                computeHiddenPositions(text.text)
                
                // Считаем сколько скрытых символов до этой позиции
                var hiddenCount = 0
                for (pos in hiddenPositions) {
                    if (pos < offset) {
                        hiddenCount++
                    } else {
                        break
                    }
                }
                
                return offset - hiddenCount
            }

            override fun transformedToOriginal(offset: Int): Int {
                computeHiddenPositions(text.text)
                
                var originalIndex = offset
                var hiddenCount = 0
                
                for (pos in hiddenPositions) {
                    if (pos <= originalIndex + hiddenCount) {
                        hiddenCount++
                    } else {
                        break
                    }
                }
                
                return originalIndex + hiddenCount
            }
        }
        
        TransformedText(
            text = buildAnnotatedStringWithStyles(transformedString, originalLines),
            offsetMapping = offsetMapping
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

