import os
import io
import zipfile
import uuid
import secrets
from datetime import datetime, timedelta
from functools import wraps

from flask import Flask, send_file, render_template_string, request, jsonify
from flask_sqlalchemy import SQLAlchemy
from sqlalchemy.orm import DeclarativeBase
from werkzeug.middleware.proxy_fix import ProxyFix
from werkzeug.security import generate_password_hash, check_password_hash

class Base(DeclarativeBase):
    pass

db = SQLAlchemy(model_class=Base)

app = Flask(__name__)
app.secret_key = os.environ.get("SESSION_SECRET", secrets.token_hex(32))
app.wsgi_app = ProxyFix(app.wsgi_app, x_proto=1, x_host=1)

app.config["SQLALCHEMY_DATABASE_URI"] = os.environ.get("DATABASE_URL")
app.config["SQLALCHEMY_ENGINE_OPTIONS"] = {
    "pool_recycle": 300,
    "pool_pre_ping": True,
}

db.init_app(app)

with app.app_context():
    import models
    db.create_all()

def token_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        token = None
        auth_header = request.headers.get('Authorization')
        
        if auth_header and auth_header.startswith('Bearer '):
            token = auth_header.split(' ')[1]
        
        if not token:
            return jsonify({'success': False, 'error': 'Token is missing'}), 401
        
        from models import AuthToken
        auth_token = AuthToken.query.filter_by(token=token).first()
        
        if not auth_token:
            return jsonify({'success': False, 'error': 'Invalid token'}), 401
        
        if auth_token.expires_at and auth_token.expires_at < datetime.utcnow():
            return jsonify({'success': False, 'error': 'Token expired'}), 401
        
        request.current_user = auth_token.user
        return f(*args, **kwargs)
    return decorated

HTML_TEMPLATE = '''
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>XyraAI - Android AI Chat Project</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: -apple-system, BlinkMacSystemFont, sans-serif; background: #F2F2F7; min-height: 100vh; }
        .container { max-width: 680px; margin: 0 auto; padding: 20px; }
        .header { text-align: center; padding: 50px 20px 40px; }
        .logo { width: 100px; height: 100px; background: linear-gradient(135deg, #007AFF, #5856D6); border-radius: 22px; display: flex; align-items: center; justify-content: center; margin: 0 auto 20px; font-size: 48px; box-shadow: 0 10px 30px rgba(0,122,255,0.3); }
        h1 { font-size: 2.2rem; font-weight: 700; margin-bottom: 8px; }
        .subtitle { color: #8E8E93; }
        .card { background: #fff; border-radius: 16px; margin-bottom: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.08); overflow: hidden; }
        .download-section { text-align: center; padding: 32px 24px; }
        .download-section p { color: #8E8E93; margin-bottom: 20px; }
        .download-btn { display: inline-flex; align-items: center; gap: 10px; background: linear-gradient(135deg, #007AFF, #5856D6); color: #fff; padding: 14px 28px; border-radius: 14px; text-decoration: none; font-weight: 600; }
        .card-header { padding: 16px 20px; border-bottom: 1px solid #E5E5EA; }
        .card-header h2 { font-size: 0.8rem; font-weight: 600; color: #8E8E93; text-transform: uppercase; }
        .list-item { display: flex; align-items: center; padding: 14px 20px; border-bottom: 0.5px solid #E5E5EA; }
        .list-item:last-child { border-bottom: none; }
        .icon { width: 44px; height: 44px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 22px; margin-right: 14px; }
        .icon.blue { background: rgba(0,122,255,0.12); }
        .icon.green { background: rgba(52,199,89,0.12); }
        .icon.purple { background: rgba(175,82,222,0.12); }
        .icon.orange { background: rgba(255,149,0,0.12); }
        .text h3 { font-size: 1rem; font-weight: 500; margin-bottom: 2px; }
        .text p { font-size: 0.85rem; color: #8E8E93; }
        .footer { text-align: center; padding: 30px 20px 40px; color: #8E8E93; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <div class="logo">🤖</div>
            <h1>XyraAI</h1>
            <p class="subtitle">Android AI Chat Application for AIDE</p>
        </div>
        <div class="card">
            <div class="download-section">
                <p>Download the complete AIDE project as a ZIP file</p>
                <a href="/download" class="download-btn">Download XyraAI.zip</a>
            </div>
        </div>
        <div class="card">
            <div class="card-header"><h2>Features</h2></div>
            <div class="list-item"><div class="icon blue">🎨</div><div class="text"><h3>Modern UI</h3><p>Beautiful dark theme with gradient accents</p></div></div>
            <div class="list-item"><div class="icon green">🤖</div><div class="text"><h3>GROQ AI</h3><p>Powered by Llama 3.3 70B model</p></div></div>
            <div class="list-item"><div class="icon purple">💬</div><div class="text"><h3>Cloud Sync</h3><p>Chat history synced across devices</p></div></div>
            <div class="list-item"><div class="icon orange">🔐</div><div class="text"><h3>User Auth</h3><p>Login/Register to save your chats</p></div></div>
        </div>
        <div class="footer"><p>Built with Flask + PostgreSQL</p></div>
    </div>
</body>
</html>
'''

