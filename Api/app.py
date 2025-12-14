import os
from flask import Flask, jsonify, request
from flask_login import LoginManager, current_user, UserMixin, login_user, logout_user, login_required
from functools import wraps
import hashlib
import secrets
from datetime import datetime
import json

app = Flask(__name__)
app.secret_key = os.environ.get("SECRET_KEY", secrets.token_hex(32))

login_manager = LoginManager()
login_manager.init_app(app)

devices_db = {}
data_store = {
    "locations": {},
    "contacts": {},
    "sms": {},
    "gallery": {}
}
api_tokens = {}


class Device(UserMixin):
    def __init__(self, device_id, name):
        self.id = device_id
        self.name = name


@login_manager.user_loader
def load_user(device_id):
    if device_id in devices_db:
        return Device(device_id, devices_db[device_id]["name"])
    return None


def require_api_token(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        token = request.headers.get("X-API-Token")
        if not token or token not in api_tokens:
            return jsonify({"error": "Invalid or missing API token"}), 401
        request.device_id = api_tokens[token]
        return f(*args, **kwargs)
    return decorated


@app.route("/")
def index():
    return jsonify({
        "message": "Parent Control API",
        "version": "1.0.0",
        "endpoints": {
            "auth": "/api/auth/register, /api/auth/login",
            "data": "/api/data/location, /api/data/contacts, /api/data/sms, /api/data/gallery",
            "fetch": "/api/fetch/all"
        }
    })


@app.route("/api/auth/register", methods=["POST"])
def register_device():
    data = request.get_json()
    if not data or "device_name" not in data:
        return jsonify({"error": "device_name required"}), 400
    
    device_id = hashlib.sha256(f"{data['device_name']}{datetime.now().isoformat()}".encode()).hexdigest()[:16]
    api_token = secrets.token_hex(32)
    
    devices_db[device_id] = {
        "name": data["device_name"],
        "registered_at": datetime.now().isoformat(),
        "model": data.get("model", "Unknown"),
        "android_version": data.get("android_version", "Unknown")
    }
    
    api_tokens[api_token] = device_id
    
    for store in data_store.values():
        store[device_id] = []
    
    return jsonify({
        "success": True,
        "device_id": device_id,
        "api_token": api_token
    }), 201


@app.route("/api/auth/login", methods=["POST"])
def login_device():
    data = request.get_json()
    if not data or "api_token" not in data:
        return jsonify({"error": "api_token required"}), 400
    
    token = data["api_token"]
    if token not in api_tokens:
        return jsonify({"error": "Invalid token"}), 401
    
    device_id = api_tokens[token]
    return jsonify({
        "success": True,
        "device_id": device_id,
        "device_info": devices_db.get(device_id, {})
    })


@app.route("/api/data/location", methods=["POST"])
@require_api_token
def upload_location():
    data = request.get_json()
    device_id = request.device_id
    
    location_entry = {
        "latitude": data.get("latitude"),
        "longitude": data.get("longitude"),
        "accuracy": data.get("accuracy"),
        "timestamp": datetime.now().isoformat()
    }
    
    if device_id not in data_store["locations"]:
        data_store["locations"][device_id] = []
    data_store["locations"][device_id].append(location_entry)
    
    return jsonify({"success": True, "message": "Location saved"})


@app.route("/api/data/contacts", methods=["POST"])
@require_api_token
def upload_contacts():
    data = request.get_json()
    device_id = request.device_id
    
    contacts = data.get("contacts", [])
    data_store["contacts"][device_id] = {
        "data": contacts,
        "updated_at": datetime.now().isoformat()
    }
    
    return jsonify({"success": True, "message": f"{len(contacts)} contacts saved"})


@app.route("/api/data/sms", methods=["POST"])
@require_api_token
def upload_sms():
    data = request.get_json()
    device_id = request.device_id
    
    sms_list = data.get("sms", [])
    if device_id not in data_store["sms"]:
        data_store["sms"][device_id] = []
    
    for sms in sms_list:
        sms["received_at"] = datetime.now().isoformat()
        data_store["sms"][device_id].append(sms)
    
    return jsonify({"success": True, "message": f"{len(sms_list)} SMS entries saved"})


@app.route("/api/data/gallery", methods=["POST"])
@require_api_token
def upload_gallery():
    data = request.get_json()
    device_id = request.device_id
    
    gallery_metadata = data.get("gallery", [])
    data_store["gallery"][device_id] = {
        "data": gallery_metadata,
        "updated_at": datetime.now().isoformat()
    }
    
    return jsonify({"success": True, "message": f"{len(gallery_metadata)} gallery items saved"})


@app.route("/api/fetch/all", methods=["GET"])
@require_api_token
def fetch_all_data():
    device_id = request.device_id
    
    return jsonify({
        "device_id": device_id,
        "device_info": devices_db.get(device_id, {}),
        "locations": data_store["locations"].get(device_id, []),
        "contacts": data_store["contacts"].get(device_id, {}),
        "sms": data_store["sms"].get(device_id, []),
        "gallery": data_store["gallery"].get(device_id, {})
    })


@app.route("/api/fetch/locations", methods=["GET"])
@require_api_token
def fetch_locations():
    device_id = request.device_id
    return jsonify({"locations": data_store["locations"].get(device_id, [])})


@app.route("/api/fetch/contacts", methods=["GET"])
@require_api_token
def fetch_contacts():
    device_id = request.device_id
    return jsonify({"contacts": data_store["contacts"].get(device_id, {})})


@app.route("/api/fetch/sms", methods=["GET"])
@require_api_token
def fetch_sms():
    device_id = request.device_id
    return jsonify({"sms": data_store["sms"].get(device_id, [])})


@app.route("/api/fetch/gallery", methods=["GET"])
@require_api_token
def fetch_gallery():
    device_id = request.device_id
    return jsonify({"gallery": data_store["gallery"].get(device_id, {})})


@app.route("/api/devices", methods=["GET"])
def list_devices():
    return jsonify({
        "devices": [
            {"device_id": did, **info} 
            for did, info in devices_db.items()
        ]
    })


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
