package com.example.smartcarrental.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartcarrental.adapter.BookingWithCar
import com.example.smartcarrental.database.AppDatabase
import com.example.smartcarrental.model.Booking
import com.example.smartcarrental.repository.BookingRepository
import com.example.smartcarrental.repository.CarRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class BookingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BookingRepository
    private val carRepository: CarRepository
    val allBookings: LiveData<List<Booking>>

    private val _bookingResult = MutableLiveData<Boolean>()
    val bookingResult: LiveData<Boolean> = _bookingResult

    private val _bookingsWithCar = MediatorLiveData<List<BookingWithCar>>()
    val bookingsWithCar: LiveData<List<BookingWithCar>> = _bookingsWithCar

    init {
        val database = AppDatabase.getDatabase(application)
        val bookingDao = database.bookingDao()
        val carDao = database.carDao()
        repository = BookingRepository(bookingDao)
        carRepository = CarRepository(carDao)
        allBookings = repository.allBookings
    }

    fun getBookingsByUser(userId: Long): LiveData<List<Booking>> {
        return repository.getBookingsByUser(userId)
    }

    fun loadUserBookings(userId: Long) {
        val bookingsSource = repository.getBookingsByUser(userId)


        _bookingsWithCar.addSource(bookingsSource) { bookings ->
            viewModelScope.launch {
                val bookingsWithCars = mutableListOf<BookingWithCar>()

                for (booking in bookings) {
                    val car = withContext(Dispatchers.IO) {
                        carRepository.getCarByIdSync(booking.carId)
                    }

                    car?.let {
                        bookingsWithCars.add(BookingWithCar(booking, it))
                    }
                }

                _bookingsWithCar.value = bookingsWithCars
            }
        }
    }

    fun getBookingsByCar(carId: Long): LiveData<List<Booking>> {
        return repository.getBookingsByCar(carId)
    }

    fun getBookingById(bookingId: Long): LiveData<Booking> {
        return repository.getBookingById(bookingId)
    }

    fun getBookingsByStatus(status: String): LiveData<List<Booking>> {
        return repository.getBookingsByStatus(status)
    }

    fun createBooking(booking: Booking) = viewModelScope.launch {
        val overlappingBookings = withContext(Dispatchers.IO) {
            repository.getOverlappingBookings(
                booking.carId,
                booking.startDate,
                booking.endDate
            )
        }

        if (overlappingBookings.isEmpty()) {
            val bookingId = withContext(Dispatchers.IO) {
                repository.insertBooking(booking)
            }

            if (bookingId > 0) {
                withContext(Dispatchers.IO) {
                    carRepository.updateCarAvailability(booking.carId, false)
                }
                _bookingResult.value = true
            } else {
                _bookingResult.value = false
            }
        } else {
            _bookingResult.value = false
        }
    }

    fun updateBooking(booking: Booking) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateBooking(booking)
    }

    fun deleteBooking(booking: Booking) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteBooking(booking)
        carRepository.updateCarAvailability(booking.carId, true)
    }

    fun updateBookingStatus(bookingId: Long, status: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateBookingStatus(bookingId, status)

        if (status == "COMPLETED" || status == "CANCELLED") {
            val booking = repository.getBookingById(bookingId).value
            booking?.let {
                carRepository.updateCarAvailability(it.carId, true)
            }
        }
    }
}