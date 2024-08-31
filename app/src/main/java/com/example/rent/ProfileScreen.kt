package com.example.rent

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.rent.model.Booking
import com.example.rent.model.Car
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


@Composable
fun ProfileScreen(navController: NavController, userRole: String?) {
    val context = LocalContext.current
    val bookings = remember { mutableStateListOf<Booking>() }
    val user = FirebaseAuth.getInstance().currentUser
    val totalBookings = remember { mutableStateOf(0) }

    LaunchedEffect(user) {
        user?.let {
            val firestore = FirebaseFirestore.getInstance()
            val userDocRef = firestore.collection("users").document(user.uid)

            userDocRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    totalBookings.value = documentSnapshot.getLong("totalBookings")?.toInt() ?: 0
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to load total bookings", Toast.LENGTH_SHORT).show()
            }

            firestore.collection("bookings").whereEqualTo("userId", user.uid).get()
                .addOnSuccessListener { querySnapshot ->
                    val bookingList = querySnapshot.documents.mapNotNull { it.toObject(Booking::class.java) }
                    bookings.clear()
                    bookings.addAll(bookingList)
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to load bookings", Toast.LENGTH_SHORT).show()
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (userRole == "admin") {
            AdminUI(navController)
        } else if (userRole == "user") {
            UserUI(navController, bookings, user, totalBookings.value)
        }
    }
}


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun AdminUI(navController: NavController) {
    var year by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var hourlyRate by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    val storage = FirebaseStorage.getInstance()
    val storageRef = storage.reference.child("car_images")

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUrl = uri
    }

    val years = (1900..2024).toList().reversed().map { it.toString() }
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        scaffoldState = scaffoldState,
        content = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        "Welcome, Admin",
                        fontSize = 32.sp,
                        modifier = Modifier.padding(bottom = 16.dp),
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    Text(
                        "Add a new vehicle",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(bottom = 16.dp),
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    Box(modifier = Modifier
                        .width(150.dp)
                        .border(BorderStroke(1.dp, Color.Black), shape = RoundedCornerShape(20.dp))
                        .padding(16.dp)
                        .fillMaxWidth()
                        .clickable { expanded = true }) {
                        Text(
                            text = if (year.isEmpty()) "Year" else year,
                            color = if (year.isEmpty()) MaterialTheme.colors.onSurface.copy(alpha = 0.7f) else Color.Black
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            years.forEach { yearOption ->
                                DropdownMenuItem(onClick = {
                                    year = yearOption
                                    expanded = false
                                }) {
                                    Text(text = yearOption)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    BasicTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier
                            .width(300.dp)
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, Color.Black), shape = RoundedCornerShape(20.dp))
                            .padding(16.dp),
                        textStyle = TextStyle(color = Color.Black),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                            ) {
                                if (name.isEmpty()) Text(
                                    text = "Car Name",
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                                innerTextField()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    BasicTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier
                            .width(300.dp)
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, Color.Black), shape = RoundedCornerShape(20.dp))
                            .padding(16.dp),
                        textStyle = TextStyle(color = Color.Black),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                if (description.isEmpty()) Text(
                                    text = "Car Description",
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                                innerTextField()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    BasicTextField(
                        value = hourlyRate,
                        onValueChange = { hourlyRate = it },
                        modifier = Modifier
                            .width(300.dp)
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, Color.Black), shape = RoundedCornerShape(20.dp))
                            .padding(16.dp),
                        textStyle = TextStyle(color = Color.Black),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                if (hourlyRate.isEmpty()) Text(
                                    text = "Hourly Rate",
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                                innerTextField()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }

                item {
                    imageUrl?.let {
                        val painter = rememberAsyncImagePainter(model = it)
                        Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier.size(128.dp)
                        )
                    }
                }

                item {
                    Button(
                        modifier = Modifier
                            .padding(16.dp),
                        onClick = { launcher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Blue,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(40.dp, 20.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Select Image")
                    }
                }

                item {
                    Button(
                        modifier = Modifier
                            .padding(16.dp),
                        onClick = {
                            if (name.isNotEmpty() && description.isNotEmpty() && hourlyRate.isNotEmpty() && imageUrl != null) {
                                val newCarId = UUID.randomUUID().toString()
                                val carRef = storageRef.child("$newCarId.jpg")
                                carRef.putFile(imageUrl!!)
                                    .addOnSuccessListener {
                                        carRef.downloadUrl.addOnSuccessListener { uri ->
                                            val car = Car(
                                                id = newCarId,
                                                name = name,
                                                description = description,
                                                isAvailable = true,
                                                year = year.toInt(),
                                                imageUrl = uri.toString(),
                                                hourlyRate = hourlyRate.toDouble()
                                            )
                                            FirebaseFirestore.getInstance().collection("cars")
                                                .document(newCarId).set(car)
                                                .addOnSuccessListener {
                                                    Toast.makeText(
                                                        context,
                                                        "Car added successfully",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    year = ""
                                                    name = ""
                                                    description = ""
                                                    hourlyRate = ""
                                                    imageUrl = null
                                                }
                                                .addOnFailureListener {
                                                    Toast.makeText(
                                                        context,
                                                        "Error adding car: ${it.message}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            context,
                                            "Error uploading image: ${it.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Please fill all fields and select an image",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Blue,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(40.dp, 20.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Add Car")
                    }
                }
                item{
                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            FirebaseAuth.getInstance().signOut()
                            val user = FirebaseAuth.getInstance().currentUser
                            Log.d("user", user.toString())
                            Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                            navController.navigate("login") {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Red,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(40.dp, 20.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Log Out")
                    }
                }
            }
        }
    )
}

@Composable
fun UserUI(navController: NavController, bookings: SnapshotStateList<Booking>, user: FirebaseUser?, totalBookings: Int) {
    val context = LocalContext.current
    val openDialog = remember { mutableStateOf(false) }
    var bookingToCancel by remember { mutableStateOf<Booking?>(null) }

    val discount = when {
        totalBookings >= 5 -> 15
        totalBookings >= 3 -> 10
        else -> 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome, User", style = MaterialTheme.typography.h4)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Total Bookings: $totalBookings",
            style = MaterialTheme.typography.h6
        )
        Text(
            text = "Current Discount: ${discount}%",
            style = MaterialTheme.typography.h6
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                navController.navigate("login") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Red,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(40.dp, 20.dp),
            shape = RoundedCornerShape(50)
        ) {
            Text("Log Out")
        }

        bookings.forEach { booking ->
            val formattedDate = formatDate(booking.timestamp)
            Card(
                backgroundColor = Color.LightGray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Car: ${booking.carName}", style = MaterialTheme.typography.h6)
                    Text(text = "Hours: ${booking.hours}", style = MaterialTheme.typography.body2, fontSize = 18.sp)
                    Text(text = "Booking Date: $formattedDate", style = MaterialTheme.typography.body2, fontSize = 18.sp)

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            bookingToCancel = booking
                            openDialog.value = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Red,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Cancel Booking")
                    }
                }
            }
        }
    }

    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = {
                openDialog.value = false
            },
            title = {
                Text(text = "Cancel Booking")
            },
            text = {
                Text("Are you sure you want to cancel this booking?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog.value = false
                        bookingToCancel?.let { booking ->
                            cancelBooking(booking, bookings, context)
                        }
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        openDialog.value = false
                    }
                ) {
                    Text("No")
                }
            }
        )
    }
}


fun cancelBooking(booking: Booking, bookings: SnapshotStateList<Booking>, context: Context) {
    val firestore = FirebaseFirestore.getInstance()
    firestore.collection("bookings").document(booking.id)
        .delete()
        .addOnSuccessListener {
            bookings.remove(booking)
            Toast.makeText(context, "Booking canceled successfully", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Failed to cancel booking", Toast.LENGTH_SHORT).show()
        }
}

fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    return format.format(date)
}




