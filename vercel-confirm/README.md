# XyraAI Email Confirmation Page

Halaman konfirmasi email untuk Supabase yang di-deploy ke Vercel.

## Cara Deploy ke Vercel

### Opsi 1: Deploy Langsung (Tanpa GitHub)

1. Install Vercel CLI:
   ```bash
   npm install -g vercel
   ```

2. Masuk ke folder ini dan deploy:
   ```bash
   cd vercel-confirm
   vercel
   ```

3. Ikuti instruksi dan dapatkan URL (contoh: `https://xyraai-confirm.vercel.app`)

### Opsi 2: Deploy via GitHub

1. Buat repository baru di GitHub
2. Push folder `vercel-confirm` ke repository
3. Buka [vercel.com](https://vercel.com) dan import repository
4. Deploy otomatis!

## Setting Supabase

Setelah deploy, update pengaturan Supabase:

1. Buka **Supabase Dashboard** > **Authentication** > **URL Configuration**

2. Set **Site URL**:
   ```
   https://NAMA-PROJECT-KAMU.vercel.app
   ```

3. Tambahkan ke **Redirect URLs**:
   ```
   https://NAMA-PROJECT-KAMU.vercel.app/**
   ```

4. **Save** perubahan

## Testing

1. Daftar akun baru di aplikasi XyraAI
2. Cek email konfirmasi
3. Klik link "Confirm your mail"
4. Akan redirect ke halaman Vercel dengan pesan sukses
5. Kembali ke aplikasi dan login

## Troubleshooting

### Link tidak berfungsi
- Pastikan Site URL di Supabase sudah diupdate ke URL Vercel
- Pastikan sudah menambahkan Redirect URLs

### Error saat verifikasi
- Coba klik link lagi (link mungkin expired)
- Daftar ulang jika masih error
