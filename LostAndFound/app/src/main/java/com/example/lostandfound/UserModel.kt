package com.example.lostandfound

import java.io.Serializable

data class UserModel(
    val name: String,
    val email: String,
    val initials: String,
    val stats: String,
    val status: String
) : Serializable
