package com.example.smartcarrental.repository

import androidx.lifecycle.LiveData
import com.example.smartcarrental.database.UserDao
import com.example.smartcarrental.model.User

class UserRepository(private val userDao: UserDao) {

    val allUsers: LiveData<List<User>> = userDao.getAllUsers()

    fun getUserById(userId: Long): LiveData<User> {
        return userDao.getUserById(userId)
    }

    suspend fun login(username: String, password: String): User? {
        return userDao.login(username, password)
    }

    suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)
    }

    suspend fun insertUser(user: User): Long {
        return userDao.insertUser(user)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user)
    }
}