package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.components.AppStatCard
import com.example.ui.components.CategoryBadge
import com.example.ui.components.SectionHeader
import com.example.ui.components.TrustScoreBadge
import com.example.ui.theme.AccentLime
import com.example.ui.theme.PrimaryGreen

@Composable
fun DashboardScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val isUrdu by viewModel.isUrdu.collectAsState()
    val users by viewModel.allUsers.collectAsState()
    val traders by viewModel.allTraders.collectAsState()
    val rates by viewModel.allRates.collectAsState()
    val newsList by viewModel.allNews.collectAsState()

    val scrollState = rememberScrollState()

    // --- Calculations ---
    val activeUsersCount = users.size
    val approvedTradersCount = traders.count { it.status == "Approved" }
    val pendingTradersCount = traders.count { it.status == "Pending" }
    val totalMandiCount = rates.size

    val wheatRates = rates.filter { it.crop.lowercase() == "wheat" }.sortedBy { it.created_at }
    val avgWheatRate = if (wheatRates.isNotEmpty()) wheatRates.map { it.rate }.average() else 3850.0
    val predictedWheatRate = avgWheatRate * 1.013

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(bottom = 80.dp) // Avoid overlap with bottom nav
    ) {
        // --- 1. HERO CAPTION CARD ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(PrimaryGreen, Color(0xFF14532D))
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(0.7f)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = Translations.t("pakistan_agri_hub", isUrdu),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = Translations.t("dashboard_tagline", isUrdu),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.changeTab(3) }, // Tab Index for Calculator
                            colors = ButtonDefaults.buttonColors(containerColor = AccentLime),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(text = "Calculator", color = PrimaryGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { viewModel.navigateToScreen("weather") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(text = "Weather", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { viewModel.navigateToScreen("crop_calendar") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(text = "Crops", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text(
                    text = "🚜",
                    fontSize = 62.sp,
                    modifier = Modifier.weight(0.3f),
                    overflow = TextOverflow.Visible
                )
            }
        }

        // --- 2. HORIZONTAL STATS ROW ---
        SectionHeader(title = "Agriculture Stats Key Indicators")
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                AppStatCard(
                    label = Translations.t("active_users", isUrdu),
                    value = activeUsersCount.toString(),
                    icon = Icons.Default.Groups
                )
            }
            item {
                AppStatCard(
                    label = Translations.t("approved_traders", isUrdu),
                    value = approvedTradersCount.toString(),
                    icon = Icons.Default.VerifiedUser
                )
            }
            item {
                AppStatCard(
                    label = Translations.t("avg_wheat_rate", isUrdu),
                    value = "Rs. ${avgWheatRate.toInt()}",
                    icon = Icons.Default.TrendingUp
                )
            }
            item {
                AppStatCard(
                    label = Translations.t("smart_prediction", isUrdu),
                    value = "Rs. ${predictedWheatRate.toInt()}",
                    icon = Icons.Default.AutoAwesome
                )
            }
            item {
                AppStatCard(
                    label = Translations.t("pending_traders", isUrdu),
                    value = pendingTradersCount.toString(),
                    icon = Icons.Default.HourglassEmpty
                )
            }
            item {
                AppStatCard(
                    label = Translations.t("mandi_entries", isUrdu),
                    value = totalMandiCount.toString(),
                    icon = Icons.Default.Storefront
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. WHEAT RATE TREND GRAFIK CANVAS ---
        SectionHeader(title = "${Translations.t("wheat_trend", isUrdu)} (Rs. / 40kg)")
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Compose Custom Drawing Canvas
                val dataPoints = if (wheatRates.isNotEmpty()) {
                    wheatRates.map { it.rate.toFloat() }
                } else {
                    listOf(3700f, 3760f, 3790f, 3850f, 3820f, 3920f, 3950f)
                }

                val graphLabels = if (wheatRates.isNotEmpty()) {
                    wheatRates.map { it.date.split(" ").firstOrNull() ?: "" }
                } else {
                    listOf("24M", "25M", "26M", "27M", "28M", "29M", "30M")
                }

                Text(
                    text = "Weekly Mandi Fluctuation Index",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val widthOffset = size.width / (dataPoints.size - 1)
                        val minVal = dataPoints.minOrNull() ?: 3700f
                        val maxVal = dataPoints.maxOrNull() ?: 4000f
                        val valueRange = if (maxVal == minVal) 100f else (maxVal - minVal)

                        val points = dataPoints.mapIndexed { idx, rate ->
                            val x = idx * widthOffset
                            val relativeValue = (rate - minVal) / valueRange
                            val y = size.height - (relativeValue * size.height * 0.8f) - (size.height * 0.1f)
                            Offset(x, y)
                        }

                        // Drawing Gradient fill under path
                        val fillPath = Path().apply {
                            moveTo(0f, size.height)
                            points.forEach { lineTo(it.x, it.y) }
                            lineTo(size.width, size.height)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(PrimaryGreen.copy(alpha = 0.3f), Color.Transparent)
                            )
                        )

                        // Draw connecting lines
                        val strokePath = Path().apply {
                            if (points.isNotEmpty()) {
                                moveTo(points[0].x, points[0].y)
                                for (i in 1 until points.size) {
                                    lineTo(points[i].x, points[i].y)
                                }
                            }
                        }
                        drawPath(
                            path = strokePath,
                            color = PrimaryGreen,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // Draw dots and annotations
                        points.forEachIndexed { i, pt ->
                            drawCircle(
                                color = AccentLime,
                                radius = 5.dp.toPx(),
                                center = pt
                            )
                            drawCircle(
                                color = PrimaryGreen,
                                radius = 2.dp.toPx(),
                                center = pt
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Render Labels horizontally
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    graphLabels.forEach { label ->
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 4. QUICK MARKET SNAPSHOT ---
        SectionHeader(
            title = Translations.t("mandi_snapshot", isUrdu),
            actionButtonText = "View Rates Port",
            onActionClick = { viewModel.changeTab(1) }
        )
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                val snapshot = rates.take(6)
                if (snapshot.isEmpty()) {
                    Text(
                        text = "No Mandi records registered yet.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    snapshot.forEachIndexed { idx, rate ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryGreen)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = rate.crop,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Rs. ${rate.rate.toInt()}",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryGreen,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = rate.city,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (idx < snapshot.size - 1) {
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 5. LATEST AGRI NEWS ---
        SectionHeader(
            title = Translations.t("latest_news", isUrdu),
            actionButtonText = "Read All News",
            onActionClick = { viewModel.navigateToScreen("news") }
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val newsSnapshot = newsList.take(3)
            newsSnapshot.forEach { news ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateToScreen("news") }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryBadge(category = news.category)
                            Text(
                                text = news.author,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = news.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (news.content.length > 120) news.content.take(120) + "..." else news.content,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 6. TOP TRADERS ---
        SectionHeader(
            title = Translations.t("top_traders", isUrdu),
            actionButtonText = Translations.t("open_trader_directory", isUrdu),
            onActionClick = { viewModel.changeTab(2) }
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val approvedTraders = traders.filter { it.status == "Approved" }
            val top4Traders = approvedTraders.sortedByDescending { it.rating_avg }.take(4)

            if (top4Traders.isEmpty()) {
                Text(
                    text = "No traders registered yet.",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(8.dp)
                )
            } else {
                top4Traders.forEach { trader ->
                    val trustScore = ((trader.rating_avg / 5.0 * 60.0) + Math.min(40.0, trader.review_count * 4.0)).toInt().coerceIn(0, 100)

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(0.7f)) {
                                Text(
                                    text = trader.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${trader.shop_name} • ${trader.city}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "RatingStar",
                                        tint = AccentLime,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "${String.format("%.1f", trader.rating_avg)} (${trader.review_count} reviews)",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Box(modifier = Modifier.weight(0.3f), contentAlignment = Alignment.CenterEnd) {
                                TrustScoreBadge(score = trustScore)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
