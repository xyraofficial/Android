# Parent Control System

## Overview
A parental monitoring system with three components:
1. **Api/** - Flask backend API for data synchronization
2. **ChildAndroid/** - Android app for data collection from child devices
3. **Termux/** - Python CLI script for viewing collected data

## Project Structure
```
├── Api/                    # Flask Backend API (Vercel deployable)
│   ├── app.py             # Main API application
│   ├── requirements.txt   # Python dependencies
│   ├── vercel.json        # Vercel deployment config
│   └── README.md          # API documentation
│
├── ChildAndroid/          # Android Application
│   ├── app/
│   │   └── src/main/
│   │       ├── java/com/parentcontrol/child/
│   │       │   ├── MainActivity.java
│   │       │   ├── DataSyncService.java
│   │       │   ├── Config.java
│   │       │   └── BootReceiver.java
│   │       ├── res/
│   │       └── AndroidManifest.xml
│   ├── build.gradle
│   └── README.md
│
├── Termux/                # CLI Control Script
│   ├── control_menu.py   # Main menu application
│   ├── requirements.txt  # Python dependencies
│   └── README.md
│
└── server.py             # Local test server (Firebase auth)
```

## API Endpoints
- `POST /api/auth/register` - Register device
- `POST /api/data/location` - Upload location
- `POST /api/data/contacts` - Upload contacts
- `POST /api/data/sms` - Upload SMS
- `POST /api/data/gallery` - Upload gallery metadata
- `GET /api/fetch/all` - Fetch all data

## Setup Instructions

### API (Vercel)
1. Push Api/ folder to GitHub
2. Import in Vercel dashboard
3. Deploy

### Android App
1. Open ChildAndroid/ in Android Studio
2. Update Config.java with API URL
3. Build APK

### Termux Client
```bash
cd Termux
pip install -r requirements.txt
export API_URL="https://your-api.vercel.app"
export API_TOKEN="your-token"
python control_menu.py
```

## Security Features
- API token authentication
- Explicit user consent in Android app
- Encrypted HTTPS transmission
- Foreground service notifications
