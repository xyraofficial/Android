# XyraAI - Android AI Chat Application

## Overview
XyraAI is an Android application built for AIDE (Android IDE) that provides AI chat functionality using the GROQ API with Llama 3.3 70B model.

**Important:** This is an Android project with Java source code. It cannot be run directly in Replit as it requires compilation via AIDE or Android Studio and installation on an Android device.

## Project Structure
```
XyraAI/
├── src/com/xyra/ai/       # Java source files
│   ├── MainActivity.java   # Main chat activity
│   ├── LoginActivity.java  # Firebase Google Sign-In
│   ├── ProfileActivity.java
│   ├── ChatAdapter.java    # RecyclerView adapter
│   ├── Message.java        # Message model
│   ├── GroqApiService.java # GROQ API integration
│   ├── Config.java         # App configuration
│   └── ...
├── res/                    # Android resources (layouts, drawables, values)
├── bin/                    # Compiled class files
├── gen/                    # Generated files (R.java, BuildConfig.java)
├── AndroidManifest.xml     # App manifest
├── google-services.json    # Firebase configuration
└── FIREBASE_SETUP.md       # Firebase setup guide (Indonesian)
```

## Key Features
- AI chat using GROQ API with Llama 3.3 70B model
- Firebase Google Sign-In authentication
- Dark theme UI with gradient accents
- Chat history persistence
- Network status indicator

## Configuration
- **Package Name:** com.xyra.ai
- **Firebase Project:** authaixyra
- **API:** GROQ API (https://api.groq.com/openai/v1/chat/completions)

## How to Build
1. Copy the XyraAI folder to an Android device
2. Open in AIDE or Android Studio
3. Add your GROQ API key in Config.java
4. Configure Firebase (see FIREBASE_SETUP.md)
5. Build and install the APK

## Recent Changes
- December 2025: Project imported to Replit for version control and collaboration
