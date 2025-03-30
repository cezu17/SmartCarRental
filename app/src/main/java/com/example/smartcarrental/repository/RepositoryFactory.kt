package com.example.smartcarrental.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.smartcarrental.database.AppDatabase

class RepositoryFactory(private val context: Context) {
    fun getUserRepository(): Any {
        return FirebaseUserRepository()
    }

    fun getCarRepository(): Any {
        return FirebaseCarRepository()
    }

    fun getBookingRepository(): Any {
        return FirebaseBookingRepository()
    }
}