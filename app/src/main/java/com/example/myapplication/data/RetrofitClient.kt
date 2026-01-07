package com.example.myapplication.data

import com.example.myapplication.rescue.RescueService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton Retrofit Client.
 * 
 * For Android Emulator: Use 10.0.2.2 to access localhost
 * For Physical Device: Use your machine's IP address (e.g., http://192.168.1.x:8000/)
 * For Production: Use "https://sensesafe-c9c8bpend7cceeh7.eastasia-01.azurewebsites.net"
 */
object RetrofitClient {
    // Your Mac's IP address for physical device testing
private const val BASE_URL = "https://sensesafe-c9c8bpend7cceeh7.eastasia-01.azurewebsites.net"

    val instance: RescueService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RescueService::class.java)
    }
}
