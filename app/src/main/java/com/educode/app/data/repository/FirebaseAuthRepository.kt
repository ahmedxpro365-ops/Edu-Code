package com.educode.app.data.repository

import com.educode.app.domain.models.User
import com.educode.app.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw Exception("User is null")
            
            // Check if user exists in Firestore
            val docRef = firestore.collection("users").document(firebaseUser.uid)
            val doc = docRef.get().await()
            
            var user = User(
                id = firebaseUser.uid,
                name = firebaseUser.displayName ?: "New User",
                email = firebaseUser.email ?: "",
                profileImageUrl = firebaseUser.photoUrl?.toString()
            )
            
            if (!doc.exists()) {
                docRef.set(user).await()
            } else {
                user = doc.toObject(User::class.java)!!
            }
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInAsGuest(): Result<User> {
        return try {
            val authResult = auth.signInAnonymously().await()
            val firebaseUser = authResult.user ?: throw Exception("User is null")
            
            val user = User(
                id = firebaseUser.uid,
                name = "Guest",
                email = ""
            )
            firestore.collection("users").document(user.id).set(user).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun linkGuestToGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.currentUser?.linkWithCredential(credential)?.await()
            val firebaseUser = auth.currentUser ?: throw Exception("User is null")
            
            // Migrate guest data to Google account here if necessary
            // For now, return the user
            val docRef = firestore.collection("users").document(firebaseUser.uid)
            val doc = docRef.get().await()
            val user = doc.toObject(User::class.java) ?: User(
                id = firebaseUser.uid,
                name = firebaseUser.displayName ?: "New User",
                email = firebaseUser.email ?: "",
                profileImageUrl = firebaseUser.photoUrl?.toString()
            )

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override fun getAuthState(): Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser != null)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }
}
