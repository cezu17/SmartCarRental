package com.example.smartcarrental.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "bookings",
    foreignKeys = [
        ForeignKey(
            entity = Car::class,
            parentColumns = ["id"],
            childColumns = ["carId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Booking(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val carId: Long = 0,
    val userId: Long = 0,
    val startDate: Date = Date(),
    val endDate: Date = Date(),
    val totalPrice: Double = 0.0,
    val status: String = ""
)