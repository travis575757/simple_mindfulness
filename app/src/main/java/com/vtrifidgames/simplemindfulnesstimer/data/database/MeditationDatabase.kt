package com.vtrifidgames.simplemindfulnesstimer.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MeditationSession::class], version = 1, exportSchema = false)
abstract class MeditationDatabase : RoomDatabase() {

    abstract fun meditationSessionDao(): MeditationSessionDao

    companion object {
        @Volatile
        private var INSTANCE: MeditationDatabase? = null

        fun getDatabase(context: Context): MeditationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MeditationDatabase::class.java,
                    "meditation_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

