package com.biogin.myapplication.data

import kotlinx.serialization.Serializable

@Serializable
data class Institute(val name: String, val areas: ArrayList<String>)
