package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.DangerZone
import com.example.data.model.EmergencyContact
import com.example.data.model.Incident
import com.example.data.model.SafePlace
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SafeGuardRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google.")
    }

    suspend fun getUserProfile(userId: String): UserProfile? {
        return try {
            val doc = db.collection("users").document(userId).get().await()
            doc.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: Exception) {
            Log.e("Repository", "Error fetching user profile", e)
            null
        }
    }

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid)
            val payload = mapOf(
                "userId" to uid,
                "name" to profile.name,
                "phone" to profile.phone,
                "emergencyMessage" to profile.emergencyMessage,
                "medicalInfo" to profile.medicalInfo,
                "guardianPin" to profile.guardianPin,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload, com.google.firebase.firestore.SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("Repository", "Error saving user profile", e)
            Result.failure(e)
        }
    }

    fun observeEmergencyContacts(): Flow<List<EmergencyContact>> = callbackFlow {
        val uid = try { requireUserId() } catch (e: Exception) { close(e); return@callbackFlow }
        val listener = db.collection("users").document(uid).collection("emergency_contacts")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val contacts = snapshot.toObjects(EmergencyContact::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    trySend(contacts)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun addEmergencyContact(contact: EmergencyContact): Result<Unit> {
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid).collection("emergency_contacts").document()
            val payload = mapOf(
                "userId" to uid,
                "name" to contact.name,
                "phone" to contact.phone,
                "relationship" to contact.relationship,
                "priority" to contact.priority,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteEmergencyContact(contactId: String): Result<Unit> {
        return try {
            val uid = requireUserId()
            db.collection("users").document(uid).collection("emergency_contacts").document(contactId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeIncidents(): Flow<List<Incident>> = callbackFlow {
        val uid = try { requireUserId() } catch (e: Exception) { close(e); return@callbackFlow }
        val listener = db.collection("users").document(uid).collection("incidents")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val incidents = snapshot.toObjects(Incident::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    trySend(incidents)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun logIncident(incident: Incident): Result<Unit> {
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid).collection("incidents").document()
            val payload = mapOf(
                "userId" to uid,
                "timestamp" to FieldValue.serverTimestamp(),
                "latitude" to incident.latitude,
                "longitude" to incident.longitude,
                "address" to incident.address,
                "status" to incident.status,
                "notes" to incident.notes,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeSafePlaces(): Flow<List<SafePlace>> = callbackFlow {
        val listener = db.collection("safe_places")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val places = snapshot.toObjects(SafePlace::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    trySend(places)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun addSafePlace(place: SafePlace): Result<Unit> {
        return try {
            val docRef = db.collection("safe_places").document()
            val payload = mapOf(
                "name" to place.name,
                "category" to place.category,
                "address" to place.address,
                "latitude" to place.latitude,
                "longitude" to place.longitude,
                "phone" to place.phone,
                "verified" to place.verified,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeDangerZones(): Flow<List<DangerZone>> = callbackFlow {
        val listener = db.collection("danger_zones")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val zones = snapshot.toObjects(DangerZone::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    trySend(zones)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun reportDangerZone(zone: DangerZone): Result<Unit> {
        return try {
            val uid = requireUserId()
            val docRef = db.collection("danger_zones").document()
            val payload = mapOf(
                "title" to zone.title,
                "description" to zone.description,
                "category" to zone.category,
                "latitude" to zone.latitude,
                "longitude" to zone.longitude,
                "radiusMeters" to zone.radiusMeters,
                "reportedBy" to uid,
                "upvotes" to 1L,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun upvoteDangerZone(zoneId: String, currentUpvotes: Long): Result<Unit> {
        return try {
            db.collection("danger_zones").document(zoneId)
                .update("upvotes", currentUpvotes + 1L)
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
