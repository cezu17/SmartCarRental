package com.example.smartcarrental.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.smartcarrental.R
import com.example.smartcarrental.adapter.CarAdapter
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
        setupPriceSlider()
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

    private fun setupPriceSlider() {
        val currencyFormat = NumberFormat.getCurrencyInstance()
        currencyFormat.currency = Currency.getInstance("EUR")
        currencyFormat.maximumFractionDigits = 0

        binding.priceSlider.addOnChangeListener { slider, _, _ ->
            val values = slider.values
            minPrice = values[0].toDouble()
            maxPrice = values[1].toDouble()

            binding.tvPriceRange.text = "Price Range: ${currencyFormat.format(minPrice)} - ${currencyFormat.format(maxPrice)}"
            applyFilters()
        }


        minPrice = binding.priceSlider.values[0].toDouble()
        maxPrice = binding.priceSlider.values[1].toDouble()
        binding.tvPriceRange.text = "Price Range: ${currencyFormat.format(minPrice)} - ${currencyFormat.format(maxPrice)}"
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