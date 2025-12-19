# Panduan Setup Supabase untuk XyraAI

## Langkah 1: Setup Tabel Database

Buka Supabase Dashboard dan jalankan SQL berikut di SQL Editor:

```sql
-- Buat tabel chats untuk menyimpan chat history
CREATE TABLE IF NOT EXISTS chats (
    id TEXT PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    preview TEXT DEFAULT 'Chat baru',
    messages TEXT DEFAULT '[]',
    message_count INTEGER DEFAULT 0,
    updated_at BIGINT DEFAULT (extract(epoch from now()) * 1000)::bigint,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Enable Row Level Security
ALTER TABLE chats ENABLE ROW LEVEL SECURITY;

-- Policy: Users can only see their own chats
CREATE POLICY "Users can view own chats" ON chats
    FOR SELECT USING (auth.uid() = user_id);

-- Policy: Users can insert their own chats
CREATE POLICY "Users can insert own chats" ON chats
    FOR INSERT WITH CHECK (auth.uid() = user_id);

-- Policy: Users can update their own chats
CREATE POLICY "Users can update own chats" ON chats
    FOR UPDATE USING (auth.uid() = user_id);

-- Policy: Users can delete their own chats
CREATE POLICY "Users can delete own chats" ON chats
    FOR DELETE USING (auth.uid() = user_id);

-- Create index for faster queries
CREATE INDEX IF NOT EXISTS idx_chats_user_id ON chats(user_id);
CREATE INDEX IF NOT EXISTS idx_chats_updated_at ON chats(updated_at DESC);
```

## Langkah 2: Aktifkan Email Authentication

1. Di Supabase Dashboard, pergi ke **Authentication** > **Providers**
2. Pastikan **Email** provider sudah aktif
3. Opsional: Nonaktifkan "Confirm email" untuk testing

## Langkah 3: Konfigurasi Sudah Tersimpan

Kredensial Supabase sudah dikonfigurasi di aplikasi:
- Project ID: `figcqxynrcnimagpswqn`
- URL: `https://figcqxynrcnimagpswqn.supabase.co`

## Fitur yang Didukung

1. **Login/Signup dengan Email & Password**
   - User dapat mendaftar dan login menggunakan email
   - Password minimal 6 karakter

2. **Cloud Chat History Sync**
   - Chat history disimpan di cloud Supabase
   - Jika user uninstall dan install ulang, chat history tetap ada
   - Sinkronisasi otomatis saat user login

3. **Row Level Security**
   - Setiap user hanya bisa melihat chat miliknya sendiri
   - Data aman dan terisolasi per user

## Troubleshooting

### Error "Email not confirmed"
- Nonaktifkan email confirmation di Supabase Dashboard
- Atau minta user untuk cek email konfirmasi

### Error "Invalid login credentials"
- Email atau password salah
- Pastikan user sudah mendaftar terlebih dahulu

### Chat history kosong setelah reinstall
- Pastikan login dengan akun yang SAMA (email yang sama)
- Gunakan tombol "Masuk", BUKAN "Daftar"
- Jika tetap kosong, cek di Supabase Dashboard > Authentication > Users
  apakah ada duplicate user dengan email yang sama
- User ID di Supabase selalu tetap sama selama menggunakan email yang sama

### User ID berbeda setelah reinstall
- Ini seharusnya TIDAK terjadi jika login dengan email yang sama
- Kemungkinan penyebab:
  1. User klik "Daftar" bukan "Masuk"
  2. Email confirmation belum selesai saat pertama daftar
- Solusi: Hapus user duplicate di Supabase Dashboard, lalu daftar ulang

### Chat tidak sinkron
- Pastikan user sudah login
- Periksa koneksi internet
- Pastikan tabel `chats` sudah dibuat di database

## Login dengan Google

1. Setup Google OAuth di Supabase Dashboard:
   - Authentication > Providers > Google
   - Enable Sign in with Google
   - Tambahkan Client ID dan Client Secret dari Google Cloud Console

2. URL Configuration:
   - Site URL: `https://android-rho-five.vercel.app`
   - Redirect URLs: `https://android-rho-five.vercel.app/**`

3. File terkait:
   - `OAuthActivity.java` - WebView untuk Google OAuth
   - `activity_oauth.xml` - Layout WebView

## Struktur File yang Diperbarui

```
XyraAI/
├── src/com/xyra/ai/
│   ├── SupabaseService.java  # Supabase auth & database
│   ├── LoginActivity.java    # Updated untuk Supabase
│   ├── ProfileActivity.java  # Updated untuk Supabase signout
│   ├── ChatHistory.java      # Updated dengan cloud sync
│   └── Config.java           # Supabase credentials
└── SUPABASE_SETUP.md         # Panduan ini
```
