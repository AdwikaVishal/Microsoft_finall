package com.example.myapplication.network

import com.example.myapplication.model.Incident
import com.example.myapplication.model.IncidentRequest
import com.example.myapplication.model.SOS
import com.example.myapplication.model.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    // TODO: Later move auth to Azure AD B2C
    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("/api/auth/login")
    suspend fun login(@Body credentials: LoginRequest): AuthResponse

    // TODO: Host backend on Azure App Service
    @POST("/api/incidents")
    suspend fun reportIncident(@Body request: IncidentRequest): Incident

    @GET("/api/incidents/user")
    suspend fun getMyIncidents(): List<Incident>

    // TODO: Later integrate Azure Notification Hub for emergency push alerts
    @POST("/api/sos")
    suspend fun sendSOS(@Body sos: SOS): SOSEvent

}

data class LoginRequest(val email: String, val pass: String)

data class RegisterRequest(val name: String, val email: String, val password: String)

data class AuthResponse(val token: String)

data class SOSEvent(val sosId: String, val status: String)
