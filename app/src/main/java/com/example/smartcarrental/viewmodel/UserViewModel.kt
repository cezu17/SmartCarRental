package com.example.smartcarrental.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartcarrental.model.User
import com.example.smartcarrental.repository.FirebaseUserRepository
import com.example.smartcarrental.repository.RepositoryFactory
import com.example.smartcarrental.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val repositoryFactory = RepositoryFactory(application)
    private val repository: Any

    val allUsers: LiveData<List<User>>
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser
    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult

    init {
        repository = repositoryFactory.getUserRepository()

        allUsers = when (repository) {
            is FirebaseUserRepository -> (repository as FirebaseUserRepository).allUsers
            is UserRepository -> (repository as UserRepository).allUsers
            else -> MutableLiveData(emptyList())
        }
    }

    fun getUserById(userId: Long): LiveData<User> {
        return when (repository) {
            is FirebaseUserRepository -> (repository as FirebaseUserRepository).getUserById(userId)
            is UserRepository -> (repository as UserRepository).getUserById(userId)
            else -> MutableLiveData()
        }
    }

    fun login(username: String, password: String) = viewModelScope.launch {
        val user = withContext(Dispatchers.IO) {
            when (repository) {
                is FirebaseUserRepository ->
                    (repository as FirebaseUserRepository).login(username, password)
                is UserRepository ->
                    (repository as UserRepository).login(username, password)
                else -> null
            }
        }

        _currentUser.value = user
        _loginResult.value = user != null
    }

    fun logout() {
        _currentUser.value = null
    }

    fun register(user: User) = viewModelScope.launch {
        val existingUser = withContext(Dispatchers.IO) {
            when (repository) {
                is FirebaseUserRepository ->
                    (repository as FirebaseUserRepository).getUserByUsername(user.username)
                is UserRepository ->
                    (repository as UserRepository).getUserByUsername(user.username)
                else -> null
            }
        }

        if (existingUser == null) {
            val userId = withContext(Dispatchers.IO) {
                when (repository) {
                    is FirebaseUserRepository ->
                        (repository as FirebaseUserRepository).insertUser(user)
                    is UserRepository ->
                        (repository as UserRepository).insertUser(user)
                    else -> -1L
                }
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
        when (repository) {
            is FirebaseUserRepository -> (repository as FirebaseUserRepository).updateUser(user)
            is UserRepository -> (repository as UserRepository).updateUser(user)
        }
    }

    fun deleteUser(user: User) = viewModelScope.launch(Dispatchers.IO) {
        when (repository) {
            is FirebaseUserRepository -> (repository as FirebaseUserRepository).deleteUser(user)
            is UserRepository -> (repository as UserRepository).deleteUser(user)
        }
    }
}