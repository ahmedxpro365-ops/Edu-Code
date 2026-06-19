package com.educode.app.domain.models

data class RankInfo(
    val rank: String,
    val arabicTitle: String,
    val minXp: Int,
    val maxXp: Int,
    val hexColor: String,
    val bonusCoinsReward: Int
)

object ProgressSystem {
    val RANKS = listOf(
        RankInfo("Beginner", "مبتدئ الكود (Beginner)", 0, 99, "#00E5FF", 0),
        RankInfo("Junior Developer", "مطور جونيور (Junior Dev)", 100, 499, "#29B6F6", 50),
        RankInfo("Developer", "مطور كوني (Developer)", 500, 999, "#AB47BC", 100),
        RankInfo("Senior Developer", "مجهر الأنظمة (Senior Dev)", 1000, 1999, "#EC407A", 200),
        RankInfo("Expert", "خبير منطقي (Expert)", 2000, 3999, "#FF7043", 350),
        RankInfo("Master", "أستاذ الخوارزميات (Master)", 4000, 7999, "#FFCA28", 500),
        RankInfo("Code Legend", "أسطورة الشفرة المظلمة (Code Legend)", 8000, Int.MAX_VALUE, "#39FF14", 1000)
    )

    fun getRankInfoForXp(xp: Int): RankInfo {
        return RANKS.firstOrNull { xp in it.minXp..it.maxXp } ?: RANKS.last()
    }

    fun getProgressPercent(xp: Int): Float {
        val currentRank = getRankInfoForXp(xp)
        if (currentRank.rank == "Code Legend") return 1f
        val range = currentRank.maxXp - currentRank.minXp + 1
        val progressInside = xp - currentRank.minXp
        return (progressInside.toFloat() / range.toFloat()).coerceIn(0f, 1f)
    }

    fun getXpNeededForNextRank(xp: Int): Int {
        val currentRank = getRankInfoForXp(xp)
        val index = RANKS.indexOf(currentRank)
        if (index >= RANKS.size - 1) return 0
        return RANKS[index + 1].minXp - xp
    }

    fun getArabicTitle(rank: String): String {
        return RANKS.firstOrNull { it.rank == rank }?.arabicTitle ?: "مبتدئ الكود"
    }

    fun getHexColor(rank: String): String {
        return RANKS.firstOrNull { it.rank == rank }?.hexColor ?: "#00E5FF"
    }
}
