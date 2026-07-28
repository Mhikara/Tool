#!/data/data/com.termux/files/usr/bin/bash
set -e

PROJECT_DIR="$HOME/Tool"
BACKUP_ROOT="$HOME/Tool_backups"
MAX_BACKUPS=5

COMMIT_MSG="$1"

if [ -z "$COMMIT_MSG" ]; then
    echo "❌ Error: Pesan commit harus diisi!"
    echo "Contoh penggunaan: ./update-project.sh \"Update fitur X\""
    exit 1
fi

echo "=== 1. Cek status project ==="
cd "$PROJECT_DIR" || { echo "❌ Folder $PROJECT_DIR tidak ditemukan!"; exit 1; }
git status

echo ""
echo "=== 2. Konfirmasi ==="
read -p "Apakah Anda ingin melanjutkan update? (y/n): " confirm
if [[ "$confirm" != "y" && "$confirm" != "Y" ]]; then
    echo "Dibatalkan."
    exit 0
fi

echo ""
echo "=== 3. Backup Project ==="
mkdir -p "$BACKUP_ROOT"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_DIR="$BACKUP_ROOT/Tool_backup_$TIMESTAMP"

# Mematikan set -e sementara agar bisa menangani error cp secara manual
set +e
cp -r "$PROJECT_DIR" "$BACKUP_DIR"
if [ $? -ne 0 ]; then
    echo "❌ Error: Backup gagal! Update dibatalkan demi keamanan."
    exit 1
fi
set -e
echo "✅ Backup berhasil disimpan di: $BACKUP_DIR"

echo ""
echo "=== 4. Membersihkan Backup Lama ==="
# ls -1dt mengurutkan dari yang terbaru, tail mengambil yang paling lama
BACKUP_COUNT=$(ls -1dt "$BACKUP_ROOT"/Tool_backup_* 2>/dev/null | wc -l)
if [ "$BACKUP_COUNT" -gt "$MAX_BACKUPS" ]; then
    DELETE_COUNT=$((BACKUP_COUNT - MAX_BACKUPS))
    echo "Menghapus $DELETE_COUNT backup lama..."
    ls -1dt "$BACKUP_ROOT"/Tool_backup_* | tail -n "$DELETE_COUNT" | xargs rm -rf
    echo "✅ Backup lama berhasil dihapus."
else
    echo "ℹ️ Jumlah backup masih dalam batas aman ($BACKUP_COUNT/$MAX_BACKUPS)."
fi

echo ""
echo "=== 5. Git Commit ==="
git add .
# Mematikan set -e sementara karena git commit akan error jika tidak ada perubahan
set +e
git commit -m "$COMMIT_MSG"
if [ $? -ne 0 ]; then
    echo "⚠️ Peringatan: Tidak ada perubahan untuk di-commit, atau commit gagal. Melanjutkan ke push..."
fi
set -e

echo ""
echo "=== 6. Git Push ==="
set +e
git push
if [ $? -eq 0 ]; then
    echo "✅ Update project selesai dan berhasil di-push!"
    echo "✅ Backup Anda tersimpan aman di: $BACKUP_DIR"
else
    echo "❌ Error: git push gagal!"
    echo "💡 Jangan khawatir, backup Anda tetap aman di: $BACKUP_DIR"
    exit 1
fi
set -e
