package com.example.myapplication.network

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // TODO: Move base URL to Azure once deployed
    // Use 10.0.2.2 for Android Emulator to access localhost
    private const val BASE_URL = "http://192.168.0.130:8000"
    
    // Demo/test credentials for development
    const val DEMO_EMAIL = "demo@sensesafe.app"
    const val DEMO_PASSWORD = "demo123"
    const val DEMO_TOKEN = "demo_token_for_testing_only"

    // Store the auth token (in memory)
    private var authToken: String? = null

    fun setAuthToken(token: String?) {
        authToken = token
    }

    val instance: ApiService by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                
                authToken?.let {
                    requestBuilder.header("Authorization", "Bearer $it")
                }
                
                val request = requestBuilder.build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        // Configure Gson to handle ISO 8601 date format
        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        retrofit.create(ApiService::class.java)
    }
}