@app.route('/')
def index():
    return render_template_string(HTML_TEMPLATE)

@app.route('/download')
def download():
    memory_file = io.BytesIO()
    
    with zipfile.ZipFile(memory_file, 'w', zipfile.ZIP_DEFLATED) as zf:
        for root, dirs, files in os.walk('XyraAI'):
            for file in files:
                file_path = os.path.join(root, file)
                arcname = file_path
                zf.write(file_path, arcname)
    
    memory_file.seek(0)
    
    return send_file(
        memory_file,
        mimetype='application/zip',
        as_attachment=True,
        download_name='XyraAI.zip'
    )

@app.route('/api/auth/register', methods=['POST'])
def register():
    from models import User, AuthToken
    
    data = request.get_json()
    
    if not data:
        return jsonify({'success': False, 'error': 'No data provided'}), 400
    
    username = data.get('username')
    email = data.get('email')
    password = data.get('password')
    
    if not username or not email or not password:
        return jsonify({'success': False, 'error': 'Missing required fields'}), 400
    
    if User.query.filter_by(email=email).first():
        return jsonify({'success': False, 'error': 'Email already registered'}), 400
    
    if User.query.filter_by(username=username).first():
        return jsonify({'success': False, 'error': 'Username already taken'}), 400
    
    user = User(
        username=username,
        email=email,
        password_hash=generate_password_hash(password)
    )
    db.session.add(user)
    db.session.flush()
    
    token = secrets.token_hex(32)
    auth_token = AuthToken(
        token=token,
        user_id=user.id,
        expires_at=datetime.utcnow() + timedelta(days=30)
    )
    db.session.add(auth_token)
    db.session.commit()
    
    return jsonify({
        'success': True,
        'data': {
            'user_id': user.id,
            'username': user.username,
            'email': user.email,
            'token': token
        }
    })

@app.route('/api/auth/login', methods=['POST'])
def login():
    from models import User, AuthToken
    
    data = request.get_json()
    
    if not data:
        return jsonify({'success': False, 'error': 'No data provided'}), 400
    
    email = data.get('email')
    password = data.get('password')
    
    if not email or not password:
        return jsonify({'success': False, 'error': 'Missing email or password'}), 400
    
    user = User.query.filter_by(email=email).first()
    
    if not user or not check_password_hash(user.password_hash, password):
        return jsonify({'success': False, 'error': 'Invalid email or password'}), 401
    
    token = secrets.token_hex(32)
    auth_token = AuthToken(
        token=token,
        user_id=user.id,
        expires_at=datetime.utcnow() + timedelta(days=30)
    )
    db.session.add(auth_token)
    db.session.commit()
    
    return jsonify({
        'success': True,
        'data': {
            'user_id': user.id,
            'username': user.username,
            'email': user.email,
            'token': token
        }
    })

