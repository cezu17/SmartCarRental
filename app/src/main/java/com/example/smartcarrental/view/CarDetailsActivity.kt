package com.example.smartcarrental.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.smartcarrental.databinding.ActivityCarDetailsBinding
import com.example.smartcarrental.model.Car
import com.example.smartcarrental.viewmodel.CarViewModel

class CarDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarDetailsBinding
    private val carViewModel: CarViewModel by viewModels()
    private var carId: Long = -1

    companion object {
        private const val EXTRA_CAR_ID = "extra_car_id"

        fun newIntent(context: Context, carId: Long): Intent {
            return Intent(context, CarDetailsActivity::class.java).apply {
                putExtra(EXTRA_CAR_ID, carId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarDetailsBinding.inflate(layoutInflater)
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

        binding.btnBookNow.setOnClickListener {
            startActivity(BookingActivity.newIntent(this, carId))
        }
    }

    private fun observeViewModel() {
        carViewModel.getCarById(carId).observe(this) { car ->
            car?.let { displayCarDetails(it) }
        }
    }

    private fun displayCarDetails(car: Car) {
        binding.tvCarNameDetail.text = "${car.make} ${car.model}"
        binding.tvCarYear.text = "Year: ${car.year}"
        binding.tvCarPriceDetail.text = "${car.price}€/day"
        binding.tvCarDescription.text = car.description


        binding.tvSpecCategory.text = car.category
        binding.tvSpecSeats.text = car.seats.toString()
        binding.tvSpecTransmission.text = car.transmission
        binding.tvSpecFuelType.text = car.fuelType


        val resourceId = resources.getIdentifier(
            car.imageUrl, "drawable", packageName
        )
        if (resourceId != 0) {
            binding.ivCarDetail.setImageResource(resourceId)
        }
    }
}