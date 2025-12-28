# XTermux Android Application

## Overview

XTermux is an Android WebView-based application that wraps a web application (https://x-termux-tools.vercel.app/) into a native Android experience. The app is built using the Median/GoNative framework, which provides native mobile app functionality around web content including navigation controls, sidebar menus, tab bars, pull-to-refresh, offline support, and JavaScript bridge communication between the web content and native Android code.

## User Preferences

Preferred communication style: Simple, everyday language.

## System Architecture

### Core Architecture Pattern
- **WebView Wrapper**: The app uses Android WebView to render web content from a remote URL, with native UI components (action bars, sidebars, tabs) wrapped around it
- **Configuration-Driven**: App behavior is primarily controlled through `appConfig.json`, allowing customization of navigation, theming, and features without code changes
- **JavaScript Bridge**: Two-way communication between web content and native Android code via `GoNativeJSBridgeLibrary.js` and native bridge classes

### Build System
- **Gradle**: Uses Gradle 8.11.1 with Kotlin compilation
- **ProGuard**: Configured for release builds with specific keep rules for JavaScript interfaces and Google Play Services
- **Build Variants**: Supports "normal" build flavor with debug/release configurations

### Native Components
- **ActionManager**: Handles action bar items and custom actions (Kotlin)
- **ProfilePicker**: JavaScript bridge for profile selection functionality
- **MainActivity**: Main entry point with status checker bridge for web-to-native communication

### Theming System
- **Dynamic Theme Generation**: `generate-theme.js` Node.js script generates Android color resources
- **Light/Dark Mode Support**: Separate color files for `values/` and `values-night/` directories
- **Customizable Colors**: Action bar, status bar, sidebar, tab bar, and accent colors are all configurable

### Web Content Integration
- **Custom CSS/JS Injection**: `customCSS.css`, `customJS.js`, and Android-specific variants allow styling and behavior modifications
- **Blob Downloads**: `BlobDownloader.js` handles file downloads with session preservation
- **Offline Support**: Custom `offline.html` page with retry functionality and dark mode support

## External Dependencies

### Web Application
- **Primary URL**: https://x-termux-tools.vercel.app/ - The main web application content
- **Median Platform**: Uses Median (formerly GoNative) SDK for WebView wrapper functionality

### Build Dependencies
- **xml2js**: Node.js package for theme generation script (parsing/building XML color resources)
- **Kotlin**: Primary language for Android native code
- **Google Play Services**: Integrated for various Google APIs (see ProGuard rules)

### Platform Services
- **Median Device Registration**: Uses device registration key for app identification
- **Public Key Authentication**: App uses public key "dyyprea" for Median platform integration

### File Storage
- **File Writer/Sharer**: Native module for handling blob downloads and file sharing between web and native layers