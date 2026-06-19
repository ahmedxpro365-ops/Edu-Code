package com.educode.app.di

import android.app.Application
import androidx.room.Room
import com.educode.app.data.local.AppDatabase
import com.educode.app.data.repository.FirebaseAuthRepository
import com.educode.app.data.repository.FirebaseUserRepository
import com.educode.app.data.repository.FirebaseCourseRepository
import com.educode.app.data.repository.FirebaseShopRepository
import com.educode.app.data.repository.LearnRepository
import com.educode.app.data.repository.FirebaseBugReportRepository
import com.educode.app.core.notification.EduCodeNotificationManager
import com.educode.app.domain.repository.AuthRepository
import com.educode.app.domain.repository.UserRepository
import com.educode.app.domain.repository.CourseRepository
import com.educode.app.domain.repository.ShopRepository
import com.educode.app.domain.repository.BugReportRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Simple Service Locator for Dependency Injection
object AppModule {
    
    // Application Context
    private lateinit var application: Application

    fun initialize(app: Application) {
        application = app
    }

    // Firebase Instances
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    
    // Room Database
    private val appDatabase by lazy {
        Room.databaseBuilder(
            application,
            AppDatabase::class.java,
            "educode_database"
        )
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
    }
    
    // Repositories
    val authRepository: AuthRepository by lazy { FirebaseAuthRepository(auth, firestore) }
    val userRepository: UserRepository by lazy { FirebaseUserRepository(firestore) }
    val courseRepository: CourseRepository by lazy { FirebaseCourseRepository(firestore) }
    val shopRepository: ShopRepository by lazy { FirebaseShopRepository(firestore) }
    val learnRepository: LearnRepository by lazy { LearnRepository(appDatabase.learnDao()) }
    val bugReportRepository: BugReportRepository by lazy { FirebaseBugReportRepository(firestore) }
    
    val notificationManager: EduCodeNotificationManager by lazy { EduCodeNotificationManager(application) }
}

