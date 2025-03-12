package com.example.smartcarrental.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartcarrental.database.AppDatabase
import com.example.smartcarrental.model.Car
import com.example.smartcarrental.repository.CarRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CarViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CarRepository
    private val _allCars = MediatorLiveData<List<Car>>()
    val allCars: LiveData<List<Car>> = _allCars

    private var selectedCategory: String? = null
    private var minPrice: Double = 0.0
    private var maxPrice: Double = 200.0
    private var sortOption: SortOption = SortOption.DEFAULT

    init {
        val carDao = AppDatabase.getDatabase(application).carDao()
        repository = CarRepository(carDao)

        _allCars.addSource(repository.allCars) { cars ->
            _allCars.value = filterAndSortCars(cars)
        }
    }

    // Add enum for sort options
    enum class SortOption {
        DEFAULT,
        PRICE_LOW_TO_HIGH,
        PRICE_HIGH_TO_LOW,
        NEWEST_FIRST
    }

    fun setSortOption(option: SortOption) {
        this.sortOption = option

        repository.allCars.value?.let { cars ->
            _allCars.value = filterAndSortCars(cars)
        }
    }

    fun setFilters(category: String?, minPrice: Double, maxPrice: Double) {
        this.selectedCategory = category
        this.minPrice = minPrice
        this.maxPrice = maxPrice

        repository.allCars.value?.let { cars ->
            _allCars.value = filterAndSortCars(cars)
        }
    }

    private fun filterAndSortCars(cars: List<Car>): List<Car> {
        val filteredCars = cars.filter { car ->
            val matchesCategory = selectedCategory == null || selectedCategory == "All" ||
                    car.category == selectedCategory
            val matchesPrice = car.price >= minPrice && car.price <= maxPrice

            matchesCategory && matchesPrice && car.isAvailable
        }

        return when (sortOption) {
            SortOption.PRICE_LOW_TO_HIGH -> filteredCars.sortedBy { it.price }
            SortOption.PRICE_HIGH_TO_LOW -> filteredCars.sortedByDescending { it.price }
            SortOption.NEWEST_FIRST -> filteredCars.sortedByDescending { it.year }
            SortOption.DEFAULT -> filteredCars
        }
    }

    fun getCarById(carId: Long): LiveData<Car> {
        return repository.getCarById(carId)
    }

    fun insertCar(car: Car) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertCar(car)
    }

    fun updateCar(car: Car) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateCar(car)
    }

    fun deleteCar(car: Car) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteCar(car)
    }

    fun updateCarAvailability(carId: Long, isAvailable: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateCarAvailability(carId, isAvailable)
    }
}