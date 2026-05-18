package com.example.lostandfound

import java.io.Serializable

data class UserModel(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val initials: String = "",
    val status: String = "Active",
    val role: String = "User",
    val stats: UserStats = UserStats()
) : Serializable

data class UserStats(
    val itemsLost: Int = 0,
    val itemsFound: Int = 0,
    val resolved: Int = 0
) : Serializable {
    override fun toString(): String {
        return "Lost: $itemsLost · Found: $itemsFound"
    }
}
