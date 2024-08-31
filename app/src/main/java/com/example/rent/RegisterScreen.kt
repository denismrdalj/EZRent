package com.example.rent

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RegisterScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Create a New Account", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(32.dp))
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        Button(
            modifier = Modifier.padding(16.dp),
            onClick = {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val currentUser = FirebaseAuth.getInstance().currentUser
                            if (currentUser != null) {
                                val db = FirebaseFirestore.getInstance()
                                val user = hashMapOf(
                                    "uid" to currentUser.uid,
                                    "role" to "user",
                                    "totalBookings" to 0
                                )
                                db.collection("users").document(currentUser.uid)
                                    .set(user)
                                    .addOnSuccessListener {
                                        navController.navigate("cars") {
                                            popUpTo("register") { inclusive = true }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                        } else {
                            Toast.makeText(context, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Blue,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(40.dp, 20.dp),
            shape = RoundedCornerShape(50)
        ) {
            Text("Register")
        }
    }
}

