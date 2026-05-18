package com.example.lostandfound


data class NotificationModel(
    val notificationId: String = "",
    val title: String = "",
    val body: String = "",
    val timestamp: Long = 0L,
    val type: String = "",
    val referenceId: String = ""
)