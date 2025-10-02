package com.example.smartcarrental.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartcarrental.adapter.BookingWithCar
import com.example.smartcarrental.model.Booking
import com.example.smartcarrental.repository.BookingRepository
import com.example.smartcarrental.repository.CarRepository
import com.example.smartcarrental.repository.FirebaseBookingRepository
import com.example.smartcarrental.repository.FirebaseCarRepository
import com.example.smartcarrental.repository.RepositoryFactory
import com.example.smartcarrental.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class BookingViewModel(application: Application) : AndroidViewModel(application) {
    private val repositoryFactory = RepositoryFactory(application)
    private val bookingRepository: Any
    private val carRepository: Any
    private val notificationHelper = NotificationHelper(application)




    val allBookings: LiveData<List<Booking>>

    private val _bookingResult = MutableLiveData<Boolean>()
    val bookingResult: LiveData<Boolean> = _bookingResult

    private val _bookingsWithCar = MediatorLiveData<List<BookingWithCar>>()
    val bookingsWithCar: LiveData<List<BookingWithCar>> = _bookingsWithCar

    init {
        bookingRepository = repositoryFactory.getBookingRepository()
        carRepository = repositoryFactory.getCarRepository()

        allBookings = when (bookingRepository) {
            is FirebaseBookingRepository -> (bookingRepository as FirebaseBookingRepository).allBookings
            is BookingRepository -> (bookingRepository as BookingRepository).allBookings
            else -> MutableLiveData(emptyList())
        }
    }

    fun getBookingsByUser(userId: Long): LiveData<List<Booking>> {
        return when (bookingRepository) {
            is FirebaseBookingRepository ->
                (bookingRepository as FirebaseBookingRepository).getBookingsByUser(userId)
            is BookingRepository ->
                (bookingRepository as BookingRepository).getBookingsByUser(userId)
            else -> MutableLiveData(emptyList())
        }
    }

    fun loadUserBookings(userId: Long) {
        when (bookingRepository) {
            is FirebaseBookingRepository -> {
                val bookingsSource = (bookingRepository as FirebaseBookingRepository).getBookingsByUser(userId)

                _bookingsWithCar.addSource(bookingsSource) { bookings ->
                    viewModelScope.launch {
                        val bookingsWithCars = mutableListOf<BookingWithCar>()

                        for (booking in bookings) {
                            val car = withContext(Dispatchers.IO) {
                                when (carRepository) {
                                    is FirebaseCarRepository ->
                                        (carRepository as FirebaseCarRepository).getCarByIdSync(booking.carId)
                                    is CarRepository ->
                                        (carRepository as CarRepository).getCarByIdSync(booking.carId)
                                    else -> null
                                }
                            }

                            car?.let {
                                bookingsWithCars.add(BookingWithCar(booking, it))
                            }
                        }

                        _bookingsWithCar.value = bookingsWithCars
                    }
                }
            }
            is BookingRepository -> {
                val bookingsSource = (bookingRepository as BookingRepository).getBookingsByUser(userId)

                _bookingsWithCar.addSource(bookingsSource) { bookings ->
                    viewModelScope.launch {
                        val bookingsWithCars = mutableListOf<BookingWithCar>()

                        for (booking in bookings) {
                            val car = withContext(Dispatchers.IO) {
                                when (carRepository) {
                                    is FirebaseCarRepository ->
                                        (carRepository as FirebaseCarRepository).getCarByIdSync(booking.carId)
                                    is CarRepository ->
                                        (carRepository as CarRepository).getCarByIdSync(booking.carId)
                                    else -> null
                                }
                            }

                            car?.let {
                                bookingsWithCars.add(BookingWithCar(booking, it))
                            }
                        }

                        _bookingsWithCar.value = bookingsWithCars
                    }
                }
            }
        }
    }

    fun getBookingsByCar(carId: Long): LiveData<List<Booking>> {
        return when (bookingRepository) {
            is FirebaseBookingRepository ->
                (bookingRepository as FirebaseBookingRepository).getBookingsByCar(carId)
            is BookingRepository ->
                (bookingRepository as BookingRepository).getBookingsByCar(carId)
            else -> MutableLiveData(emptyList())
        }
    }

    fun getBookingById(bookingId: Long): LiveData<Booking> {
        return when (bookingRepository) {
            is FirebaseBookingRepository ->
                (bookingRepository as FirebaseBookingRepository).getBookingById(bookingId)
            is BookingRepository ->
                (bookingRepository as BookingRepository).getBookingById(bookingId)
            else -> MutableLiveData()
        }
    }

    fun getBookingsByStatus(status: String): LiveData<List<Booking>> {
        return when (bookingRepository) {
            is FirebaseBookingRepository ->
                (bookingRepository as FirebaseBookingRepository).getBookingsByStatus(status)
            is BookingRepository ->
                (bookingRepository as BookingRepository).getBookingsByStatus(status)
            else -> MutableLiveData(emptyList())
        }
    }

    fun createBooking(booking: Booking) = viewModelScope.launch {
        val overlappingBookings = withContext(Dispatchers.IO) {
            when (bookingRepository) {
                is FirebaseBookingRepository ->
                    (bookingRepository as FirebaseBookingRepository).getOverlappingBookings(
                        booking.carId, booking.startDate, booking.endDate
                    )
                is BookingRepository ->
                    (bookingRepository as BookingRepository).getOverlappingBookings(
                        booking.carId, booking.startDate, booking.endDate
                    )
                else -> emptyList()
            }
        }

        if (overlappingBookings.isEmpty()) {
            val bookingId = withContext(Dispatchers.IO) {
                when (bookingRepository) {
                    is FirebaseBookingRepository ->
                        (bookingRepository as FirebaseBookingRepository).insertBooking(booking)
                    is BookingRepository ->
                        (bookingRepository as BookingRepository).insertBooking(booking)
                    else -> -1L
                }
            }

            if (bookingId > 0) {
                withContext(Dispatchers.IO) {
                    when (carRepository) {
                        is FirebaseCarRepository ->
                            (carRepository as FirebaseCarRepository).updateCarAvailability(booking.carId, false)
                        is CarRepository ->
                            (carRepository as CarRepository).updateCarAvailability(booking.carId, false)
                    }

                    val car = when (carRepository) {
                        is FirebaseCarRepository ->
                            (carRepository as FirebaseCarRepository).getCarByIdSync(booking.carId)
                        is CarRepository ->
                            (carRepository as CarRepository).getCarByIdSync(booking.carId)
                        else -> null
                    }

                    car?.let {
                        withContext(Dispatchers.Main) {
                            notificationHelper.showBookingConfirmationNotification(
                                booking.copy(id = bookingId), it
                            )
                        }
                    }
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
        when (bookingRepository) {
            is FirebaseBookingRepository ->
                (bookingRepository as FirebaseBookingRepository).updateBooking(booking)
            is BookingRepository ->
                (bookingRepository as BookingRepository).updateBooking(booking)
        }
    }

    fun deleteBooking(booking: Booking) = viewModelScope.launch(Dispatchers.IO) {
        when (bookingRepository) {
            is FirebaseBookingRepository -> {
                (bookingRepository as FirebaseBookingRepository).deleteBooking(booking)
                when (carRepository) {
                    is FirebaseCarRepository ->
                        (carRepository as FirebaseCarRepository).updateCarAvailability(booking.carId, true)
                    is CarRepository ->
                        (carRepository as CarRepository).updateCarAvailability(booking.carId, true)
                }
            }
            is BookingRepository -> {
                (bookingRepository as BookingRepository).deleteBooking(booking)
                when (carRepository) {
                    is FirebaseCarRepository ->
                        (carRepository as FirebaseCarRepository).updateCarAvailability(booking.carId, true)
                    is CarRepository ->
                        (carRepository as CarRepository).updateCarAvailability(booking.carId, true)
                }
            }
        }
    }

    fun updateBookingStatus(bookingId: Long, status: String) = viewModelScope.launch(Dispatchers.IO) {
        when (bookingRepository) {
            is FirebaseBookingRepository -> {
                (bookingRepository as FirebaseBookingRepository).updateBookingStatus(bookingId, status)
                if (status == "COMPLETED" || status == "CANCELLED") {
                    val booking = (bookingRepository as FirebaseBookingRepository).getBookingById(bookingId).value
                    booking?.let {
                        when (carRepository) {
                            is FirebaseCarRepository ->
                                (carRepository as FirebaseCarRepository).updateCarAvailability(it.carId, true)
                            is CarRepository ->
                                (carRepository as CarRepository).updateCarAvailability(it.carId, true)
                        }
                    }
                }
            }
            is BookingRepository -> {
                (bookingRepository as BookingRepository).updateBookingStatus(bookingId, status)
                if (status == "COMPLETED" || status == "CANCELLED") {
                    val booking = (bookingRepository as BookingRepository).getBookingById(bookingId).value
                    booking?.let {
                        when (carRepository) {
                            is FirebaseCarRepository ->
                                (carRepository as FirebaseCarRepository).updateCarAvailability(it.carId, true)
                            is CarRepository ->
                                (carRepository as CarRepository).updateCarAvailability(it.carId, true)
                        }
                    }
                }
            }
        }
    }
}