package com.example.smartcarrental.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartcarrental.database.AppDatabase
import com.example.smartcarrental.model.User
import com.example.smartcarrental.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserRepository
    val allUsers: LiveData<List<User>>
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser
    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult

    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        repository = UserRepository(userDao)
        allUsers = repository.allUsers
    }

    fun getUserById(userId: Long): LiveData<User> {
        return repository.getUserById(userId)
    }

    fun login(username: String, password: String) = viewModelScope.launch {
        val user = withContext(Dispatchers.IO) {
            repository.login(username, password)
        }
        _currentUser.value = user
        _loginResult.value = user != null
    }

    fun logout() {
        _currentUser.value = null
    }

    fun register(user: User) = viewModelScope.launch {
        val existingUser = withContext(Dispatchers.IO) {
            repository.getUserByUsername(user.username)
        }

        if (existingUser == null) {
            val userId = withContext(Dispatchers.IO) {
                repository.insertUser(user)
            }

            if (userId > 0) {
                _currentUser.value = user.copy(id = userId)
                _loginResult.value = true
            } else {
                _loginResult.value = false
            }
        } else {
            _loginResult.value = false
        }
    }

    fun updateUser(user: User) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateUser(user)
    }

    fun deleteUser(user: User) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteUser(user)
    }
}