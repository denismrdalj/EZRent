package com.example.rent

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.rent.model.Car
import com.google.firebase.firestore.FirebaseFirestore
import androidx.navigation.NavHostController
import com.example.rent.model.Booking

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun CarsListScreen(navController: NavHostController) {
    val scaffoldState = rememberScaffoldState()
    val cars = remember { mutableStateOf<List<Car>>(emptyList()) }
    val bookings = remember { mutableStateOf<Map<String, Booking>>(emptyMap()) }

    // Fetch cars and bookings from Firestore
    LaunchedEffect(Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("cars")
            .get()
            .addOnSuccessListener { result ->
                cars.value = result.map { document ->
                    document.toObject(Car::class.java).copy(id = document.id)
                }
            }

        firestore.collection("bookings")
            .get()
            .addOnSuccessListener { result ->
                bookings.value = result.documents.associate { document ->
                    document.getString("carId")!! to document.toObject(Booking::class.java)!!
                }
            }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        content = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    ) {
                        Text(text = "Browse Available Cars", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
                items(cars.value) { car ->
                    val booking = bookings.value[car.id]
                    CarItem(car, booking) { carId ->
                        if (booking == null) {
                            navController.navigate("car_details/$carId")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    )
}

@Composable
fun CarItem(car: Car, booking: Booking?, onClick: (String) -> Unit) {
    val painter = rememberAsyncImagePainter(model = car.imageUrl)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Black, RoundedCornerShape(10.dp))
            .padding(16.dp, 0.dp)
            .clickable(enabled = booking == null) { // Handle click event, disabled if not available
                if (booking == null) {
                    onClick(car.id)
                }
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Column {
                    Text(
                        text = car.year.toString(),
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = car.name,
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Hourly Rate: \$${car.hourlyRate}", color = Color.Black)
            }

            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .width(140.dp)
                    .height(120.dp)
            )
        }
        if (booking != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x80aa0000), RoundedCornerShape(50))
                    .padding(2.dp),
                contentAlignment = Alignment.BottomCenter


            ) {
                Text(
                    text = "Unavailable for ${calculateRemainingHours(booking.timestamp, booking.hours)} more hours",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

fun calculateRemainingHours(bookingTimestamp: Long, bookedHours: Int): Int {
    val currentTime = System.currentTimeMillis()
    val endTime = bookingTimestamp + bookedHours * 3600 * 1000
    return ((endTime - currentTime) / 3600 / 1000).toInt()
}



