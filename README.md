# Markdown Notes - Android App

A simple and convenient Android application for creating and editing notes in Markdown format.

## Features

- **Create new notes** - Create notes with title and content in Markdown format
- **Edit existing notes** - Edit any previously created note
- **Local storage** - All notes are stored locally on your device using Room database
- **Markdown support** - Write notes using Markdown syntax (headers, lists, code blocks, quotes, etc.)
- **Sorted by date** - Notes are displayed in a list sorted by last modified date (newest first)
- **Modern UI** - Built with Jetpack Compose and Material Design 3

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room (SQLite abstraction)
- **Navigation**: Navigation Compose
- **Dependencies**: 
  - AndroidX Core KTX
  - Lifecycle Runtime KTX
  - Activity Compose
  - Material 3
  - Room Database
  - Markwon (Markdown rendering)

## Project Structure

```
app/src/main/kotlin/com/notesapp/markdownnotes/
├── data/
│   ├── local/
│   │   ├── NoteDao.kt          # Data Access Object for Room
│   │   └── NoteDatabase.kt     # Room Database definition
│   └── repository/
│       └── NoteRepository.kt   # Repository pattern implementation
├── domain/
│   └── model/
│       └── Note.kt             # Note data model
├── ui/
│   ├── screens/
│   │   ├── components/
│   │   │   └── MarkdownPreview.kt  # Markdown preview component
│   │   ├── NoteEditorScreen.kt     # Note editing screen
│   │   └── NotesListScreen.kt      # Notes list screen
│   └── theme/
│       ├── Color.kt            # Color definitions
│       ├── Theme.kt            # App theme
│       └── Type.kt             # Typography
├── viewmodel/
│   └── NoteViewModel.kt        # ViewModel for notes
├── MainActivity.kt             # Main activity
└── NotesApp.kt                 # App navigation and composition
```

## How to Build

1. Open the project in Android Studio
2. Sync Gradle files
3. Build and run on an Android device or emulator (API 24+)

## Usage

1. Launch the app
2. Tap the "+" button to create a new note
3. Enter a title and write content in Markdown format
4. Tap the checkmark to save
5. Tap on any note in the list to edit it

## Markdown Support

The app supports basic Markdown syntax:
- Headers (`#`, `##`, `###`)
- Bullet lists (`-`, `*`)
- Blockquotes (`>`)
- Code blocks (```)
- Plain text

## License

This project is open source and available for educational purposes.
