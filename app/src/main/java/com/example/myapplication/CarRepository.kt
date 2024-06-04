package com.example.myapplication

class CarRepository {
    private val cars = listOf(
        Car("1", "Car 1", "Description for car 1", 100.0),
        Car("2", "Car 2", "Description for car 2", 200.0),
        Car("3", "Car 3", "Description for car 3", 300.0)
    )

    fun getCars(): List<Car> = cars

    fun getCarById(id: String): Car? = cars.find { it.id == id }
}
