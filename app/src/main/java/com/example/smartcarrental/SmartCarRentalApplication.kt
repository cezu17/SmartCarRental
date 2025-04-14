package com.example.smartcarrental

import android.app.Application
import com.example.smartcarrental.database.AppDatabase
import com.example.smartcarrental.model.Car
import com.example.smartcarrental.utils.DatabaseSeeder
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class SmartCarRentalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        val settings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings

        seedCarsToFirebase()
    }
    private fun seedCarsToFirebase() {
        val db = FirebaseFirestore.getInstance()

        db.collection("cars").get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    val cars = listOf(
                        Car(
                            id = 1,
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
                            id = 2,
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
                            id = 3,
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
                            id = 4,
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
                            id = 5,
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
                        )
                    )

                    for (car in cars) {
                        db.collection("cars").document(car.id.toString()).set(car)
                    }
                }
            }
    }
}