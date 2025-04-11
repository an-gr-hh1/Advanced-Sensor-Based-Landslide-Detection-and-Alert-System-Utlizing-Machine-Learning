package com.example.advancedlandslideapp.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.advancedlandslideapp.R
import com.example.advancedlandslideapp.components.LottieAlertDialog
import com.example.advancedlandslideapp.utils.createNotificationChannel
import com.example.advancedlandslideapp.utils.showAlertNotification
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController
) {
    val auth = Firebase.auth
    val uid = auth.currentUser?.uid ?: ""
    val alertData = Firebase.database.reference.child("alerts")
    val userData = Firebase.database.reference.child("users").child(uid)
    val sensorData = Firebase.database.reference.child("sensor_readings")
    val context = LocalContext.current
    val apiKey = context.getString(R.string.weather_api_key)

    var alertText by remember { mutableStateOf("") }
    val alert by remember { mutableStateOf("No alerts") }

    var name by remember { mutableStateOf("Guest") }

    // IoT sensor data states
    var rainfall by remember { mutableStateOf("N/A") }
    var soilMoisture by remember { mutableStateOf("N/A") }
    var alertActive by remember { mutableStateOf(false) }

    // Weather state and current location states
    var weather by remember { mutableStateOf("Loading...") }
    var currentLat by remember { mutableStateOf<Double?>(null) }
    var currentLon by remember { mutableStateOf<Double?>(null) }


    LaunchedEffect(uid) {
        userData.get().addOnSuccessListener { snapshot ->
            name = snapshot.child("name").value?.toString() ?: "Guest"
        }
    }

    LaunchedEffect(Unit) {
        alertData.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                alertText = (snapshot.getValue<String>() ?: "").toString()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching alerts: ${error.message}")
            }
        })
    }

    // Launcher for location permission
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            fetchLocation(context) { lat, lon ->
                currentLat = lat
                currentLon = lon
            }
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }


    // Request location permission on start
    LaunchedEffect(Unit) {
        val fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(context, fineLocationPermission) == PackageManager.PERMISSION_GRANTED) {
            fetchLocation(context) { lat, lon ->
                currentLat = lat
                currentLon = lon
            }
        } else {
            locationPermissionLauncher.launch(fineLocationPermission)
        }
    }

    // Once location is available, fetch weather data from the API
    LaunchedEffect(currentLat, currentLon) {
        if (currentLat != null && currentLon != null) {
            weather = fetchWeatherData(
                latitude = currentLat!!,
                longitude = currentLon!!,
                apiKey = apiKey
            )
        }
    }

    // Listen for IoT sensor data changes from Firebase
    DisposableEffect(Unit) {
        val sensorListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rainValue = snapshot.child("rain_sensor").value
                rainfall = rainValue?.toString() ?: "N/A"
                val soilValue = snapshot.child("soil_moisture").value
                soilMoisture = soilValue?.toString() ?: "N/A"
                alertActive = snapshot.child("alert").getValue(Boolean::class.java) ?: false
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        sensorData.addValueEventListener(sensorListener)
        onDispose { sensorData.removeEventListener(sensorListener) }
    }


    // Main UI with Scaffold (and bottom navigation)
    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF000000)
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                // Greeting & day icon (top section)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        TimeBasedText()
                        Text(
                            text = name,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    TimeBasedIcon()
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Row of Rainfall & Soil Moisture
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoCard(
                        title = "Rainfall",
                        value = "$rainfall%",
                        backgroundColor = Color(0xFFFAFAFA),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    InfoCard(
                        title = "Soil Moisture",
                        value = "$soilMoisture%",
                        backgroundColor = Color(0xFFE8F5E9),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Weather from API
                InfoCard(
                    title = "Weather",
                    value = weather,
                    backgroundColor = Color(0xFFE8F1F5),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Alert Box
                AlertsCard(
                        alertMessage = if (alertActive) alertText else alert,
                        backgroundColor = Color(0xFFF8ECEC),
                        )
                Spacer(modifier = Modifier.height(18.dp))

                // Bottom row
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Card 1: Map View
                    BottomActionCard(
                        title = "Map View",
                        painter = painterResource(id = R.drawable.ic_map),
                        backgroundColor = Color(0xFFF2F2F2),
                        onClick = { navController.navigate("map") }
                    )
                    // Card 2: Report Incident
                    BottomActionCard(
                        title = "Report",
                        painter = painterResource(id = R.drawable.ic_incident),
                        backgroundColor = Color(0xFFF2F2F2),
                        onClick = { navController.navigate("report") }
                    )
                }
                if (alertActive) {
                    createNotificationChannel(context)
                    showAlertNotification(context, alertText)
                    LottieAlertDialog(message = alertText)
                }
            }
        }
    }
}


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


