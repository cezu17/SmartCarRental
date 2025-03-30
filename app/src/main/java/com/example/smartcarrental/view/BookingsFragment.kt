package com.example.smartcarrental.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        bookingAdapter = BookingAdapter()

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}