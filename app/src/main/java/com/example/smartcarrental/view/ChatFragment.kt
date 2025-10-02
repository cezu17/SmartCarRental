package com.example.smartcarrental.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartcarrental.R
import com.example.smartcarrental.adapter.ChatAdapter
import com.example.smartcarrental.adapter.MatchAdapter
import com.example.smartcarrental.databinding.FragmentChatBinding
import com.example.smartcarrental.viewmodel.CarViewModel
import com.example.smartcarrental.viewmodel.ChatViewModel
import androidx.lifecycle.lifecycleScope
import com.example.smartcarrental.view.BookingActivity
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.util.Locale
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import com.example.smartcarrental.repository.FirebaseBookingRepository
import com.example.smartcarrental.repository.RepositoryFactory

class ChatFragment : Fragment(R.layout.fragment_chat) {
    private val vm: ChatViewModel by activityViewModels()
    private val carViewModel: CarViewModel by viewModels()

    private var _binding: FragmentChatBinding? = null
    private val b get() = _binding!!

    private val chatAdapter = ChatAdapter()
    private lateinit var matchAdapter: MatchAdapter

    private val DAY_MONTH_REGEX = """\b(\d{1,2}\s+[A-Za-z]+)\b""".toRegex()

    private val bookingRepo by lazy {
        RepositoryFactory(requireContext())
            .getBookingRepository() as FirebaseBookingRepository
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentChatBinding.bind(view)

        b.rvChat.adapter = chatAdapter
        b.rvChat.layoutManager = LinearLayoutManager(requireContext())

        matchAdapter = MatchAdapter { car ->
            startActivity(BookingActivity.newIntent(requireContext(), car.id))
        }
        b.rvMatches.apply {
            adapter = matchAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }

        carViewModel.allCars.observe(viewLifecycleOwner) { cars ->
            if (vm.history.value.isNullOrEmpty()) {
                vm.initializeContext(cars)
            }
            updateMatchStrip()
        }

        vm.history.observe(viewLifecycleOwner) { allMsgs ->
            val visible = allMsgs.filter { it.role != "system" }
            chatAdapter.submitList(visible)
            if (visible.isNotEmpty()) {
                b.rvChat.scrollToPosition(visible.lastIndex)
            }
            updateMatchStrip()
        }

        b.btnSend.setOnClickListener {
            val text = b.etMessage.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            val asksAvail = text.contains("available", ignoreCase = true)
                    && text.contains("between",  ignoreCase = true)

            val dayMonthRegex = """\b(\d{1,2}\s+[A-Za-z]+)\b""".toRegex()
            val found = dayMonthRegex.findAll(text).map { it.value }.toList()

            if (asksAvail && found.size >= 2) {
                val year = Calendar.getInstance().get(Calendar.YEAR)
                val sdf = SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH)

                val startDate: Date = sdf.parse("${found[0]} $year")!!
                val endDate:   Date = sdf.parse("${found[1]} $year")!!

                chatAdapter.addUserMessage(text)
                b.etMessage.text?.clear()

                lifecycleScope.launch {
                    val fleet = carViewModel.allCars.value.orEmpty()
                    val bookedIds = mutableSetOf<Long>()

                    for (car in fleet) {
                        val overlaps = bookingRepo.getOverlappingBookings(
                            car.id,
                            startDate,
                            endDate
                        )
                        if (overlaps.isNotEmpty()) bookedIds += car.id
                    }

                    val available = fleet.filter { it.id !in bookedIds }

                    if (available.isEmpty()) {
                        chatAdapter.addBotMessage(
                            "Sorry, no cars are free between ${found[0]} and ${found[1]}."
                        )
                        b.rvMatches.visibility = View.GONE
                    } else {
                        chatAdapter.addBotMessage(
                            "Cars available between ${found[0]} and ${found[1]}:"
                        )
                        matchAdapter.submitList(available)
                        b.rvMatches.visibility = View.VISIBLE
                    }

                    val total = chatAdapter.itemCount
                    if (total > 0) b.rvChat.scrollToPosition(total - 1)
                }

            } else {
                vm.sendMessage(text)
                b.etMessage.text?.clear()
            }
        }

    }

    private fun updateMatchStrip() {
        val visible = vm.history.value.orEmpty().filter { it.role != "system" }

        val lastBot = visible.lastOrNull { it.role == "assistant" }?.content.orEmpty()

        val fleet = carViewModel.allCars.value.orEmpty()

        val matches = fleet.filter { car ->
            lastBot.contains("${car.make} ${car.model}", ignoreCase = true)
        }

        if (matches.isNotEmpty()) {
            b.rvMatches.visibility = View.VISIBLE
            matchAdapter.submitList(matches)
        } else {
            b.rvMatches.visibility = View.GONE
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
