package com.example.smartcarrental.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cars")
data class Car(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val make: String = "",
    val model: String = "",
    val year: Int = 0,
    val price: Double = 0.0,
    val category: String = "",
    val imageUrl: String = "",
    val isAvailable: Boolean = true,
    val description: String = "",
    val seats: Int = 0,
    val transmission: String = "",
    val fuelType: String = ""
)