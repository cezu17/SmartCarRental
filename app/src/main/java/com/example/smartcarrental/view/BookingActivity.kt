package com.example.smartcarrental.view

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.smartcarrental.R
import com.example.smartcarrental.databinding.ActivityBookingBinding
import com.example.smartcarrental.model.Booking
import com.example.smartcarrental.utils.UserSession
import com.example.smartcarrental.viewmodel.BookingViewModel
import com.example.smartcarrental.viewmodel.CarViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Currency
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class BookingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingBinding
    private val carViewModel: CarViewModel by viewModels()
    private val bookingViewModel: BookingViewModel by viewModels()

    private var carId: Long = -1
    private var startDate: Date? = null
    private var endDate: Date? = null
    private var carPrice: Double = 0.0

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val currencyFormat = NumberFormat.getCurrencyInstance().apply {
        currency = Currency.getInstance("EUR")
    }

    companion object {
        private const val EXTRA_CAR_ID = "extra_car_id"

        fun newIntent(context: Context, carId: Long): Intent {
            return Intent(context, BookingActivity::class.java).apply {
                putExtra(EXTRA_CAR_ID, carId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        carId = intent.getLongExtra(EXTRA_CAR_ID, -1)
        if (carId == -1L) {
            finish()
            return
        }

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { onBackPressed() }

        binding.tvStartDate.setOnClickListener { showDatePickerDialog(true) }
        binding.tvEndDate.setOnClickListener { showDatePickerDialog(false) }

        binding.btnConfirmBooking.setOnClickListener { confirmBooking() }
    }

    private fun observeViewModel() {
        carViewModel.getCarById(carId).observe(this) { car ->
            car?.let {
                binding.tvCarNameBooking.text = "${car.make} ${car.model}"
                binding.tvCarCategoryBooking.text = car.category
                binding.tvCarPriceBooking.text = "${currencyFormat.format(car.price)}/day"

                carPrice = car.price

                val resourceId = resources.getIdentifier(
                    car.imageUrl, "drawable", packageName
                )
                if (resourceId != 0) {
                    binding.ivCarBooking.setImageResource(resourceId)
                }

                updateSummary()
            }
        }

        bookingViewModel.bookingResult.observe(this) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(this, getString(R.string.booking_successful), Toast.LENGTH_LONG).show()

                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("open_tab", "bookings")
                }
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Booking failed. The dates may conflict with existing bookings.", Toast.LENGTH_LONG).show()
                binding.tvErrorMessage.text = "The selected dates are not available for this car."
                binding.tvErrorMessage.visibility = View.VISIBLE
            }
        }
    }

    private fun showDatePickerDialog(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()

        val minDate = if (isStartDate) {
            calendar.timeInMillis
        } else {
            startDate?.let {
                val cal = Calendar.getInstance()
                cal.time = it
                cal.add(Calendar.DAY_OF_MONTH, 1)
                cal.timeInMillis
            } ?: calendar.timeInMillis
        }

        val defaultDate = if (isStartDate) startDate else endDate
        defaultDate?.let {
            calendar.time = it
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)

                if (isStartDate) {
                    startDate = selectedCalendar.time
                    binding.tvStartDate.text = dateFormat.format(startDate!!)

                    endDate?.let {
                        if (it.before(startDate)) {
                            endDate = null
                            binding.tvEndDate.text = getString(R.string.select_date)
                        }
                    }
                } else {
                    endDate = selectedCalendar.time
                    binding.tvEndDate.text = dateFormat.format(endDate!!)
                }

                updateSummary()
            },
            year, month, day
        )

        datePickerDialog.datePicker.minDate = minDate

        datePickerDialog.show()
    }

    private fun updateSummary() {
        if (startDate != null && endDate != null) {
            val diffInMillis = endDate!!.time - startDate!!.time
            val days = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS)

            if (days > 0) {
                binding.tvDaysValue.text = "$days days"
                binding.tvDailyRateValue.text = currencyFormat.format(carPrice)

                val totalPrice = days * carPrice
                binding.tvTotalValue.text = currencyFormat.format(totalPrice)

                binding.tvErrorMessage.visibility = View.GONE
            }
        }
    }

    private fun confirmBooking() {

        if (startDate == null || endDate == null) {
            binding.tvErrorMessage.text = getString(R.string.please_select_dates)
            binding.tvErrorMessage.visibility = View.VISIBLE
            return
        }

        val diffInMillis = endDate!!.time - startDate!!.time
        val days = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS)

        if (days <= 0) {
            binding.tvErrorMessage.text = getString(R.string.invalid_dates)
            binding.tvErrorMessage.visibility = View.VISIBLE
            return
        }


        if (!UserSession.isLoggedIn()) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val userId = UserSession.getUserId()


        val totalPrice = days * carPrice


        val booking = Booking(
            carId = carId,
            userId = userId,
            startDate = startDate!!,
            endDate = endDate!!,
            totalPrice = totalPrice,
            status = "PENDING"
        )

        bookingViewModel.createBooking(booking)
    }
}