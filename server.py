import os

from flask import Flask, render_template, redirect, url_for
from flask_login import LoginManager, current_user, UserMixin, login_user, logout_user

app = Flask(__name__)
app.secret_key = os.environ.get("SECRET_KEY", os.urandom(24))

login_manager = LoginManager()
login_manager.init_app(app)


class User(UserMixin):
    def __init__(self, user_id):
        self.id = user_id


@login_manager.user_loader
def load_user(user_id):
    return User(user_id)


@app.route("/")
def index():
    if current_user.is_authenticated:
        return redirect(url_for('dashboard'))
    return render_template(
        "index.html",
        firebase_api_key=os.environ.get("FIREBASE_API_KEY"),
        firebase_project_id=os.environ.get("FIREBASE_PROJECT_ID"),
        firebase_app_id=os.environ.get("FIREBASE_APP_ID"),
    )


@app.route("/dashboard")
def dashboard():
    if not current_user.is_authenticated:
        return redirect(url_for('index'))
    return render_template("dashboard.html")


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
