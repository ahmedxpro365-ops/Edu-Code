package com.educode.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val language: String,
    val coverImage: String?,
    val totalChapters: Int,
    val totalLessons: Int,
    val orderIndex: Int
)
