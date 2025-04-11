package com.example.advancedlandslideapp.models

data class ForumPost(
    val id: String = "",
    val uid: String = "",
    val userName: String = "",
    val content: String = "",
    val timestamp: String = "",
    val trusted: Boolean = false,
    val userProfileUrl: String = ""
)

data class Incident(
    val id: String = "",
    val uid: String = "",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val imageUrl: String = "",
    val timestamp: String = "",
    val trusted: Boolean = false
)