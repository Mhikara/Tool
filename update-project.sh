#!/bin/bash

# ==========================================
# Script Update & Backup Otomatis (Termux)
# ==========================================

# Konfigurasi
BACKUP_BASE_DIR="../Tool_backups" # Simpan di folder induk agar tidak masuk git
MAX_BACKUPS=5

COMMIT_MSG=$1

if [ -z "$COMMIT_MSG" ]; then
    echo "❌ Error: Pesan commit harus diisi!"
    echo "Contoh: ./update-project.sh \"Update fitur X dari Gemini\""
    exit 1
fi

echo "🔍 Memeriksa status Git..."
if ! git diff-index --quiet HEAD --; then
    echo "⚠️ Ada perubahan yang belum di-commit di working directory."
    read -p "Lanjut backup dan commit perubahan ini? (y/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "🛑 Operasi dibatalkan."
        exit 1
    fi
fi

# 1. BUAT BACKUP (WAJIB)
mkdir -p "$BACKUP_BASE_DIR"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="$BACKUP_BASE_DIR/Tool_backup_$TIMESTAMP.tar.gz"

echo "📦 Memulai backup ke $BACKUP_FILE..."
# Mengecualikan folder cache dan build agar backup ringan
tar -czf "$BACKUP_FILE" --exclude='.git' --exclude='app/build' --exclude='.gradle' --exclude='build' .

if [ $? -eq 0 ]; then
    echo "✅ Backup sukses!"
else
    echo "❌ Error: Backup gagal! Update dibatalkan demi keamanan."
    exit 1
fi

# 2. BERSIHKAN BACKUP LAMA
echo "🧹 Memeriksa backup lama (Batas maksimum: $MAX_BACKUPS)..."
BACKUP_COUNT=$(ls -1q "$BACKUP_BASE_DIR"/Tool_backup_*.tar.gz 2>/dev/null | wc -l)
if [ "$BACKUP_COUNT" -gt "$MAX_BACKUPS" ]; then
    # Hapus file paling lama
    ls -1tr "$BACKUP_BASE_DIR"/Tool_backup_*.tar.gz | head -n -"$MAX_BACKUPS" | xargs rm -f
    echo "✅ Backup terlama telah dihapus otomatis untuk menghemat ruang."
fi

# 3. GIT ADD, COMMIT, & PUSH
echo "🚀 Menyiapkan pembaruan ke GitHub..."
git add .
git commit -m "$COMMIT_MSG"

echo "☁️ Mendorong (push) ke remote..."
if git push; then
    echo "✅ Update project selesai dan berhasil di-push!"
else
    echo "❌ Peringatan: git push gagal. Periksa koneksi atau kredensial GitHub Anda."
    echo "💡 Jangan khawatir, file backup Anda tetap aman di $BACKUP_FILE"
    exit 1
fi
