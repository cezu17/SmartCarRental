package com.example.smartcarrental.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.smartcarrental.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseUserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    private val _allUsers = MutableLiveData<List<User>>()
    val allUsers: LiveData<List<User>> = _allUsers

    init {
        loadAllUsers()
    }

    private fun loadAllUsers() {
        usersCollection.addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }

            val usersList = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(User::class.java)?.copy(id = doc.id.toLongOrNull() ?: 0)
            } ?: listOf()

            _allUsers.value = usersList
        }
    }

    fun getUserById(userId: Long): LiveData<User> {
        val result = MutableLiveData<User>()
        usersCollection.document(userId.toString())
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    return@addSnapshotListener
                }

                val user = snapshot.toObject(User::class.java)
                if (user != null) {
                    result.value = user.copy(id = userId)
                }
            }
        return result
    }

    suspend fun login(username: String, password: String): User? {
        val querySnapshot = usersCollection
            .whereEqualTo("username", username)
            .whereEqualTo("password", password)
            .limit(1)
            .get()
            .await()

        return querySnapshot.documents.firstOrNull()?.let { doc ->
            doc.toObject(User::class.java)?.copy(id = doc.id.toLongOrNull() ?: 0)
        }
    }

    suspend fun getUserByUsername(username: String): User? {
        val querySnapshot = usersCollection
            .whereEqualTo("username", username)
            .limit(1)
            .get()
            .await()

        return querySnapshot.documents.firstOrNull()?.let { doc ->
            doc.toObject(User::class.java)?.copy(id = doc.id.toLongOrNull() ?: 0)
        }
    }

    suspend fun insertUser(user: User): Long {
        val id = user.id.takeIf { it > 0 } ?: System.currentTimeMillis()
        val userWithId = user.copy(id = id)

        usersCollection.document(id.toString())
            .set(userWithId)
            .await()

        return id
    }

    suspend fun updateUser(user: User) {
        usersCollection.document(user.id.toString())
            .set(user)
            .await()
    }

    suspend fun deleteUser(user: User) {
        usersCollection.document(user.id.toString())
            .delete()
            .await()
    }
}