package com.example.rent.model

data class Booking(
    val id: String = "",
    val userId: String = "",
    val carId: String = "",
    val carName: String = "",
    val hours: Int = 0,
    val totalPrice: Float = 0f,
    val timestamp: Long = 0L
)