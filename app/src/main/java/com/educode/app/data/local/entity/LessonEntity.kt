package com.educode.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: String,
    val chapterId: String,
    val title: String,
    val description: String,
    val contentMarkdown: String, // Rich text content or markdown
    val difficulty: String, // Beginner, Intermediate, Advanced
    val estimatedDurationMinutes: Int,
    val xpReward: Int,
    val orderIndex: Int,
    val sourceName: String?, // MDN, W3Schools, etc.
    val sourceUrl: String?,
    val isCompleted: Boolean = false,
    val codeExamplesJson: String? = null, // Serialized JSON list of examples
    val lastUpdatedDate: Long = System.currentTimeMillis()
)
