package com.example.rent

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.rent.model.Booking
import com.example.rent.model.Car
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun CarDetailsScreen(carId: String, navController: NavController) {
    val car = remember { mutableStateOf<Car?>(null) }
    val hoursState = remember { mutableStateOf(1) }
    val priceState = remember { mutableStateOf(0f) }
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val discount = remember { mutableStateOf(0f) }
    val userRole = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(user) {
        user?.let {
            FirebaseFirestore.getInstance().collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    userRole.value = document.getString("role")
                }
        }
    }

    LaunchedEffect(carId) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("cars").document(carId).get().addOnSuccessListener { document ->
            car.value = document.toObject(Car::class.java)
        }
    }

    LaunchedEffect(user) {
        user?.let {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("bookings").whereEqualTo("userId", user.uid).get()
                .addOnSuccessListener { result ->
                    val totalBookings = result.size()
                    discount.value = when {
                        totalBookings >= 5 -> 0.15f
                        totalBookings >= 3 -> 0.10f
                        else -> 0f
                    }
                }
        }
    }

    LaunchedEffect(hoursState.value, car.value, discount.value) {
        car.value?.let {
            val basePrice = it.hourlyRate * hoursState.value
            val discountedPrice = basePrice * (1 - discount.value)
            priceState.value = discountedPrice.toFloat()
        }
    }

    car.value?.let { car ->
        val painter = rememberAsyncImagePainter(model = car.imageUrl)
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .size(300.dp)
                )
            }
            item {
                Text(
                    text = car.name,
                    style = MaterialTheme.typography.h4
                )
            }
            item {
                Text(
                    text = car.description,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                TextField(
                    value = hoursState.value.toString(),
                    onValueChange = {
                        val newValue = it.toIntOrNull()
                        if (newValue != null && newValue > 0) {
                            hoursState.value = newValue
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    label = { Text("Hours to book") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Total Price: \$${String.format("%.2f", priceState.value)}",
                    style = MaterialTheme.typography.h6
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    if (user != null) {
                        val newBooking = Booking(
                            userId = user.uid,
                            carId = carId,
                            carName = car.name,
                            hours = hoursState.value,
                            totalPrice = priceState.value,
                            timestamp = System.currentTimeMillis()
                        )

                        FirebaseFirestore.getInstance().collection("bookings")
                            .add(newBooking)
                            .addOnSuccessListener { documentReference ->
                                val updatedBooking = newBooking.copy(id = documentReference.id)

                                FirebaseFirestore.getInstance().collection("bookings")
                                    .document(updatedBooking.id)
                                    .set(updatedBooking)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Booking confirmed", Toast.LENGTH_SHORT).show()
                                        navController.popBackStack()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Booking failed", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Booking failed", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(context, "Please log in to book a car", Toast.LENGTH_SHORT).show()
                    }
                },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Blue,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(40.dp, 20.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Book Now")
                }
            }

            if (userRole.value == "admin") {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            FirebaseFirestore.getInstance().collection("cars")
                                .document(carId)
                                .delete()
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Car deleted successfully", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Failed to delete car", Toast.LENGTH_SHORT).show()
                                }
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Red,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(40.dp, 20.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Delete Car", color = Color.White)
                    }
                }
            }
        }
    }
}




