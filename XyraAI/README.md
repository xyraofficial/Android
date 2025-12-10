# XyraAI - AI Chat Application for AIDE

An Android AI Chat application built for AIDE (Android IDE) that uses GROQ API with Llama 3.3 70B model.

## Features

- Modern dark theme UI with gradient accents
- Real-time AI chat using GROQ API
- Chat message history
- Beautiful message bubbles with timestamps
- Network status indicator
- Conversation context memory

## Project Structure

```
XyraAI/
├── AndroidManifest.xml          # App configuration & permissions
├── .classpath                   # Eclipse/AIDE project file
├── .project                     # Project metadata
├── project.properties           # Android SDK target
├── libs/                        # External JAR libraries (if needed)
├── src/
│   └── com/xyra/ai/
│       ├── MainActivity.java    # Main chat activity
│       ├── ChatAdapter.java     # RecyclerView adapter
│       ├── Message.java         # Message model
│       ├── GroqApiService.java  # GROQ API integration
│       ├── Config.java          # App configuration
│       ├── NetworkUtils.java    # Network utilities
│       └── ChatHistory.java     # Chat persistence
└── res/
    ├── layout/
    │   ├── activity_main.xml    # Main layout
    │   ├── item_message_user.xml
    │   └── item_message_ai.xml
    ├── values/
    │   ├── strings.xml          # String resources
    │   ├── colors.xml           # Color definitions
    │   └── styles.xml           # App theme
    └── drawable/
        ├── ic_launcher.xml      # App icon
        ├── ic_send.xml          # Send button icon
        ├── bg_input.xml         # Input field background
        ├── bg_send_button.xml   # Send button background
        ├── bg_bubble_user.xml   # User message bubble
        └── bg_bubble_ai.xml     # AI message bubble
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

Edit `src/com/xyra/ai/MainActivity.java` and replace:
```java
private static final String API_KEY = "YOUR_GROQ_API_KEY_HERE";
```

With your actual GROQ API key:
```java
private static final String API_KEY = "gsk_your_actual_key_here";
```

Also update `src/com/xyra/ai/Config.java` with your key.

### 4. Build and Run

1. In AIDE, press Menu → Run
2. AIDE will compile the project and generate APK
3. Install the APK on your device
4. Start chatting with XyraAI!

## API Information

This app uses the GROQ API with the following configuration:

- **Endpoint**: `https://api.groq.com/openai/v1/chat/completions`
- **Model**: `llama-3.3-70b-versatile`
- **Temperature**: 0.7
- **Max Tokens**: 2048

## Requirements

- Android 5.0 (API 21) or higher
- AIDE - Android IDE app
- Internet connection
- GROQ API key (get from https://console.groq.com)

## Permissions

- `INTERNET` - For API calls
- `ACCESS_NETWORK_STATE` - For network status checking

## Customization

### Change Colors
Edit `res/values/colors.xml` to customize the app theme.

### Change AI Personality
Edit the system prompt in `GroqApiService.java`:
```java
systemMessage.put("content", "You are Xyra, a helpful...");
```

### Change AI Model
Edit `GroqApiService.java` or `Config.java`:
```java
private static final String MODEL = "llama-3.3-70b-versatile";
```

Available models:
- `llama-3.3-70b-versatile`
- `llama3-70b-8192`
- `mixtral-8x7b-32768`
- `llama-3.1-8b-instant`

## Troubleshooting

### "Network Error"
- Check your internet connection
- Verify your API key is correct
- Make sure GROQ API is accessible

### Build Errors
- Ensure all files are in correct locations
- Check for syntax errors in Java files
- Verify AndroidManifest.xml is valid

## License

MIT License - Feel free to use and modify!

## Credits

- GROQ API for AI inference
- Llama 3.3 model by Meta
- Built with AIDE for Android
