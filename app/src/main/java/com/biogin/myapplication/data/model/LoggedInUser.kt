package com.biogin.myapplication.data.model

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class LoggedInUser(
    var dni: String,
    val name: String,
    val surname: String,
    val email: String,
    val category: String,
    val state: String,
    val institutes: ArrayList<String>,
    val areasAllowed: ArrayList<String>,

    )