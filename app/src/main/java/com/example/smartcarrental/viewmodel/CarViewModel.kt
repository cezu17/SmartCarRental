package com.example.smartcarrental.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartcarrental.model.Car
import com.example.smartcarrental.repository.CarRepository
import com.example.smartcarrental.repository.FirebaseCarRepository
import com.example.smartcarrental.repository.RepositoryFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CarViewModel(application: Application) : AndroidViewModel(application) {
    private val repositoryFactory = RepositoryFactory(application)
    private val repository: Any

    private val _allCars = MediatorLiveData<List<Car>>()
    val allCars: LiveData<List<Car>> = _allCars

    private var selectedCategory: String? = null
    private var minPrice: Double = 0.0
    private var maxPrice: Double = 200.0
    private var sortOption: SortOption = SortOption.DEFAULT

    enum class SortOption {
        DEFAULT,
        PRICE_LOW_TO_HIGH,
        PRICE_HIGH_TO_LOW,
        NEWEST_FIRST
    }

    init {
        repository = repositoryFactory.getCarRepository()

        when (repository) {
            is FirebaseCarRepository -> {
                _allCars.addSource((repository as FirebaseCarRepository).allCars) { cars ->
                    _allCars.value = filterAndSortCars(cars)
                }
            }
            is CarRepository -> {
                _allCars.addSource((repository as CarRepository).allCars) { cars ->
                    _allCars.value = filterAndSortCars(cars)
                }
            }
        }
    }

    fun setSortOption(option: SortOption) {
        this.sortOption = option
        _allCars.value?.let { cars ->
            _allCars.value = filterAndSortCars(cars)
        }
    }

    fun setFilters(category: String?, minPrice: Double, maxPrice: Double) {
        this.selectedCategory = category
        this.minPrice = minPrice
        this.maxPrice = maxPrice
        _allCars.value?.let { cars ->
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
        return when (repository) {
            is FirebaseCarRepository -> (repository as FirebaseCarRepository).getCarById(carId)
            is CarRepository -> (repository as CarRepository).getCarById(carId)
            else -> MediatorLiveData()
        }
    }

    fun insertCar(car: Car) = viewModelScope.launch(Dispatchers.IO) {
        when (repository) {
            is FirebaseCarRepository -> (repository as FirebaseCarRepository).insertCar(car)
            is CarRepository -> (repository as CarRepository).insertCar(car)
        }
    }

    fun updateCar(car: Car) = viewModelScope.launch(Dispatchers.IO) {
        when (repository) {
            is FirebaseCarRepository -> (repository as FirebaseCarRepository).updateCar(car)
            is CarRepository -> (repository as CarRepository).updateCar(car)
        }
    }

    fun deleteCar(car: Car) = viewModelScope.launch(Dispatchers.IO) {
        when (repository) {
            is FirebaseCarRepository -> (repository as FirebaseCarRepository).deleteCar(car)
            is CarRepository -> (repository as CarRepository).deleteCar(car)
        }
    }

    fun updateCarAvailability(carId: Long, isAvailable: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        when (repository) {
            is FirebaseCarRepository -> (repository as FirebaseCarRepository).updateCarAvailability(carId, isAvailable)
            is CarRepository -> (repository as CarRepository).updateCarAvailability(carId, isAvailable)
        }
    }
}