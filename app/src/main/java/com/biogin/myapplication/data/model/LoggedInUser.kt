package com.biogin.myapplication.data.model

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class LoggedInUser(
    val userDni: String,
    val displayName: String
)