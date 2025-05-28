package com.example.smartcarrental.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartcarrental.adapter.BookingAdapter
import com.example.smartcarrental.adapter.BookingWithCar
import com.example.smartcarrental.databinding.FragmentBookingsBinding
import com.example.smartcarrental.utils.UserSession
import com.example.smartcarrental.viewmodel.BookingViewModel
import java.util.Date
import androidx.lifecycle.lifecycleScope
import com.example.smartcarrental.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class BookingsFragment : Fragment() {

    private var _binding: FragmentBookingsBinding? = null
    private val binding get() = _binding!!

    private val bookingViewModel: BookingViewModel by viewModels()
    private lateinit var bookingAdapter: BookingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadBookings()
    }

    private fun setupRecyclerView() {
        bookingAdapter = BookingAdapter { bwc ->
            if (bwc.booking.status == "COMPLETED") {
                handleRatingFlow(bwc)
            } else {
                Toast.makeText(requireContext(),
                    "You can only rate completed bookings",
                    Toast.LENGTH_SHORT).show()
            }
        }

        binding.rvBookings.apply {
            adapter = bookingAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun loadBookings() {
        if (!UserSession.isLoggedIn()) {
            binding.tvNoBookings.text = "Please login to view your bookings"
            binding.tvNoBookings.visibility = View.VISIBLE
            binding.rvBookings.visibility = View.GONE
            return
        }

        val userId = UserSession.getUserId()
        binding.progressBar.visibility = View.VISIBLE

        bookingViewModel.loadUserBookings(userId)

        bookingViewModel.bookingsWithCar.observe(viewLifecycleOwner) { bookings ->
            binding.progressBar.visibility = View.GONE

            if (bookings.isEmpty()) {
                binding.tvNoBookings.visibility = View.VISIBLE
                binding.rvBookings.visibility = View.GONE
            } else {
                binding.tvNoBookings.visibility = View.GONE
                binding.rvBookings.visibility = View.VISIBLE
                bookingAdapter.submitList(bookings)
                updateBookingStatuses(bookings)
            }
        }
    }

    private fun updateBookingStatuses(bookings: List<BookingWithCar>) {
        val currentDate = Date()
        viewLifecycleOwner.lifecycleScope.launch {
            bookings.forEach { bookingWithCar ->
                val booking = bookingWithCar.booking
                val newStatus = when {
                    currentDate.before(booking.startDate) -> "PENDING"
                    currentDate.after(booking.endDate) -> "COMPLETED"
                    else -> "ACTIVE"
                }

                if (booking.status != newStatus) {
                    bookingViewModel.updateBookingStatus(booking.id, newStatus)
                }
            }
        }
    }

    private fun showRatingDialog(bwc: BookingWithCar, initialRating: Float?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_rate_booking, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
        val titleView = dialogView.findViewById<TextView>(R.id.tvRateTitle)
        titleView.text = "Rate ${bwc.car.make} ${bwc.car.model}"

        initialRating?.let { ratingBar.rating = it }

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Submit") { _, _ ->
                val newRating = ratingBar.rating
                val updated = bwc.booking.copy(rating = newRating)
                bookingViewModel.updateBooking(updated)
                Toast.makeText(requireContext(),
                    "Your rating ($newRating) has been saved",
                    Toast.LENGTH_SHORT).show()
            }
            .show()
    }


    private fun handleRatingFlow(bwc: BookingWithCar) {
        val existing = bwc.booking.rating
        if (existing == null) {
            showRatingDialog(bwc, null)
        } else {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Already Rated")
                .setMessage("You rated this ${bwc.car.make} ${bwc.car.model} with $existing stars.\n\nChange rating?")
                .setPositiveButton("Yes") { _, _ ->
                    showRatingDialog(bwc, existing)
                }
                .setNegativeButton("No", null)
                .show()
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}