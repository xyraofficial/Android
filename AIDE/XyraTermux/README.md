# Xyra Termux WebView App

A simple Android WebView application that opens https://xyra-termux.vercel.app/

## Features
- WebView-based application
- Loads remote web content
- Back button navigation support
- JavaScript enabled

## Building with AIDE

1. Open this folder in AIDE (Android IDE)
2. Project should auto-configure
3. Build APK using AIDE's build menu
4. Install on your Android device

## Build Requirements
- Android SDK 21+
- AndroidX libraries

## Structure
```
├── src/
│   └── MainActivity.java       # Main WebView activity
├── res/
│   ├── layout/
│   │   └── activity_main.xml   # Main layout with WebView
│   └── values/
│       ├── strings.xml         # App strings
│       └── styles.xml          # App styles
├── AndroidManifest.xml         # App manifest
├── build.gradle               # Gradle build config
└── proguard-rules.pro         # ProGuard rules
```

## Permissions
- INTERNET - To load web content
- ACCESS_NETWORK_STATE - To check network connectivity
