package com.example.smartcarrental.repository

import androidx.lifecycle.LiveData
import com.example.smartcarrental.database.BookingDao
import com.example.smartcarrental.model.Booking
import java.util.Date

class BookingRepository(private val bookingDao: BookingDao) {

    val allBookings: LiveData<List<Booking>> = bookingDao.getAllBookings()

    fun getBookingsByUser(userId: Long): LiveData<List<Booking>> {
        return bookingDao.getBookingsByUser(userId)
    }

    fun getBookingsByCar(carId: Long): LiveData<List<Booking>> {
        return bookingDao.getBookingsByCar(carId)
    }

    fun getBookingById(bookingId: Long): LiveData<Booking> {
        return bookingDao.getBookingById(bookingId)
    }

    fun getBookingsByStatus(status: String): LiveData<List<Booking>> {
        return bookingDao.getBookingsByStatus(status)
    }

    suspend fun getOverlappingBookings(carId: Long, start: Date, end: Date): List<Booking> {
        return bookingDao.getOverlappingBookings(carId, start, end)
    }

    suspend fun insertBooking(booking: Booking): Long {
        return bookingDao.insertBooking(booking)
    }

    suspend fun updateBooking(booking: Booking) {
        bookingDao.updateBooking(booking)
    }

    suspend fun deleteBooking(booking: Booking) {
        bookingDao.deleteBooking(booking)
    }

    suspend fun updateBookingStatus(bookingId: Long, status: String) {
        bookingDao.updateBookingStatus(bookingId, status)
    }

    fun getBookingsWithCarByUser(userId: Long): LiveData<List<Booking>> {
        return bookingDao.getBookingsWithCarByUser(userId)
    }
}