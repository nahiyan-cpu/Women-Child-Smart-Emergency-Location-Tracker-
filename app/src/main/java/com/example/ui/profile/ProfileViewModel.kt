package com.example.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserProfile
import com.example.data.repository.SafeGuardRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SafeGuardRepository(application)

    private val _profile = MutableStateFlow(UserProfile())
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val uid = Firebase.auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val fetched = repository.getUserProfile(uid)
            if (fetched != null) {
                _profile.value = fetched
            }
        }
    }

    fun saveProfile(name: String, phone: String, message: String, medical: String, pin: String) {
        val uid = Firebase.auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val updated = UserProfile(
                userId = uid,
                name = name,
                phone = phone,
                emergencyMessage = message,
                medicalInfo = medical,
                guardianPin = pin
            )
            repository.saveUserProfile(updated)
            _profile.value = updated
        }
    }
}
