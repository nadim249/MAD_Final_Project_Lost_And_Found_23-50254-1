package com.example.lostandfound


import java.io.Serializable

// Model for an individual text bubble
data class MessageModel(
    val messageId: String = "",
    val senderId: String = "",
    val messageText: String = "",
    val timestamp: Long = 0L
)

// Model for a row item in the Chat List
data class ChatThreadModel(
    val peerId: String = "",
    val peerName: String = "",
    val lastMessage: String = "",
    val timestamp: Long = 0L
) : Serializable