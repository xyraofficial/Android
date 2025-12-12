from flask import Flask, send_file, render_template_string, request, jsonify
from supabase import create_client, Client
from functools import wraps
import os
import zipfile
import io
import bcrypt
import uuid
from datetime import datetime

app = Flask(__name__)

SUPABASE_URL = os.environ.get('SUPABASE_URL')
SUPABASE_KEY = os.environ.get('SUPABASE_KEY')

supabase: Client = None
supabase_error = None
if SUPABASE_URL and SUPABASE_KEY:
    try:
        supabase = create_client(SUPABASE_URL, SUPABASE_KEY)
    except Exception as e:
        supabase_error = str(e)

def hash_password(password):
    return bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt()).decode('utf-8')

def verify_password(password, hashed):
    return bcrypt.checkpw(password.encode('utf-8'), hashed.encode('utf-8'))

def require_auth(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        auth_header = request.headers.get('Authorization')
        if not auth_header or not auth_header.startswith('Bearer '):
            return jsonify({"success": False, "error": "Missing or invalid token"}), 401
        
        token = auth_header.split(' ')[1]
        
        try:
            result = supabase.table('users').select('*').eq('token', token).execute()
            if not result.data:
                return jsonify({"success": False, "error": "Invalid token"}), 401
            request.user = result.data[0]
        except Exception as e:
            return jsonify({"success": False, "error": str(e)}), 401
        
        return f(*args, **kwargs)
    return decorated

@app.route('/api/auth/register', methods=['POST'])
def register():
    if not supabase:
        return jsonify({"success": False, "error": "Database not configured"}), 500
    
    try:
        data = request.get_json()
        username = data.get('username')
        email = data.get('email')
        password = data.get('password')
        
        if not username or not email or not password:
            return jsonify({"success": False, "error": "Missing required fields"}), 400
        
        existing = supabase.table('users').select('id').eq('email', email).execute()
        if existing.data:
            return jsonify({"success": False, "error": "Email already registered"}), 400
        
        existing_username = supabase.table('users').select('id').eq('username', username).execute()
        if existing_username.data:
            return jsonify({"success": False, "error": "Username already taken"}), 400
        
        token = str(uuid.uuid4())
        user_id = str(uuid.uuid4())
        
        result = supabase.table('users').insert({
            "id": user_id,
            "username": username,
            "email": email,
            "password_hash": hash_password(password),
            "token": token,
            "created_at": datetime.utcnow().isoformat()
        }).execute()
        
        return jsonify({
            "success": True,
            "data": {
                "user_id": user_id,
                "username": username,
                "email": email,
                "token": token
            }
        }), 201
        
    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500

@app.route('/api/auth/login', methods=['POST'])
def login():
    if not supabase:
        return jsonify({"success": False, "error": "Database not configured"}), 500
    
    try:
        data = request.get_json()
        email = data.get('email')
        password = data.get('password')
        
        if not email or not password:
            return jsonify({"success": False, "error": "Missing email or password"}), 400
        
        result = supabase.table('users').select('*').eq('email', email).execute()
        
        if not result.data:
            return jsonify({"success": False, "error": "Invalid email or password"}), 401
        
        user = result.data[0]
        
        if not verify_password(password, user['password_hash']):
            return jsonify({"success": False, "error": "Invalid email or password"}), 401
        
        new_token = str(uuid.uuid4())
        supabase.table('users').update({"token": new_token}).eq('id', user['id']).execute()
        
        return jsonify({
            "success": True,
            "data": {
                "user_id": user['id'],
                "username": user['username'],
                "email": user['email'],
                "token": new_token
            }
        })
        
    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500

@app.route('/api/auth/me', methods=['GET'])
@require_auth
def get_me():
    user = request.user
    return jsonify({
        "success": True,
        "data": {
            "user_id": user['id'],
            "username": user['username'],
            "email": user['email']
        }
    })

@app.route('/api/auth/logout', methods=['POST'])
@require_auth
def logout():
    try:
        user = request.user
        supabase.table('users').update({"token": None}).eq('id', user['id']).execute()
        return jsonify({"success": True, "message": "Logged out successfully"})
    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500

@app.route('/api/chats', methods=['GET'])
@require_auth
def get_chats():
    try:
        user = request.user
        result = supabase.table('chats').select('*').eq('user_id', user['id']).order('updated_at', desc=True).execute()
        return jsonify({"success": True, "data": result.data})
    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500

@app.route('/api/chats', methods=['POST'])
@require_auth
def create_chat():
    try:
        user = request.user
        data = request.get_json() or {}
        
        chat_id = str(uuid.uuid4())
        now = datetime.utcnow().isoformat()
        
        result = supabase.table('chats').insert({
            "id": chat_id,
            "user_id": user['id'],
            "title": data.get('title', 'Chat baru'),
            "preview": data.get('preview', ''),
            "created_at": now,
            "updated_at": now
        }).execute()
        
        return jsonify({"success": True, "data": result.data[0]}), 201
    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500

@app.route('/api/chats/<chat_id>', methods=['DELETE'])
@require_auth
def delete_chat(chat_id):
    try:
        user = request.user
        
        supabase.table('messages').delete().eq('chat_id', chat_id).execute()
        supabase.table('chats').delete().eq('id', chat_id).eq('user_id', user['id']).execute()
        
        return jsonify({"success": True, "message": "Chat deleted"})
    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500

@app.route('/api/chats/<chat_id>/messages', methods=['GET'])
@require_auth
def get_messages(chat_id):
    try:
        user = request.user
        
        chat = supabase.table('chats').select('id').eq('id', chat_id).eq('user_id', user['id']).execute()
        if not chat.data:
            return jsonify({"success": False, "error": "Chat not found"}), 404
        
        result = supabase.table('messages').select('*').eq('chat_id', chat_id).order('timestamp', desc=False).execute()
        return jsonify({"success": True, "data": result.data})
    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500

@app.route('/api/chats/<chat_id>/messages', methods=['POST'])
@require_auth
def save_message(chat_id):
    try:
        user = request.user
        data = request.get_json()
        
        chat = supabase.table('chats').select('id').eq('id', chat_id).eq('user_id', user['id']).execute()
        if not chat.data:
            return jsonify({"success": False, "error": "Chat not found"}), 404
        
        message_id = str(uuid.uuid4())
        now = datetime.utcnow().isoformat()
        
        message_data = {
            "id": message_id,
            "chat_id": chat_id,
            "content": data.get('content', ''),
            "type": data.get('type', 0),
            "timestamp": data.get('timestamp', int(datetime.utcnow().timestamp() * 1000)),
            "image_base64": data.get('image_base64'),
            "created_at": now
        }
        
        result = supabase.table('messages').insert(message_data).execute()
        
        preview = data.get('content', '')[:50]
        if len(data.get('content', '')) > 50:
            preview += '...'
        
        supabase.table('chats').update({
            "preview": preview,
            "updated_at": now
        }).eq('id', chat_id).execute()
        
        return jsonify({"success": True, "data": result.data[0]}), 201
    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500

@app.route('/api/chats/<chat_id>/sync', methods=['POST'])
@require_auth
def sync_messages(chat_id):
    try:
        user = request.user
        data = request.get_json()
        messages = data.get('messages', [])
        
        chat = supabase.table('chats').select('id').eq('id', chat_id).eq('user_id', user['id']).execute()
        
        if not chat.data:
            now = datetime.utcnow().isoformat()
            supabase.table('chats').insert({
                "id": chat_id,
                "user_id": user['id'],
                "title": "Synced Chat",
                "preview": "",
                "created_at": now,
                "updated_at": now
            }).execute()
        
        supabase.table('messages').delete().eq('chat_id', chat_id).execute()
        
        if messages:
            for msg in messages:
                supabase.table('messages').insert({
                    "id": msg.get('id', str(uuid.uuid4())),
                    "chat_id": chat_id,
                    "content": msg.get('content', ''),
                    "type": msg.get('type', 0),
                    "timestamp": msg.get('timestamp', int(datetime.utcnow().timestamp() * 1000)),
                    "image_base64": msg.get('image_base64'),
                    "created_at": datetime.utcnow().isoformat()
                }).execute()
            
            last_user_msg = None
            for msg in reversed(messages):
                if msg.get('type') == 0:
                    last_user_msg = msg.get('content', '')[:50]
                    break
            
            if last_user_msg:
                supabase.table('chats').update({
                    "preview": last_user_msg,
                    "updated_at": datetime.utcnow().isoformat()
                }).eq('id', chat_id).execute()
        
        return jsonify({"success": True, "message": "Messages synced", "count": len(messages)})
    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500

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
        .api-info { background: rgba(52,199,89,0.1); padding: 16px; margin: 16px; border-radius: 12px; }
        .api-info h3 { color: #34C759; margin-bottom: 8px; }
        .api-info code { background: #1C1C1E; color: #fff; padding: 8px 12px; border-radius: 8px; display: block; margin: 8px 0; font-size: 0.85rem; overflow-x: auto; }
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
                <a href="/api/download" class="download-btn">Download XyraAI.zip</a>
            </div>
        </div>
        <div class="card">
            <div class="card-header"><h2>Features</h2></div>
            <div class="list-item"><div class="icon blue">🎨</div><div class="text"><h3>Modern UI</h3><p>Beautiful dark theme with gradient accents</p></div></div>
            <div class="list-item"><div class="icon green">🤖</div><div class="text"><h3>GROQ AI</h3><p>Powered by Llama 3.3 70B model</p></div></div>
            <div class="list-item"><div class="icon purple">💬</div><div class="text"><h3>Cloud Sync</h3><p>Chat history synced across devices</p></div></div>
            <div class="list-item"><div class="icon orange">🔐</div><div class="text"><h3>User Auth</h3><p>Login/Register to save your chats</p></div></div>
        </div>
        <div class="card">
            <div class="card-header"><h2>API Endpoints</h2></div>
            <div class="api-info">
                <h3>Authentication</h3>
                <code>POST /api/auth/register</code>
                <code>POST /api/auth/login</code>
                <code>GET /api/auth/me</code>
                <code>POST /api/auth/logout</code>
                <h3>Chat Sync</h3>
                <code>GET /api/chats</code>
                <code>POST /api/chats</code>
                <code>GET /api/chats/{id}/messages</code>
                <code>POST /api/chats/{id}/sync</code>
            </div>
        </div>
        <div class="footer"><p>Built with Supabase + Vercel</p></div>
    </div>
</body>
</html>
'''

@app.route('/')
@app.route('/api')
def index():
    return render_template_string(HTML_TEMPLATE)

@app.route('/api/download')
def download():
    memory_file = io.BytesIO()
    xyra_path = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'XyraAI')
    
    if not os.path.exists(xyra_path):
        return "XyraAI folder not found", 404
    
    with zipfile.ZipFile(memory_file, 'w', zipfile.ZIP_DEFLATED) as zf:
        for root, dirs, files in os.walk(xyra_path):
            for file in files:
                file_path = os.path.join(root, file)
                arcname = os.path.relpath(file_path, os.path.dirname(xyra_path))
                zf.write(file_path, arcname)
    
    memory_file.seek(0)
    return send_file(memory_file, mimetype='application/zip', as_attachment=True, download_name='XyraAI.zip')

@app.route('/api/health')
def health():
    status_info = {
        "status": "ok",
        "supabase": "connected" if supabase else "not configured",
        "supabase_url_set": bool(SUPABASE_URL),
        "supabase_key_set": bool(SUPABASE_KEY)
    }
    if supabase_error:
        status_info["supabase_error"] = supabase_error
    return jsonify(status_info)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
