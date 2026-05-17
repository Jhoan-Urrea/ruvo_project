package com.example.ruvo_app.domain.model

data class User(
    val id: String = "",
    val fullName: String,
    val phone: String = "",
    val username: String = "",
    val email: String,
    val profilePictureUrl: String? = null,
    val role: UserRole = UserRole.USER,
    val status: AccountStatus = AccountStatus.ACTIVE,
    val lastActive: String = "",
    val location: Location? = null,
    val reputation: Reputation = Reputation(),
    val stats: UserStats = UserStats()
)

enum class UserRole {
    USER,
    MODERATOR
}

enum class AccountStatus {
    ACTIVE,
    WARNING,
    BLOCKED
}

data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)

data class Reputation(
    val points: Int = 0,
    val rating: Float = 0f,
    val level: UserLevel = UserLevel.PRINCIPIANTE,
    val badges: List<String> = emptyList()
) {
    // Lógica para la barra de progreso
    fun getProgressToNextLevel(): Float {
        val currentLevelMin = level.minPoints
        val nextLevelMin = level.next()?.minPoints ?: (currentLevelMin * 2) // Si es Maestro, duplicamos
        val range = nextLevelMin - currentLevelMin
        val progress = points - currentLevelMin
        return (progress.toFloat() / range.toFloat()).coerceIn(0f, 1f)
    }
    
    fun getPointsNeededForNextLevel(): Int {
        val nextLevelMin = level.next()?.minPoints ?: return 0
        return nextLevelMin - points
    }
}

enum class UserLevel(val minPoints: Int) {
    PRINCIPIANTE(0),
    PROFESIONAL(501),
    EXPERTO(1501),
    MAESTRO(4001);

    fun next(): UserLevel? = entries.getOrNull(ordinal + 1)
    
    companion object {
        fun fromPoints(points: Int): UserLevel {
            return entries.findLast { points >= it.minPoints } ?: PRINCIPIANTE
        }
    }
}

data class UserStats(
    val activePosts: Int = 0,
    val finishedPosts: Int = 0,
    val pendingVerification: Int = 0,
    val reportsCount: Int = 0,
    val totalReviews: Int = 0
)
