package com.example.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CarViewModel(private val repository: CarRepository) : ViewModel() {

    private val _cars = MutableStateFlow<List<Car>>(emptyList())
    val cars: StateFlow<List<Car>> get() = _cars

    init {
        loadCars()
    }

    private fun loadCars() {
        viewModelScope.launch {
            _cars.value = repository.getCars()
        }
    }

    fun getCarById(id: String): Car? {
        return repository.getCarById(id)
    }
}