@app.route('/api/auth/logout', methods=['POST'])
@token_required
def logout():
    from models import AuthToken
    
    token = request.headers.get('Authorization').split(' ')[1]
    auth_token = AuthToken.query.filter_by(token=token).first()
    
    if auth_token:
        db.session.delete(auth_token)
        db.session.commit()
    
    return jsonify({'success': True})

@app.route('/api/chats', methods=['GET'])
@token_required
def get_chats():
    from models import Chat
    
    chats = Chat.query.filter_by(user_id=request.current_user.id).order_by(Chat.updated_at.desc()).all()
    
    chat_list = []
    for chat in chats:
        chat_list.append({
            'id': chat.id,
            'title': chat.title,
            'preview': chat.preview,
            'timestamp': int(chat.updated_at.timestamp() * 1000),
            'messageCount': len(chat.messages)
        })
    
    return jsonify({'success': True, 'data': chat_list})

@app.route('/api/chats', methods=['POST'])
@token_required
def create_chat():
    from models import Chat
    
    data = request.get_json() or {}
    title = data.get('title', 'Chat baru')
    
    chat = Chat(
        user_id=request.current_user.id,
        title=title,
        preview=title
    )
    db.session.add(chat)
    db.session.commit()
    
    return jsonify({
        'success': True,
        'data': {
            'id': chat.id,
            'title': chat.title
        }
    })

@app.route('/api/chats/<chat_id>', methods=['DELETE'])
@token_required
def delete_chat(chat_id):
    from models import Chat
    
    chat = Chat.query.filter_by(id=chat_id, user_id=request.current_user.id).first()
    
    if not chat:
        return jsonify({'success': False, 'error': 'Chat not found'}), 404
    
    db.session.delete(chat)
    db.session.commit()
    
    return jsonify({'success': True})

@app.route('/api/chats/<chat_id>/messages', methods=['GET'])
@token_required
def get_messages(chat_id):
    from models import Chat, Message
    
    chat = Chat.query.filter_by(id=chat_id, user_id=request.current_user.id).first()
    
    if not chat:
        return jsonify({'success': False, 'error': 'Chat not found'}), 404
    
    messages = Message.query.filter_by(chat_id=chat_id).order_by(Message.timestamp).all()
    
    message_list = []
    for msg in messages:
        msg_data = {
            'content': msg.content,
            'type': msg.message_type,
            'timestamp': msg.timestamp
        }
        if msg.image_base64:
            msg_data['image_base64'] = msg.image_base64
        message_list.append(msg_data)
    
    return jsonify({'success': True, 'data': message_list})

@app.route('/api/chats/<chat_id>/sync', methods=['POST'])
@token_required
def sync_messages(chat_id):
    from models import Chat, Message
    
    existing_chat = Chat.query.filter_by(id=chat_id).first()
    
    if existing_chat:
        if existing_chat.user_id != request.current_user.id:
            return jsonify({'success': False, 'error': 'Chat not found'}), 404
        chat = existing_chat
    else:
        chat = Chat(
            id=chat_id,
            user_id=request.current_user.id,
            title='Chat baru'
        )
        db.session.add(chat)
    
    data = request.get_json()
    messages = data.get('messages', [])
    
    Message.query.filter_by(chat_id=chat_id).delete()
    
    for msg in messages:
        message = Message(
            chat_id=chat_id,
            content=msg.get('content', ''),
            message_type=msg.get('type', 0),
            timestamp=msg.get('timestamp', int(datetime.utcnow().timestamp() * 1000)),
            image_base64=msg.get('image_base64')
        )
        db.session.add(message)
    
    if messages:
        last_user_msg = None
        for msg in reversed(messages):
            if msg.get('type') == 0:
                last_user_msg = msg.get('content', '')
                break
        
        if last_user_msg:
            preview = last_user_msg
            if preview.startswith('[Gambar] '):
                preview = preview[9:]
            if len(preview) > 50:
                preview = preview[:47] + '...'
            chat.preview = preview
        
        chat.updated_at = datetime.utcnow()
    
    db.session.commit()
    
    return jsonify({'success': True})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
