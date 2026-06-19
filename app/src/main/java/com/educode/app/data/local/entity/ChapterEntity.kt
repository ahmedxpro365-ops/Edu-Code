package com.educode.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val title: String,
    val description: String,
    val orderIndex: Int
)
