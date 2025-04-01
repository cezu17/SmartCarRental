package com.example.smartcarrental.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.smartcarrental.model.Car
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

class FirebaseCarRepository {
    private val db = FirebaseFirestore.getInstance()
    private val carsCollection = db.collection("cars")

    private val _allCars = MutableLiveData<List<Car>>()
    val allCars: LiveData<List<Car>> = _allCars

    private val _availableCars = MutableLiveData<List<Car>>()
    val availableCars: LiveData<List<Car>> = _availableCars

    init {
        loadAllCars()
        loadAvailableCars()
    }

    private fun loadAllCars() {
        carsCollection.addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }

            val carsList = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Car::class.java)?.copy(id = doc.id.toLongOrNull() ?: 0)
            } ?: listOf()

            _allCars.value = carsList
        }
    }

    private fun loadAvailableCars() {
        carsCollection
            .whereEqualTo("isAvailable", true)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                val carsList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Car::class.java)?.copy(id = doc.id.toLongOrNull() ?: 0)
                } ?: listOf()

                _availableCars.value = carsList
            }
    }

    fun getCarById(carId: Long): LiveData<Car> {
        val result = MutableLiveData<Car>()
        carsCollection.document(carId.toString())
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    return@addSnapshotListener
                }

                val car = snapshot.toObject(Car::class.java)
                if (car != null) {
                    result.value = car.copy(id = carId)
                }
            }
        return result
    }

    fun getCarsByCategory(category: String): LiveData<List<Car>> {
        val result = MutableLiveData<List<Car>>()
        carsCollection
            .whereEqualTo("category", category)
            .whereEqualTo("isAvailable", true)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                val carsList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Car::class.java)?.copy(id = doc.id.toLongOrNull() ?: 0)
                } ?: listOf()

                result.value = carsList
            }
        return result
    }

    suspend fun insertCar(car: Car): Long {
        val id = car.id.takeIf { it > 0 } ?: System.currentTimeMillis()
        val carWithId = car.copy(id = id)

        carsCollection.document(id.toString())
            .set(carWithId)
            .await()

        return id
    }

    suspend fun getCarByIdSync(carId: Long): Car? {
        val document = carsCollection.document(carId.toString())
            .get()
            .await()

        return document.toObject(Car::class.java)?.copy(id = carId)
    }

    suspend fun updateCar(car: Car) {
        carsCollection.document(car.id.toString())
            .set(car)
            .await()
    }

    suspend fun deleteCar(car: Car) {
        carsCollection.document(car.id.toString())
            .delete()
            .await()
    }

    suspend fun updateCarAvailability(carId: Long, isAvailable: Boolean) {
        carsCollection.document(carId.toString())
            .update("isAvailable", isAvailable)
            .await()
    }

    suspend fun getAvailableCarsOnDate(date: Date): List<Car> {
        val allCarsSnapshot = carsCollection.get().await()
        val allCars = allCarsSnapshot.documents.mapNotNull { doc ->
            doc.toObject(Car::class.java)?.copy(id = doc.id.toLongOrNull() ?: 0)
        }

        val result = mutableListOf<Car>()

        for (car in allCars) {
            val isAvailable = checkCarAvailabilityOnDate(car.id, date)
            if (isAvailable) {
                result.add(car)
            }
        }

        return result
    }

    private suspend fun checkCarAvailabilityOnDate(carId: Long, date: Date): Boolean {
        val startOfDay = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val endOfDay = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        val bookingsOnDate = db.collection("bookings")
            .whereEqualTo("carId", carId)
            .whereGreaterThanOrEqualTo("startDate", startOfDay)
            .whereLessThanOrEqualTo("startDate", endOfDay)
            .get()
            .await()
            .documents

        if (bookingsOnDate.isNotEmpty()) {
            return false
        }

        val spanningBookings = db.collection("bookings")
            .whereEqualTo("carId", carId)
            .whereLessThanOrEqualTo("startDate", startOfDay)
            .whereGreaterThanOrEqualTo("endDate", endOfDay)
            .get()
            .await()
            .documents

        return spanningBookings.isEmpty()
    }
}