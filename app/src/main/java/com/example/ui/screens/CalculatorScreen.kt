package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppViewModel
import com.example.data.Translations
import com.example.ui.components.DragHandle
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AccentLime
import com.example.ui.theme.PrimaryGreen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalculatorScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val isUrdu by viewModel.isUrdu.collectAsState()
    var selectedTabState by remember { mutableStateOf(0) } // 0: Income Calc, 1: Profit Analyzer

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Mode Selector Tab Header
        TabRow(
            selectedTabIndex = selectedTabState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PrimaryGreen
        ) {
            Tab(
                selected = selectedTabState == 0,
                onClick = { selectedTabState = 0 },
                text = { Text(Translations.t("income_calc", isUrdu), fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            )
            Tab(
                selected = selectedTabState == 1,
                onClick = { selectedTabState = 1 },
                text = { Text(Translations.t("profit_analyzer", isUrdu), fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            )
        }

        if (selectedTabState == 0) {
            IncomeCalculatorTab(viewModel)
        } else {
            ProfitAnalyzerTab(viewModel)
        }
    }
}

// ==========================================
// 1. INCOME CALCULATOR TAB
// ==========================================
@Composable
fun IncomeCalculatorTab(viewModel: AppViewModel) {
    val isUrdu by viewModel.isUrdu.collectAsState()
    var modeSec by remember { mutableStateOf(0) } // 0: KGs, 1: Mons & KGs, 2: Multi-Item Ledger

    // Core variables
    var weightKgInput by remember { mutableStateOf("") }
    var weightMonInput by remember { mutableStateOf("") }
    var weightRemKgInput by remember { mutableStateOf("") }
    var rateInput by remember { mutableStateOf("") }

    // Deduction dropdown
    var selectedDeductionIndex by remember { mutableStateOf(1) } // 1 KG per 50 KG is default
    var customCutX by remember { mutableStateOf("") }
    var customCutY by remember { mutableStateOf("") }
    var expandedDeductionMenu by remember { mutableStateOf(false) }

    val deductionRules = listOf(
        "No Cut",
        "1 KG per 50 KG",
        "1.5 KG per 50 KG",
        "2 KG per 50 KG",
        "1 KG per 40 KG",
        "Custom Rule"
    )

    // Ledger States
    var ledgerInput by remember { mutableStateOf("") }
    val ledgerItems = remember { mutableStateListOf<Double>() }

    // Printable Dialog View State
    var showReceiptPreview by remember { mutableStateOf(false) }

    // Calculations based on MODE with high-precision BigDecimal
    val grossWeightKgBD = when (modeSec) {
        0 -> {
            val value = weightKgInput.toDoubleOrNull() ?: 0.0
            java.math.BigDecimal.valueOf(value)
        }
        1 -> {
            val mons = weightMonInput.toDoubleOrNull() ?: 0.0
            val rem = weightRemKgInput.toDoubleOrNull() ?: 0.0
            java.math.BigDecimal.valueOf(mons).multiply(java.math.BigDecimal.valueOf(40.0))
                .add(java.math.BigDecimal.valueOf(rem))
        }
        else -> {
            var sum = java.math.BigDecimal.ZERO
            ledgerItems.forEach {
                sum = sum.add(java.math.BigDecimal.valueOf(it))
            }
            sum
        }
    }
    val grossWeightKg = grossWeightKgBD.toDouble()

    // Apply Deduction Formula with precise Rounding
    val deductedKgBD = when (selectedDeductionIndex) {
        0 -> java.math.BigDecimal.ZERO
        1 -> grossWeightKgBD.divide(java.math.BigDecimal.valueOf(50.0), 8, java.math.RoundingMode.HALF_UP).multiply(java.math.BigDecimal.valueOf(1.0))
        2 -> grossWeightKgBD.divide(java.math.BigDecimal.valueOf(50.0), 8, java.math.RoundingMode.HALF_UP).multiply(java.math.BigDecimal.valueOf(1.5))
        3 -> grossWeightKgBD.divide(java.math.BigDecimal.valueOf(50.0), 8, java.math.RoundingMode.HALF_UP).multiply(java.math.BigDecimal.valueOf(2.0))
        4 -> grossWeightKgBD.divide(java.math.BigDecimal.valueOf(40.0), 8, java.math.RoundingMode.HALF_UP).multiply(java.math.BigDecimal.valueOf(1.0))
        5 -> {
            val cx = customCutX.toDoubleOrNull() ?: 0.0
            val cy = customCutY.toDoubleOrNull() ?: 50.0
            if (cy > 0.0) {
                grossWeightKgBD.divide(java.math.BigDecimal.valueOf(cy), 8, java.math.RoundingMode.HALF_UP).multiply(java.math.BigDecimal.valueOf(cx))
            } else {
                java.math.BigDecimal.ZERO
            }
        }
        else -> java.math.BigDecimal.ZERO
    }
    val deductedKg = deductedKgBD.toDouble()

    val netWeightKgBD = grossWeightKgBD.subtract(deductedKgBD).coerceAtLeast(java.math.BigDecimal.ZERO)
    val netWeightKg = netWeightKgBD.toDouble()

    val rateBD = java.math.BigDecimal.valueOf(rateInput.toDoubleOrNull() ?: 0.0)
    val rate = rateBD.toDouble()

    val fortyBD = java.math.BigDecimal.valueOf(40.0)
    val totalMaundsBD = netWeightKgBD.divide(fortyBD, 8, java.math.RoundingMode.HALF_UP)
    val totalMaunds = totalMaundsBD.toDouble()

    val grossAmountBD = grossWeightKgBD.divide(fortyBD, 8, java.math.RoundingMode.HALF_UP).multiply(rateBD)
    val grossAmount = grossAmountBD.toDouble()

    val netAmountBD = totalMaundsBD.multiply(rateBD)
    val netAmount = netAmountBD.toDouble()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Mode Subheader Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("KGs", "Mons & KGs", "Ledger").forEachIndexed { index, name ->
                Button(
                    onClick = { modeSec = index },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (modeSec == index) PrimaryGreen else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = name, color = if (modeSec == index) Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Variable Inputs Section
        when (modeSec) {
            0 -> {
                OutlinedTextField(
                    value = weightKgInput,
                    onValueChange = { weightKgInput = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text(Translations.t("total_weight", isUrdu)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
            1 -> {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = weightMonInput,
                        onValueChange = { weightMonInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Maunds (من)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = weightRemKgInput,
                        onValueChange = { input ->
                            val num = input.toIntOrNull() ?: 0
                            if (num in 0..39) {
                                weightRemKgInput = input.filter { c -> c.isDigit() }
                            }
                        },
                        label = { Text("KG (كلو)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        placeholder = { Text("0-39") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
            2 -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = ledgerInput,
                        onValueChange = { ledgerInput = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Add Item Weight (e.g. 40.500)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    IconButton(
                        onClick = {
                            val v = ledgerInput.toDoubleOrNull() ?: 0.0
                            if (v > 0.0 && ledgerItems.size < 300) {
                                ledgerItems.add(v)
                                ledgerInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimaryGreen)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add ledger item", tint = Color.White)
                    }
                }

                if (ledgerItems.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    ) {
                        Box(modifier = Modifier.padding(8.dp)) {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                ledgerItems.forEachIndexed { i, item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "Item #${i + 1}:  $item kg", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove",
                                            tint = Color.Red,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { ledgerItems.removeAt(i) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Rate per Maund input
        OutlinedTextField(
            value = rateInput,
            onValueChange = { rateInput = it.filter { c -> c.isDigit() } },
            label = { Text(Translations.t("rate_per_maund", isUrdu)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        // Deduction Rule picker dropdown
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                readOnly = true,
                value = deductionRules[selectedDeductionIndex],
                onValueChange = {},
                label = { Text(Translations.t("deduction_rule", isUrdu)) },
                trailingIcon = { IconButton(onClick = { expandedDeductionMenu = !expandedDeductionMenu }) {
                    Icon(Icons.Default.Add, contentDescription = "Deduction options")
                }},
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
            DropdownMenu(
                expanded = expandedDeductionMenu,
                onDismissRequest = { expandedDeductionMenu = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                deductionRules.forEachIndexed { idx, rule ->
                    DropdownMenuItem(
                        text = { Text(rule) },
                        onClick = {
                            selectedDeductionIndex = idx
                            expandedDeductionMenu = false
                        }
                    )
                }
            }
        }

        // Conditional Custom Rule Inputs
        if (selectedDeductionIndex == 5) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = customCutX,
                    onValueChange = { customCutX = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Cut size (kg)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = customCutY,
                    onValueChange = { customCutY = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Per weight (kg)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Live calculation results card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryGreen.copy(alpha = 0.08f)),
            border = BorderStroke(1.5.dp, PrimaryGreen.copy(alpha = 0.15f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "LIVE BILL BREAKDOWN", fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))

                ResultRow(label = Translations.t("gross_weight", isUrdu), value = "${String.format("%.2f", grossWeightKg)} KG")
                ResultRow(label = Translations.t("deducted_kg", isUrdu), value = "${String.format("%.2f", deductedKg)} KG", valueColor = Color.Red)
                ResultRow(label = Translations.t("net_weight", isUrdu), value = "${String.format("%.2f", netWeightKg)} KG")
                
                val netMaundsFull = netWeightKgBD.divideToIntegralValue(fortyBD).toInt()
                val netRemKg = netWeightKgBD.remainder(fortyBD).toDouble()
                ResultRow(
                    label = if (isUrdu) "خالص من اور کلو متبادل" else "Net Maund & KG Split",
                    value = "$netMaundsFull Mon & ${String.format("%.2f", netRemKg)} KG"
                )
                ResultRow(label = Translations.t("total_maunds", isUrdu), value = "${String.format("%.3f", totalMaunds)} Maunds")

                Divider(color = PrimaryGreen.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                ResultRow(label = Translations.t("gross_amount", isUrdu) + " (Before cuts)", value = "Rs. ${String.format("%,.0f", grossAmount)}")
                ResultRow(
                    label = Translations.t("net_amount", isUrdu) + " (In Pocket)",
                    value = "Rs. ${String.format("%,.0f", netAmount)}",
                    valueColor = PrimaryGreen,
                    isLarge = true
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // PDF Print simulated button
        Button(
            onClick = {
                if (grossWeightKg > 0.0 && rate > 0.0) {
                    showReceiptPreview = true
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Print, contentDescription = "Print")
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = Translations.t("print_receipt", isUrdu), color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showReceiptPreview) {
        AlertDialog(
            onDismissRequest = { showReceiptPreview = false },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    DragHandle()
                    Text(text = "Receipt Invoice A4 Generated", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🌱 GREEN AGRO PAKISTAN", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                        Text(text = "Agriculture Hub 2026", fontSize = 10.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))

                        // Transaction elements
                        val formattedDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(Date())
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Bill Date:", fontSize = 11.sp, color = Color.DarkGray)
                            Text(formattedDate, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Portal User:", fontSize = 11.sp, color = Color.DarkGray)
                            Text(viewModel.currentUser.value?.fname ?: "Farmer Account", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))

                        ReceiptDetailRow(label = "Gross Weight Received", value = "${String.format("%.2f", grossWeightKg)} KG")
                        ReceiptDetailRow(label = "Deduction Rule Applied", value = deductionRules[selectedDeductionIndex])
                        ReceiptDetailRow(label = "Deducted Quantity", value = "- ${String.format("%.2f", deductedKg)} KG", color = Color.Red)
                        ReceiptDetailRow(label = "Net Output Weight", value = "${String.format("%.2f", netWeightKg)} KG")
                        
                        val rMaundsFull = netWeightKgBD.divideToIntegralValue(fortyBD).toInt()
                        val rRemKg = netWeightKgBD.remainder(fortyBD).toDouble()
                        ReceiptDetailRow(label = "Maund & KG Split", value = "$rMaundsFull Mon & ${String.format("%.2f", rRemKg)} KG")
                        
                        ReceiptDetailRow(label = "Maund Equivalency", value = "${String.format("%.3f", totalMaunds)} Maunds")
                        ReceiptDetailRow(label = "Settled Price per Maund", value = "Rs. ${String.format("%,.0f", rate)}")

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("NET AMOUNT PAID", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                            Text("Rs. ${String.format("%,.0f", netAmount)}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryGreen)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Developer: Muhamad Naeem Akram • Offline Secure Ledger", fontSize = 9.sp, color = Color.Gray)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showReceiptPreview = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text(text = "Close Receipt", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun ResultRow(label: String, value: String, valueColor: Color = Color.Unspecified, isLarge: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = if (isLarge) 14.sp else 12.sp,
            fontWeight = if (isLarge) FontWeight.Bold else FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            fontSize = if (isLarge) 18.sp else 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun ReceiptDetailRow(label: String, value: String, color: Color = Color.Black) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = Color.DarkGray)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
    }
}


// ==========================================
// 2. PROFIT ANALYZER TAB
// ==========================================
@Composable
fun ProfitAnalyzerTab(viewModel: AppViewModel) {
    val isUrdu by viewModel.isUrdu.collectAsState()
    var modeToggleState by remember { mutableStateOf(0) } // 0: Trader Context, 1: Farmer Costs

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Mode Header Selectors
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { modeToggleState = 0 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (modeToggleState == 0) Color(0xFF3B82F6) else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = Translations.t("trader_context", isUrdu), color = if (modeToggleState == 0) Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { modeToggleState = 1 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (modeToggleState == 1) PrimaryGreen else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = Translations.t("farmer_costs", isUrdu), color = if (modeToggleState == 1) Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (modeToggleState == 0) {
            // TRADER MODE
            var weightKg by remember { mutableStateOf("") }
            var expectedRate by remember { mutableStateOf("") }
            var actualRate by remember { mutableStateOf("") }
            var forcedCut by remember { mutableStateOf("") }

            OutlinedTextField(
                value = weightKg,
                onValueChange = { weightKg = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Crop Weight (KG)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = expectedRate,
                onValueChange = { expectedRate = it.filter { c -> c.isDigit() } },
                label = { Text(Translations.t("expected_rate", isUrdu)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = actualRate,
                onValueChange = { actualRate = it.filter { c -> c.isDigit() } },
                label = { Text(Translations.t("actual_rate", isUrdu)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = forcedCut,
                onValueChange = { forcedCut = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text(Translations.t("forced_cut", isUrdu)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            // Results calculations
            val wKg = weightKg.toDoubleOrNull() ?: 0.0
            val expR = expectedRate.toDoubleOrNull() ?: 0.0
            val actR = actualRate.toDoubleOrNull() ?: 0.0
            val fCut = forcedCut.toDoubleOrNull() ?: 0.0

            val grossExpected = (wKg / 40.0) * expR
            val actualNetWeight = (wKg - fCut).coerceAtLeast(0.0)
            val grossActual = (actualNetWeight / 40.0) * actR

            val lossFromPrice = (wKg / 40.0) * (expR - actR).coerceAtLeast(0.0)
            val lossFromWeight = (fCut / 40.0) * actR
            val totalLossValue = lossFromPrice + lossFromWeight
            val lossPercent = if (grossExpected > 0.0) (totalLossValue / grossExpected) * 100.0 else 0.0

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                border = BorderStroke(1.5.dp, Color(0xFF93C5FD)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "TRADER PORT CONTEXT ANALYSIS", fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))

                    ResultRow(label = Translations.t("gross_income", isUrdu), value = "Rs. ${String.format("%,.0f", grossExpected)}")
                    ResultRow(label = Translations.t("actual_income", isUrdu), value = "Rs. ${String.format("%,.0f", grossActual)}")
                    ResultRow(label = Translations.t("loss_rate", isUrdu), value = "Rs. ${String.format("%,.0f", lossFromPrice)}", valueColor = Color.Red)
                    ResultRow(label = Translations.t("loss_weight", isUrdu), value = "Rs. ${String.format("%,.0f", lossFromWeight)}", valueColor = Color.Red)
                    ResultRow(label = Translations.t("total_loss", isUrdu), value = "Rs. ${String.format("%,.0f", totalLossValue)}", valueColor = Color.Red)
                    ResultRow(label = "Total Loss Percentage", value = "${String.format("%.1f", lossPercent)}%", valueColor = Color.Red)
                }
            }

            // Draw Expected vs Actual revenue comparison bar chart
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Revenue Comparison", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val maxVal = Math.max(grossExpected, grossActual).toFloat().coerceAtLeast(100f)

                    val expectedRatio = (grossExpected / maxVal).toFloat()
                    val actualRatio = (grossActual / maxVal).toFloat()

                    val barHeight = 28.dp.toPx()
                    val gap = 16.dp.toPx()

                    // Row 1: Expected Revenue
                    drawRect(
                        color = Color(0xFF3B82F6).copy(alpha = 0.2f),
                        size = Size(size.width, barHeight),
                        topLeft = Offset(0f, 0f)
                    )
                    drawRect(
                        color = Color(0xFF3B82F6),
                        size = Size(size.width * expectedRatio, barHeight),
                        topLeft = Offset(0f, 0f)
                    )

                    // Row 2: Actual Revenue
                    drawRect(
                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                        size = Size(size.width, barHeight),
                        topLeft = Offset(0f, barHeight + gap)
                    )
                    drawRect(
                        color = Color(0xFF10B981),
                        size = Size(size.width * actualRatio, barHeight),
                        topLeft = Offset(0f, barHeight + gap)
                    )
                }

                // Overlay labels on top
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Expected Gross", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(start = 8.dp, top = 6.dp))
                        Text("Rs. ${String.format("%,.0f", grossExpected)}", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(top = 6.dp))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Actual Net", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(start = 8.dp, bottom = 12.dp))
                        Text("Rs. ${String.format("%,.0f", grossActual)}", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(bottom = 12.dp))
                    }
                }
            }

        } else {
            // FARMER MODE (PER ACRE COST SHEET LOGS)
            var acresInput by remember { mutableStateOf("1") }
            var dapPriceInput by remember { mutableStateOf("12500") }
            var dapBagsInput by remember { mutableStateOf("1.5") }
            var ureaPriceInput by remember { mutableStateOf("4800") }
            var ureaBagsInput by remember { mutableStateOf("2.5") }

            // Other general presets
            var prepOptionState by remember { mutableStateOf(1) } // 0:Once, 1:Twice, 2:Three Times, 3:Rotavator
            var prepCostMatrix = listOf(2500.0, 3500.0, 4500.0, 2800.0)

            var waterCostInput by remember { mutableStateOf("5000") }
            var pesticideCostInput by remember { mutableStateOf("6000") }
            var seedCostInput by remember { mutableStateOf("5500") }
            var harvestCostInput by remember { mutableStateOf("9000") }

            // Yield metrics
            var expectedYieldMonsInput by remember { mutableStateOf("45") }
            var expectedFarmerRateInput by remember { mutableStateOf("3900") }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = acresInput,
                    onValueChange = { acresInput = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Number of Cultivated Acres") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dapPriceInput,
                        onValueChange = { dapPriceInput = it },
                        label = { Text("DAP Price/Bag") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = dapBagsInput,
                        onValueChange = { dapBagsInput = it },
                        label = { Text("DAP Bags/Acre") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ureaPriceInput,
                        onValueChange = { ureaPriceInput = it },
                        label = { Text("Urea Price/Bag") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = ureaBagsInput,
                        onValueChange = { ureaBagsInput = it },
                        label = { Text("Urea Bags/Acre") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Land prep picker row
                Text(text = "Land Preparation Mode", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val prepLabels = listOf("Once (2500)", "Twice (3500)", "Three (4500)", "Rotavator (2800)")
                    prepLabels.forEachIndexed { i, label ->
                        Button(
                            onClick = { prepOptionState = i },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (prepOptionState == i) PrimaryGreen else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                        ) {
                            Text(text = label, color = if (prepOptionState == i) Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 8.sp, maxLines = 1)
                        }
                    }
                }

                OutlinedTextField(
                    value = waterCostInput,
                    onValueChange = { waterCostInput = it },
                    label = { Text("Water / Tubewell cost per acre") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pesticideCostInput,
                    onValueChange = { pesticideCostInput = it },
                    label = { Text("Pesticides cost per acre") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = expectedYieldMonsInput,
                        onValueChange = { expectedYieldMonsInput = it },
                        label = { Text("Expected Yield (Mons/Acre)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = expectedFarmerRateInput,
                        onValueChange = { expectedFarmerRateInput = it },
                        label = { Text("Rate (Rs./40kg)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Analysis cost values computation
                val multiplierAcres = acresInput.toDoubleOrNull() ?: 1.0
                val dapPrice = dapPriceInput.toDoubleOrNull() ?: 12500.0
                val dapBags = dapBagsInput.toDoubleOrNull() ?: 1.5
                val ureaPrice = ureaPriceInput.toDoubleOrNull() ?: 4800.0
                val ureaBags = ureaBagsInput.toDoubleOrNull() ?: 2.5

                val prepVal = prepCostMatrix.getOrNull(prepOptionState) ?: 3500.0
                val waterCost = waterCostInput.toDoubleOrNull() ?: 5000.0
                val pesticideCost = pesticideCostInput.toDoubleOrNull() ?: 6000.0
                val seedCost = seedCostInput.toDoubleOrNull() ?: 5500.0
                val harvestCost = harvestCostInput.toDoubleOrNull() ?: 9000.0

                val dapTotal = dapPrice * dapBags
                val ureaTotal = ureaPrice * ureaBags
                val basicCostPerAcre = dapTotal + ureaTotal + prepVal + waterCost + pesticideCost + seedCost + harvestCost
                val totalCostValue = basicCostPerAcre * multiplierAcres

                val expectedYieldMonsTotal = (expectedYieldMonsInput.toDoubleOrNull() ?: 45.0) * multiplierAcres
                val expectedRatePkr = expectedFarmerRateInput.toDoubleOrNull() ?: 3900.0
                val totalRevenueValue = expectedYieldMonsTotal * expectedRatePkr

                val profitLossValue = totalRevenueValue - totalCostValue
                val profitMargin = if (totalRevenueValue > 0.0) (profitLossValue / totalRevenueValue) * 100.0 else 0.0
                val costPerMon = if (expectedYieldMonsTotal > 0.0) totalCostValue / expectedYieldMonsTotal else 0.0

                // Analysis summary card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryGreen.copy(alpha = 0.08f)),
                    border = BorderStroke(1.5.dp, PrimaryGreen.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "FARMER PER-ACRE COST ANALYSIS", fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))

                        ResultRow(label = "Total Cultivated Costs (All Acres)", value = "Rs. ${String.format("%,.0f", totalCostValue)}")
                        ResultRow(label = "Expected Gross Revenue", value = "Rs. ${String.format("%,.0f", totalRevenueValue)}")
                        ResultRow(
                            label = if (profitLossValue >= 0.0) "Net Estimated Profit" else "Net Estimated Loss",
                            value = "Rs. ${String.format("%,.0f", profitLossValue)}",
                            valueColor = if (profitLossValue >= 0.0) PrimaryGreen else Color.Red
                        )
                        ResultRow(label = "Profit Margin Indicator", value = "${String.format("%.1f", profitMargin)}%")
                        ResultRow(label = "Break-Even Cost Per Maund (40kg)", value = "Rs. ${String.format("%,.0f", costPerMon)}")
                    }
                }

                // Category costs pie diagram drawn inside Canvas
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Acre Cost Allocation Model", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Canvas(modifier = Modifier.size(140.dp)) {
                            // Elements proportions representation
                            val totalElements = dapTotal + ureaTotal + prepVal + waterCost + pesticideCost + seedCost + harvestCost
                            val angles = listOf(
                                (dapTotal / totalElements * 360).toFloat(),
                                (ureaTotal / totalElements * 360).toFloat(),
                                (prepVal / totalElements * 360).toFloat(),
                                (waterCost / totalElements * 360).toFloat(),
                                (pesticideCost / totalElements * 360).toFloat(),
                                (seedCost / totalElements * 360).toFloat(),
                                (harvestCost / totalElements * 360).toFloat()
                            )
                            val colors = listOf(
                                Color(0xFF166534), Color(0xFF84CC16), Color(0xFF3B82F6),
                                Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFFEC4899), Color(0xFF8B5CF6)
                            )

                            var currentStartAngle = 0f
                            angles.forEachIndexed { i, angle ->
                                drawArc(
                                    color = colors[i],
                                    startAngle = currentStartAngle,
                                    sweepAngle = angle,
                                    useCenter = true
                                )
                                currentStartAngle += angle
                            }
                        }

                        // Category Legends indicators
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(start = 12.dp)
                        ) {
                            CostPieLegend(color = Color(0xFF166534), text = "DAP Fertilizer")
                            CostPieLegend(color = Color(0xFF84CC16), text = "Urea Nitrogen")
                            CostPieLegend(color = Color(0xFF3B82F6), text = "Land Prep Setup")
                            CostPieLegend(color = Color(0xFFF59E0B), text = "Water Hydration")
                            CostPieLegend(color = Color(0xFFEF4444), text = "Chemical Spray")
                            CostPieLegend(color = Color(0xFFEC4899), text = "Cultivated Seed")
                            CostPieLegend(color = Color(0xFF8B5CF6), text = "Combine Harvest")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun CostPieLegend(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}
