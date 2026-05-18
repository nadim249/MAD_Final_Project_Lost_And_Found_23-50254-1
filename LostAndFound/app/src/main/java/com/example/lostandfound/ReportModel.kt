package com.example.lostandfound

import java.io.Serializable

data class ReportModel(
    var id: String = "",
    val itemId: String = "",
    val itemTitle: String = "",
    val reportedBy: String = "",
    val reportedByEmail: String = "",
    val reason: String = "",
    val timestamp: Long = 0L,
    val status: String = "Pending"
) : Serializable