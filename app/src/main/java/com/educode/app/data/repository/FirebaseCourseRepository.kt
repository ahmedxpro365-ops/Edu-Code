package com.educode.app.data.repository

import com.educode.app.domain.models.Challenge
import com.educode.app.domain.models.Course
import com.educode.app.domain.models.Lesson
import com.educode.app.domain.repository.CourseRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseCourseRepository(
    private val firestore: FirebaseFirestore
) : CourseRepository {

    override suspend fun getCourses(): Result<List<Course>> {
        return try {
            val result = firestore.collection("courses")
                .orderBy("order")
                .get()
                .await()
            val courses = result.toObjects(Course::class.java)
            Result.success(courses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLessonsForCourse(courseId: String): Result<List<Lesson>> {
        return try {
            val result = firestore.collection("lessons")
                .whereEqualTo("courseId", courseId)
                .orderBy("order")
                .get()
                .await()
            val lessons = result.toObjects(Lesson::class.java)
            Result.success(lessons)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getChallenges(): Result<List<Challenge>> {
        return try {
            val result = firestore.collection("challenges")
                .get()
                .await()
            val challenges = result.toObjects(Challenge::class.java)
            Result.success(challenges)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
