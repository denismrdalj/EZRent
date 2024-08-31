package com.example.rent.model

data class Car(
    val id: String = "",
    val year: Int = 0,
    val name: String = "",
    val description: String = "",
    val isAvailable: Boolean = true,
    val imageUrl: String = "",  // Optional: URL to an image of the car
    val hourlyRate: Double = 0.0 // Hourly rate for renting the car
)