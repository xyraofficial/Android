# XyraAI - Android AI Chat Application

## Overview
XyraAI is an Android AI Chat application built for AIDE (Android IDE). It uses the GROQ API with Llama 3.3 70B model for AI conversations.

## Project Type
This is an **Android project** (not a web application). It is designed to be:
1. Copied to an Android device
2. Opened in AIDE (Android IDE)
3. Compiled and built into an APK on the device

## Project Structure
```
XyraAI/
├── AndroidManifest.xml      # App configuration & permissions
├── project.properties       # Android SDK target
├── README.md               # Setup instructions
├── SUPABASE_SETUP.md       # Supabase configuration guide
├── src/com/xyra/ai/        # Java source files
│   ├── MainActivity.java   # Main chat activity
│   ├── LoginActivity.java  # User authentication
│   ├── ChatAdapter.java    # RecyclerView adapter
│   ├── GroqApiService.java # GROQ API integration
│   ├── SupabaseService.java # Supabase auth & database
│   └── ... (other activities and utilities)
├── res/                    # Android resources
│   ├── layout/            # XML layouts
│   ├── values/            # Strings, colors, styles
│   └── drawable/          # Icons and backgrounds
├── bin/                   # Pre-compiled .class files
└── gen/                   # Generated R.java files
```

## Features
- Modern dark theme UI with gradient accents
- Real-time AI chat using GROQ API
- Chat message history with cloud sync (Supabase)
- User authentication (Email/Password + Google OAuth)
- Beautiful message bubbles with timestamps
- Network status indicator
- Conversation context memory
- **Voice Input (STT)**: Talk to AI using voice with multi-language support
- **Voice Output (TTS)**: AI reads answers aloud with 11 languages
- **Share to WhatsApp**: Share conversations easily
- **Bookmarks**: Save important messages
- **AI Personas**: Customize AI personality
- **Code Execution**: Run code directly in app
- **Document Analysis**: Analyze uploaded documents
- **Web Search**: AI searches current internet info
- **Daily Reminders**: Get AI tips via notifications
- **Quick Reply Templates**: Pre-made prompts for common tasks

## API Configuration
- **GROQ API**: `https://api.groq.com/openai/v1/chat/completions`
- **Model**: `llama-3.3-70b-versatile`
- **Supabase**: Used for authentication and chat history cloud sync

## Requirements
- Android 5.0 (API 21) or higher
- AIDE - Android IDE app
- Internet connection
- GROQ API key (from https://console.groq.com)
- Supabase project (for cloud features)

## How to Use
1. Copy the XyraAI folder to Android device storage
2. Open in AIDE
3. Configure API keys in Config.java
4. Build and run to generate APK

## Notes
- The Firebase integration installed is for Flask/Python and does not apply to this Android project
- This project cannot run as a web server on Replit
- Pre-compiled class files are available in bin/ directory

---

# XyraAdmin - Android Admin Panel

## Overview
XyraAdmin is the companion admin panel app for XyraAI. It allows administrators to manage users, configure AI settings, and monitor the system.

## Package Info
- **Package**: `com.xyra.admin`
- **Min SDK**: 21 (Android 5.0)
- **Target SDK**: 34

## Project Structure
```
XyraAdmin/
├── AndroidManifest.xml
├── src/com/xyra/admin/
│   ├── LoginActivity.java      # Admin login with JWT
│   ├── DashboardActivity.java  # Stats overview & navigation
│   ├── UserManagementActivity.java  # User CRUD operations
│   ├── AIConfigActivity.java   # AI/API configuration
│   ├── SupabaseSettingsActivity.java  # Database settings
│   ├── ActivityLogsActivity.java  # Activity logs viewer
│   ├── AdminApiService.java    # HTTP client with auth
│   ├── AdminConfig.java        # Session management
│   └── UserAdapter.java        # User list adapter
├── res/
│   ├── layout/                 # UI layouts
│   ├── drawable/               # Icons & backgrounds
│   └── values/                 # Strings, colors, styles
```

## Features
- Admin authentication with JWT + refresh tokens
- Dashboard with user statistics
- User management (list, search, ban/unban, delete)
- AI configuration (API key, model, rate limits)
- Supabase settings management
- Activity logs viewer
- Dark theme matching XyraAI

## Backend Requirements
Requires a backend API (Vercel + Supabase Edge Functions) with endpoints:
- `/api/admin/login` - Admin authentication
- `/api/admin/refresh` - Token refresh
- `/api/admin/dashboard/stats` - Dashboard statistics
- `/api/admin/users` - User management
- `/api/admin/config/ai` - AI configuration
- `/api/admin/config/supabase` - Database settings
- `/api/admin/logs` - Activity logs

## How to Build
1. Copy XyraAdmin folder to Android Studio
2. Open as Android project
3. Sync Gradle and build APK