@Composable
fun InfoCard(
    title: String,
    value: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color.Black,
                    fontWeight = FontWeight.Bold)
            )
        }
    }
}


@Composable
fun BottomActionCard(
    title: String,
    painter: Painter,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .width(150.dp)
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painter,
                contentDescription = title,
                modifier = Modifier
                    .size(85.dp)

            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun AlertsCard(
    alertMessage: String,
    backgroundColor: Color,
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Alert Icon",
                tint = Color.Red,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = alertMessage,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold,  fontSize = 14.sp),
                textAlign = TextAlign.Center,
                color = Color.Black
            )
        }
    }
}


@Composable
fun TimeBasedIcon(modifier: Modifier = Modifier) {
    val currentTime = SimpleDateFormat("HH", Locale.getDefault()).format(Date())
    val hour = currentTime.toInt()

    val iconImageVector = when(hour) {
        in 5..11 -> Icons.Default.WbSunny
        in 12..17 -> Icons.Default.WbTwilight
        else -> Icons.Default.ModeNight
    }

    Icon(
        imageVector = iconImageVector,
        contentDescription = "Weather Icon",
        tint = Color.DarkGray,
        modifier = modifier.size(36.dp)
    )
}

@Composable
fun TimeBasedText(modifier: Modifier = Modifier) {
    val currentTime = SimpleDateFormat("HH", Locale.getDefault()).format(Date())
    val hour = currentTime.toInt()

    val timeText = when(hour) {
        in 5..11 -> "Good Morning,"
        in 12..17 -> "Good Afternoon,"
        else -> "Good Night,"
    }

    Text(
        text = timeText,
        style = MaterialTheme.typography.bodyLarge.copy(
            color = Color.Black,
            fontSize = 20.sp
        ),
        modifier = modifier
    )
}


@Composable
fun BottomNavigationBar(navController: NavController) {
    val navItems = listOf(
        NavItem("Home", Icons.Default.Home, "dashboard"),
        NavItem("Map", Icons.Default.Map, "map"),
        NavItem("Forum", Icons.Default.Forum, "forum"),
        NavItem("Report", Icons.Default.Queue, "report"),
        NavItem("Disaster", Icons.Default.School, "disaster"),
        NavItem("Profile", Icons.Default.Person, "profile")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color.White)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        navItems.forEach { item ->
            IconButton(
                onClick = {
                    navController.navigate(item.route) {
                        // Preserve ViewModel state
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = Color.Gray
                )
            }
        }
    }
}


data class NavItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)


suspend fun fetchWeatherData(latitude: Double, longitude: Double, apiKey: String): String {
    val client = OkHttpClient()
    val url = "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&units=metric&appid=$apiKey"
    val request = Request.Builder().url(url).build()

    return withContext(Dispatchers.IO) {
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string()?.let { body ->
                    val json = JSONObject(body)
                    val main = json.getJSONObject("main")
                    val temp = main.getDouble("temp")
                    return@withContext "$temp°"
                }
                "No data"
            } else {
                "Error: ${response.code}"
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}