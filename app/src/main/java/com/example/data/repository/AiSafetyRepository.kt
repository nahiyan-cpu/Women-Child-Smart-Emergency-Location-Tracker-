package com.example.data.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai

class AiSafetyRepository {
    private val generativeModel by lazy {
        try {
            Firebase.ai.generativeModel("gemini-2.5-flash")
        } catch (e: Exception) {
            Log.e("AiSafetyRepository", "Failed to initialize generative model", e)
            null
        }
    }

    suspend fun getSafetyAdvice(userPrompt: String): String {
        val model = generativeModel ?: return "AI safety assistant is offline. Please call local emergency services immediately if in danger."
        return try {
            val prompt = "You are an expert women and child personal safety and emergency advisor. Provide clear, concise, actionable, and reassuring guidance for this situation or query: $userPrompt"
            val response = model.generateContent(prompt)
            response.text ?: "Stay calm, move to a crowded or well-lit area, and contact your emergency contacts."
        } catch (e: Exception) {
            Log.e("AiSafetyRepository", "Error generating safety advice", e)
            "Unable to reach safety assistant. Please contact emergency services (112 / 911) or your trusted emergency contacts."
        }
    }
}
