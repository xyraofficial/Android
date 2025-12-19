# Xyra Termux WebView App

A simple Android WebView application that opens https://xyra-termux.vercel.app/

## Features
- WebView-based application
- Loads remote web content from Vercel
- Back button navigation support
- JavaScript enabled for full web functionality

## Building with AIDE

1. Open the `AIDE/XyraTermux` folder in AIDE (Android IDE)
2. AIDE will automatically detect and configure the project
3. Build the APK using AIDE's Build menu
4. Install on your Android device

## Project Structure
```
AIDE/XyraTermux/
├── src/main/
│   ├── java/com/xyra/termux/
│   │   └── MainActivity.java       # WebView Activity
│   ├── res/
│   │   ├── layout/
│   │   │   └── activity_main.xml   # Main layout
│   │   └── values/
│   │       ├── strings.xml         # App strings
│   │       └── styles.xml          # App themes
│   └── AndroidManifest.xml         # App manifest
├── build.gradle                    # Module build config
├── settings.gradle                 # Project settings
└── proguard-rules.pro             # Obfuscation rules
```

## Requirements
- Android API Level 21 (Android 5.0) or higher
- AndroidX libraries
- Internet permission

## Permissions
- `INTERNET` - To load web content from Vercel
- `ACCESS_NETWORK_STATE` - To check network connectivity
