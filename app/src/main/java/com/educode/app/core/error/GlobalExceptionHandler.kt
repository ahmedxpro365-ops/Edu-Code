package com.educode.app.core.error

import android.app.Application
import android.content.Intent
import android.os.Process
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlin.system.exitProcess

class GlobalExceptionHandler(
    private val application: Application,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        // Log to Firebase Crashlytics
        try {
            FirebaseCrashlytics.getInstance().recordException(throwable)
        } catch (e: Exception) {
            // Ignore if Firebase fails
        }

        // Start Error Activity
        val intent = Intent(application, ErrorActivity::class.java).apply {
            putExtra(ErrorActivity.EXTRA_ERROR_MESSAGE, throwable.localizedMessage ?: "Unknown Error")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        
        application.startActivity(intent)
        
        // Kill the process so it doesn't show standard system 'App stopped' dialog immediately if we can avoid it
        Process.killProcess(Process.myPid())
        exitProcess(1)
    }
    
    companion object {
        fun initialize(application: Application) {
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler(application, defaultHandler))
        }
    }
}
