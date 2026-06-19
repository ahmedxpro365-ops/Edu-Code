package com.educode.app.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.educode.app.MainActivity
import com.educode.app.R
import java.util.*
import java.util.concurrent.TimeUnit

class EduCodeNotificationManager(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "educode_notifications"
        private const val CHANNEL_NAME = "إشعارات Edu Code"
        private const val CHANNEL_DESC = "تنبيهات التعلم والتحديات والمكافآت"
        private const val DAILY_REMINDER_TAG = "daily_reminder_task"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(title: String, message: String, notificationId: Int = Random().nextInt()) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use existing icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }

    fun scheduleDailyReminder(time: String) { // format HH:mm
        val parts = time.split(":")
        if (parts.size != 2) return
        
        val hour = parts[0].toIntOrNull() ?: 9
        val min = parts[1].toIntOrNull() ?: 0

        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()

        dueDate.set(Calendar.HOUR_OF_DAY, hour)
        dueDate.set(Calendar.MINUTE, min)
        dueDate.set(Calendar.SECOND, 0)

        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }

        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis

        val dailyWorkRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .addTag(DAILY_REMINDER_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_REMINDER_TAG,
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyWorkRequest
        )
    }

    fun cancelDailyReminder() {
        WorkManager.getInstance(context).cancelAllWorkByTag(DAILY_REMINDER_TAG)
    }

    fun notifyLessonCompleted(lessonTitle: String) {
        showNotification("إنجاز جديد! 🌟", "لقد أتممت نظام $lessonTitle بنجاح. استمر في التقدم!")
    }

    fun notifyChallengeCompleted(reward: String) {
        showNotification("تحدي مكتمل! 🏆", "حصلت على $reward بمناسبة إكمال التحدي اليومي.")
    }

    fun notifyAchievementUnlocked(achievement: String) {
        showNotification("إنجاز مفتوح! 🎖️", "مبروك! لقد حصلت على وسام: $achievement")
    }

    fun notifyCertificateIssued(courseTitle: String) {
        showNotification("شهادة جديدة! 🎓", "تم إصدار شهادة إتمام $courseTitle. تفقد ملفك الشخصي!")
    }
}

class ReminderWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val notificationManager = EduCodeNotificationManager(applicationContext)
        notificationManager.showNotification(
            "وقت البرمجة! 💻",
            "لا تنسى إكمال درس اليوم وتحقيق أهدافك التعليمية."
        )
        return Result.success()
    }
}
