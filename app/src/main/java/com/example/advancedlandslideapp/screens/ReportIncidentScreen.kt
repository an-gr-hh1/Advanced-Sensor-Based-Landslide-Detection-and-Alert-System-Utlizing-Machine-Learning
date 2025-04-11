package com.example.advancedlandslideapp.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.advancedlandslideapp.models.Incident
import com.example.advancedlandslideapp.utils.fetchLocation
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportIncidentScreen(
    navController: NavController
) {
    val context = LocalContext.current

    // State variables
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    // Location states
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            fetchLocation(context) { lat: Double, lon: Double ->
                latitude = lat
                longitude = lon
            }
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Check & request location permission when this screen appears
    LaunchedEffect(Unit) {
        val fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(context, fineLocationPermission) == PackageManager.PERMISSION_GRANTED) {
            fetchLocation(context) { lat: Double, lon: Double ->
                latitude = lat
                longitude = lon
            }
        } else {
            locationPermissionLauncher.launch(fineLocationPermission)
        }
    }

    // Wrap both top bar and report form in a Column
    Column(modifier = Modifier.fillMaxSize()) {
        // Custom Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF000000))
                .padding(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Report Incident",
                    style = MaterialTheme.typography.headlineSmall.copy(color = Color.White, fontSize = 19.sp)
                )
            }
        }

        // Card for Report Form; uses weight(1f) to fill remaining space
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Report",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Image selected", style = MaterialTheme.typography.bodySmall)
                } else {
                    Text(text = "No image selected", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF000000))
                ) {
                    Text("Select Image (Optional)")
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Describe the incident") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (latitude != null && longitude != null) {
                    Text(
                        text = "Location: (${latitude?.format(4)}, ${longitude?.format(4)})",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = "Fetching location...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (description.isBlank()) {
                            Toast.makeText(context, "Description cannot be empty", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        submitReport(
                            context = context,
                            description = description,
                            imageUri = imageUri,
                            latitude = latitude ?: 0.0,
                            longitude = longitude ?: 0.0,
                            onUploadingChange = { uploading -> isUploading = uploading },
                            onDone = {
                                description = ""
                                imageUri = null
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF000000)),
                    enabled = !isUploading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isUploading) Text("Submitting...") else Text("Submit Report")
                }
            }
        }
    }
}


fun Double.format(decimalPlaces: Int): String {
    return "%.${decimalPlaces}f".format(this)
}


 // Function  to submit the incident report.
fun submitReport(
    context: Context,
    description: String,
    imageUri: Uri?,
    latitude: Double,
    longitude: Double,
    onUploadingChange: (Boolean) -> Unit,
    onDone: () -> Unit
) {
    val auth = Firebase.auth
    val database = Firebase.database.reference.child("incidents")
    val storage = Firebase.storage.reference.child("incident_images")

    onUploadingChange(true)
    val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    val uid = auth.currentUser?.uid ?: "guest"
    val isTrusted = auth.currentUser?.isAnonymous == false
    val reportId = database.push().key ?: ""

    fun submitReportToDB(imageUrl: String = "") {
        val report = Incident(
            id = reportId,
            uid = uid,
            description = description,
            latitude = latitude,
            longitude = longitude,
            imageUrl = imageUrl,
            timestamp = currentTime,
            trusted = isTrusted
        )
        database.child(reportId).setValue(report).addOnCompleteListener { task ->
            onUploadingChange(false)
            if (task.isSuccessful) {
                Toast.makeText(context, "Incident reported", Toast.LENGTH_SHORT).show()
                onDone()
            } else {
                Toast.makeText(context, "Report submission failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (imageUri != null) {
        val imageRef = storage.child("${UUID.randomUUID()}.jpg")
        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    submitReportToDB(uri.toString())
                }
            }
            .addOnFailureListener {
                onUploadingChange(false)
                Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
            }
    } else {
        submitReportToDB()
    }
}