from flask import Flask, send_file, render_template_string, request, jsonify
import os
import zipfile
import io

app = Flask(__name__)

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
        <div class="footer"><p>Built with Supabase + Vercel</p></div>
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

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
