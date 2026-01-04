package com.example.myapplication.network

import com.example.myapplication.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    // ---------------- AUTH ----------------
    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("/api/auth/login")
    suspend fun login(@Body credentials: LoginRequest): AuthResponse


    // --------------- INCIDENTS ---------------
    @POST("/api/incidents")
    suspend fun reportIncident(@Body request: IncidentRequest): Incident

    @GET("/api/incidents/user")
    suspend fun getMyIncidents(): IncidentListResponse


    // --------------- SOS (PUBLIC) ---------------
    @POST("/api/sos")
    suspend fun sendSOS(@Body sos: SOSRequest): SOSResponse
}


// -------- AUTH MODELS --------
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String
)

// -------- INCIDENT LIST RESPONSE --------
data class IncidentListResponse(
    val incidents: List<Incident>,
    val total: Int,
    val page: Int,
    val page_size: Int
)
