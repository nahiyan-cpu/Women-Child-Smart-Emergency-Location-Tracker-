package com.example.ui.contacts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.EmergencyContact
import com.example.data.model.UiState
import com.example.data.repository.SafeGuardRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContactsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SafeGuardRepository(application)

    val contactsState: StateFlow<UiState<List<EmergencyContact>>> = repository.observeEmergencyContacts()
        .map<List<EmergencyContact>, UiState<List<EmergencyContact>>> { UiState.Success(it) }
        .catch { e -> emit(UiState.Error(e.message ?: "Failed to load contacts")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    fun addContact(name: String, phone: String, relationship: String) {
        viewModelScope.launch {
            repository.addEmergencyContact(
                EmergencyContact(
                    name = name,
                    phone = phone,
                    relationship = relationship,
                    priority = 1L
                )
            )
        }
    }

    fun deleteContact(contactId: String) {
        viewModelScope.launch {
            repository.deleteEmergencyContact(contactId)
        }
    }
}
