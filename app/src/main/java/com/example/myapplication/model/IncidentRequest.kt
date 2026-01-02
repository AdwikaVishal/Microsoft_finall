package com.example.myapplication.model

data class IncidentRequest(
    val type: String,
    val description: String,
    val lat: Double,
    val lng: Double,
    val image_url: String? = null
)
