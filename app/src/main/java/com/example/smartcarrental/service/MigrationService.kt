package com.example.smartcarrental.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.smartcarrental.database.AppDatabase
import com.example.smartcarrental.repository.FirebaseBookingRepository
import com.example.smartcarrental.repository.FirebaseCarRepository
import com.example.smartcarrental.repository.FirebaseUserRepository
import com.example.smartcarrental.repository.RepositoryFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MigrationService : Service() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scope.launch {
            migrateData()
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private suspend fun migrateData() {
        try {
            val database = AppDatabase.getDatabase(applicationContext)

            val firebaseUserRepo = FirebaseUserRepository()
            val firebaseCarRepo = FirebaseCarRepository()
            val firebaseBookingRepo = FirebaseBookingRepository()

            val users = withContext(Dispatchers.IO) {
                database.userDao().getAllUsers().value ?: listOf()
            }

            users.forEach { user ->
                try {
                    firebaseUserRepo.insertUser(user)
                    Log.d("Migration", "Migrated user: ${user.username}")
                } catch (e: Exception) {
                    Log.e("Migration", "Error migrating user: ${user.username}", e)
                }
            }

            val cars = withContext(Dispatchers.IO) {
                database.carDao().getAllCars().value ?: listOf()
            }

            cars.forEach { car ->
                try {
                    firebaseCarRepo.insertCar(car)
                    Log.d("Migration", "Migrated car: ${car.make} ${car.model}")
                } catch (e: Exception) {
                    Log.e("Migration", "Error migrating car: ${car.make} ${car.model}", e)
                }
            }

            val bookings = withContext(Dispatchers.IO) {
                database.bookingDao().getAllBookings().value ?: listOf()
            }

            bookings.forEach { booking ->
                try {
                    firebaseBookingRepo.insertBooking(booking)
                    Log.d("Migration", "Migrated booking: ${booking.id}")
                } catch (e: Exception) {
                    Log.e("Migration", "Error migrating booking: ${booking.id}", e)
                }
            }

            val factory = RepositoryFactory(applicationContext)


            Log.d("Migration", "Migration completed successfully")
        } catch (e: Exception) {
            Log.e("Migration", "Migration failed", e)
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    companion object {
        fun startMigration(context: Context) {
            context.startService(Intent(context, MigrationService::class.java))
        }
    }
}