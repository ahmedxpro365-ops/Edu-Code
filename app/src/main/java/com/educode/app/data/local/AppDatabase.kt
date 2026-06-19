package com.educode.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.educode.app.data.local.entity.ChapterEntity
import com.educode.app.data.local.entity.CourseEntity
import com.educode.app.data.local.entity.LessonEntity

@Database(
    entities = [
        CourseEntity::class,
        ChapterEntity::class,
        LessonEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun learnDao(): LearnDao
}
