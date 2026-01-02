package com.example.myapplication.utils

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import com.example.myapplication.accessibility.AccessibilityManager

/**
 * FEATURE 1: BLIND USER INTERACTION
 * Handles custom gestures for blind mode navigation.
 * 
 * FINAL MAPPING (STRICT):
 * - Double Tap: SOS
 * - Long Press: Voice (Sensa)
 * - Swipes: REMOVED
 */
class GestureManager(
    context: Context,
    private val accessibilityManager: AccessibilityManager,
    private val onDoubleTap: () -> Unit,
    private val onLongPress: () -> Unit
) : View.OnTouchListener {

    private val gestureDetector = GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
        
        override fun onDoubleTap(e: MotionEvent): Boolean {
            onDoubleTap()
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            onLongPress()
        }

        // Swipes removed explicitly
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            return false
        }
    })

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event != null) {
            gestureDetector.onTouchEvent(event)
        }
        // Return true to indicate we processed it (or tried to), 
        // but MainActivity might need to return false to let buttons work.
        // The GestureDetector doesn't return the consumption of onTouch directly well.
        // We will rely on the callbacks.
        return true
    }
}
