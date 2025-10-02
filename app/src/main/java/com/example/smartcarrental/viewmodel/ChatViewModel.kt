package com.example.smartcarrental.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcarrental.BuildConfig
import com.example.smartcarrental.model.Car
import com.example.smartcarrental.network.ChatRequest
import com.example.smartcarrental.network.Message
import com.example.smartcarrental.network.NetworkModule
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private companion object {
        const val MAX_MESSAGE_PAIRS = 5
        const val SUMMARY_THRESHOLD = 12
        const val MAX_SUMMARY_TOKENS = 50
    }

    private val _history = MutableLiveData<List<Message>>(emptyList())
    val history: LiveData<List<Message>> = _history

    fun initializeContext(cars: List<Car>) {
        _history.value = initialContext(cars)
    }

    fun sendMessage(userText: String) = viewModelScope.launch {
        val history = _history.value!!.toMutableList()
        history.add(Message("user", userText))

        val resp = NetworkModule.openAIService.chat(
            "Bearer ${BuildConfig.OPENAI_API_KEY}",
            ChatRequest(messages = history)
        )
        val botMsg = resp.choices.first().message
        history.add(botMsg)
        pruneHistory(history)

        if (history.size > SUMMARY_THRESHOLD) {
            summarizeHistory(history)
        }

        _history.value = history
    }


    private fun initialContext(cars: List<Car>): MutableList<Message> {
        val system = Message(
            role    = "system",
            content = buildString {
                append("You are SmartCarRental’s assistant. ")
                append("Keep replies under 50 words. ")
                append("Recommend from our fleet only:\n")
                cars.forEach { car ->
                    append("${car.make} ${car.model}(${car.category},€${car.price}); ")
                }
            }
        )
        return mutableListOf(system)
    }

    private fun pruneHistory(history: MutableList<Message>) {
        if (history.isEmpty()) return

        val system = history.first()

        val payload = history.drop(1).toMutableList()

        while (payload.size > MAX_MESSAGE_PAIRS * 2) {
            payload.removeAt(0)
        }

        history.clear()
        history.add(system)
        history.addAll(payload)
    }


    private suspend fun summarizeHistory(history: MutableList<Message>) {
        val summaryPrompt = Message(
            role = "user",
            content = "Summarize the above conversation in under $MAX_SUMMARY_TOKENS tokens."
        )
        val resp = NetworkModule.openAIService.chat(
            "Bearer ${BuildConfig.OPENAI_API_KEY}",
            ChatRequest(messages = history + summaryPrompt)
        )
        val summary = resp.choices.first().message.content
        val system = history.first()
        history.clear()
        history.add(system)
        history.add(Message(role="assistant", content = "Conversation summary: $summary"))
    }

}
