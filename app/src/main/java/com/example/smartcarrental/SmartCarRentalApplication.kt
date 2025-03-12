package com.example.smartcarrental

import android.app.Application
import com.example.smartcarrental.database.AppDatabase

class SmartCarRentalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppDatabase.getDatabase(this)
    }
}