package com.example.advancedlandslideapp.screens

import android.content.Context
import android.content.Intent
import android.webkit.WebView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.core.net.toUri
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.RunCircle
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisasterPreparednessScreen(
    navController: NavController
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Preparedness Guide",
                        color = Color.White
                    )
                },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Card for Preparedness, Prevention & Mitigation Guide
            ContentCard(
                title = "Landslide Guide",
                icon = Icons.Default.Warning,
                iconTint = Color(0xFFE65100)
            ) {
                Text(
                    text = "Landslides are the downward movement of rock, soil, and debris triggered by factors like heavy rainfall, earthquakes, volcanic activity, or human activities.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                SectionTitle(text = "Signs of an Impending Landslide:")

                BulletPoint(text = "Cracks in soil, walls, or roads")
                BulletPoint(text = "Tilting trees, fences, or utility poles")
                BulletPoint(text = "Sudden changes in water flow")
                BulletPoint(text = "Unusual sounds like rumbling or trees cracking")

                Spacer(modifier = Modifier.height(16.dp))

                SectionTitle(text = "What to Do Before a Landslide:")

                BulletPoint(text = "Avoid high-risk zones: Steep slopes, mountain edges, river valleys")
                BulletPoint(text = "Plant vegetation: Deep-rooted plants stabilize soil")
                BulletPoint(text = "Install drainage systems: Preventing water accumulation")
                BulletPoint(text = "Consult experts: Assess slope stability if building in landslide-prone areas")
            }

            Spacer(modifier = Modifier.height(16.dp))

            ContentCard(
                title = "During a Landslide: Immediate Actions",
                icon = Icons.Default.RunCircle,
                iconTint = Color(0xFFD32F2F)
            ) {
                SectionTitle(text = "If You're Indoors:")

                BulletPoint(text = "Evacuate immediately if authorities issue warnings or you hear unusual sounds")
                BulletPoint(text = "Avoid rooms on the downhill side of the house. Shelter under sturdy furniture if trapped")

                Spacer(modifier = Modifier.height(16.dp))

                SectionTitle(text = "If You're Outdoors:")

                BulletPoint(text = "Run to the nearest high ground away from the landslide path")
                BulletPoint(text = "Avoid river valleys, steep slopes, and debris flow channels")
                BulletPoint(text = "If escape isn't possible, curl into a tight ball to protect your head")

                Spacer(modifier = Modifier.height(16.dp))

                SectionTitle(text = "If You're Driving:")

                BulletPoint(text = "Watch for collapsed pavement, mud, or falling rocks")
                BulletPoint(text = "Abandon your car if debris approaches; move on foot to higher ground")

                Spacer(modifier = Modifier.height(16.dp))

                SectionTitle(text = "Critical Don'ts:", textColor = Color(0xFFD32F2F))

                BulletPoint(text = "Never try to outrun a landslide—debris flows can exceed 35 mph", isWarning = true)
                BulletPoint(text = "Do not enter landslide areas to rescue others; call emergency services instead", isWarning = true)
                BulletPoint(text = "Avoid downed power lines or gas leaks (risk of explosions)", isWarning = true)
            }

            Spacer(modifier = Modifier.height(16.dp))

            ContentCard(
                title = "Emergency Contacts",
                icon = Icons.Default.Call,
                iconTint = Color(0xFF1B5E20)
            ) {
                ContactSection(
                    title = "National Emergency Helpline:",
                    contacts = listOf("112")
                )

                Spacer(modifier = Modifier.height(12.dp))

                ContactSection(
                    title = "Police:",
                    contacts = listOf(
                        "Trivandrum: 0471-2331843", "Kollam: 0474-2746000",
                        "Pathanamthitta: 0468-2222226", "Alappuzha: 0477-2251166",
                        "Kottayam: 0481-5550400", "Idukki: 04862-221100",
                        "Ernakulam: 0484-2359200", "Thrissur: 0487-2424193",
                        "Palakkad: 0491-2522340", "Malappuram: 0483-2734966",
                        "Kozhikode: 0495-2721831", "Wayanad: 04936-205808",
                        "Kannur: 0497-2763337", "Kasargod: 04994-222960"
                    ),
                    columns = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                ContactSection(
                    title = "Fire and Rescue:",
                    contacts = listOf("101")
                )

                Spacer(modifier = Modifier.height(12.dp))

                ContactSection(
                    title = "State Disaster Management Helpline:",
                    contacts = listOf("1070")
                )

                Spacer(modifier = Modifier.height(12.dp))

                ContactSection(
                    title = "Medical Emergency:",
                    contacts = listOf("108")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card for YouTube Video
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE57373))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Watch this video for more information",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.allowFileAccess = false
                                settings.allowContentAccess = false
                                loadData(
                                    """
                                    <html>
                                      <body style="margin:0;padding:0;">
                                        <iframe width="100%" height="200" 
                                          src="https://www.youtube.com/embed/9j_StYqR_Pg?si=AKR8v0rrrQvigIhE" 
                                          frameborder="0" allowfullscreen>
                                        </iframe>
                                      </body>
                                    </html>
                                    """.trimIndent(),
                                    "text/html",
                                    "utf-8"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card for Informative Articles/Blogs
            ContentCard(
                title = "Informative Articles",
                icon = Icons.AutoMirrored.Filled.Article,
                iconTint = Color(0xFF1565C0)
            ) {
                BlogLinkCard(
                    title = "Tips for staying safe during a landslide",
                    url = "https://www.chubb.com/us-en/individuals-families/resources/tips-for-staying-safe-during-a-landslide.html",
                    context = context
                )

                Spacer(modifier = Modifier.height(12.dp))

                BlogLinkCard(
                    title = "Landslide Preparedness: Survival actions",
                    url = "https://www.washington.edu/news/2020/10/22/simple-actions-can-help-people-survive-landslides-uw-analysis-shows/",
                    context = context
                )
            }
        }
    }
}


@Composable
fun ContentCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(iconTint.copy(alpha = 0.2f))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = iconTint
                )
            }
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun SectionTitle(text: String, textColor: Color = Color(0xFF263238)) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold
        ),
        color = textColor,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun BulletPoint(text: String, isWarning: Boolean = false) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .padding(top = 6.dp)
                .background(
                    if (isWarning) Color(0xFFD32F2F) else Color(0xFF1976D2),
                    shape = CircleShape
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isWarning) Color(0xFFD32F2F) else Color.DarkGray
        )
    }
}

@Composable
fun ContactSection(title: String, contacts: List<String>, columns: Int = 1) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold
        ),
        color = Color(0xFF263238)
    )

    Spacer(modifier = Modifier.height(4.dp))

    if (columns == 1) {
        contacts.forEach { contact ->
            Text(
                text = contact,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 16.dp, bottom = 2.dp),
                color = Color(0xFF424242)
            )
        }
    } else {
        val chunkedContacts = contacts.chunked(contacts.size / columns + if (contacts.size % columns != 0) 1 else 0)

        Row(modifier = Modifier.fillMaxWidth()) {
            chunkedContacts.forEach { columnContacts ->
                Column(modifier = Modifier.weight(1f)) {
                    columnContacts.forEach { contact ->
                        Text(
                            text = contact,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                            color = Color(0xFF424242)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun BlogLinkCard(title: String, url: String, context: Context) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                    color = Color(0xFF0D47A1)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Tap to read the full article",
                    color = Color(0xFF1976D2),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}