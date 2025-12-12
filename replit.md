# XyraAI - Android AI Chat Project

## Overview
This is an AIDE (Android IDE) project for an AI Chat application called XyraAI. It's designed to be opened and compiled using the AIDE app on Android devices. The project includes a Flask backend API for user authentication and cloud chat synchronization.

## Project Details
- **Package Name**: `com.xyra.ai`
- **App Name**: XyraAI
- **AI Backend**: GROQ API with Llama 3.3 70B model (with vision support)
- **Chat Sync Backend**: Flask + PostgreSQL (this Replit)
- **Target SDK**: Android 33
- **Minimum SDK**: Android 21 (5.0 Lollipop)

## Project Structure

```
Root/
├── app.py                     # Flask backend API
├── models.py                  # SQLAlchemy database models
├── main.py                    # Entry point for gunicorn
└── XyraAI/                    # Android AIDE project
    ├── AndroidManifest.xml
    ├── src/com/xyra/ai/       # Java source files
    │   ├── LoginActivity.java
    │   ├── MainActivity.java
    │   ├── ApiService.java    # Backend sync service
    │   ├── ChatHistory.java   # Local chat persistence
    │   └── ...
    └── res/                   # Android resources
```

## Backend API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user
- `POST /api/auth/logout` - Logout user

### Chat Sync
- `GET /api/chats` - Get all user chats
- `POST /api/chats` - Create new chat
- `DELETE /api/chats/<id>` - Delete a chat
- `GET /api/chats/<id>/messages` - Get messages for a chat
- `POST /api/chats/<id>/sync` - Sync messages to server

## Database Models
- **User**: id, username, email, password_hash
- **AuthToken**: token-based authentication with expiry
- **Chat**: id, user_id, title, preview, timestamps
- **Message**: chat_id, content, type, timestamp, image_base64

## How to Use

1. **Download the XyraAI folder** from this Replit
2. **Copy to your Android device** at `/sdcard/AppProjects/XyraAI/`
3. **Open in AIDE app**
4. **Add your GROQ API key** in `Config.java`
5. **Build and Run** from AIDE

## API Key Setup
The GROQ API key needs to be added to:
- `src/com/xyra/ai/Config.java` (GROQ_API_KEY constant)

## Features
- **Login Screen** with smooth animations
- **Cloud Chat Sync** - Chat history synced to server
- **Light Neumorphism/Soft UI theme** with clean white aesthetic
- Real-time AI responses using GROQ/Llama 3.3 70B
- Image analysis with actual image preview in messages
- Multi-chat system with sidebar drawer navigation
- Search functionality for finding old chats

## Recent Changes (December 2025)
- Created Flask backend API for chat synchronization
- Added user authentication (register, login, logout)
- Added cloud sync for chat history - survives logout/reinstall
- Database models: User, AuthToken, Chat, Message
- Updated Android ApiService.java to use Flask backend

## Environment Variables
- `DATABASE_URL` - PostgreSQL connection string
- `SESSION_SECRET` - Flask session secret key
