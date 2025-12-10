# XyraAI - Android AI Chat Project

## Overview
This is an AIDE (Android IDE) project for an AI Chat application called XyraAI. It's designed to be opened and compiled using the AIDE app on Android devices.

## Project Details
- **Package Name**: `com.xyra.ai`
- **App Name**: XyraAI
- **AI Backend**: GROQ API with Llama 3.3 70B model (with vision support)
- **Target SDK**: Android 33
- **Minimum SDK**: Android 21 (5.0 Lollipop)

## Project Structure

```
XyraAI/
├── AndroidManifest.xml        # App manifest
├── .classpath                 # Eclipse/AIDE classpath
├── .project                   # Project metadata
├── project.properties         # SDK target
├── libs/                      # JAR libraries folder
├── src/com/xyra/ai/           # Java source files
│   ├── MainActivity.java      # Main activity with drawer
│   ├── ChatAdapter.java       # RecyclerView adapter with chatId tracking
│   ├── ChatHistoryAdapter.java # Drawer chat list adapter
│   ├── Message.java           # Message model with image support
│   ├── GroqApiService.java    # API service with vision support
│   ├── Config.java            # Configuration
│   ├── NetworkUtils.java      # Network utilities
│   ├── ChatHistory.java       # Multi-chat persistence system
│   ├── TypingAnimator.java    # Fast typing animation
│   └── SettingsActivity.java  # Settings screen
└── res/                       # Android resources
    ├── layout/                # XML layouts
    ├── values/                # Strings, colors, styles
    └── drawable/              # Icons and backgrounds
```

## How to Use

1. **Download the XyraAI folder** from this Replit
2. **Copy to your Android device** at `/sdcard/AppProjects/XyraAI/`
3. **Open in AIDE app**
4. **Add your GROQ API key** in `MainActivity.java`
5. **Build and Run** from AIDE

## API Key Setup
The GROQ API key needs to be added to:
- `src/com/xyra/ai/MainActivity.java` (line with `API_KEY`)
- `src/com/xyra/ai/Config.java` (GROQ_API_KEY constant)

## Features
- Beautiful dark theme chat interface
- Real-time AI responses using GROQ/Llama 3.3 70B
- Image analysis with actual image preview in messages
- Fast typing animation (chunk-based with minimal delays)
- Multi-chat system with sidebar drawer navigation
- Chat history persistence with per-chat storage
- Search functionality for finding old chats
- Settings screen with smooth UI and Exit App button
- Network status indicator
- Typing/thinking indicator

## Architecture Notes
- **Multi-chat persistence**: Uses per-chatId JSON storage in SharedPreferences
- **Async handling**: Captures chatId at send time, saves responses to correct chat
- **MVP design**: Chat switching blocked during pending responses to ensure data integrity
- **Drawer refresh**: Previews update immediately after any save operation

## Secrets
- `GROQ_API_KEY` - Your GROQ API key for AI chat functionality
