# XyraAI - AI Chat Application for Android

## Overview
XyraAI is an Android AI Chat application built for AIDE (Android IDE) that uses GROQ API with Llama 3.3 70B model. The app now uses **Supabase** for authentication and cloud chat history storage.

## Recent Changes (December 2025)
- Added File Manager with hierarchical tree view and CRUD operations
- Added Code Editor with syntax highlighting for 15+ programming languages
- Created FileManagerActivity.java, CodeEditorActivity.java
- Created CodeEditorEngine.java with Monokai, Dracula, One Dark, Light themes
- Added FileItem.java and FolderItem.java model classes
- Added File Manager and Code Editor menu items in sidebar drawer
- Migrated from Firebase to Supabase authentication
- Added cloud chat history sync - chats persist even after app uninstall
- Created SupabaseService.java for auth and database operations
- Updated LoginActivity, ProfileActivity, ChatHistory for Supabase integration
- Fixed build errors: "variable must be final" in LoginActivity.java and SupabaseService.java
- Added circular profile avatar transformation in ProfileActivity.java

## Project Architecture

### Directory Structure
```
XyraAI/
├── src/com/xyra/ai/
│   ├── MainActivity.java         # Main chat activity
│   ├── LoginActivity.java        # Supabase email auth login/signup
│   ├── ProfileActivity.java      # User profile with signout
│   ├── SupabaseService.java      # Supabase auth & database operations
│   ├── FileManagerActivity.java  # File Manager with tree view
│   ├── CodeEditorActivity.java   # Code Editor with syntax highlighting
│   ├── FileManagerAdapter.java   # ListView adapter for file tree
│   ├── CodeEditorEngine.java     # Syntax highlighting engine
│   ├── FileItem.java             # File data model
│   ├── FolderItem.java           # Folder data model
│   ├── ChatHistory.java          # Local + cloud chat storage
│   ├── ChatAdapter.java          # RecyclerView adapter
│   ├── Message.java              # Message model
│   ├── GroqApiService.java       # GROQ API integration
│   ├── Config.java               # App configuration
│   └── ...
├── res/
│   ├── layout/
│   ├── values/
│   └── drawable/
├── AndroidManifest.xml
└── SUPABASE_SETUP.md          # Database setup guide
```

### Key Features
- Modern dark theme UI with gradient accents
- Real-time AI chat using GROQ API
- Supabase email authentication (login/signup) + Google/GitHub OAuth
- Cloud chat history sync
- Local offline support with sync when online
- **File Manager**: Hierarchical tree view, create/rename/delete files and folders
- **Code Editor**: Syntax highlighting for Python, JavaScript, Java, Dart, HTML, CSS, JSON, SQL, PHP, and more
  - Line numbers with cursor position tracking
  - Undo/Redo support
  - Theme switching (Monokai, Dracula, One Dark, Light)
  - Symbol bar for quick character insertion
  - Find & Replace, Go to Line features

### Authentication Flow
1. User signs up/logs in via Supabase email auth
2. Access token and user data stored in SharedPreferences
3. Chat history syncs to Supabase database
4. On reinstall, user logs in and chats are restored from cloud

### Supabase Configuration
- Project ID: figcqxynrcnimagpswqn
- URL: https://figcqxynrcnimagpswqn.supabase.co
- See SUPABASE_SETUP.md for database table creation SQL

## Development Notes
- This is an Android project meant to be built with AIDE on Android device
- Cannot be run directly in Replit (requires Android SDK)
- Java source files follow Android app conventions
