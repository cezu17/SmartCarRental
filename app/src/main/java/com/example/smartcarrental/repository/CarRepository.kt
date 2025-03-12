package com.example.smartcarrental.repository

import androidx.lifecycle.LiveData
import com.example.smartcarrental.database.CarDao
import com.example.smartcarrental.model.Car

class CarRepository(private val carDao: CarDao) {

    val allCars: LiveData<List<Car>> = carDao.getAllCars()
    val availableCars: LiveData<List<Car>> = carDao.getAvailableCars()

    fun getCarById(carId: Long): LiveData<Car> {
        return carDao.getCarById(carId)
    }

    fun getCarsByCategory(category: String): LiveData<List<Car>> {
        return carDao.getCarsByCategory(category)
    }

    suspend fun insertCar(car: Car): Long {
        return carDao.insertCar(car)
    }

    suspend fun getCarByIdSync(carId: Long): Car? {
        return carDao.getCarByIdSync(carId)
    }

    suspend fun updateCar(car: Car) {
        carDao.updateCar(car)
    }

    suspend fun deleteCar(car: Car) {
        carDao.deleteCar(car)
    }

    suspend fun updateCarAvailability(carId: Long, isAvailable: Boolean) {
        carDao.updateCarAvailability(carId, isAvailable)
    }
}