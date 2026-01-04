package com.example.myapplication.model

data class User(
    val id: String,
    val name: String,
    val abilityType: AbilityType,
    val language: String
)

enum class AbilityType {
    BLIND,
    LOW_VISION,
    DEAF,
    HARD_OF_HEARING,
    NON_VERBAL,
    ELDERLY,
    OTHER,
    NONE
}
