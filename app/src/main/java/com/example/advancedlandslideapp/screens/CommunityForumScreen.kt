package com.example.advancedlandslideapp.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.advancedlandslideapp.R
import com.example.advancedlandslideapp.models.ForumPost
import com.example.advancedlandslideapp.models.Incident
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityForumScreen(
    navController: NavController
) {
    // Two tabs: Forum and Reports
    val tabTitles = listOf("Forum", "Reports")
    var selectedTabIndex by remember { mutableIntStateOf(0) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Community Forum", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF000000)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Row for switching between Forum and Reports
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = Color.Black,
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Color.Black
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = (selectedTabIndex == index),
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTabIndex == index) Color.Black else Color.Gray
                            )
                        }
                    )
                }
            }

            // Display content based on selected tab
            when (selectedTabIndex) {
                0 -> ForumTabContent()
                1 -> ReportsTabContent()
            }
        }
    }
}


@Composable
fun ForumTabContent() {
    val auth = Firebase.auth
    val uid = auth.currentUser?.uid ?: "guest"
    val forumData = Firebase.database.reference.child("forum")
    val userData = Firebase.database.reference.child("users").child(uid)
    var posts by remember { mutableStateOf(listOf<ForumPost>()) }
    var newPost by remember { mutableStateOf("") }
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    LaunchedEffect(uid) {
        userData.get().addOnSuccessListener { snapshot ->
            name = snapshot.child("name").value?.toString() ?: ""
        }
    }
    // Listen for changes in "forum" node
    DisposableEffect(Unit) {
        val forumListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ForumPost>()
                snapshot.children.forEach { child ->
                    child.getValue(ForumPost::class.java)?.let { list.add(it) }
                }
                // Sort posts descending by timestamp
                posts = list.sortedByDescending { it.timestamp }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        forumData.addValueEventListener(forumListener)
        onDispose { forumData.removeEventListener(forumListener) }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
        // Input field for new forum post
        OutlinedTextField(
            value = newPost,
            onValueChange = { newPost = it },
            label = { Text("Write your post here") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Button(onClick = {
            if (newPost.isNotBlank()) {
                val postId = forumData.push().key ?: ""
                val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                val isTrusted = auth.currentUser?.isAnonymous == false
                val userName = if (auth.currentUser?.isAnonymous == true) {
                    "Guest"
                } else {
                    auth.currentUser?.displayName ?: name
                }
                // For now, we store an empty string for userProfileUrl
                val userProfileUrl = ""
                val post = ForumPost(
                    id = postId,
                    uid = uid,
                    userName = userName,
                    content = newPost,
                    timestamp = currentTime,
                    trusted = isTrusted,
                    userProfileUrl = userProfileUrl
                )
                forumData.child(postId).setValue(post).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        newPost = ""
                        Toast.makeText(context, "Post submitted", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to post", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF000000))
        ) {
            Text("Post")
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Display forum posts
        LazyColumn {
            items(posts) { post ->
                ForumPostCard(post = post)
            }
        }
    }
}


@Composable
fun ReportsTabContent() {
    val reportData = Firebase.database.reference.child("incidents")
    var incidents by remember { mutableStateOf(emptyList<Incident>()) }

    DisposableEffect(Unit) {
        val incidentListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Incident>()
                snapshot.children.forEach { child ->
                    child.getValue(Incident::class.java)?.let { list.add(it) }
                }
                // Sort by descending timestamp (newest first)
                incidents = list.sortedByDescending { it.timestamp }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        reportData.addValueEventListener(incidentListener)
        onDispose { reportData.removeEventListener(incidentListener) }
    }

    if (incidents.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No reports found", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(incidents) { incident ->
                IncidentCard(incident = incident)
            }
        }
    }
}


@Composable
fun ForumPostCard(post: ForumPost) {
    // Split timestamp into date and time
    val dateTimeParts = post.timestamp.split(" ")
    val datePart = dateTimeParts.getOrNull(0) ?: "N/A"
    val timePart = dateTimeParts.getOrNull(1) ?: "N/A"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Row for profile image and user info
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Profile image (placeholder)
                Image(
                    painter = painterResource(id = R.drawable.ic_profile_placeholder),
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.userName.ifBlank { "Guest" },
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        if (post.trusted) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Verified",
                                tint = Color(0xFF1DA1F2),
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Post content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Date and time row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "Date: $datePart",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Time: $timePart",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}


@Composable
fun IncidentCard(incident: Incident) {
    // Split timestamp into date and time
    val parts = incident.timestamp.split(" ")
    val datePart = parts.getOrNull(0) ?: "N/A"
    val timePart = parts.getOrNull(1) ?: "N/A"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Incident Report",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (incident.trusted) {
                Text(
                    text = "Reported by Verified User",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF1DA1F2))
                )
            } else {
                Text(
                    text = "Reported by Guest",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Description: ${incident.description}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Location: (${incident.latitude}, ${incident.longitude})",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Date: $datePart", style = MaterialTheme.typography.bodySmall)
            Text(text = "Time: $timePart", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            if (incident.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = incident.imageUrl,
                    contentDescription = "Incident Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = "No image attached",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}