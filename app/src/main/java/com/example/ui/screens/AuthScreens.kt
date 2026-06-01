package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppViewModel
import com.example.data.Translations
import com.example.data.UserEntity
import com.example.ui.theme.AccentLime
import com.example.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AppViewModel,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUrdu by viewModel.isUrdu.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val authSuccess by viewModel.authSuccessMessage.collectAsState()

    var cnic by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Large beautiful app logo centered
        Image(
            painter = painterResource(id = com.example.R.drawable.green_agro_logo),
            contentDescription = "Green Agro Pakistan Logo",
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = Translations.t("pakistan_agri_hub", isUrdu),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryGreen,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Agriculture Hub 2026 | Pakistan\nOffline-First",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // CNIC Input with format: 00000-0000000-0
        OutlinedTextField(
            value = cnic,
            onValueChange = { input ->
                cnic = formatCnic(input)
            },
            label = { Text(Translations.t("cnic", isUrdu)) },
            placeholder = { Text("33100-1234567-1") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Input
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(Translations.t("password", isUrdu)) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = icon, contentDescription = "Toggle password visibility")
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Inline Error State
        if (authError != null) {
            Text(
                text = authError!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (authSuccess != null) {
            Text(
                text = authSuccess!!,
                color = PrimaryGreen,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Login Button
        Button(
            onClick = {
                val cleanedCnic = cnic.trim()
                if (cleanedCnic.length < 15) {
                    viewModel.login("", "") // triggers empty warning
                } else {
                    viewModel.login(cleanedCnic, password)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = Translations.t("login", isUrdu),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "New to Portal? ",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = Translations.t("register", isUrdu),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen,
                modifier = Modifier.clickable { onNavigateToRegister() }
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AppViewModel,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUrdu by viewModel.isUrdu.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val authSuccess by viewModel.authSuccessMessage.collectAsState()

    var fname by remember { mutableStateOf("") }
    var lname by remember { mutableStateOf("") }
    var cnic by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Farmer") } // Farmer / Trader
    var city by remember { mutableStateOf("Lahore") }

    // Conditional Fields
    var wa by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    // Farmer-only details
    var acres by remember { mutableStateOf("") }
    var mainCrop by remember { mutableStateOf("Wheat") }

    // Trader-only details
    var shopName by remember { mutableStateOf("") }
    var traderType by remember { mutableStateOf("Commission Agent") }

    // Selected simulated profile avatar image index (out of 4 colors/seeds)
    var selectedAvatarIndex by remember { mutableStateOf(0) }
    val avatars = listOf("Plant Green", "Earth Brown", "Gold Wheat", "Sky Blue")
    val avatarColors = listOf(Color(0xFF166534), Color(0xFF78350F), Color(0xFFCA8A04), Color(0xFF1D4ED8))

    var expandedRole by remember { mutableStateOf(false) }
    var expandedCity by remember { mutableStateOf(false) }
    var expandedCrop by remember { mutableStateOf(false) }
    var expandedTraderType by remember { mutableStateOf(false) }

    val cities = listOf(
        "Lahore", "Karachi", "Islamabad", "Faisalabad", "Multan",
        "Rawalpindi", "Gujranwala", "Peshawar", "Sargodha", "Bahawalpur",
        "Sahiwal", "Okara", "Lodhran", "Mailsi", "Vehari"
    )

    val crops = listOf("Wheat", "Basmati Rice", "Cotton", "Sugarcane", "Maize", "Vegetables")
    val traderTypes = listOf("Commission Agent", "Arhtia", "Wholesaler", "Retailer", "Exporter")

    var validationError by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Create Portal Profile",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryGreen
        )
        Text(
            text = "Enter details below to establish offline agriculture profile",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Avatar Picker
        Text(text = "Choose Profile Accent Banner", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            avatars.forEachIndexed { idx, name ->
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(avatarColors[idx].copy(alpha = if (selectedAvatarIndex == idx) 1f else 0.3f))
                        .clickable { selectedAvatarIndex = idx }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.take(1) + name.split(" ").last().take(1),
                        color = if (selectedAvatarIndex == idx) Color.White else avatarColors[idx],
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Fields side by side (FNAME & LNAME)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = fname,
                onValueChange = { fname = it },
                label = { Text("First Name") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            )
            OutlinedTextField(
                value = lname,
                onValueChange = { lname = it },
                label = { Text("Last Name") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // CNIC Input
        OutlinedTextField(
            value = cnic,
            onValueChange = { cnic = formatCnic(it) },
            label = { Text(Translations.t("cnic", isUrdu)) },
            placeholder = { Text("33100-1234567-1") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password & Confirm
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Phone Input
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = formatPhone(it) },
            label = { Text("Phone (e.g. 0300-1234567)") },
            placeholder = { Text("0300-1234567") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Role Dropdown
        ExposedDropdownMenuBox(
            expanded = expandedRole,
            onExpandedChange = { expandedRole = !expandedRole },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                readOnly = true,
                value = role,
                onValueChange = {},
                label = { Text("Select Role") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRole) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(10.dp)
            )
            ExposedDropdownMenu(
                expanded = expandedRole,
                onDismissRequest = { expandedRole = false }
            ) {
                listOf("Farmer", "Trader").forEach { roleName ->
                    DropdownMenuItem(
                        text = { Text(roleName) },
                        onClick = {
                            role = roleName
                            expandedRole = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // City Dropdown
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

        Spacer(modifier = Modifier.height(12.dp))

        // Optional Inputs Common fields
        OutlinedTextField(
            value = wa,
            onValueChange = { wa = formatPhone(it) },
            label = { Text("WhatsApp (Optional)") },
            placeholder = { Text("0300-1234567") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email (Optional)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Home/Shop Address (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        // --- Role Conditional Sections ---
        if (role == "Farmer") {
            Spacer(modifier = Modifier.height(20.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Farmer Specifications", fontWeight = FontWeight.Bold, color = PrimaryGreen, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = acres,
                onValueChange = { acres = it },
                label = { Text("Cultivated Land Size (Acres)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = expandedCrop,
                onExpandedChange = { expandedCrop = !expandedCrop },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = mainCrop,
                    onValueChange = {},
                    label = { Text("Main Cultivated Crop") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCrop) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedCrop,
                    onDismissRequest = { expandedCrop = false }
                ) {
                    crops.forEach { cropName ->
                        DropdownMenuItem(
                            text = { Text(cropName) },
                            onClick = {
                                mainCrop = cropName
                                expandedCrop = false
                            }
                        )
                    }
                }
            }
        } else if (role == "Trader") {
            Spacer(modifier = Modifier.height(20.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Trader Specifications", fontWeight = FontWeight.Bold, color = PrimaryGreen, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = shopName,
                onValueChange = { shopName = it },
                label = { Text("Shop/Firm Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = expandedTraderType,
                onExpandedChange = { expandedTraderType = !expandedTraderType },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = traderType,
                    onValueChange = {},
                    label = { Text("Trader Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTraderType) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedTraderType,
                    onDismissRequest = { expandedTraderType = false }
                ) {
                    traderTypes.forEach { typeName ->
                        DropdownMenuItem(
                            text = { Text(typeName) },
                            onClick = {
                                traderType = typeName
                                expandedTraderType = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Errors rendering
        if (validationError != null) {
            Text(text = validationError!!, color = Color.Red, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (authError != null) {
            Text(text = authError!!, color = Color.Red, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (authSuccess != null) {
            Text(text = authSuccess!!, color = PrimaryGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Submit button
        Button(
            onClick = {
                validationError = null
                if (fname.isBlank() || lname.isBlank() || password.isBlank() || phone.isBlank()) {
                    validationError = "Please fill in all core credentials."
                } else if (cnic.length < 15) {
                    validationError = "CNIC must be exactly 13 digits (format XXXXX-XXXXXXX-X)."
                } else if (password.length < 6) {
                    validationError = "Password must be at least 6 characters."
                } else if (password != confirmPassword) {
                    validationError = "Passwords do not match."
                } else {
                    val entity = UserEntity(
                        fname = fname.trim(),
                        lname = lname.trim(),
                        username = "${fname.trim().lowercase()}.${lname.trim().lowercase()}",
                        cnic = cnic.trim(),
                        password = password,
                        role = role,
                        phone = phone.trim(),
                        wa = if (wa.isNotBlank()) wa else phone,
                        email = email,
                        recovery_email = email,
                        dob = "1994-01-01",
                        city = city,
                        address = address,
                        acres = acres.toDoubleOrNull() ?: 0.0,
                        main_crop = if (role == "Farmer") mainCrop else "",
                        shop_name = if (role == "Trader") shopName else "",
                        trader_type = if (role == "Trader") traderType else "",
                        img = avatars[selectedAvatarIndex] // Save banner color design name
                    )
                    viewModel.register(entity)
                    // Auto redirect to login upon success
                    onNavigateToLogin()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(text = "Submit & Sign Up", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Back to Login",
            color = PrimaryGreen,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clickable { onNavigateToLogin() }
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))
    }
}

// --- CNIC Format Helper ---
fun formatCnic(input: String): String {
    val digits = input.filter { it.isDigit() }
    val sb = java.lang.StringBuilder()
    for (i in digits.indices) {
        sb.append(digits[i])
        if (i == 4) sb.append("-")
        else if (i == 11) sb.append("-")
    }
    return sb.toString().take(15) // Max 15 chars: 13 digits + 2 dashes
}

// --- Phone Format Helper ---
fun formatPhone(input: String): String {
    val digits = input.filter { it.isDigit() }
    val sb = java.lang.StringBuilder()
    for (i in digits.indices) {
        sb.append(digits[i])
        if (i == 3) sb.append("-")
    }
    return sb.toString().take(12) // Max 12 chars e.g. 0300-1234567
}
