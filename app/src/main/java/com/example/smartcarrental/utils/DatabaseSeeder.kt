package com.example.smartcarrental.utils

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.smartcarrental.database.AppDatabase
import com.example.smartcarrental.model.Car
import com.example.smartcarrental.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DatabaseSeeder(private val context: Context) {

    private val database = AppDatabase.getDatabase(context)


    fun seed() {
        CoroutineScope(Dispatchers.IO).launch {
            seedUsers()
            seedCars()
        }
    }

    private suspend fun seedUsers() {
        val userDao = database.userDao()

        if (userDao.getUserByUsername("admin") == null) {
            val adminUser = User(
                username = "admin",
                email = "admin@smartcarrental.com",
                password = "admin123",
                fullName = "Admin",
                phoneNumber = "0711111111",
                isAdmin = true
            )
            userDao.insertUser(adminUser)
        }
    }

    private suspend fun seedCars() {
        val carDao = database.carDao()


        if (carDao.getCarCount() == 0) {
            val cars = listOf(
                Car(
                    make = "Toyota",
                    model = "Corolla",
                    year = 2022,
                    price = 50.0,
                    category = "Economy",
                    imageUrl = "car_corolla",
                    description = "Fuel-efficient compact sedan with excellent reliability.",
                    seats = 5,
                    transmission = "Automatic",
                    fuelType = "Gasoline"
                ),
                Car(
                    make = "Honda",
                    model = "Civic",
                    year = 2023,
                    price = 55.0,
                    category = "Economy",
                    imageUrl = "car_civic",
                    description = "Sporty compact car with modern features and good fuel economy.",
                    seats = 5,
                    transmission = "Automatic",
                    fuelType = "Gasoline"
                ),
                Car(
                    make = "Ford",
                    model = "Focus",
                    year = 2022,
                    price = 90.0,
                    category = "Sports",
                    imageUrl = "car_focus",
                    description = "Iconic American muscle car with powerful engine and sleek design.",
                    seats = 4,
                    transmission = "Automatic",
                    fuelType = "Gasoline"
                ),
                Car(
                    make = "BMW",
                    model = "X5",
                    year = 2023,
                    price = 150.0,
                    category = "Luxury SUV",
                    imageUrl = "car_bmw_x5",
                    description = "Premium SUV with luxurious interior and advanced technology.",
                    seats = 5,
                    transmission = "Automatic",
                    fuelType = "Diesel"
                ),
                Car(
                    make = "Tesla",
                    model = "Model 3",
                    year = 2023,
                    price = 130.0,
                    category = "Electric",
                    imageUrl = "car_tesla_model3",
                    description = "All-electric sedan with long range and cutting-edge tech features.",
                    seats = 5,
                    transmission = "Automatic",
                    fuelType = "Electric"
                ),
                Car(
                    make = "Volkswagen",
                    model = "Golf",
                    year = 2022,
                    price = 60.0,
                    category = "Compact",
                    imageUrl = "car_golf",
                    description = "Versatile hatchback with German engineering and practicality.",
                    seats = 5,
                    transmission = "Manual",
                    fuelType = "Gasoline"
                ),
                Car(
                    make = "Jeep",
                    model = "Wrangler",
                    year = 2023,
                    price = 110.0,
                    category = "SUV",
                    imageUrl = "car_wrangler",
                    description = "Rugged off-road SUV perfect for adventure seekers.",
                    seats = 4,
                    transmission = "Automatic",
                    fuelType = "Gasoline"
                )
            )

            cars.forEach { car ->
                carDao.insertCar(car)
            }
        }
    }

    class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            DatabaseSeeder(context).seed()
        }
    }
}