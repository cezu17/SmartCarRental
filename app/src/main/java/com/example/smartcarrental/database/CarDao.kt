package com.example.smartcarrental.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.smartcarrental.model.Car

@Dao
interface CarDao {
    @Query("SELECT * FROM cars")
    fun getAllCars(): LiveData<List<Car>>

    @Query("SELECT * FROM cars WHERE isAvailable = 1")
    fun getAvailableCars(): LiveData<List<Car>>

    @Query("SELECT * FROM cars WHERE id = :carId")
    fun getCarById(carId: Long): LiveData<Car>

    @Query("SELECT * FROM cars WHERE category = :category AND isAvailable = 1")
    fun getCarsByCategory(category: String): LiveData<List<Car>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCar(car: Car): Long

    @Update
    suspend fun updateCar(car: Car)

    @Delete
    suspend fun deleteCar(car: Car)

    @Query("SELECT COUNT(*) FROM cars")
    suspend fun getCarCount(): Int

    @Query("UPDATE cars SET isAvailable = :isAvailable WHERE id = :carId")
    suspend fun updateCarAvailability(carId: Long, isAvailable: Boolean)

    @Query("SELECT * FROM cars WHERE id = :carId")
    suspend fun getCarByIdSync(carId: Long): Car?
}