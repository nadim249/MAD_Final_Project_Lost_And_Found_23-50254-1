package com.example.lostandfound

import java.io.Serializable

data class ItemModel(
    var id: String = "",
    val title: String = "",
    val description: String = "",
    val type: String = "", // "Lost" or "Found"
    val location: String = "",
    val date: String = "",
    val postedBy: String = "",
    val postedByEmail: String = "",
    val status: String = "Active",
    val imagePath: String = "bell",
    val color: String = "",
    val brand: String = "",
    val size: String = ""
) : Serializable