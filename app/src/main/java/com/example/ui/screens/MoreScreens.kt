package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.components.CategoryBadge
import com.example.ui.components.DragHandle
import com.example.ui.components.EmptyState
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AccentLime
import com.example.ui.theme.PrimaryGreen

@Composable
fun MoreScreens(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val activeScreen by viewModel.currentActiveScreen.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (activeScreen) {
            "weather" -> WeatherScreen(viewModel)
            "profile" -> ProfileScreen(viewModel)
            "news" -> NewsScreen(viewModel)
            "notifications" -> NotificationsScreen(viewModel)
            "admin_panel" -> AdminPanelScreen(viewModel)
            "image_manager" -> ImageManagerScreen(viewModel)
            else -> MoreOptionsList(viewModel)
        }
    }
}

// --------------------------------------------------
// More Options Index List
// --------------------------------------------------
@Composable
fun MoreOptionsList(viewModel: AppViewModel) {
    val isUrdu by viewModel.isUrdu.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val notificationCount by viewModel.notificationsCount.collectAsState()
    val pendingTradersCount = viewModel.allTraders.collectAsState().value.count { it.status == "Pending" }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SectionHeader(title = Translations.t("more_services", isUrdu))

        // 1. Weather
        MoreOptionRow(
            title = Translations.t("weather_adv", isUrdu),
            desc = "Open-Meteo forecasts with smart agricultural recommendations.",
            icon = Icons.Default.CloudQueue,
            color = Color(0xFF3B82F6),
            onClick = { viewModel.navigateToScreen("weather") }
        )

        // 2. Profile
        MoreOptionRow(
            title = Translations.t("profile", isUrdu),
            desc = "Review permissions, acreage details and crop selections.",
            icon = Icons.Default.AccountCircle,
            color = PrimaryGreen,
            onClick = { viewModel.navigateToScreen("profile") }
        )

        // 3. News
        MoreOptionRow(
            title = Translations.t("agri_news", isUrdu),
            desc = "Latest updates, weather warnings, and wheat policy journals.",
            icon = Icons.Default.Feed,
            color = Color(0xFFFF8C00),
            onClick = { viewModel.navigateToScreen("news") }
        )

        // 4. Notifications
        MoreOptionRow(
            title = Translations.t("noti", isUrdu),
            desc = "System alerts, price updates, and portal registration cues.",
            icon = Icons.Default.Notifications,
            color = Color(0xFFEC4899),
            badgeCount = notificationCount,
            onClick = { viewModel.navigateToScreen("notifications") }
        )

        // 5. Image uploads portal
        MoreOptionRow(
            title = Translations.t("mandi_images", isUrdu),
            desc = "Browse or upload geo-tagged load images of mandi crops directly.",
            icon = Icons.Default.Image,
            color = Color(0xFF14B8A6),
            onClick = { viewModel.navigateToScreen("image_manager") }
        )

        // 6. Admin Panel (Conditional)
        if (user?.role == "Admin") {
            MoreOptionRow(
                title = Translations.t("admin_p", isUrdu),
                desc = "Authorize listings, manage traders databases, and prune accounts.",
                icon = Icons.Default.AdminPanelSettings,
                color = Color(0xFF991B1B),
                badgeCount = pendingTradersCount,
                onClick = { viewModel.navigateToScreen("admin_panel") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 7. Logout
        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = "Exit")
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Log Out of Portal Account", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun MoreOptionRow(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    if (badgeCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.Red)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = badgeCount.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text(text = desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
    }
}


// ==================================================
// Open-Meteo Weather Screen
// ==================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(viewModel: AppViewModel) {
    val isUrdu by viewModel.isUrdu.collectAsState()
    val weatherState by viewModel.weatherState.collectAsState()
    val user by viewModel.currentUser.collectAsState()

    var selectedCity by remember { mutableStateOf(user?.city ?: "Lahore") }
    var expandedDropdown by remember { mutableStateOf(false) }

    val citiesList = listOf(
        "Lahore", "Karachi", "Islamabad", "Faisalabad", "Multan",
        "Rawalpindi", "Gujranwala", "Peshawar", "Sargodha", "Bahawalpur",
        "Sahiwal", "Okara", "Lodhran", "Mailsi", "Vehari"
    )

    // Initial weather fetch triggers
    LaunchedEffect(selectedCity) {
        viewModel.fetchWeather(selectedCity)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Simple Navigation Header back button
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.navigateBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryGreen)
            }
            Text(text = Translations.t("weather_adv", isUrdu), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimaryGreen)
        }

        // City selective dropdown autocomplete autocomplete
        ExposedDropdownMenuBox(
            expanded = expandedDropdown,
            onExpandedChange = { expandedDropdown = !expandedDropdown },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                readOnly = true,
                value = selectedCity,
                onValueChange = {},
                label = { Text("Select Weather City Location") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(10.dp)
            )
            ExposedDropdownMenu(
                expanded = expandedDropdown,
                onDismissRequest = { expandedDropdown = false }
            ) {
                citiesList.forEach { city ->
                    DropdownMenuItem(
                        text = { Text(city) },
                        onClick = {
                            selectedCity = city
                            expandedDropdown = false
                        }
                    )
                }
            }
        }

        if (weatherState.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        } else if (weatherState.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Red.copy(alpha = 0.08f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = weatherState.error!!, color = Color.Red, fontSize = 13.sp, textAlign = TextAlign.Center)
            }
        } else {
            // Success State rendering
            if (weatherState.isOfflineCache) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.DarkGray)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Relying on Offline weather forecast cache.", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Current weather Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                border = BorderStroke(1.dp, Color(0xFFBFDBFE))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = selectedCity.uppercase(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = weatherState.conditionText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${weatherState.temperature.toInt()}°C",
                        fontSize = 54.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E40AF),
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Wind Speed", fontSize = 11.sp, color = Color.Gray)
                            Text(text = "${weatherState.windSpeed} km/h", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Forecasts 7 Days row card
            Text(text = "7-Day Local forecast Index", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryGreen)

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(weatherState.forecastList) { fore ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.width(90.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = fore.dayName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = if (fore.precipitation > 2.0) "🌧️" else "☀️", fontSize = 20.sp)
                            Text(text = "${fore.maxTemp.toInt()}°C", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${fore.minTemp.toInt()}°C", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Professional agronomist advice cards
            Text(text = "📋 Personalized Agricultural Warnings", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryGreen)
            weatherState.adviceList.forEach { adv ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.15f))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Eco, contentDescription = "eco", tint = AccentLime, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = adv, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
            }
        }
    }
}


