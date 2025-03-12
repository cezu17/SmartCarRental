package com.example.smartcarrental.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.smartcarrental.model.Booking
import com.example.smartcarrental.model.Car
import com.example.smartcarrental.model.User
import com.example.smartcarrental.utils.DatabaseSeeder

@Database(entities = [Car::class, User::class, Booking::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun carDao(): CarDao
    abstract fun userDao(): UserDao
    abstract fun bookingDao(): BookingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_car_rental_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseSeeder.DatabaseCallback(context.applicationContext)) // Add this line
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}