package com.educode.app.domain.repository

import com.educode.app.domain.models.*
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun linkGuestToGoogle(idToken: String): Result<User>
    suspend fun signInAsGuest(): Result<User>
    suspend fun signOut(): Result<Unit>
    fun getCurrentUserId(): String?
    fun getAuthState(): Flow<Boolean>
}

interface UserRepository {
    suspend fun getUserProfile(userId: String): Result<User>
    suspend fun updateUserProfile(user: User): Result<Unit>
    fun getUserProfileFlow(userId: String): Flow<User?>
    
    suspend fun updateXPAndCoins(userId: String, xp: Int, coins: Int): Result<Unit>
    suspend fun updateHearts(userId: String, hearts: Int, lastHeartRestoreTime: Long = 0L): Result<Unit>
    
    suspend fun addCertificate(userId: String, certificate: Certificate): Result<Unit>
    suspend fun updateNotificationSettings(userId: String, settings: NotificationSettings): Result<Unit>
    suspend fun markCourseCompleted(userId: String, courseId: String): Result<Unit>
}

interface CourseRepository {
    suspend fun getCourses(): Result<List<Course>>
    suspend fun getLessonsForCourse(courseId: String): Result<List<Lesson>>
    suspend fun getChallenges(): Result<List<Challenge>>
}

interface ShopRepository {
    suspend fun getShopItems(): Result<List<ShopItem>>
    suspend fun purchaseItem(userId: String, item: ShopItem): Result<Unit>
    suspend fun equipItem(userId: String, item: ShopItem): Result<Unit>
    suspend fun unequipItem(userId: String, category: ItemCategory): Result<Unit>
}
