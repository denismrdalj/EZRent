package com.example.myapplication

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController) {
    val cars = listOf("Car 1", "Car 2", "Car 3") // Example data

    LazyColumn {
        items(cars) { car ->
            Text(
                text = car,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("carDetails/$car")
                    }
            )
        }
    }
}
