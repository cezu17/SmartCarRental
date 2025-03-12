package com.example.smartcarrental.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.smartcarrental.model.Booking
import java.util.Date

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings")
    fun getAllBookings(): LiveData<List<Booking>>

    @Query("SELECT * FROM bookings WHERE userId = :userId")
    fun getBookingsByUser(userId: Long): LiveData<List<Booking>>

    @Query("SELECT * FROM bookings WHERE carId = :carId")
    fun getBookingsByCar(carId: Long): LiveData<List<Booking>>

    @Query("SELECT * FROM bookings WHERE id = :bookingId")
    fun getBookingById(bookingId: Long): LiveData<Booking>

    @Query("SELECT * FROM bookings WHERE status = :status")
    fun getBookingsByStatus(status: String): LiveData<List<Booking>>

    @Query("SELECT * FROM bookings WHERE carId = :carId AND ((startDate BETWEEN :start AND :end) OR (endDate BETWEEN :start AND :end) OR (startDate <= :start AND endDate >= :end))")
    suspend fun getOverlappingBookings(carId: Long, start: Date, end: Date): List<Booking>

    @Query("SELECT * FROM bookings WHERE userId = :userId ORDER BY startDate DESC")
    fun getBookingsWithCarByUser(userId: Long): LiveData<List<Booking>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking): Long

    @Update
    suspend fun updateBooking(booking: Booking)

    @Delete
    suspend fun deleteBooking(booking: Booking)

    @Query("UPDATE bookings SET status = :status WHERE id = :bookingId")
    suspend fun updateBookingStatus(bookingId: Long, status: String)
}