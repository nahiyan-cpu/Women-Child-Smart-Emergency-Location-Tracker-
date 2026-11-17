package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val userId: String = "",
    val name: String = "",
    val phone: String = "",
    val emergencyMessage: String = "EMERGENCY! I need immediate assistance. My location is being tracked.",
    val medicalInfo: String = "",
    val guardianPin: String = "1234",
    val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class EmergencyContact(
    @DocumentId val contactId: String = "",
    val userId: String = "",
    val name: String = "",
    val phone: String = "",
    val relationship: String = "Family",
    val priority: Long = 1L,
    val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Incident(
    @DocumentId val incidentId: String = "",
    val userId: String = "",
    val timestamp: Timestamp? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val status: String = "ACTIVE",
    val notes: String = "",
    val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class SafePlace(
    @DocumentId val placeId: String = "",
    val name: String = "",
    val category: String = "POLICE", // POLICE, HOSPITAL, SAFE_HOUSE, WOMEN_HELP_CENTER
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val phone: String = "",
    val verified: Boolean = true,
    val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class DangerZone(
    @DocumentId val zoneId: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "HARASSMENT", // HARASSMENT, POOR_LIGHTING, ISOLATED, CRIME
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radiusMeters: Double = 100.0,
    val reportedBy: String = "",
    val upvotes: Long = 1L,
    val createdAt: Timestamp? = null
)
