package com.example.lostandfound


data class ClaimModel(
    val claimId: String = "",
    val itemId: String = "",
    val itemTitle: String = "",
    val claimedBy: String = "",
    val claimantName: String = "",
    val itemOwnerId: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val status: String = "Pending"
)