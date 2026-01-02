package com.example.myapplication.model

import java.util.Date

data class Incident(
    val id: String,
    val category: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Date,
    val status: String,
    val riskScore: Double?,
    val riskLevel: String?
)
