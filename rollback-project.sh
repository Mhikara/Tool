#!/data/data/com.termux/files/usr/bin/bash
set -e

PROJECT_DIR="$HOME/Tool"
BACKUP_ROOT="$HOME/Tool_backups"

echo "=== 1. Daftar Backup ==="
if [ ! -d "$BACKUP_ROOT" ]; then
    echo "❌ Folder backup tidak ditemukan di $BACKUP_ROOT!"
    exit 1
fi

# Menyimpan daftar file ke dalam array (diurutkan dari yang terbaru ke terlama)
mapfile -t BACKUPS < <(ls -1dt "$BACKUP_ROOT"/Tool_backup_* 2>/dev/null)

if [ ${#BACKUPS[@]} -eq 0 ]; then
    echo "❌ Tidak ada folder backup yang tersedia!"
    exit 1
fi

for i in "${!BACKUPS[@]}"; do
    FILENAME=$(basename "${BACKUPS[$i]}")
    echo "  [$i] $FILENAME"
done

echo ""
read -p "Pilih nomor backup yang ingin di-restore (0-$((${#BACKUPS[@]} - 1))): " SELECTION

if ! [[ "$SELECTION" =~ ^[0-9]+$ ]] || [ "$SELECTION" -ge "${#BACKUPS[@]}" ]; then
    echo "❌ Pilihan tidak valid."
    exit 1
fi

SELECTED_BACKUP="${BACKUPS[$SELECTION]}"
echo "Anda memilih: $(basename "$SELECTED_BACKUP")"

echo ""
echo "=== 2. Konfirmasi Rollback ==="
echo "⚠️ PERINGATAN: Rollback akan MENGHAPUS seluruh isi folder $PROJECT_DIR"
echo "dan menggantinya dengan isi dari $(basename "$SELECTED_BACKUP")!"
read -p "Ketik 'YAKIN' untuk melanjutkan: " CONFIRM

if [ "$CONFIRM" != "YAKIN" ]; then
    echo "🛑 Rollback dibatalkan."
    exit 0
fi

echo ""
echo "=== 3. Proses Restore ==="
set +e
rm -rf "$PROJECT_DIR"
cp -r "$SELECTED_BACKUP" "$PROJECT_DIR"
if [ $? -eq 0 ]; then
    echo "✅ Restore berhasil! Project $PROJECT_DIR telah dikembalikan ke kondisi backup."
else
    echo "❌ Error: Gagal me-restore backup!"
    exit 1
fi
set -e
