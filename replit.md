# XTermux project documentation

## Project Overview
XTermux is an Android WebView wrapper for https://x-termux-tools.vercel.app/.

## Firebase Integration
1. Register Android app in Firebase Console with package name: `com.xtermux.app`
2. Use SHA-1: `11:7D:66:A1:45:AF:1B:AB:97:8E:9A:5F:CB:4A:49:2C:7C:1E:FB:8C`
3. Download `google-services.json` and place in `app/` directory.

## Build Requirements
The build environment requires `ANDROID_HOME` or `sdk.dir` in `local.properties`.
Current status: Identifying SDK path in Replit environment.