package com.example.smartcarrental.utils

import com.example.smartcarrental.model.User

object UserSession {
    private var currentUser: User? = null

    fun setUser(user: User?) {
        currentUser = user
    }

    fun getUser(): User? {
        return currentUser
    }

    fun isLoggedIn(): Boolean {
        return currentUser != null
    }

    fun getUserId(): Long {
        return currentUser?.id ?: -1L
    }
}