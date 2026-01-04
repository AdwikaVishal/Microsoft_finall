package com.example.myapplication.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Repository for location operations
 * 
 * Abstracts FusedLocationProviderClient for:
 * - Last known location (fast)
 * - Current location (fresh, single update)
 * - Live location updates (optional)
 * 
 * Uses:
 * - Coroutines for async operations
 * - Timeout handling (10 seconds)
 * - Battery-optimized location requests
 */
class LocationRepository(context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Get last known location (fast, may be stale)
     * 
     * @return Location or null if unavailable
     */
    @SuppressLint("MissingPermission")
    suspend fun getLastKnownLocation(): Location? {
        return try {
            fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get last known location", e)
            null
        }
    }

    /**
     * Get current location with fresh update (slower, accurate)
     * 
     * Uses:
     * - High accuracy priority
     * - Single update
     * - 10 second timeout
     * 
     * @return Location or null if timeout/error
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        return withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
            suspendCancellableCoroutine { continuation ->
                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    UPDATE_INTERVAL_MS
                ).apply {
                    setMinUpdateIntervalMillis(FASTEST_INTERVAL_MS)
                    setMaxUpdates(1) // Single update
                }.build()

                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        fusedLocationClient.removeLocationUpdates(this)
                        val location = result.lastLocation
                        if (continuation.isActive) {
                            continuation.resume(location)
                        }
                    }
                }

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )

                continuation.invokeOnCancellation {
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                }
            }
        }
    }

    companion object {
        private const val TAG = "LocationRepository"
        private const val UPDATE_INTERVAL_MS = 5000L // 5 seconds
        private const val FASTEST_INTERVAL_MS = 2000L // 2 seconds
        private const val LOCATION_TIMEOUT_MS = 10000L // 10 seconds
    }
}
