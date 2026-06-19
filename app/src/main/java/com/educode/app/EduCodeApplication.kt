package com.educode.app

import android.app.Application
import com.educode.app.core.error.GlobalExceptionHandler
import com.educode.app.di.AppModule
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

class EduCodeApplication : Application() {
    companion object {
        lateinit var instance: EduCodeApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize AppModule explicitly
        AppModule.initialize(this)

        // Initialize Global Exception Handler
        GlobalExceptionHandler.initialize(this)
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Initialize App Check
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
    }
}
