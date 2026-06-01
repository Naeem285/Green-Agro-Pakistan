package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppViewModel
import com.example.data.Translations
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModel = ViewModelProvider(this, AppViewModel.Factory(application))[AppViewModel::class.java]

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            MyApplicationTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppMainEntry(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AppMainEntry(viewModel: AppViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    var showRegisterScreen by remember { mutableStateOf(false) }

    if (currentUser == null) {
        if (showRegisterScreen) {
            RegisterScreen(
                viewModel = viewModel,
                onNavigateToLogin = { showRegisterScreen = false }
            )
        } else {
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = { showRegisterScreen = true }
            )
        }
    } else {
        AppShell(viewModel = viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShell(viewModel: AppViewModel) {
    val isUrdu by viewModel.isUrdu.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val activeSubScreen by viewModel.currentActiveScreen.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val countUnread by viewModel.notificationsCount.collectAsState()

    // System Back Press Handler inside Compose
    BackHandler(enabled = activeSubScreen != null) {
        viewModel.navigateBack()
    }

    Scaffold(
        topBar = {
            Surface(
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                color = PrimaryGreen,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                TopAppBar(
                    title = {
                        Column {
                            val greeting = if (isUrdu) {
                                "السلام علیکم، ${user?.fname ?: "صارف"}"
                            } else {
                                "Assalam-o-Alaikum, ${user?.fname ?: "Member"}"
                            }
                            Text(
                                text = greeting,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = Translations.t("pakistan_agri_hub", isUrdu),
                                fontSize = 10.sp,
                                color = AccentLime,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Developer: Muhamad Naeem Akram",
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    },
                    actions = {
                        // Urdu lang switcher button
                        Button(
                            onClick = { viewModel.toggleLanguage() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(30.dp)
                                .padding(end = 12.dp)
                        ) {
                            Text(
                                text = if (isUrdu) "EN" else "اردو",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Alerts notifications badge icon
                        Box(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .clickable { viewModel.navigateToScreen("notifications") },
                            contentAlignment = Alignment.TopEnd
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Alerts",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                            if (countUnread > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(Color.Red),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = countUnread.toString(),
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        },
        bottomBar = {
            Surface(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    contentColor = PrimaryGreen
                ) {
                    // Dashboard
                    NavigationBarItem(
                        selected = currentTab == 0 && activeSubScreen == null,
                        onClick = { viewModel.changeTab(0) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                        label = { Text(Translations.t("home", isUrdu), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryGreen,
                            selectedTextColor = PrimaryGreen,
                            indicatorColor = AccentLime.copy(alpha = 0.4f)
                        )
                    )

                    // Mandi Rates
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { viewModel.changeTab(1) },
                        icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Rates") },
                        label = { Text(Translations.t("mandi_rates", isUrdu), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryGreen,
                            selectedTextColor = PrimaryGreen,
                            indicatorColor = AccentLime.copy(alpha = 0.4f)
                        )
                    )

                    // Traders Directory
                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { viewModel.changeTab(2) },
                        icon = { Icon(Icons.Default.Groups, contentDescription = "Traders") },
                        label = { Text(Translations.t("traders", isUrdu), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryGreen,
                            selectedTextColor = PrimaryGreen,
                            indicatorColor = AccentLime.copy(alpha = 0.4f)
                        )
                    )

                    // Income Calculator
                    NavigationBarItem(
                        selected = currentTab == 3,
                        onClick = { viewModel.changeTab(3) },
                        icon = { Icon(Icons.Default.Calculate, contentDescription = "Calc") },
                        label = { Text("Calculator", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryGreen,
                            selectedTextColor = PrimaryGreen,
                            indicatorColor = AccentLime.copy(alpha = 0.4f)
                        )
                    )

                    // More Menu Services
                    NavigationBarItem(
                        selected = currentTab == 4 || activeSubScreen != null,
                        onClick = { viewModel.changeTab(4) },
                        icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "More") },
                        label = { Text(Translations.t("more", isUrdu), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryGreen,
                            selectedTextColor = PrimaryGreen,
                            indicatorColor = AccentLime.copy(alpha = 0.4f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (activeSubScreen != null) {
                // Renders weather / profile / notifications / admin panel subpages
                MoreScreens(viewModel = viewModel)
            } else {
                // Standard main tab rows
                when (currentTab) {
                    0 -> DashboardScreen(viewModel = viewModel)
                    1 -> MandiRatesScreen(viewModel = viewModel)
                    2 -> TraderScreen(viewModel = viewModel)
                    3 -> CalculatorScreen(viewModel = viewModel)
                    4 -> MoreScreens(viewModel = viewModel)
                }
            }
        }
    }
}
