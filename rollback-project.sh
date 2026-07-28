#!/bin/bash

# ==========================================
# Script Rollback Otomatis (Termux)
# ==========================================

BACKUP_BASE_DIR="../Tool_backups"

echo "🔍 Mencari backup yang tersedia..."
if [ ! -d "$BACKUP_BASE_DIR" ]; then
    echo "❌ Folder backup tidak ditemukan di $BACKUP_BASE_DIR!"
    exit 1
fi

# Menyimpan daftar file ke dalam array (diurutkan dari yang terlama ke terbaru)
mapfile -t BACKUPS < <(ls -1tr "$BACKUP_BASE_DIR"/Tool_backup_*.tar.gz 2>/dev/null)

if [ ${#BACKUPS[@]} -eq 0 ]; then
    echo "❌ Tidak ada file backup yang tersedia!"
    exit 1
fi

echo "📋 Daftar backup yang tersedia:"
for i in "${!BACKUPS[@]}"; do
    # Tampilkan hanya nama file, bukan path lengkap
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

# KONFIRMASI WAJIB (KEAMANAN)
echo "⚠️ PERINGATAN: Rollback akan MENIMPA semua file project saat ini dengan file dari backup!"
read -p "Apakah Anda YAKIN ingin melanjutkan? (Ketik 'YAKIN' untuk lanjut): " CONFIRM

if [ "$CONFIRM" != "YAKIN" ]; then
    echo "🛑 Rollback dibatalkan."
    exit 0
fi

echo "🔄 Memulai proses ekstrak backup..."

# Ekstrak backup untuk menimpa file saat ini
tar -xzf "$SELECTED_BACKUP" -C .

if [ $? -eq 0 ]; then
    echo "✅ Restore berhasil! File project telah dikembalikan ke kondisi backup."
    echo "💡 Catatan: File baru yang Anda buat *setelah* backup ini diambil mungkin masih tersisa di folder."
    echo "   (Jika Anda ingin menghapus file-file baru tersebut, Anda bisa menjalankan 'git clean -fd' secara manual)"
else
    echo "❌ Error: Gagal mengekstrak backup!"
    exit 1
fi
