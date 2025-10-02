package com.example.smartcarrental.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.smartcarrental.R
import com.example.smartcarrental.databinding.FragmentProfileBinding
import com.example.smartcarrental.utils.UserSession
import com.example.smartcarrental.viewmodel.BookingViewModel
import com.example.smartcarrental.viewmodel.CarViewModel
import com.example.smartcarrental.view.LoginActivity
import java.util.concurrent.TimeUnit

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val bookingVm: BookingViewModel by viewModels()
    private val carVm: CarViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentProfileBinding.bind(view)

        UserSession.getUser()?.let { user ->
            binding.tvUserName.text  = user.fullName
            binding.tvUserEmail.text = user.email
            binding.tvUserPhone.text = user.phoneNumber

            bookingVm.loadUserBookings(user.id)
            bookingVm.bookingsWithCar.observe(viewLifecycleOwner) { list ->
                // total
                binding.tvStatTotalBookings.text = list.size.toString()

                val longestBooking = list
                    .map { it.booking }
                    .maxByOrNull { it.endDate.time - it.startDate.time }

                if (longestBooking != null) {
                    val df = java.text.SimpleDateFormat("MMM dd yyyy", java.util.Locale.getDefault())
                    val start = df.format(longestBooking.startDate)
                    val end   = df.format(longestBooking.endDate)
                    binding.tvStatLongestRental.text = "$start – $end"
                } else {
                    binding.tvStatLongestRental.text = "—"
                }

                val favoriteCarId = list
                    .groupingBy { it.car.id }
                    .eachCount()
                    .maxByOrNull { it.value }
                    ?.key

                if (favoriteCarId != null) {
                    carVm.getCarById(favoriteCarId).observe(viewLifecycleOwner) { car ->
                        binding.tvStatFavoriteCar.text = "${car.make} ${car.model}"
                    }
                } else {
                    binding.tvStatFavoriteCar.text = "—"
                }
            }
        }
        
        binding.btnLogout.setOnClickListener {
            UserSession.setUser(null)
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
