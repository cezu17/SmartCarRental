package com.example.smartcarrental.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.smartcarrental.model.Booking
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseBookingRepository {
    private val db = FirebaseFirestore.getInstance()
    private val bookingsCollection = db.collection("bookings")

    private val _allBookings = MutableLiveData<List<Booking>>()
    val allBookings: LiveData<List<Booking>> = _allBookings

    init {
        loadAllBookings()
    }

    private fun loadAllBookings() {
        bookingsCollection
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                val bookingsList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Booking::class.java)?.copy(id = doc.id.toLongOrNull() ?: 0)
                } ?: listOf()

                _allBookings.value = bookingsList
            }
    }

    fun getBookingsByUser(userId: Long): LiveData<List<Booking>> {
        val result = MutableLiveData<List<Booking>>()
        bookingsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                val bookingsList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Booking::class.java)?.copy(id = doc.id.toLongOrNull() ?: 0)
                } ?: listOf()

                result.value = bookingsList
            }
        return result
    }

    fun getBookingsByCar(carId: Long): LiveData<List<Booking>> {
        val result = MutableLiveData<List<Booking>>()
        bookingsCollection
            .whereEqualTo("carId", carId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                val bookingsList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Booking::class.java)?.copy(id = doc.id.toLongOrNull() ?: 0)
                } ?: listOf()

                result.value = bookingsList
            }
        return result
    }

    fun getBookingById(bookingId: Long): LiveData<Booking> {
        val result = MutableLiveData<Booking>()
        bookingsCollection.document(bookingId.toString())
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    return@addSnapshotListener
                }

                val booking = snapshot.toObject(Booking::class.java)
                if (booking != null) {
                    result.value = booking.copy(id = bookingId)
                }
            }
        return result
    }

    fun getBookingsByStatus(status: String): LiveData<List<Booking>> {
        val result = MutableLiveData<List<Booking>>()
        bookingsCollection
            .whereEqualTo("status", status)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                val bookingsList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Booking::class.java)?.copy(id = doc.id.toLongOrNull() ?: 0)
                } ?: listOf()

                result.value = bookingsList
            }
        return result
    }

    suspend fun getOverlappingBookings(carId: Long, start: Date, end: Date): List<Booking> {

        val query1 = bookingsCollection
            .whereEqualTo("carId", carId)
            .whereGreaterThanOrEqualTo("startDate", start)
            .whereLessThanOrEqualTo("startDate", end)
            .get()
            .await()

        val query2 = bookingsCollection
            .whereEqualTo("carId", carId)
            .whereGreaterThanOrEqualTo("endDate", start)
            .whereLessThanOrEqualTo("endDate", end)
            .get()
            .await()

        val query3 = bookingsCollection
            .whereEqualTo("carId", carId)
            .whereLessThanOrEqualTo("startDate", start)
            .whereGreaterThanOrEqualTo("endDate", end)
            .get()
            .await()

        val result = mutableListOf<Booking>()

        listOf(query1, query2, query3).forEach { querySnapshot ->
            querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(Booking::class.java)?.copy(id = doc.id.toLongOrNull() ?: 0)
            }.forEach { booking ->
                if (!result.any { it.id == booking.id }) {
                    result.add(booking)
                }
            }
        }

        return result
    }

    fun getBookingsWithCarByUser(userId: Long): LiveData<List<Booking>> {
        val result = MutableLiveData<List<Booking>>()
        bookingsCollection
            .whereEqualTo("userId", userId)
            .orderBy("startDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                val bookingsList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Booking::class.java)?.copy(id = doc.id.toLongOrNull() ?: 0)
                } ?: listOf()

                result.value = bookingsList
            }
        return result
    }

    suspend fun insertBooking(booking: Booking): Long {
        val id = booking.id.takeIf { it > 0 } ?: System.currentTimeMillis()
        val bookingWithId = booking.copy(id = id)

        bookingsCollection.document(id.toString())
            .set(bookingWithId)
            .await()

        return id
    }

    suspend fun updateBooking(booking: Booking) {
        bookingsCollection.document(booking.id.toString())
            .set(booking)
            .await()
    }

    suspend fun deleteBooking(booking: Booking) {
        bookingsCollection.document(booking.id.toString())
            .delete()
            .await()
    }

    suspend fun updateBookingStatus(bookingId: Long, status: String) {
        bookingsCollection.document(bookingId.toString())
            .update("status", status)
            .await()
    }
}