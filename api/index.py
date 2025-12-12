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
    <link href="https://fonts.googleapis.com/css2?family=SF+Pro+Display:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        :root {
            --ios-bg: #F2F2F7;
            --ios-card: #FFFFFF;
            --ios-blue: #007AFF;
            --ios-gray: #8E8E93;
            --ios-gray-5: #E5E5EA;
            --ios-gray-6: #F2F2F7;
            --ios-text: #1C1C1E;
            --ios-text-secondary: #3C3C43;
            --ios-shadow: 0 2px 10px rgba(0,0,0,0.08);
            --ios-shadow-lg: 0 8px 30px rgba(0,0,0,0.12);
        }
        
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
            background: var(--ios-bg);
            min-height: 100vh;
            color: var(--ios-text);
            padding: 0;
            -webkit-font-smoothing: antialiased;
        }
        
        .container {
            max-width: 680px;
            margin: 0 auto;
            padding: 20px;
        }
        
        .header {
            text-align: center;
            padding: 50px 20px 40px;
        }
        
        .logo {
            width: 100px;
            height: 100px;
            background: linear-gradient(135deg, #007AFF 0%, #5856D6 100%);
            border-radius: 22px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 20px;
            font-size: 48px;
            box-shadow: 0 10px 30px rgba(0, 122, 255, 0.3);
        }
        
        h1 {
            font-size: 2.2rem;
            font-weight: 700;
            color: var(--ios-text);
            margin-bottom: 8px;
        }
        
        .subtitle {
            color: var(--ios-gray);
            font-size: 1rem;
        }
        
        .ios-card {
            background: var(--ios-card);
            border-radius: 16px;
            padding: 0;
            margin-bottom: 20px;
            box-shadow: var(--ios-shadow);
            overflow: hidden;
        }
        
        .card-header {
            padding: 16px 20px 12px;
            border-bottom: 1px solid var(--ios-gray-5);
        }
        
        .card-header h2 {
            font-size: 0.8rem;
            font-weight: 600;
            color: var(--ios-gray);
            text-transform: uppercase;
        }
        
        .download-section {
            text-align: center;
            padding: 32px 24px;
        }
        
        .download-section p {
            color: var(--ios-gray);
            margin-bottom: 20px;
        }
        
        .download-btn {
            display: inline-flex;
            align-items: center;
            gap: 10px;
            background: linear-gradient(135deg, #007AFF 0%, #5856D6 100%);
            color: #fff;
            padding: 14px 28px;
            border-radius: 14px;
            text-decoration: none;
            font-weight: 600;
            box-shadow: 0 4px 15px rgba(0, 122, 255, 0.3);
        }
        
        .ios-list-item {
            display: flex;
            align-items: center;
            padding: 14px 20px;
            border-bottom: 0.5px solid var(--ios-gray-5);
        }
        
        .ios-list-item:last-child {
            border-bottom: none;
        }
        
        .feature-icon {
            width: 44px;
            height: 44px;
            border-radius: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 22px;
            margin-right: 14px;
        }
        
        .feature-icon.blue { background: rgba(0, 122, 255, 0.12); }
        .feature-icon.green { background: rgba(52, 199, 89, 0.12); }
        .feature-icon.purple { background: rgba(175, 82, 222, 0.12); }
        .feature-icon.orange { background: rgba(255, 149, 0, 0.12); }
        
        .feature-text h3 {
            font-size: 1rem;
            font-weight: 500;
            margin-bottom: 2px;
        }
        
        .feature-text p {
            font-size: 0.85rem;
            color: var(--ios-gray);
        }
        
        .footer {
            text-align: center;
            padding: 30px 20px 40px;
            color: var(--ios-gray);
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

        <div class="ios-card">
            <div class="download-section">
                <p>Download the complete AIDE project as a ZIP file</p>
                <a href="/download" class="download-btn">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                        <polyline points="7 10 12 15 17 10"/>
                        <line x1="12" y1="15" x2="12" y2="3"/>
                    </svg>
                    Download XyraAI.zip
                </a>
            </div>
        </div>

        <div class="ios-card">
            <div class="card-header">
                <h2>Features</h2>
            </div>
            <div class="ios-list-item">
                <div class="feature-icon blue">🎨</div>
                <div class="feature-text">
                    <h3>Modern UI</h3>
                    <p>Beautiful dark theme with gradient accents and smooth animations</p>
                </div>
            </div>
            <div class="ios-list-item">
                <div class="feature-icon green">🤖</div>
                <div class="feature-text">
                    <h3>GROQ AI</h3>
                    <p>Powered by Llama 3.3 70B model via GROQ API</p>
                </div>
            </div>
            <div class="ios-list-item">
                <div class="feature-icon purple">💬</div>
                <div class="feature-text">
                    <h3>Chat History</h3>
                    <p>Message persistence with timestamps and context</p>
                </div>
            </div>
            <div class="ios-list-item">
                <div class="feature-icon orange">📱</div>
                <div class="feature-text">
                    <h3>AIDE Ready</h3>
                    <p>Complete Eclipse-compatible project structure</p>
                </div>
            </div>
        </div>

        <div class="footer">
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
