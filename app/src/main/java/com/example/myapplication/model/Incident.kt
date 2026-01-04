package com.example.myapplication.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Incident(
    val id: String,
    @SerializedName("type")
    val category: String,
    val description: String,
    @SerializedName("lat")
    val latitude: Double,
    @SerializedName("lng")
    val longitude: Double,
    @SerializedName("created_at")
    val timestamp: Date,
    val status: String,
    @SerializedName("risk_score")
    val riskScore: Double?,
    @SerializedName("risk_level")
    val riskLevel: String?
)
