package com.example.advancedlandslideapp.utils

import android.content.Context
import android.widget.Toast
import com.google.android.gms.location.LocationServices

fun fetchLocation(context: Context, onLocationFetched: (Double, Double) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    try {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                onLocationFetched(location.latitude, location.longitude)
            } else {
                Toast.makeText(context, "Unable to get current location", Toast.LENGTH_SHORT).show()
            }
        }
    } catch (e: SecurityException) {
        Toast.makeText(context, "Location permission not granted", Toast.LENGTH_SHORT).show()
    }
}