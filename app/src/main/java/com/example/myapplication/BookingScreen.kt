package com.example.myapplication

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun BookingScreen(carId: String?) {
    Column {
        Text(text = "Booking for $carId")
        // Add booking form
        Button(onClick = { /* Handle booking */ }) {
            Text(text = "Book Now")
        }
    }
}