// ==================================================
// Profile & Edit Details Screen
// ==================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: AppViewModel) {
    val isUrdu by viewModel.isUrdu.collectAsState()
    val user by viewModel.currentUser.collectAsState()

    var fname by remember { mutableStateOf(user?.fname ?: "") }
    var lname by remember { mutableStateOf(user?.lname ?: "") }
    var phone by remember { mutableStateOf(user?.phone ?: "") }
    var wa by remember { mutableStateOf(user?.wa ?: "") }

    var feedbackMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.navigateBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryGreen)
            }
            Text(text = Translations.t("profile", isUrdu), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimaryGreen)
        }

        // Profile avatar centered representation
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(PrimaryGreen)
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            val initials = if (fname.isNotBlank()) fname.take(1).uppercase() + lname.take(1).uppercase() else "M"
            Text(text = initials, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Core Credentials (CNIC Verified)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Identity CNIC", fontSize = 12.sp)
                    Text(user?.cnic ?: "", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Assigned Role", fontSize = 12.sp)
                    Text(user?.role ?: "", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryGreen)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Listed City Location", fontSize = 12.sp)
                    Text(user?.city ?: "", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        OutlinedTextField(
            value = fname,
            onValueChange = { fname = it },
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
            value = lname,
            onValueChange = { lname = it },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = formatPhone(it) },
            label = { Text("Primary Phone Contact") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
            value = wa,
            onValueChange = { wa = formatPhone(it) },
            label = { Text("WhatsApp Connection") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        if (feedbackMsg != null) {
            Text(text = feedbackMsg!!, color = PrimaryGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = {
                if (user != null && fname.isNotBlank()) {
                    val nextUser = user!!.copy(
                        fname = fname.trim(),
                        lname = lname.trim(),
                        phone = phone.trim(),
                        wa = wa.trim()
                    )
                    viewModel.updateProfile(nextUser)
                    feedbackMsg = "Your profile changes saved successfully."
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(text = "Save Profile Changes", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}


// ==================================================
// Agri News Advisory Feed Screen
// ==================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(viewModel: AppViewModel) {
    val isUrdu by viewModel.isUrdu.collectAsState()
    val newsList by viewModel.allNews.collectAsState()
    val user by viewModel.currentUser.collectAsState()

    var showAddNewsBox by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryGreen)
                }
                Text(text = Translations.t("agri_news", isUrdu), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimaryGreen)
            }

            if (user?.role == "Admin") {
                IconButton(onClick = { showAddNewsBox = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Add news", tint = PrimaryGreen)
                }
            }
        }

        newsList.forEach { news ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CategoryBadge(category = news.category)
                        Text(text = news.created_at, fontSize = 10.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = news.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = news.content, fontSize = 12.sp, color = Color.DarkGray, lineHeight = 16.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Advisory Source: ${news.author}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                }
            }
        }
    }

    if (showAddNewsBox) {
        AddNewsDialog(viewModel = viewModel, onDismiss = { showAddNewsBox = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewsDialog(viewModel: AppViewModel, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Prices") }

    val cats = listOf("Prices", "Weather", "Tips", "Policy", "Market", "General")
    var expandedCat by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Publish Agri Advisory", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Advisory Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expandedCat,
                    onExpandedChange = { expandedCat = !expandedCat },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = category,
                        onValueChange = {},
                        label = { Text("Advisory Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCat) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCat,
                        onDismissRequest = { expandedCat = false }
                    ) {
                        cats.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c) },
                                onClick = {
                                    category = c
                                    expandedCat = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        viewModel.publishNews(title, category, content)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text("Publish", color = Color.White)
            }
        }
    )
}


// ==================================================
// Notifications Hub Screen
// ==================================================
@Composable
fun NotificationsScreen(viewModel: AppViewModel) {
    val isUrdu by viewModel.isUrdu.collectAsState()
    val notificationItems by viewModel.currentNotifications.collectAsState()

    // Mark all read on entry
    LaunchedEffect(Unit) {
        viewModel.markAllNotificationsRead()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryGreen)
                }
                Text(text = Translations.t("noti", isUrdu), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimaryGreen)
            }
            TextButton(
                onClick = { viewModel.markAllNotificationsRead() }
            ) {
                Text("Clear All Unread", color = PrimaryGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (notificationItems.isEmpty()) {
            EmptyState(emoji = "🔔", title = "Zero Alerts", subtitle = "Your notification log feed is clear.")
        } else {
            notificationItems.forEach { alert ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimaryGreen.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (alert.type) {
                                    "rate" -> Icons.Default.TrendingUp
                                    "trader" -> Icons.Default.VerifiedUser
                                    "review" -> Icons.Default.Star
                                    else -> Icons.Default.Info
                                },
                                contentDescription = "*",
                                tint = PrimaryGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = alert.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (alert.is_read == 0) PrimaryGreen else Color.Black
                                )
                                if (alert.is_read == 0) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.Red))
                                }
                            }
                            Text(text = alert.message, fontSize = 11.sp, color = Color.DarkGray, lineHeight = 15.sp)
                        }
                    }
                }
            }
        }
    }
}


