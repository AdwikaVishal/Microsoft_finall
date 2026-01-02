package com.example.myapplication.network

import com.example.myapplication.data.DisasterPayload
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 5️⃣ Rescue Mode
 * Retrofit interface to send data to backend.
 */
interface RescueService {
    @POST("rescue/alert")
    suspend fun sendAlert(@Body payload: DisasterPayload): Response<Unit>
}
