# Child Android App - Parent Control

## Overview
Android application for parental monitoring. Collects device data with explicit user consent.

## Features
- Location tracking
- Contact list sync
- SMS log collection
- Gallery metadata sync
- Background service with WorkManager
- Boot persistence
- Encrypted API transmission

## Permissions Required
- ACCESS_FINE_LOCATION
- ACCESS_COARSE_LOCATION
- READ_CONTACTS
- READ_SMS
- READ_EXTERNAL_STORAGE

## Legal Requirements
- Explicit user consent dialog before data collection
- Clear disclosure of data types collected
- User must acknowledge monitoring purpose

## Build Instructions
1. Open in Android Studio
2. Configure API URL in Config.java
3. Build APK: Build > Build Bundle(s) / APK(s) > Build APK(s)

## Configuration
Edit `Config.java` to set your API server URL before building.
