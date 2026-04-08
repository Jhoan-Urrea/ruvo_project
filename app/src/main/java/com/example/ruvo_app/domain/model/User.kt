package com.example.ruvo_app.domain.model

data class User(
    val id: String = "",
    val fullName: String,
    val phone: String = "",
    val username: String = "",
    val email: String,
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
)

enum class UserLevel {
    PRINCIPIANTE,
    PROFESIONAL,
    EXPERTO,
    MAESTRO
}

data class UserStats(
    val activePosts: Int = 0,
    val finishedPosts: Int = 0,
    val pendingVerification: Int = 0,
    val reportsCount: Int = 0
)