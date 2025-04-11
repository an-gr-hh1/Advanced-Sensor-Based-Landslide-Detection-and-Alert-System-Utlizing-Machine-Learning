package com.example.advancedlandslideapp.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.advancedlandslideapp.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController
) {
    val auth = Firebase.auth
    val user = auth.currentUser
    val uid = user?.uid ?: ""
    val database = Firebase.database.reference.child("users").child(uid)
    val context = LocalContext.current

    var name by remember { mutableStateOf("Guest") }
    var email by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var statusMsg by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }

    // Fetch profile data from Realtime Database
    LaunchedEffect(uid) {
        database.get().addOnSuccessListener { snapshot ->
            name = snapshot.child("name").value?.toString() ?: "Guest"
            email = snapshot.child("email").value?.toString() ?: ""
            location = snapshot.child("location").value?.toString() ?: ""
            contact = snapshot.child("contact").value?.toString() ?: ""
        }
    }



    // Use Scaffold to include a top bar with a back button
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Profile Settings", color = Color.White) },
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
        containerColor = Color(0xFFF0F0F0)
    ) { innerPadding ->
        // Main content is offset by innerPadding from Scaffold's top bar.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F0F0))
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top section with a horizontal gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(Color.White)
                ) {
                    // Centered column for avatar, username, email, and back button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Placeholder avatar
                        Box(
                            modifier = Modifier
                                .size(80.dp),
                            content = {
                                Image(
                                    painter = painterResource(id = R.drawable.baseline_account_circle_24),
                                    contentDescription = "Account Icon",
                                    modifier = Modifier.size(80.dp),
                                    colorFilter = ColorFilter.tint(Color.Black)
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(2.dp))

                        // User handle (or just the name)
                        Text(
                            text = "@$name",
                            color = Color.Black,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 18.sp
                        )
                        // User email
                        Text(
                            text = email,
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Back button
                        Button(
                            onClick = {
                                auth.signOut()
                                navController.navigate("login") {
                                    popUpTo("dashboard") { inclusive = true }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF000000))
                        ) {
                            Text(text = "Logout", color = Color.White)
                        }
                    }
                }

                // Content section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 38.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // "Account Info" title
                    Text(
                        text = "Account Info",
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 19.sp),
                        color = Color.Black,
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    if (!isEditing) {
                        // READ-ONLY MODE
                        ProfileInfoRow(
                            icon = Icons.Default.Person,
                            label = "Name",
                            value = name
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ProfileInfoRow(
                            icon = Icons.Default.Email,
                            label = "Email",
                            value = email
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ProfileInfoRow(
                            icon = Icons.Default.LocationOn,
                            label = "Location",
                            value = location
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ProfileInfoRow(
                            icon = Icons.Default.Phone,
                            label = "Contact Info",
                            value = contact
                        )
                        Spacer(modifier = Modifier.height(22.dp))

                        // Distinguish between "guest" & "logged in" for editing
                        val isGuestOrAnonymous = (user == null || user.isAnonymous || name == "Guest")

                        Button(
                            onClick = {
                                if (isGuestOrAnonymous) {
                                    Toast.makeText(
                                        context,
                                        "Login before you can edit profile.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    isEditing = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isGuestOrAnonymous, // disable if guest
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF000000))
                        ) {
                            Text("Edit", color = Color.White)
                        }
                    } else {
                        // EDIT MODE
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Location") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = contact,
                            onValueChange = { contact = it },
                            label = { Text("Contact Info") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val userMap = mapOf(
                                    "name" to name,
                                    "email" to email,
                                    "location" to location,
                                    "contact" to contact
                                )
                                database.setValue(userMap).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        statusMsg = "Profile updated successfully"
                                        isEditing = false
                                    } else {
                                        statusMsg = "Profile update failed: ${task.exception?.message}"
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF000000))
                        ) {
                            Text("Save", color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = {
                                isEditing = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                        ) {
                            Text("Cancel")
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    // Status message
                    if (statusMsg.isNotEmpty()) {
                        Text(
                            text = statusMsg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (statusMsg.contains("failed", ignoreCase = true)) Color.Red else Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF000000),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold , fontSize = 17.sp))
            Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp))
        }
    }
}