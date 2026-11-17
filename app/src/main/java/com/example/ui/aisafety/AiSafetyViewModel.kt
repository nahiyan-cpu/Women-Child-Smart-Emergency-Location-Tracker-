package com.example.ui.aisafety

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AiSafetyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AiSafetyViewModel : ViewModel() {
    private val repository = AiSafetyRepository()

    private val _advice = MutableStateFlow<String>("Ask me for safety tips, route risk assessment, or emergency guidance.")
    val advice: StateFlow<String> = _advice.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun askSafetyAssistant(prompt: String) {
        if (prompt.isBlank()) return
        viewModelScope.launch {
            _isLoading.value = true
            val response = repository.getSafetyAdvice(prompt)
            _advice.value = response
            _isLoading.value = false
        }
    }
}
