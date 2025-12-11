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
            --ios-gray-2: #AEAEB2;
            --ios-gray-3: #C7C7CC;
            --ios-gray-4: #D1D1D6;
            --ios-gray-5: #E5E5EA;
            --ios-gray-6: #F2F2F7;
            --ios-text: #1C1C1E;
            --ios-text-secondary: #3C3C43;
            --ios-green: #34C759;
            --ios-orange: #FF9500;
            --ios-purple: #AF52DE;
            --ios-pink: #FF2D55;
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
            -moz-osx-font-smoothing: grayscale;
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
            transition: transform 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
        }
        
        .logo:hover {
            transform: scale(1.05);
        }
        
        h1 {
            font-size: 2.2rem;
            font-weight: 700;
            color: var(--ios-text);
            margin-bottom: 8px;
            letter-spacing: -0.5px;
        }
        
        .subtitle {
            color: var(--ios-gray);
            font-size: 1rem;
            font-weight: 400;
        }
        
        .ios-card {
            background: var(--ios-card);
            border-radius: 16px;
            padding: 0;
            margin-bottom: 20px;
            box-shadow: var(--ios-shadow);
            overflow: hidden;
            transition: transform 0.2s ease, box-shadow 0.2s ease;
        }
        
        .ios-card:hover {
            transform: translateY(-2px);
            box-shadow: var(--ios-shadow-lg);
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
            letter-spacing: 0.5px;
        }
        
        .card-content {
            padding: 0;
        }
        
        .download-section {
            text-align: center;
            padding: 32px 24px;
        }
        
        .download-section p {
            color: var(--ios-gray);
            margin-bottom: 20px;
            font-size: 0.95rem;
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
            font-size: 1rem;
            transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
            box-shadow: 0 4px 15px rgba(0, 122, 255, 0.3);
        }
        
        .download-btn:hover {
            transform: scale(1.02);
            box-shadow: 0 6px 25px rgba(0, 122, 255, 0.4);
        }
        
        .download-btn:active {
            transform: scale(0.98);
        }
        
        .ios-list-item {
            display: flex;
            align-items: center;
            padding: 14px 20px;
            border-bottom: 0.5px solid var(--ios-gray-5);
            transition: background 0.15s ease;
        }
        
        .ios-list-item:last-child {
            border-bottom: none;
        }
        
        .ios-list-item:active {
            background: var(--ios-gray-6);
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
            flex-shrink: 0;
        }
        
        .feature-icon.blue { background: rgba(0, 122, 255, 0.12); }
        .feature-icon.green { background: rgba(52, 199, 89, 0.12); }
        .feature-icon.purple { background: rgba(175, 82, 222, 0.12); }
        .feature-icon.orange { background: rgba(255, 149, 0, 0.12); }
        
        .feature-text h3 {
            font-size: 1rem;
            font-weight: 500;
            color: var(--ios-text);
            margin-bottom: 2px;
        }
        
        .feature-text p {
            font-size: 0.85rem;
            color: var(--ios-gray);
            line-height: 1.3;
        }
        
        .file-tree {
            background: #1C1C1E;
            border-radius: 12px;
            padding: 16px;
            margin: 16px;
            font-family: 'SF Mono', Monaco, Menlo, monospace;
            font-size: 0.8rem;
            color: #A1A1A6;
            overflow-x: auto;
            line-height: 1.6;
        }
        
        .file-tree .folder { color: #64D2FF; }
        .file-tree .java { color: #FF6B6B; }
        .file-tree .xml { color: #5AC8FA; }
        
        .steps-list {
            padding: 0;
        }
        
        .step-item {
            display: flex;
            align-items: flex-start;
            padding: 16px 20px;
            border-bottom: 0.5px solid var(--ios-gray-5);
        }
        
        .step-item:last-child {
            border-bottom: none;
        }
        
        .step-number {
            width: 28px;
            height: 28px;
            background: linear-gradient(135deg, #007AFF 0%, #5856D6 100%);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #fff;
            font-size: 0.85rem;
            font-weight: 600;
            margin-right: 14px;
            flex-shrink: 0;
        }
        
        .step-content h3 {
            font-size: 1rem;
            font-weight: 500;
            color: var(--ios-text);
            margin-bottom: 4px;
        }
        
        .step-content p {
            font-size: 0.9rem;
            color: var(--ios-gray);
            line-height: 1.4;
        }
        
        code {
            background: var(--ios-gray-6);
            padding: 3px 8px;
            border-radius: 6px;
            font-family: 'SF Mono', Monaco, Menlo, monospace;
            font-size: 0.85em;
            color: var(--ios-blue);
        }
        
        .api-note {
            display: flex;
            align-items: flex-start;
            background: rgba(255, 149, 0, 0.08);
            border-radius: 12px;
            padding: 14px 16px;
            margin: 16px;
        }
        
        .api-note-icon {
            font-size: 20px;
            margin-right: 12px;
            flex-shrink: 0;
        }
        
        .api-note-text {
            font-size: 0.9rem;
            color: var(--ios-text-secondary);
            line-height: 1.4;
        }
        
        .api-note-text a {
            color: var(--ios-orange);
            text-decoration: none;
            font-weight: 500;
        }
        
        .specs-table {
            width: 100%;
        }
        
        .spec-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 14px 20px;
            border-bottom: 0.5px solid var(--ios-gray-5);
        }
        
        .spec-row:last-child {
            border-bottom: none;
        }
        
        .spec-label {
            font-size: 1rem;
            color: var(--ios-text);
        }
        
        .spec-value {
            font-size: 1rem;
            color: var(--ios-gray);
        }
        
        .footer {
            text-align: center;
            padding: 30px 20px 40px;
            color: var(--ios-gray);
            font-size: 0.9rem;
        }
        
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(10px); }
            to { opacity: 1; transform: translateY(0); }
        }
        
        .ios-card {
            animation: fadeIn 0.4s ease forwards;
        }
        
        .ios-card:nth-child(1) { animation-delay: 0.1s; }
        .ios-card:nth-child(2) { animation-delay: 0.15s; }
        .ios-card:nth-child(3) { animation-delay: 0.2s; }
        .ios-card:nth-child(4) { animation-delay: 0.25s; }
        .ios-card:nth-child(5) { animation-delay: 0.3s; }
        
        @media (max-width: 480px) {
            .container {
                padding: 12px;
            }
            .header {
                padding: 30px 16px;
            }
            h1 {
                font-size: 1.8rem;
            }
            .download-btn {
                padding: 12px 24px;
                font-size: 0.95rem;
            }
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
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
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
            <div class="card-content">
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
        </div>

        <div class="ios-card">
            <div class="card-header">
                <h2>Project Structure</h2>
            </div>
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
    ├── <span class="folder">values/</span>
    └── <span class="folder">drawable/</span>
            </div>
        </div>

        <div class="ios-card">
            <div class="card-header">
                <h2>Setup Instructions</h2>
            </div>
            <div class="steps-list">
                <div class="step-item">
                    <div class="step-number">1</div>
                    <div class="step-content">
                        <h3>Download the ZIP</h3>
                        <p>Click the download button above to get XyraAI.zip</p>
                    </div>
                </div>
                <div class="step-item">
                    <div class="step-number">2</div>
                    <div class="step-content">
                        <h3>Extract to Device</h3>
                        <p>Copy the <code>XyraAI</code> folder to <code>/sdcard/AppProjects/</code></p>
                    </div>
                </div>
                <div class="step-item">
                    <div class="step-number">3</div>
                    <div class="step-content">
                        <h3>Open in AIDE</h3>
                        <p>Navigate to XyraAI folder and open <code>MainActivity.java</code></p>
                    </div>
                </div>
                <div class="step-item">
                    <div class="step-number">4</div>
                    <div class="step-content">
                        <h3>Add API Key</h3>
                        <p>Replace <code>YOUR_GROQ_API_KEY_HERE</code> with your key</p>
                    </div>
                </div>
                <div class="step-item">
                    <div class="step-number">5</div>
                    <div class="step-content">
                        <h3>Build & Run</h3>
                        <p>Press Menu → Run in AIDE to compile and install</p>
                    </div>
                </div>
            </div>
            <div class="api-note">
                <span class="api-note-icon">⚠️</span>
                <div class="api-note-text">
                    You need a GROQ API key. Get one free at <a href="https://console.groq.com">console.groq.com</a>
                </div>
            </div>
        </div>

        <div class="ios-card">
            <div class="card-header">
                <h2>Technical Details</h2>
            </div>
            <div class="specs-table">
                <div class="spec-row">
                    <span class="spec-label">Package Name</span>
                    <span class="spec-value">com.xyra.ai</span>
                </div>
                <div class="spec-row">
                    <span class="spec-label">Min SDK</span>
                    <span class="spec-value">Android 5.0 (API 21)</span>
                </div>
                <div class="spec-row">
                    <span class="spec-label">Target SDK</span>
                    <span class="spec-value">Android 13 (API 33)</span>
                </div>
                <div class="spec-row">
                    <span class="spec-label">AI Model</span>
                    <span class="spec-value">Llama 3.3 70B</span>
                </div>
                <div class="spec-row">
                    <span class="spec-label">API Provider</span>
                    <span class="spec-value">GROQ</span>
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
