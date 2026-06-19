package com.educode.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.educode.app.data.local.entity.ChapterEntity
import com.educode.app.data.local.entity.CourseEntity
import com.educode.app.data.local.entity.LessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LearnDao {
    @Query("SELECT * FROM courses WHERE language = :language ORDER BY orderIndex ASC")
    fun getCoursesByLanguage(language: String): Flow<List<CourseEntity>>

    @Query("SELECT * FROM chapters WHERE courseId = :courseId ORDER BY orderIndex ASC")
    fun getChaptersForCourse(courseId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM lessons WHERE chapterId = :chapterId ORDER BY orderIndex ASC")
    fun getLessonsForChapter(chapterId: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    suspend fun getLessonById(lessonId: String): LessonEntity?

    @Query("UPDATE lessons SET isCompleted = 1 WHERE id = :lessonId")
    suspend fun markLessonCompleted(lessonId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonEntity>)
    
    @Query("SELECT COUNT(*) FROM lessons WHERE chapterId = :chapterId AND isCompleted = 1")
    suspend fun getCompletedLessonsCountInChapter(chapterId: String): Int

    @Query("SELECT * FROM chapters WHERE id = :chapterId")
    suspend fun getChapterById(chapterId: String): ChapterEntity?

    @Query("SELECT l.* FROM lessons l INNER JOIN chapters c ON l.chapterId = c.id WHERE c.courseId = :courseId")
    suspend fun getLessonsForCourseSync(courseId: String): List<LessonEntity>
}
