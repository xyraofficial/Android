# XyraAI - AI Chat Application for Android

## Overview
XyraAI is an Android AI Chat application built for AIDE (Android IDE) that uses GROQ API with Llama 3.3 70B model. The app now uses **Supabase** for authentication and cloud chat history storage.

## Recent Changes (December 2025)
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
│   ├── MainActivity.java      # Main chat activity
│   ├── LoginActivity.java     # Supabase email auth login/signup
│   ├── ProfileActivity.java   # User profile with signout
│   ├── SupabaseService.java   # Supabase auth & database operations
│   ├── ChatHistory.java       # Local + cloud chat storage
│   ├── ChatAdapter.java       # RecyclerView adapter
│   ├── Message.java           # Message model
│   ├── GroqApiService.java    # GROQ API integration
│   ├── Config.java            # App configuration
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
- Supabase email authentication (login/signup)
- Cloud chat history sync
- Local offline support with sync when online

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
