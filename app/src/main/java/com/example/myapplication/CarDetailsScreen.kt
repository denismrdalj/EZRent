package com.example.myapplication

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun CarDetailsScreen(carId: String?) {
    Column {
        Text(text = "Details for $carId")
        // Add more details and booking button
    }
}
