package com.example.lostandfound


import java.io.Serializable

data class MessageModel(
    val messageId: String = "",
    val senderId: String = "",
    val messageText: String = "",
    val timestamp: Long = 0L
)

data class ChatThreadModel(
    val peerId: String = "",
    val peerName: String = "",
    val lastMessage: String = "",
    val timestamp: Long = 0L
) : Serializable