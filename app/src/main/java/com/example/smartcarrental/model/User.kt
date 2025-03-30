package com.example.smartcarrental.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val isAdmin: Boolean = false
)