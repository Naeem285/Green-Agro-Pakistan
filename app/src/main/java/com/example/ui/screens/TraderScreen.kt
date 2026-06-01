package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppViewModel
import com.example.data.TraderEntity
import com.example.data.Translations
import com.example.ui.components.DragHandle
import com.example.ui.components.SectionHeader
import com.example.ui.components.TrustScoreBadge
import com.example.ui.theme.AccentLime
import com.example.ui.theme.PrimaryGreen

@Composable
fun TraderScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val isUrdu by viewModel.isUrdu.collectAsState()
    var selectedTabState by remember { mutableStateOf(0) } // 0: Trader List, 1: Add/Request Listing

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TabRow(
            selectedTabIndex = selectedTabState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PrimaryGreen
        ) {
            Tab(
                selected = selectedTabState == 0,
                onClick = { selectedTabState = 0 },
                text = { Text(Translations.t("trader_dir", isUrdu), fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            )
            Tab(
                selected = selectedTabState == 1,
                onClick = { selectedTabState = 1 },
                text = { Text(Translations.t("add_trader", isUrdu), fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            )
        }

        if (selectedTabState == 0) {
            TraderListTab(viewModel)
        } else {
            AddTraderTab(viewModel, onListingSubmitted = { selectedTabState = 0 })
        }
    }
}

// --------------------------------------------------
// 1. TRADER LIST TAB VIEW
// --------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraderListTab(viewModel: AppViewModel) {
    val isUrdu by viewModel.isUrdu.collectAsState()
    val traders by viewModel.allTraders.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf("All Cities") }
    var selectedType by remember { mutableStateOf("All Categories") }

    var expandedCityMenu by remember { mutableStateOf(false) }
    var expandedTypeMenu by remember { mutableStateOf(false) }

    // Active details drawer selector
    var activeTraderDetailId by remember { mutableStateOf<Int?>(null) }

    val cities = listOf(
        "All Cities", "Lahore", "Karachi", "Islamabad", "Faisalabad", "Multan",
        "Rawalpindi", "Gujranwala", "Peshawar", "Sargodha", "Bahawalpur",
        "Sahiwal", "Okara"
    )

    val types = listOf(
        "All Categories", "Commission Agent", "Arhtia", "Wholesaler", "Retailer", "Exporter"
    )

    // Approve only public listing matching filters
    val approvedTraders = traders.filter { it.status == "Approved" }
    val filteredTraders = approvedTraders.filter { trader ->
        val matchesSearch = trader.name.lowercase().contains(searchQuery.lowercase()) ||
                trader.shop_name.lowercase().contains(searchQuery.lowercase())
        val matchesCity = selectedCity == "All Cities" || trader.city.equals(selectedCity, ignoreCase = true)
        val matchesType = selectedType == "All Categories" || trader.type.equals(selectedType, ignoreCase = true)
        matchesSearch && matchesCity && matchesType
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(Translations.t("search_trader", isUrdu)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "SearchIcon") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Filters chips row dropdowns
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // City Dropdown Filter
                    Box(modifier = Modifier.weight(1f)) {
                        Button(
                            onClick = { expandedCityMenu = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "City", tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = selectedCity, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, maxLines = 1)
                        }
                        DropdownMenu(
                            expanded = expandedCityMenu,
                            onDismissRequest = { expandedCityMenu = false }
                        ) {
                            cities.forEach { city ->
                                DropdownMenuItem(
                                    text = { Text(city) },
                                    onClick = {
                                        selectedCity = city
                                        expandedCityMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Type Dropdown Filter
                    Box(modifier = Modifier.weight(1f)) {
                        Button(
                            onClick = { expandedTypeMenu = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Type", tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = selectedType, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, maxLines = 1)
                        }
                        DropdownMenu(
                            expanded = expandedTypeMenu,
                            onDismissRequest = { expandedTypeMenu = false }
                        ) {
                            types.forEach { t ->
                                DropdownMenuItem(
                                    text = { Text(t) },
                                    onClick = {
                                        selectedType = t
                                        expandedTypeMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Traders scrolling list grid
        Spacer(modifier = Modifier.height(8.dp))
        SectionHeader(title = "Verified Traders & Commission Agents (${filteredTraders.size} listed)")

        if (filteredTraders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No approved traders found in directory query.", color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredTraders) { trader ->
                    val trustScore = ((trader.rating_avg / 5.0 * 60.0) + Math.min(40.0, trader.review_count * 4.0)).toInt().coerceIn(0, 100)

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeTraderDetailId = trader.id }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(0.7f)) {
                                    Text(text = trader.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(text = "${trader.shop_name} • ${trader.city}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Box(modifier = Modifier.weight(0.3f), contentAlignment = Alignment.CenterEnd) {
                                    TrustScoreBadge(score = trustScore)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Category Tag
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(PrimaryGreen.copy(alpha = 0.08f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(text = trader.type, color = PrimaryGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                // Interactive Stars Average Rating representation
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = "star", tint = AccentLime, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "${String.format("%.1f", trader.rating_avg)} (${trader.review_count} reviews)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Expanding detail modal overlay sheet for reviews integration
    if (activeTraderDetailId != null) {
        val traderItem = traders.find { it.id == activeTraderDetailId }
        if (traderItem != null) {
            TraderDetailDialog(
                trader = traderItem,
                viewModel = viewModel,
                onDismiss = { activeTraderDetailId = null }
            )
        } else {
            activeTraderDetailId = null
        }
    }
}

// --------------------------------------------------
// 2. REQUEST LISTING TAB VIEW
// --------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTraderTab(
    viewModel: AppViewModel,
    onListingSubmitted: () -> Unit
) {
    var traderName by remember { mutableStateOf("") }
    var shopName by remember { mutableStateOf("") }
    var cnic by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var wa by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Lahore") }
    var traderType by remember { mutableStateOf("Commission Agent") }

    val cities = listOf(
        "Lahore", "Karachi", "Islamabad", "Faisalabad", "Multan",
        "Rawalpindi", "Gujranwala", "Peshawar", "Sargodha", "Bahawalpur",
        "Sahiwal", "Okara"
    )

    val types = listOf(
        "Commission Agent", "Arhtia", "Wholesaler", "Retailer", "Exporter"
    )

    var expandedCity by remember { mutableStateOf(false) }
    var expandedType by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Submit Directory Request Listing", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
        Text(text = "Enter firm credentials. All directory entries are subject to automated validation locks and subsequent admin approval queues.", fontSize = 12.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = traderName,
            onValueChange = { traderName = it },
            label = { Text("Trader / Owner Full Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
            value = shopName,
            onValueChange = { shopName = it },
            label = { Text("Shop / Firm Name (e.g. Al-Razzaq Arhat Shop)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
            value = cnic,
            onValueChange = { cnic = formatCnic(it) },
            label = { Text("CNIC Number") },
            placeholder = { Text("33100-1234567-1") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = formatPhone(it) },
            label = { Text("Phone Number") },
            placeholder = { Text("0300-1234567") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
            value = wa,
            onValueChange = { wa = formatPhone(it) },
            label = { Text("WhatsApp Number (Optional)") },
            placeholder = { Text("0300-1234567") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Market / Shop Address location") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        // Dropdown City
        ExposedDropdownMenuBox(
            expanded = expandedCity,
            onExpandedChange = { expandedCity = !expandedCity },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                readOnly = true,
                value = city,
                onValueChange = {},
                label = { Text("Select City") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCity) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(10.dp)
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

        // Dropdown Category
        ExposedDropdownMenuBox(
            expanded = expandedType,
            onExpandedChange = { expandedType = !expandedType },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                readOnly = true,
                value = traderType,
                onValueChange = {},
                label = { Text("Trader Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(10.dp)
            )
            ExposedDropdownMenu(
                expanded = expandedType,
                onDismissRequest = { expandedType = false }
            ) {
                types.forEach { typeName ->
                    DropdownMenuItem(
                        text = { Text(typeName) },
                        onClick = {
                            traderType = typeName
                            expandedType = false
                        }
                    )
                }
            }
        }

        if (validationError != null) {
            Text(text = validationError!!, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                validationError = null
                if (traderName.isBlank() || shopName.isBlank() || phone.isBlank()) {
                    validationError = "Please fill in all core credentials."
                } else if (cnic.length < 15) {
                    validationError = "CNIC must be exactly 13 digits."
                } else {
                    val entity = TraderEntity(
                        name = traderName.trim(),
                        shop_name = shopName.trim(),
                        phone = phone,
                        wa = if (wa.isNotBlank()) wa else phone,
                        sms = phone,
                        cnic = cnic,
                        city = city,
                        address = address.trim(),
                        type = traderType,
                        crops = "Wheat, Rice, Cotton",
                        status = "Pending",
                        rating_avg = 4.0,
                        review_count = 1
                    )
                    viewModel.submitTrader(entity)
                    onListingSubmitted()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(text = "Submit For Approval", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// --------------------------------------------------
// 3. EXPANDED DETAIL & REVIEWS MODAL OVERLAY SHEET
// --------------------------------------------------
@Composable
fun TraderDetailDialog(
    trader: TraderEntity,
    viewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    val reviewItems by viewModel.allReviews.collectAsState()
    val traderReviews = reviewItems.filter { it.trader_id == trader.id }

    var userCommentInput by remember { mutableStateOf("") }
    var selectedStarCount by remember { mutableStateOf(5) } // slider or clickable stars range (1-5)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DragHandle()
                Text(text = trader.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimaryGreen)
                Text(text = "${trader.shop_name} • ${trader.city}", fontSize = 12.sp, color = Color.Gray)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Divider()

                // Information fields
                Text(text = "Contact Details", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryGreen)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Phone Number:", fontSize = 12.sp, color = Color.Gray)
                    Text(text = trader.phone, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "WhatsApp:", fontSize = 12.sp, color = Color.Gray)
                    Text(text = trader.wa, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Mandi Location:", fontSize = 12.sp, color = Color.Gray)
                    Text(text = trader.address, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Divider()

                // Historical review logs
                Text(text = "Reviews / Client Testimonials (${traderReviews.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryGreen)
                if (traderReviews.isEmpty()) {
                    Text(text = "No previous reviews for this trader yet.", fontSize = 11.sp, color = Color.Gray)
                } else {
                    traderReviews.forEach { rev ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = rev.user_name, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Row {
                                        repeat(rev.rating) {
                                            Icon(Icons.Default.Star, contentDescription = "*", tint = AccentLime, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                                Text(text = rev.comment, fontSize = 11.sp, color = Color.DarkGray, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }

                Divider()

                // Add Review block
                Text(text = "Add Your Feedback Review", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryGreen)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..5).forEach { star ->
                        IconButton(onClick = { selectedStarCount = star }) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "$star Stars",
                                tint = if (selectedStarCount >= star) AccentLime else Color.Gray.copy(alpha = 0.5f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = userCommentInput,
                    onValueChange = { userCommentInput = it },
                    placeholder = { Text("Write your review commenting...", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (userCommentInput.isNotBlank()) {
                        viewModel.submitReview(
                            traderId = trader.id,
                            traderName = trader.name,
                            shopName = trader.shop_name,
                            rating = selectedStarCount,
                            comment = userCommentInput.trim()
                        )
                        userCommentInput = ""
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text(text = "Send Review", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Close Profile")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
