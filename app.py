from flask import Flask, send_file, render_template_string
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
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
            background: linear-gradient(135deg, #0F0F23 0%, #1A1A2E 50%, #16213E 100%);
            min-height: 100vh;
            color: #fff;
            padding: 20px;
        }
        .container {
            max-width: 900px;
            margin: 0 auto;
        }
        .header {
            text-align: center;
            padding: 40px 20px;
        }
        .logo {
            width: 100px;
            height: 100px;
            background: linear-gradient(135deg, #6366F1, #8B5CF6);
            border-radius: 24px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 20px;
            font-size: 48px;
        }
        h1 {
            font-size: 2.5rem;
            background: linear-gradient(135deg, #6366F1, #8B5CF6, #A855F7);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            margin-bottom: 10px;
        }
        .subtitle {
            color: #A0AEC0;
            font-size: 1.1rem;
        }
        .card {
            background: rgba(255, 255, 255, 0.05);
            border: 1px solid rgba(255, 255, 255, 0.1);
            border-radius: 16px;
            padding: 24px;
            margin-bottom: 20px;
        }
        .card h2 {
            color: #8B5CF6;
            margin-bottom: 16px;
            font-size: 1.3rem;
        }
        .download-btn {
            display: inline-block;
            background: linear-gradient(135deg, #6366F1, #8B5CF6);
            color: #fff;
            padding: 16px 32px;
            border-radius: 12px;
            text-decoration: none;
            font-weight: 600;
            font-size: 1.1rem;
            transition: transform 0.2s, box-shadow 0.2s;
        }
        .download-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 40px rgba(99, 102, 241, 0.3);
        }
        .features {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 16px;
        }
        .feature {
            background: rgba(99, 102, 241, 0.1);
            border-radius: 12px;
            padding: 16px;
        }
        .feature h3 {
            color: #A855F7;
            margin-bottom: 8px;
        }
        .feature p {
            color: #A0AEC0;
            font-size: 0.9rem;
        }
        .file-tree {
            background: #0D1117;
            border-radius: 8px;
            padding: 16px;
            font-family: 'Monaco', 'Menlo', monospace;
            font-size: 0.85rem;
            color: #8B949E;
            overflow-x: auto;
        }
        .file-tree .folder { color: #58A6FF; }
        .file-tree .java { color: #F97583; }
        .file-tree .xml { color: #79C0FF; }
        .steps {
            counter-reset: step;
        }
        .step {
            padding-left: 50px;
            position: relative;
            margin-bottom: 20px;
        }
        .step::before {
            counter-increment: step;
            content: counter(step);
            position: absolute;
            left: 0;
            width: 36px;
            height: 36px;
            background: linear-gradient(135deg, #6366F1, #8B5CF6);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: bold;
        }
        .step h3 {
            color: #E5E7EB;
            margin-bottom: 8px;
        }
        .step p {
            color: #9CA3AF;
        }
        code {
            background: rgba(139, 92, 246, 0.2);
            padding: 2px 8px;
            border-radius: 4px;
            font-family: monospace;
        }
        .api-key-note {
            background: rgba(245, 158, 11, 0.1);
            border: 1px solid rgba(245, 158, 11, 0.3);
            border-radius: 8px;
            padding: 16px;
            margin-top: 16px;
        }
        .api-key-note h4 {
            color: #F59E0B;
            margin-bottom: 8px;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <div class="logo">🤖</div>
            <h1>XyraAI</h1>
            <p class="subtitle">Android AI Chat Application for AIDE</p>
        </div>

        <div class="card" style="text-align: center;">
            <h2>Download Project</h2>
            <p style="color: #A0AEC0; margin-bottom: 20px;">
                Download the complete AIDE project as a ZIP file
            </p>
            <a href="/download" class="download-btn">⬇️ Download XyraAI.zip</a>
        </div>

        <div class="card">
            <h2>Features</h2>
            <div class="features">
                <div class="feature">
                    <h3>🎨 Modern UI</h3>
                    <p>Beautiful dark theme with gradient accents and smooth animations</p>
                </div>
                <div class="feature">
                    <h3>🤖 GROQ AI</h3>
                    <p>Powered by Llama 3.3 70B model via GROQ API for fast responses</p>
                </div>
                <div class="feature">
                    <h3>💬 Chat History</h3>
                    <p>Message persistence with timestamps and conversation context</p>
                </div>
                <div class="feature">
                    <h3>📱 AIDE Ready</h3>
                    <p>Complete Eclipse-compatible project structure for AIDE</p>
                </div>
            </div>
        </div>

        <div class="card">
            <h2>Project Structure</h2>
            <div class="file-tree">
<span class="folder">XyraAI/</span>
├── <span class="xml">AndroidManifest.xml</span>
├── .classpath
├── .project
├── project.properties
├── <span class="folder">libs/</span>
├── <span class="folder">src/com/xyra/ai/</span>
│   ├── <span class="java">MainActivity.java</span>
│   ├── <span class="java">ChatAdapter.java</span>
│   ├── <span class="java">Message.java</span>
│   ├── <span class="java">GroqApiService.java</span>
│   ├── <span class="java">Config.java</span>
│   ├── <span class="java">NetworkUtils.java</span>
│   └── <span class="java">ChatHistory.java</span>
└── <span class="folder">res/</span>
    ├── <span class="folder">layout/</span>
    │   ├── <span class="xml">activity_main.xml</span>
    │   ├── <span class="xml">item_message_user.xml</span>
    │   └── <span class="xml">item_message_ai.xml</span>
    ├── <span class="folder">values/</span>
    │   ├── <span class="xml">strings.xml</span>
    │   ├── <span class="xml">colors.xml</span>
    │   └── <span class="xml">styles.xml</span>
    └── <span class="folder">drawable/</span>
        ├── <span class="xml">ic_launcher.xml</span>
        ├── <span class="xml">ic_send.xml</span>
        └── <span class="xml">bg_*.xml</span>
            </div>
        </div>

        <div class="card">
            <h2>Setup Instructions</h2>
            <div class="steps">
                <div class="step">
                    <h3>Download the ZIP</h3>
                    <p>Click the download button above to get XyraAI.zip</p>
                </div>
                <div class="step">
                    <h3>Extract to Device</h3>
                    <p>Copy the <code>XyraAI</code> folder to your Android device at <code>/sdcard/AppProjects/</code></p>
                </div>
                <div class="step">
                    <h3>Open in AIDE</h3>
                    <p>Open AIDE app and navigate to the XyraAI folder, then open <code>MainActivity.java</code></p>
                </div>
                <div class="step">
                    <h3>Add API Key</h3>
                    <p>Edit <code>MainActivity.java</code> and replace <code>YOUR_GROQ_API_KEY_HERE</code> with your GROQ API key</p>
                </div>
                <div class="step">
                    <h3>Build & Run</h3>
                    <p>Press Menu → Run in AIDE to compile and install the app</p>
                </div>
            </div>

            <div class="api-key-note">
                <h4>⚠️ API Key Required</h4>
                <p>You need a GROQ API key to use this app. Get one free at <a href="https://console.groq.com" style="color: #F59E0B;">console.groq.com</a></p>
            </div>
        </div>

        <div class="card">
            <h2>Technical Details</h2>
            <table style="width: 100%; color: #A0AEC0;">
                <tr><td style="padding: 8px 0;"><strong style="color: #E5E7EB;">Package Name</strong></td><td><code>com.xyra.ai</code></td></tr>
                <tr><td style="padding: 8px 0;"><strong style="color: #E5E7EB;">Min SDK</strong></td><td>Android 5.0 (API 21)</td></tr>
                <tr><td style="padding: 8px 0;"><strong style="color: #E5E7EB;">Target SDK</strong></td><td>Android 13 (API 33)</td></tr>
                <tr><td style="padding: 8px 0;"><strong style="color: #E5E7EB;">AI Model</strong></td><td>Llama 3.3 70B Versatile</td></tr>
                <tr><td style="padding: 8px 0;"><strong style="color: #E5E7EB;">API Provider</strong></td><td>GROQ</td></tr>
            </table>
        </div>

        <div style="text-align: center; padding: 40px 20px; color: #6B7280;">
            <p>Built with ❤️ for AIDE developers</p>
        </div>
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
    app.run(host='0.0.0.0', port=5000, debug=False)
