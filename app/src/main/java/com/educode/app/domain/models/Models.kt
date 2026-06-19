package com.educode.app.domain.models

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val xp: Int = 0,
    val coins: Int = 0,
    val hearts: Int = 5,
    val rank: String = "Beginner",
    val profileImageUrl: String? = null,
    val lastHeartRestoreTime: Long = 0L,
    val certificates: List<Certificate> = emptyList(),
    val notificationSettings: NotificationSettings = NotificationSettings(),
    val purchasedItemIds: List<String> = emptyList(),
    val equippedItems: EquippedItems = EquippedItems()
)

data class EquippedItems(
    val themeId: String? = null,
    val avatarId: String? = null,
    val frameId: String? = null,
    val effectId: String? = null
)

data class NotificationSettings(
    val enabled: Boolean = true,
    val dailyReminder: Boolean = true,
    val reminderTime: String = "09:00",
    val lessonReminders: Boolean = true,
    val challengeReminders: Boolean = true,
    val rewardReminders: Boolean = true,
    val taskReminders: Boolean = true,
    val newAchievements: Boolean = true,
    val newCertificates: Boolean = true
)

data class BugReport(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val appVersion: String = "",
    val androidVersion: String = "",
    val deviceModel: String = "",
    val errorDetails: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class UserProgress(
    val completedChallenges: List<String> = emptyList(),
    val unlockedLessons: List<String> = emptyList(),
    val completedCourses: List<String> = emptyList()
)

data class Course(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val order: Int = 0
)

data class Lesson(
    val id: String = "",
    val courseId: String = "",
    val title: String = "",
    val content: String = "",
    val xpReward: Int = 10,
    val order: Int = 0
)

data class Challenge(
    val id: String = "",
    val title: String = "",
    val question: String = "",
    val correctAnswers: List<String> = emptyList(),
    val xpReward: Int = 50,
    val coinReward: Int = 10
)

data class Certificate(
    val id: String = "",
    val userId: String = "",
    val courseId: String = "",
    val title: String = "",
    val issueDate: Long = 0L,
    val certificateNumber: String = ""
)

data class ShopItem(
    val id: String = "",
    val category: ItemCategory = ItemCategory.THEME,
    val name: String = "",
    val description: String = "",
    val price: Int = 0,
    val imageUrl: String? = null,
    val rarity: String = "Common" // Common, Rare, Epic, Legendary
)

enum class ItemCategory {
    THEME, AVATAR, FRAME, EFFECT
}
