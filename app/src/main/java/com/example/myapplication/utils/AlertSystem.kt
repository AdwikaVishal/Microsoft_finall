package com.example.myapplication.utils

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import com.example.myapplication.data.UserStatus

/**
 * FEATURE 3 & UPGRADE 4: ELDERLY SOS + FAMILY NOTIFICATION
 * Handles sending alerts to family members via SMS/Notification.
 * 
 * Improvements:
 * - Added logging for confirmation steps
 * - Robust error handling
 */
object AlertSystem {

    private const val FAMILY_CONTACT = "5551234567" // Mock contact number

    fun sendFamilyAlert(context: Context, status: UserStatus, location: String) {
        val message = "EMERGENCY: User is ${status.description} at $location. Please help!"
        
        // 1. Send SMS (Requires SEND_SMS permission, handled gracefully if missing)
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(FAMILY_CONTACT, null, message, null, null)
            Log.d("AlertSystem", "SMS sent to family: $message")
        } catch (e: Exception) {
            Log.e("AlertSystem", "Failed to send SMS", e)
        }

        // 2. Mock Push Notification to Caregiver App
        // In real app, this would hit a backend API to push to family's device
        Log.d("AlertSystem", "Push notification trigger: $message")
    }
}
