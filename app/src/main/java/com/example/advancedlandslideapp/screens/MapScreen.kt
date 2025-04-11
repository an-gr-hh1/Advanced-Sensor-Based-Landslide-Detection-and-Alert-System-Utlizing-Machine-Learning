package com.example.advancedlandslideapp.screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import com.example.advancedlandslideapp.R
import com.example.advancedlandslideapp.models.HazardPoint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.advancedlandslideapp.utils.fetchLocation
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.CameraPosition
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.json.JSONObject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController
) {
    val context = LocalContext.current

    val sensorData = Firebase.database.reference.child("sensor_readings")

    var gpsLat by remember { mutableStateOf<Double?>(null) }
    var gpsLong by remember { mutableStateOf<Double?>(null) }
    var rainfall by remember { mutableStateOf("") }
    var soilMoisture by remember { mutableStateOf("") }
    var vibration by remember { mutableStateOf("") }
    var alertActive by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val sensorListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                gpsLat = snapshot.child("gps_latitude").getValue(Double::class.java)
                gpsLong = snapshot.child("gps_longitude").getValue(Double::class.java)
                rainfall = snapshot.child("rain_sensor").value.toString()
                soilMoisture = snapshot.child("soil_moisture").value.toString()
                vibration = snapshot.child("vibration").value.toString()
                alertActive = snapshot.child("alert").getValue(Boolean::class.java) ?: false
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Database read cancelled: ${error.message}")
            }
        }
        sensorData.addValueEventListener(sensorListener)
        onDispose { sensorData.removeEventListener(sensorListener) }
    }

    var hazardPoints by remember { mutableStateOf<List<HazardPoint>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            val inputStream = context.resources.openRawResource(R.raw.hazard_points)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            Log.d("MapScreen", "Local JSON: $jsonString")
            // Parse JSON manually using JSONObject
            val jsonObj = JSONObject(jsonString)
            val keys = jsonObj.keys()
            val pointsList = mutableListOf<HazardPoint>()
            while (keys.hasNext()) {
                val key = keys.next()
                val pointObj = jsonObj.getJSONObject(key)
                val lat = pointObj.getDouble("latitude")
                val lon = pointObj.getDouble("longitude")
                val prob = pointObj.getDouble("probability")
                pointsList.add(HazardPoint(latitude = lat, longitude = lon, probability = prob))
            }
            Log.d("MapScreen", "Parsed ${pointsList.size} hazard points")
            hazardPoints = pointsList
        } catch (e: Exception) {
            Log.e("MapScreen", "Error reading local JSON", e)
        }
    }

    // --- User Location Handling ---
    var userLat by remember { mutableStateOf<Double?>(null) }
    var userLon by remember { mutableStateOf<Double?>(null) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            fetchLocation(context) { lat, lon ->
                userLat = lat
                userLon = lon
            }
        }
    }
    LaunchedEffect(Unit) {
        val fineLocationPermission = android.Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(context, fineLocationPermission) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            fetchLocation(context) { lat, lon ->
                userLat = lat
                userLon = lon
            }
        } else {
            locationPermissionLauncher.launch(fineLocationPermission)
        }
    }

    // --- Setup Camera Position State for Google Maps Compose ---
    val initialPosition = if (userLat != null && userLon != null) {
        LatLng(userLat!!, userLon!!)
    } else {
        // Fallback center; you can adjust based on your hazard points if needed.
        LatLng(20.5, 78.9)
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 10f)
    }
    // Update camera position when user location becomes available
    LaunchedEffect(userLat, userLon) {
        if (userLat != null && userLon != null) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(userLat!!, userLon!!), 15f)
        }
    }

    // Use Scaffold with a TopAppBar for the MapScreen
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Map", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF000000))
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                hazardPoints.forEach { point ->
                    Marker(
                        state = MarkerState(position = LatLng(point.latitude, point.longitude)),
                        title = "Risk Score: ${point.probability}",
                        onClick = { false }
                    )
                }
                if (alertActive)
                {
                    Marker(
                        state = MarkerState(position = LatLng(gpsLat!!, gpsLong!!)),
                        title = "Landslide Alert",
                        snippet = "Rainfall: $rainfall%, Soil Moisture: $soilMoisture%, Vibration: $vibration Hz",
                        onClick = { false }
                    )
                }
            }
        }
    }
}

