package com.example.notesbysubject.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Project::class, Material::class, Step::class, Photo::class], version = 1, exportSchema = false)
abstract class LevelMateDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao

    companion object {
        @Volatile private var instance: LevelMateDatabase? = null

        fun getInstance(context: Context): LevelMateDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext, LevelMateDatabase::class.java, "levelmate.db"
            ).build().also { instance = it }
        }
    }
}
