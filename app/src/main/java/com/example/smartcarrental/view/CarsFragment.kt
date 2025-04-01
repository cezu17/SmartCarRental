package com.example.smartcarrental.view

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.smartcarrental.R
import com.example.smartcarrental.adapter.CarAdapter
import com.example.smartcarrental.adapter.DateAdapter
import com.example.smartcarrental.databinding.FragmentCarsBinding
import com.example.smartcarrental.viewmodel.CarViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.slider.RangeSlider
import java.text.NumberFormat
import java.util.Currency
import kotlin.math.roundToInt

class CarsFragment : Fragment() {

    private var _binding: FragmentCarsBinding? = null
    private val binding get() = _binding!!

    private val carViewModel: CarViewModel by viewModels()
    private lateinit var carAdapter: CarAdapter

    private var selectedCategory: String? = "All"
    private var minPrice: Double = 0.0
    private var maxPrice: Double = 200.0

    private var currentMinPrice: Double = 0.0
    private var currentMaxPrice: Double = 200.0


    private var currentSortOption = CarViewModel.SortOption.DEFAULT

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarsBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilterChips()
        //setupPriceSlider()
        setupDatePicker()
        setupPriceFilterButton()
        observeViewModel()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.sort_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sort_default -> {
                updateSortOption(CarViewModel.SortOption.DEFAULT)
                item.isChecked = true
                return true
            }
            R.id.sort_price_low_high -> {
                updateSortOption(CarViewModel.SortOption.PRICE_LOW_TO_HIGH)
                item.isChecked = true
                return true
            }
            R.id.sort_price_high_low -> {
                updateSortOption(CarViewModel.SortOption.PRICE_HIGH_TO_LOW)
                item.isChecked = true
                return true
            }
            R.id.sort_newest -> {
                updateSortOption(CarViewModel.SortOption.NEWEST_FIRST)
                item.isChecked = true
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateSortOption(option: CarViewModel.SortOption) {
        if (currentSortOption != option) {
            currentSortOption = option
            carViewModel.setSortOption(option)
        }
    }

    private fun setupRecyclerView() {
        carAdapter = CarAdapter { car ->
            startActivity(CarDetailsActivity.newIntent(requireContext(), car.id))
        }

        binding.rvCars.apply {
            adapter = carAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }

    private fun setupPriceFilterButton() {
        binding.fabPriceFilter.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_price_filter, null)
            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            val rangeSlider = dialogView.findViewById<RangeSlider>(R.id.priceSlider)
            val tvPriceRange = dialogView.findViewById<TextView>(R.id.tvPriceRange)
            val btnApply = dialogView.findViewById<Button>(R.id.btnApplyFilter)
            val btnReset = dialogView.findViewById<Button>(R.id.btnResetFilter)

            val currentValues = listOf(0f, 200f)
            rangeSlider.values = currentValues

            val updatePriceRangeText = {
                val values = rangeSlider.values
                tvPriceRange.text = "${values[0].toInt()}€ - ${values[1].toInt()}€"
            }

            updatePriceRangeText()

            rangeSlider.addOnChangeListener { slider, _, _ ->
                updatePriceRangeText()
            }

            btnApply.setOnClickListener {
                val minPrice = rangeSlider.values[0].toDouble()
                val maxPrice = rangeSlider.values[1].toDouble()
                currentMinPrice = minPrice
                currentMaxPrice = maxPrice

                carViewModel.setFilters(selectedCategory, minPrice, maxPrice)
                dialog.dismiss()
            }

            btnReset.setOnClickListener {
                rangeSlider.values = listOf(0f, 200f)
                updatePriceRangeText()
            }

            dialog.show()
        }
    }

    private fun setupDatePicker() {
        val dateAdapter = DateAdapter { selectedDate ->
            carViewModel.setSelectedDate(selectedDate)
        }
        binding.rvDates.adapter = dateAdapter
    }

    private fun setupFilterChips() {
        binding.chipAll.isChecked = true

        binding.chipAll.setOnClickListener {
            selectedCategory = "All"
            applyFilters()
        }

        binding.chipEconomy.setOnClickListener {
            selectedCategory = "Economy"
            applyFilters()
        }

        binding.chipCompact.setOnClickListener {
            selectedCategory = "Compact"
            applyFilters()
        }

        binding.chipLuxury.setOnClickListener {
            selectedCategory = "Luxury SUV"
            applyFilters()
        }

        binding.chipSports.setOnClickListener {
            selectedCategory = "Sports"
            applyFilters()
        }

        binding.chipSuv.setOnClickListener {
            selectedCategory = "SUV"
            applyFilters()
        }

        binding.chipElectric.setOnClickListener {
            selectedCategory = "Electric"
            applyFilters()
        }
    }

    private fun applyFilters() {
        carViewModel.setFilters(selectedCategory, minPrice, maxPrice)
    }

    private fun observeViewModel() {
        binding.progressBar.visibility = View.VISIBLE

        carViewModel.allCars.observe(viewLifecycleOwner) { cars ->
            binding.progressBar.visibility = View.GONE
            carAdapter.submitList(cars)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}