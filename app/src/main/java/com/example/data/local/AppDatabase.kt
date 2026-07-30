package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DownloadHistory::class,
        ApiConfigEntity::class,
        QrHistoryEntity::class,
        SavedItem::class,
        WebApiConfigEntity::class,
        ApiKeyEntity::class,
        ProcessHistoryEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
    abstract fun apiConfigDao(): ApiConfigDao
    abstract fun qrHistoryDao(): QrHistoryDao
    abstract fun savedItemDao(): SavedItemDao
    abstract fun webApiConfigDao(): WebApiConfigDao
    abstract fun apiKeyDao(): ApiKeyDao
    abstract fun processHistoryDao(): ProcessHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "atbkz_tools_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
