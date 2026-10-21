package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AnalysisDao
import com.example.data.entity.AnalysisEntity

@Database(entities = [AnalysisEntity::class], version = 1, exportSchema = false)
abstract class DeepShieldDatabase : RoomDatabase() {
    abstract fun analysisDao(): AnalysisDao

    companion object {
        @Volatile
        private var INSTANCE: DeepShieldDatabase? = null

        fun getDatabase(context: Context): DeepShieldDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DeepShieldDatabase::class.java,
                    "deepshield_forensics.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
