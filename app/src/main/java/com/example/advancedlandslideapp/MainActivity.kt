package com.example.advancedlandslideapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.advancedlandslideapp.navigation.AppNavHost
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Firebase – ensure google-services.json is in your app folder
        FirebaseApp.initializeApp(this)
        setContent {
            AppNavHost()
        }
    }
}