package com.educode.app.data.repository

import com.educode.app.domain.models.*
import com.educode.app.domain.repository.UserRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import java.util.Calendar

class FirebaseUserRepository(
    private val firestore: FirebaseFirestore
) : UserRepository {

    private suspend fun applyHeartRestoration(user: User): User {
        // ... (rest of the method remains the same)
        if (user.hearts >= 5) {
            if (user.lastHeartRestoreTime != 0L) {
                try {
                    firestore.collection("users").document(user.id).update("lastHeartRestoreTime", 0L).await()
                } catch (e: Exception) {
                    // ignore
                }
                return user.copy(lastHeartRestoreTime = 0L)
            }
            return user
        }

        val currentTime = System.currentTimeMillis()
        val lastRestore = user.lastHeartRestoreTime
        if (lastRestore == 0L) {
            try {
                firestore.collection("users").document(user.id).update("lastHeartRestoreTime", currentTime).await()
            } catch (e: Exception) {
                // ignore
            }
            return user.copy(lastHeartRestoreTime = currentTime)
        }

        val twoHoursInMillis = 2L * 60L * 60L * 1000L
        val diff = currentTime - lastRestore
        if (diff >= twoHoursInMillis) {
            val restoredCount = (diff / twoHoursInMillis).toInt()
            if (restoredCount > 0) {
                val newHearts = minOf(5, user.hearts + restoredCount)
                val newLastRestoreTime = if (newHearts >= 5) 0L else lastRestore + (restoredCount * twoHoursInMillis)
                try {
                    firestore.collection("users").document(user.id).update(
                        "hearts", newHearts,
                        "lastHeartRestoreTime", newLastRestoreTime
                    ).await()
                } catch (e: Exception) {
                    // ignore
                }
                return user.copy(hearts = newHearts, lastHeartRestoreTime = newLastRestoreTime)
            }
        }
        return user
    }

    override suspend fun getUserProfile(userId: String): Result<User> {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            val rawUser = doc.toObject(User::class.java) ?: throw Exception("User not found")
            val finalUser = applyHeartRestoration(rawUser)
            Result.success(finalUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUserProfileFlow(userId: String): Flow<User?> = callbackFlow {
        val subscription = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val rawUser = snapshot.toObject(User::class.java)
                    if (rawUser != null) {
                        launch {
                            val finalUser = applyHeartRestoration(rawUser)
                            trySend(finalUser)
                        }
                    } else {
                        trySend(null)
                    }
                } else {
                    trySend(null)
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            firestore.collection("users").document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateXPAndCoins(userId: String, xp: Int, coins: Int): Result<Unit> {
        return try {
            firestore.collection("users").document(userId).update(
                "xp", FieldValue.increment(xp.toLong()),
                "coins", FieldValue.increment(coins.toLong())
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateHearts(userId: String, hearts: Int, lastHeartRestoreTime: Long): Result<Unit> {
        return try {
            firestore.collection("users").document(userId).update(
                "hearts", hearts,
                "lastHeartRestoreTime", lastHeartRestoreTime
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addCertificate(userId: String, certificate: Certificate): Result<Unit> {
        return try {
            firestore.collection("users").document(userId).update(
                "certificates", FieldValue.arrayUnion(certificate)
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateNotificationSettings(userId: String, settings: NotificationSettings): Result<Unit> {
        return try {
            firestore.collection("users").document(userId).update(
                "notificationSettings", settings
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markCourseCompleted(userId: String, courseId: String): Result<Unit> {
        return try {
            val userProgressDoc = firestore.collection("userProgress").document(userId).get().await()
            if (!userProgressDoc.exists()) {
                firestore.collection("userProgress").document(userId).set(
                    UserProgress(completedCourses = listOf(courseId))
                ).await()
            } else {
                firestore.collection("userProgress").document(userId).update(
                    "completedCourses", FieldValue.arrayUnion(courseId)
                ).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
