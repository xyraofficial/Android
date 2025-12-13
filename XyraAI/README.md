# XyraAI - AI Chat Application for AIDE

An Android AI Chat application built for AIDE (Android IDE) that uses GROQ API with Llama 3.3 70B model.

## Features

- Modern dark theme UI with gradient accents
- Real-time AI chat using GROQ API
- Chat message history with cloud sync
- Voice input (Speech-to-Text)
- Text-to-Speech
- Markdown rendering
- Code syntax highlighting
- User authentication with Supabase
- Multiple AI personas
- Bookmarks
- Beautiful message bubbles with timestamps
- Network status indicator
- Conversation context memory

## Quick Fix for Build Errors

Jika Anda mendapat error saat build, ikuti langkah berikut:

### 1. Error "Unknown entity 'R'" atau "No resource found"

Pastikan semua file drawable ada:
- `bg_persona_icon.xml`
- `bg_dialog.xml`
- `bg_pulse_circle.xml`
- `bg_mic_button.xml`

### 2. Error "Unknown entity 'NotificationCompat'" atau "Unknown type or package 'core'"

Anda perlu menambahkan AndroidX Core library:

1. Download AndroidX Core library dari:
   - https://mvnrepository.com/artifact/androidx.core/core
   - Pilih versi terbaru, download file `.aar`

2. Extract file `.aar` dan ambil `classes.jar`

3. Rename menjadi `androidx-core.jar`

4. Copy ke folder `XyraAI/libs/`

5. Di AIDE, tambahkan library:
   - Menu → Project → Add Library
   - Pilih file `androidx-core.jar`

### 3. Error "This variable must be final to be used in local class"

Ini sudah diperbaiki di kode. Jika masih muncul, pastikan Anda menggunakan versi terbaru file.

## Project Structure

```
XyraAI/
├── AndroidManifest.xml          # App configuration & permissions
├── project.properties           # Android SDK target
├── libs/                        # External JAR libraries
├── src/
│   └── com/xyra/ai/
│       ├── LoginActivity.java   # Login/Register screen
│       ├── MainActivity.java    # Main chat activity
│       ├── SettingsActivity.java # Settings screen
│       ├── InfoActivity.java    # App info
│       ├── ProfileActivity.java # User profile
│       ├── PersonasActivity.java # AI personas
│       ├── BookmarksActivity.java # Bookmarks
│       ├── ChatAdapter.java     # RecyclerView adapter
│       ├── Message.java         # Message model
│       ├── GroqApiService.java  # GROQ API integration
│       ├── SupabaseService.java # Supabase auth & sync
│       ├── STTService.java      # Speech-to-Text
│       ├── TTSService.java      # Text-to-Speech
│       ├── NotificationService.java # Daily notifications
│       ├── Config.java          # App configuration
│       ├── NetworkUtils.java    # Network utilities
│       ├── ChatHistory.java     # Chat persistence
│       ├── ThemeManager.java    # Theme management
│       └── ...
└── res/
    ├── layout/                  # All layout files
    ├── values/                  # Strings, colors, styles
    └── drawable/                # Icons and backgrounds
```

## Setup Instructions

### 1. Copy Project to Device

Copy the entire `XyraAI` folder to your Android device's storage:
- `/sdcard/AppProjects/XyraAI/`

### 2. Open in AIDE

1. Open AIDE app on your Android device
2. Navigate to the XyraAI folder
3. Open `MainActivity.java`

### 3. Add Your API Key

Edit `src/com/xyra/ai/Config.java` and add your GROQ API key:
```java
public static final String GROQ_API_KEY = "gsk_your_actual_key_here";
```

### 4. Build and Run

1. In AIDE, press Menu → Run
2. AIDE will compile the project and generate APK
3. Install the APK on your device
4. Start chatting with XyraAI!

## API Information

This app uses the GROQ API with the following configuration:

- **Endpoint**: `https://api.groq.com/openai/v1/chat/completions`
- **Model**: `llama-3.3-70b-versatile` (default)
- **Available Models**:
  - Llama 3.3 70B
  - Llama 4 Scout (Vision)
  - Mixtral 8x7B

## Requirements

- Android 5.0 (API 21) or higher
- AIDE - Android IDE app
- Internet connection
- GROQ API key (get from https://console.groq.com)

## Permissions

- `INTERNET` - For API calls
- `ACCESS_NETWORK_STATE` - For network status checking
- `CAMERA` - For image analysis
- `RECORD_AUDIO` - For voice input
- `READ/WRITE_EXTERNAL_STORAGE` - For file operations
- `VIBRATE` - For haptic feedback

## Troubleshooting

### "Network Error"
- Check your internet connection
- Verify your API key is correct
- Make sure GROQ API is accessible

### Build Errors
- Ensure all files are in correct locations
- Check for syntax errors in Java files
- Verify AndroidManifest.xml is valid
- Add AndroidX Core library if NotificationCompat errors appear

### "Unknown entity 'R'"
- This usually means a resource file is missing or has errors
- Check all drawable XML files for syntax errors
- Rebuild the project (Clean & Build)

## License

MIT License - Feel free to use and modify!

## Credits

- GROQ API for AI inference
- Llama 3.3 model by Meta
- Supabase for authentication
- Built with AIDE for Android