// ==================================================
// Admin Panel Approvals Controls
// ==================================================
@Composable
fun AdminPanelScreen(viewModel: AppViewModel) {
    val users by viewModel.allUsers.collectAsState()
    val traders by viewModel.allTraders.collectAsState()

    val pendingTradersList = traders.filter { it.status == "Pending" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.navigateBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryGreen)
            }
            Text(text = "Administrative Console", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimaryGreen)
        }

        // 1. Pending authorizations list
        Text(text = "Trader Registration Requests (${pendingTradersList.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryGreen)
        if (pendingTradersList.isEmpty()) {
            Text(text = "No pending directory applications currently.", fontSize = 11.sp, color = Color.Gray)
        } else {
            pendingTradersList.forEach { pending ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = pending.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "Firm: ${pending.shop_name} | City: ${pending.city} | CNIC: ${pending.cnic}", fontSize = 12.sp, color = Color.DarkGray)
                        Text(text = "Primary Contact: ${pending.phone} | Type: ${pending.type}", fontSize = 11.sp, color = Color.DarkGray)

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.approveTrader(pending.id, pending.name) },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Approve Listed", color = Color.White, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { viewModel.rejectTrader(pending.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reject / Prune", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        Divider()

        // 2. User list directory deletion controls
        Text(text = "System User Account Log DB (${users.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryGreen)
        users.forEach { usr ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "${usr.fname} ${usr.lname}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "CNIC: ${usr.cnic} | Role: ${usr.role}", fontSize = 11.sp, color = Color.Gray)
                    }
                    if (usr.role != "Admin") {
                        IconButton(onClick = { viewModel.deleteUser(usr.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Prune Account", tint = Color.Red, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}


// ==================================================
// Geo-Tagged Mandi Image Manager Screen
// ==================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageManagerScreen(viewModel: AppViewModel) {
    val imageItems by viewModel.allImages.collectAsState()

    var selectCategoryState by remember { mutableStateOf("All Categories") }
    val categoriesList = listOf("All Categories", "Crop Quality", "Mandi Loads", "Receipts Logs", "Geo Tagged")

    var uploadFeedbackMsg by remember { mutableStateOf<String?>(null) }

    val filteredItems = imageItems.filter { p ->
        selectCategoryState == "All Categories" || p.category.equals(selectCategoryState, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryGreen)
                }
                Text(text = "Mandi Load Media Vault", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimaryGreen)
            }

            Button(
                onClick = {
                    // Simulates a mock photo capture or camera upload with complete data parameters
                    val uuid = java.util.UUID.randomUUID().toString()
                    viewModel.insertImage(
                        filename = "AGRI_LOAD_${uuid.take(6)}.png",
                        originalName = "DSC_991823.png",
                        filePath = "https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?w=400",
                        category = "Mandi Loads",
                        title = "Mandi wheat cargo consignment package load verification.",
                        fileSize = 485122L
                    )
                    uploadFeedbackMsg = "Mandi load verification image logged to secure local ledger!"
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = "Upload mock", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Geo-Tag Upload", fontSize = 11.sp)
            }
        }

        if (uploadFeedbackMsg != null) {
            Text(text = uploadFeedbackMsg!!, color = PrimaryGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        // Horizontal scrolling category chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(categoriesList) { cat ->
                FilterChip(
                    selected = selectCategoryState == cat,
                    onClick = { selectCategoryState = cat },
                    label = { Text(cat, fontSize = 11.sp) }
                )
            }
        }

        if (filteredItems.isEmpty()) {
            EmptyState(emoji = "🖼️", title = "No Crop Media", subtitle = "Zero images uploaded under this category filters block.")
        } else {
            filteredItems.forEach { asset ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        // Image view
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(Color.LightGray)
                        ) {
                            Text(
                                text = "AGRI CROP LOAD PRESET\n${asset.filename}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.4f))
                            )
                        }

                        // Info rows
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = asset.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "Category: ${asset.category} • Author: ${asset.uploaded_by}", fontSize = 11.sp, color = Color.Gray)
                            Text(text = "ID Reference: ${asset.id} • Date: ${asset.created_at}", fontSize = 10.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = { viewModel.deleteImage(asset.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                modifier = Modifier.align(Alignment.End),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                            ) {
                                Text(text = "Remove Photo Reference", fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
