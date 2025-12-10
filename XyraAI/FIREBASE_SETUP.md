# Panduan Setup Firebase Authentication untuk XyraAI

## Langkah 1: Konfigurasi Firebase Console

1. Buka [Firebase Console](https://console.firebase.google.com/)
2. Pilih project **authaixyra** (atau buat baru jika belum ada)
3. Pastikan aplikasi Android sudah terdaftar dengan package name: `com.xyra.ai`

## Langkah 2: Aktifkan Google Sign-In

1. Di Firebase Console, pergi ke **Authentication** > **Sign-in method**
2. Klik **Google** dan aktifkan
3. Isi **Project support email**
4. Klik **Save**

## Langkah 3: Generate dan Tambahkan SHA-1 Fingerprint

### Untuk Debug Key (Development):
Buka terminal/command prompt dan jalankan:

**Windows:**
```bash
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

**Mac/Linux:**
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

### Untuk Release Key (Production):
```bash
keytool -list -v -keystore path/to/your-release-key.keystore -alias your-alias
```

### Tambahkan SHA-1 ke Firebase:
1. Di Firebase Console, pergi ke **Project Settings** (ikon gear)
2. Scroll ke bagian **Your apps** dan pilih aplikasi Android
3. Klik **Add fingerprint**
4. Paste SHA-1 fingerprint yang sudah di-generate
5. Klik **Save**

**PENTING:** Tambahkan KEDUA SHA-1 (debug dan release) jika Anda ingin login bekerja di development dan production!

## Langkah 4: Setup Dependencies di AIDE/Android Studio

### Jika menggunakan Gradle (Android Studio):

**build.gradle (Project level):**
```gradle
buildscript {
    dependencies {
        classpath 'com.google.gms:google-services:4.4.0'
    }
}
```

**build.gradle (App level):**
```gradle
plugins {
    id 'com.android.application'
    id 'com.google.gms.google-services'
}

dependencies {
    implementation 'com.google.firebase:firebase-auth:22.3.0'
    implementation 'com.google.android.gms:play-services-auth:20.7.0'
}
```

### Jika menggunakan AIDE:

1. Download file JAR berikut dan letakkan di folder `libs/`:
   - firebase-auth-*.jar
   - play-services-auth-*.jar
   - play-services-base-*.jar
   - firebase-common-*.jar

2. Atau gunakan AAR dependencies jika AIDE mendukung

## Langkah 5: Pastikan google-services.json Sudah Benar

File `google-services.json` sudah ada di project dengan konfigurasi:
- Project ID: `authaixyra`
- Package Name: `com.xyra.ai`
- Web Client ID: `253516519538-m0vik284rkmvpkuel9dvi5j2tardv4j1.apps.googleusercontent.com`

## Langkah 6: Test Login

1. Build dan install APK ke device
2. Pastikan device memiliki Google Play Services terbaru
3. Klik tombol "Sign in with Google"
4. Pilih akun Google
5. Jika berhasil, Anda akan masuk ke MainActivity

## Troubleshooting

### Error Code 10 (Developer Error)
- SHA-1 fingerprint belum ditambahkan ke Firebase Console
- Pastikan SHA-1 debug key sudah ditambahkan

### Error Code 12501 (Sign In Cancelled)
- User membatalkan login, ini normal

### Error Code 7 (Network Error)
- Periksa koneksi internet device

### Error "Google Play Services out of date"
- Update Google Play Services di device

## Struktur File yang Diperbarui

```
XyraAI/
├── src/com/xyra/ai/
│   ├── LoginActivity.java    # Updated dengan Firebase Auth
│   ├── ProfileActivity.java  # Updated dengan Firebase Sign Out
│   └── ...
├── google-services.json      # Firebase config
└── FIREBASE_SETUP.md         # Panduan ini
```

## Catatan Penting

1. **WEB_CLIENT_ID** di kode sudah sesuai dengan `google-services.json`
2. Jangan lupa tambahkan **SHA-1 fingerprint** untuk SEMUA keystore yang digunakan
3. Untuk release build, generate keystore baru dan tambahkan SHA-1 nya ke Firebase
