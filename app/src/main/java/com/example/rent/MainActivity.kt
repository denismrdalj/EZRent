package com.example.rent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.firestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent {
            RentApp()
        }
    }
}

@Composable
fun RentApp() {
    val navController = rememberNavController()
    var currentUser by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }
    var userRole by remember { mutableStateOf<String?>(null) }
    val authStateListener = rememberUpdatedState { user: FirebaseUser? ->
        currentUser = user
        if (user != null) {
            val db = Firebase.firestore
            val docRef = db.collection("users").document(user.uid)
            docRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    userRole = document.getString("role")
                } else {
                    userRole = "user"
                }
            }.addOnFailureListener {
                userRole = "user"
            }
        } else {
            userRole = null
        }
    }

    val auth = FirebaseAuth.getInstance()
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { authStateListener.value(it.currentUser) }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController, currentUser)
        }
    ) { innerPadding ->
        NavGraph(navController = navController, userRole = userRole, innerPadding = innerPadding)
    }
}

@Composable
fun NavGraph(navController: NavHostController, userRole: String?, innerPadding: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = if (FirebaseAuth.getInstance().currentUser != null) "cars" else "login",
        modifier = Modifier.padding(innerPadding)
    ) {
        composable("cars") { CarsListScreen(navController) }
        composable("profile") { ProfileScreen(navController, userRole) }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("car_details/{carId}") { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId") ?: return@composable
            CarDetailsScreen(carId, navController)
        }
    }
}

