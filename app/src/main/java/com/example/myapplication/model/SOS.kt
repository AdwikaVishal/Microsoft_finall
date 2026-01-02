package com.example.myapplication.model

import java.util.Date

data class SOS(
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Date,
    val status: SOSStatus,
    val batteryPercentage: Int,
    val abilityType: AbilityType
)

enum class SOSStatus {
    IM_TRAPPED,
    IM_INJURED,
    I_NEED_HELP,
    IM_SAFE
}
