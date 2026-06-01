package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppViewModel
import com.example.data.RateEntity
import com.example.data.Translations
import com.example.ui.components.DragHandle
import com.example.ui.theme.AccentLime
import com.example.ui.theme.PrimaryGreen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MandiRatesScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val isUrdu by viewModel.isUrdu.collectAsState()
    val rates by viewModel.allRates.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCityFilter by remember { mutableStateOf("All Cities") }
    var showAddRateSheet by remember { mutableStateOf(false) }

    val cities = listOf(
        "All Cities", "Lahore", "Karachi", "Islamabad", "Faisalabad", "Multan",
        "Rawalpindi", "Gujranwala", "Peshawar", "Sargodha", "Bahawalpur",
        "Sahiwal", "Okara", "Lodhran", "Mailsi", "Vehari"
    )

    // --- Price Averaging ---
    val avgWheat = rates.filter { it.crop.lowercase().contains("wheat") }.map { it.rate }.average().let { if (it.isNaN()) 3850.0 else it }
    val avgRice = rates.filter { it.crop.lowercase().contains("rice") }.map { it.rate }.average().let { if (it.isNaN()) 6200.0 else it }
    val avgCotton = rates.filter { it.crop.lowercase().contains("cotton") }.map { it.rate }.average().let { if (it.isNaN()) 7800.0 else it }
    val avgSugarcane = rates.filter { it.crop.lowercase().contains("sugarcane") }.map { it.rate }.average().let { if (it.isNaN()) 445.0 else it }

    // --- Filter logic ---
    val filteredRates = rates.filter { rate ->
        val matchesSearch = rate.crop.lowercase().contains(searchQuery.lowercase()) ||
                rate.market.lowercase().contains(searchQuery.lowercase())
        val matchesCity = selectedCityFilter == "All Cities" || rate.city.equals(selectedCityFilter, ignoreCase = true)
        matchesSearch && matchesCity
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. FILTER BAR
            Card(
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Search textfield
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(Translations.t("search_crop_market", isUrdu)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "SearchIcon") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // City Filter Dropdown Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Filter by Location:", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                        var expandedCityDropdown by remember { mutableStateOf(false) }
                        Box {
                            Button(
                                onClick = { expandedCityDropdown = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = selectedCityFilter, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                            }
                            DropdownMenu(
                                expanded = expandedCityDropdown,
                                onDismissRequest = { expandedCityDropdown = false }
                            ) {
                                cities.forEach { city ->
                                    DropdownMenuItem(
                                        text = { Text(city) },
                                        onClick = {
                                            selectedCityFilter = city
                                            expandedCityDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. AVERAGES HORIZONTAL SCROLLER
            Text(
                text = "Commodity Average Mandi Price (PKR / Mon)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    CommodityAvgCard(title = "Wheat 🌾", avg = avgWheat)
                }
                item {
                    CommodityAvgCard(title = "Rice 🍚", avg = avgRice)
                }
                item {
                    CommodityAvgCard(title = "Cotton 🌸", avg = avgCotton)
                }
                item {
                    CommodityAvgCard(title = "Sugarcane 🎋", avg = avgSugarcane)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. RATES LIST VIEW
            Text(
                text = "Live Mandi Daily Registry (${filteredRates.size} rows)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )

            if (filteredRates.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No rates logged matching search filters.", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredRates) { rate ->
                        RateListRow(
                            rate = rate,
                            isAdmin = currentUser?.role == "Admin",
                            onDelete = { viewModel.deleteRate(rate.id) }
                        )
                    }
                }
            }
        }

        // 4. FLOATING ACTION BUTTON (Visible to all logged in users)
        FloatingActionButton(
            onClick = { showAddRateSheet = true },
            containerColor = PrimaryGreen,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 96.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Publish Rate")
        }

        // Add Rate Slide Sheet Dialog
        if (showAddRateSheet) {
            AddRateDialog(
                viewModel = viewModel,
                onDismiss = { showAddRateSheet = false }
            )
        }
    }
}

@Composable
fun CommodityAvgCard(title: String, avg: Double) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .width(130.dp)
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Rs. ${avg.toInt()}",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryGreen,
                fontFamily = FontFamily.Monospace
            )
            Text(text = "Avg per 40kg", fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Composable
fun RateListRow(
    rate: RateEntity,
    isAdmin: Boolean,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(0.7f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = rate.crop,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${rate.market} • ${rate.city}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Date: ${rate.date} | Published: ${rate.published_by}",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }

            Row(
                modifier = Modifier.weight(0.3f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rs. ${rate.rate.toInt()}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryGreen,
                    fontFamily = FontFamily.Monospace
                )

                if (isAdmin) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Rate",
                            tint = Color.Red,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRateDialog(
    viewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    var crop by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Lahore") }
    var market by remember { mutableStateOf("") }

    val cities = listOf(
        "Lahore", "Karachi", "Islamabad", "Faisalabad", "Multan",
        "Rawalpindi", "Gujranwala", "Peshawar", "Sargodha", "Bahawalpur",
        "Sahiwal", "Okara", "Lodhran", "Mailsi", "Vehari"
    )

    val cropsPreset = listOf("Wheat", "Basmati Rice", "Cotton", "Sugarcane", "Maize", "Vegetables")

    var expandedCity by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DragHandle()
                Text(text = "Log New Mandi Rate", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimaryGreen)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Crop Input with chips
                OutlinedTextField(
                    value = crop,
                    onValueChange = { crop = it },
                    label = { Text("Crop Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Chips horizontal scroll wrapper
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(cropsPreset) { preset ->
                        AssistChip(
                            onClick = { crop = preset },
                            label = { Text(preset) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (crop == preset) AccentLime else MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }

                // Rate Input
                OutlinedTextField(
                    value = rate,
                    onValueChange = { rate = it.filter { c -> c.isDigit() } },
                    label = { Text("Rate per 40kg (Rs.)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // City Select
                ExposedDropdownMenuBox(
                    expanded = expandedCity,
                    onExpandedChange = { expandedCity = !expandedCity },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = city,
                        onValueChange = {},
                        label = { Text("City") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCity) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCity,
                        onDismissRequest = { expandedCity = false }
                    ) {
                        cities.forEach { cityName ->
                            DropdownMenuItem(
                                text = { Text(cityName) },
                                onClick = {
                                    city = cityName
                                    expandedCity = false
                                }
                            )
                        }
                    }
                }

                // Market Input
                OutlinedTextField(
                    value = market,
                    onValueChange = { market = it },
                    label = { Text("Mandi / Market Name") },
                    placeholder = { Text("e.g. Central Ghalla Mandi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (validationError != null) {
                    Text(text = validationError!!, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    validationError = null
                    val rateVal = rate.toDoubleOrNull()
                    if (crop.isBlank() || rateVal == null || market.isBlank()) {
                        validationError = "Please fill in all details correctly."
                    } else {
                        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
                        val formattedDate = sdf.format(Date())

                        viewModel.publishRate(
                            crop = crop.trim(),
                            rate = rateVal,
                            city = city,
                            market = market.trim(),
                            date = formattedDate
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text(text = "Publish", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = MaterialTheme.colorScheme.primary)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
