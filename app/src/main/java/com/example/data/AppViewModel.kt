package com.example.data

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AppRepository(db.appDao())
    private val prefs: SharedPreferences = application.getSharedPreferences("green_agro_prefs", Context.MODE_PRIVATE)

    // --- Core UI State flows ---
    private val _isUrdu = MutableStateFlow(prefs.getBoolean("is_urdu", false))
    val isUrdu: StateFlow<Boolean> = _isUrdu.asStateFlow()

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("is_dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _currentTab = MutableStateFlow(0) // 0: Dashboard, 1: Rates, 2: Traders, 3: Calculator, 4: More
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _currentActiveScreen = MutableStateFlow<String?>(null) // e.g. "weather", "crop_calendar", "profit_analyzer", "profile", "news", "notifications", "admin_panel", "image_manager"
    val currentActiveScreen: StateFlow<String?> = _currentActiveScreen.asStateFlow()

    // --- Dynamic Navigation Stack to handle "More" screens ---
    private val screenHistory = mutableListOf<String>()

    fun navigateToScreen(screen: String) {
        if (screenHistory.lastOrNull() != screen) {
            screenHistory.add(screen)
        }
        _currentActiveScreen.value = screen
    }

    fun navigateBack(): Boolean {
        if (screenHistory.isNotEmpty()) {
            screenHistory.removeAt(screenHistory.size - 1)
            _currentActiveScreen.value = screenHistory.lastOrNull()
            return true
        }
        return false
    }

    // --- Auth State ---
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authSuccessMessage = MutableStateFlow<String?>(null)
    val authSuccessMessage: StateFlow<String?> = _authSuccessMessage.asStateFlow()

    // --- Notifications Counter ---
    private val _notificationsCount = MutableStateFlow(0)
    val notificationsCount: StateFlow<Int> = _notificationsCount.asStateFlow()

    // --- Observable Flows from Database ---
    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTraders: StateFlow<List<TraderEntity>> = repository.allTraders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRates: StateFlow<List<RateEntity>> = repository.allRates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNews: StateFlow<List<NewsEntity>> = repository.allNews
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReviews: StateFlow<List<ReviewEntity>> = repository.allReviews
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allImages: StateFlow<List<ImageEntity>> = repository.allImages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live list of notifications targeting the current user or broadcast (-1)
    val currentNotifications = currentUser.flatMapLatest { user ->
        if (user != null) {
            repository.getNotificationsForUser(user.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Auto-login session retrieval
        val savedUserId = prefs.getInt("session_user_id", -1)
        if (savedUserId != -1) {
            viewModelScope.launch {
                val user = repository.getUserById(savedUserId)
                if (user != null) {
                    _currentUser.value = user
                } else {
                    clearSession()
                }
            }
        }

        // Count unread notifications
        viewModelScope.launch {
            currentNotifications.collect { notifications ->
                _notificationsCount.value = notifications.count { it.is_read == 0 }
            }
        }

        // Always check connectivity and seed database if first launch
        viewModelScope.launch(Dispatchers.IO) {
            try {
                AppDatabase.seedData(db.appDao())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- Connectivity Helper ---
    val isOnline: Boolean
        get() {
            val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val net = cm.activeNetwork ?: return false
            val cap = cm.getNetworkCapabilities(net) ?: return false
            return cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }

    // --- Weather State ---
    private val _weatherState = MutableStateFlow(WeatherState())
    val weatherState: StateFlow<WeatherState> = _weatherState.asStateFlow()

    // --- Toggles ---
    fun toggleLanguage() {
        val nextValue = !_isUrdu.value
        _isUrdu.value = nextValue
        prefs.edit().putBoolean("is_urdu", nextValue).apply()
    }

    fun toggleDarkMode() {
        val nextValue = !_isDarkMode.value
        _isDarkMode.value = nextValue
        prefs.edit().putBoolean("is_dark_mode", nextValue).apply()
    }

    fun changeTab(tab: Int) {
        _currentTab.value = tab
        // Clear screen history when switching tabs
        screenHistory.clear()
        _currentActiveScreen.value = null
    }

    // --- Auth Logic ---
    fun login(cnic: String, pass: String) {
        _authError.value = null
        _authSuccessMessage.value = null
        viewModelScope.launch {
            val user = repository.getUserByCnic(cnic)
            if (user == null) {
                _authError.value = "CNIC not registered. Please sign up."
            } else if (user.password != pass) {
                _authError.value = "Incorrect password. Please try again."
            } else if (user.status == "Pending") {
                _authError.value = "Your account is pending admin approval."
            } else {
                _currentUser.value = user
                saveSession(user)
                _authSuccessMessage.value = "Welcome back, ${user.fname}!"
            }
        }
    }

    fun register(user: UserEntity) {
        _authError.value = null
        _authSuccessMessage.value = null
        viewModelScope.launch {
            val existing = repository.getUserByCnic(user.cnic)
            if (existing != null) {
                _authError.value = "This CNIC is already registered."
                return@launch
            }
            val id = repository.insertUser(user)
            if (id > 0) {
                _authSuccessMessage.value = "Registration successful! Welcome to the portal."
                // Seed an entry notification for new user
                repository.insertNotification(
                    NotificationEntity(
                        user_id = id.toInt(),
                        type = "system",
                        title = "Account Setup",
                        message = "Welcome, ${user.fname}! Your profile has been created successfully."
                    )
                )
            } else {
                _authError.value = "Failed to insert user. Please retry."
            }
        }
    }

    fun logout() {
        clearSession()
        _currentUser.value = null
        _currentTab.value = 0
        screenHistory.clear()
        _currentActiveScreen.value = null
    }

    private fun saveSession(user: UserEntity) {
        prefs.edit()
            .putInt("session_user_id", user.id)
            .putString("session_role", user.role)
            .putString("session_fname", user.fname)
            .putString("session_city", user.city)
            .apply()
    }

    private fun clearSession() {
        prefs.edit()
            .remove("session_user_id")
            .remove("session_role")
            .remove("session_fname")
            .remove("session_city")
            .apply()
    }

    // --- User Profile Edit ---
    fun updateProfile(updatedUser: UserEntity) {
        viewModelScope.launch {
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser
            saveSession(updatedUser)
        }
    }

    // --- Rates Operations ---
    fun publishRate(crop: String, rate: Double, city: String, market: String, date: String) {
        viewModelScope.launch {
            val userName = currentUser.value?.let { "${it.fname} ${it.lname}" } ?: "Member"
            val id = repository.insertRate(
                RateEntity(
                    crop = crop,
                    rate = rate,
                    city = city,
                    market = market,
                    date = date,
                    published_by = userName
                )
            )

            if (id > 0) {
                // System notification broadcasting rate change
                repository.insertNotification(
                    NotificationEntity(
                        user_id = -1, // Broadcast
                        type = "rate",
                        title = "New Mandi Rate Added",
                        message = "$crop rate in $city updated to Rs. $rate/40kg."
                    )
                )
            }
        }
    }

    fun deleteRate(id: Int) {
        viewModelScope.launch {
            repository.deleteRateById(id)
        }
    }

    // --- Traders Directory Operations ---
    fun submitTrader(trader: TraderEntity) {
        viewModelScope.launch {
            val id = repository.insertTrader(trader)
            if (id > 0) {
                val isApproved = trader.status == "Approved"
                val systemTitle = if (isApproved) "New Trader Registered" else "Trader Request Pending"
                val systemMsg = if (isApproved) "${trader.name} (${trader.shop_name}) in ${trader.city} is approved." else "${trader.name} (${trader.shop_name}) has requested listing."

                repository.insertNotification(
                    NotificationEntity(
                        user_id = -1,
                        type = "trader",
                        title = systemTitle,
                        message = systemMsg
                    )
                )
            }
        }
    }

    fun updateTrader(trader: TraderEntity) {
        viewModelScope.launch {
            repository.updateTrader(trader)
        }
    }

    fun approveTrader(id: Int, traderName: String) {
        viewModelScope.launch {
            repository.updateTraderStatus(id, "Approved")
            repository.insertNotification(
                NotificationEntity(
                    user_id = -1,
                    type = "trader",
                    title = "Trader Approved",
                    message = "Trader '$traderName' has been approved by admin and is now public."
                )
            )
        }
    }

    fun rejectTrader(id: Int) {
        viewModelScope.launch {
            repository.deleteTraderById(id)
        }
    }

    // --- Reviews Operations ---
    fun submitReview(traderId: Int, traderName: String, shopName: String, rating: Int, comment: String) {
        viewModelScope.launch {
            val userName = currentUser.value?.let { "${it.fname} ${it.lname}" } ?: "Anonymous"
            repository.insertReview(
                ReviewEntity(
                    trader_id = traderId,
                    trader_name = traderName,
                    shop_name = shopName,
                    rating = rating,
                    comment = comment,
                    user_name = userName
                )
            )

            // Recalculate Trader Rating averages
            recalculateTraderRating(traderId)

            // Add notification
            repository.insertNotification(
                NotificationEntity(
                    user_id = -1,
                    type = "review",
                    title = "New Review Added",
                    message = "A $rating-Star review was added for $traderName by $userName."
                )
            )
        }
    }

    private suspend fun recalculateTraderRating(traderId: Int) {
        val reviews = repository.allReviews.first()
        val traderReviews = reviews.filter { it.trader_id == traderId }
        if (traderReviews.isNotEmpty()) {
            val count = traderReviews.size
            val avg = traderReviews.sumOf { it.rating }.toDouble() / count
            repository.updateTraderRating(traderId, avg, count)
        }
    }

    // --- News Operations ---
    fun publishNews(title: String, category: String, content: String) {
        viewModelScope.launch {
            val userName = currentUser.value?.let { "${it.fname} ${it.lname}" } ?: "Admin"
            val id = repository.insertNews(
                NewsEntity(
                    title = title,
                    category = category,
                    content = content,
                    author = userName
                )
            )
            if (id > 0) {
                repository.insertNotification(
                    NotificationEntity(
                        user_id = -1,
                        type = "news",
                        title = "Agriculture News Broadcast",
                        message = "New advisory update: '$title' in category '$category'."
                    )
                )
            }
        }
    }

    fun deleteNews(id: Int) {
        viewModelScope.launch {
            repository.deleteNewsById(id)
        }
    }

    // --- Notification Operations ---
    fun markAllNotificationsRead() {
        viewModelScope.launch {
            val currentUserId = currentUser.value?.id ?: -1
            repository.markAllNotificationsAsRead(currentUserId)
        }
    }

    fun markNotificationRead(id: Int) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    // --- Admin Operations ---
    fun deleteUser(id: Int) {
        viewModelScope.launch {
            repository.deleteUserById(id)
        }
    }

    fun adminCreateUser(user: UserEntity) {
        viewModelScope.launch {
            repository.insertUser(user)
        }
    }

    // --- Images Operations ---
    fun insertImage(filename: String, originalName: String, filePath: String, category: String, title: String, fileSize: Long) {
        viewModelScope.launch {
            val userName = currentUser.value?.let { "${it.fname} ${it.lname}" } ?: "Admin"
            repository.insertImage(
                ImageEntity(
                    filename = filename,
                    original_name = originalName,
                    file_path = filePath,
                    category = category,
                    title = title,
                    uploaded_by = userName,
                    file_size = fileSize,
                    mime_type = "image/png"
                )
            )
        }
    }

    fun deleteImage(id: Int) {
        viewModelScope.launch {
            repository.deleteImageById(id)
        }
    }

    // --- Weather Fetch Engine ---
    fun fetchWeather(city: String) {
        _weatherState.update { it.copy(isLoading = true, error = null) }
        val finalCity = city.trim().lowercase()

        // Coordinate Map
        val coordinates = mapOf(
            "lahore" to Pair(31.5497, 74.3436),
            "karachi" to Pair(24.8607, 67.0011),
            "islamabad" to Pair(33.7215, 73.0433),
            "faisalabad" to Pair(31.4154, 73.0886),
            "multan" to Pair(30.1575, 71.5249),
            "rawalpindi" to Pair(33.5651, 73.0169),
            "gujranwala" to Pair(32.1609, 74.1883),
            "peshawar" to Pair(34.0151, 71.5249),
            "sargodha" to Pair(32.0836, 72.6711),
            "bahawalpur" to Pair(29.3956, 71.6722),
            "sahiwal" to Pair(30.6682, 73.1045),
            "okara" to Pair(30.8138, 73.4534),
            "lodhran" to Pair(29.5337, 71.6324),
            "mailsi" to Pair(29.9040, 72.1740),
            "vehari" to Pair(30.0419, 72.3551)
        )

        val pair = coordinates[finalCity] ?: Pair(31.5497, 74.3436) // Default to Lahore
        val lat = pair.first
        val lon = pair.second

        viewModelScope.launch {
            if (isOnline) {
                val urlString = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current_weather=true&hourly=temperature_2m,weathercode,windspeed_10m&daily=weathercode,temperature_2m_max,temperature_2m_min,precipitation_sum&timezone=Asia/Karachi&forecast_days=7"
                val result = withContext(Dispatchers.IO) {
                    try {
                        val url = URL(urlString)
                        val conn = url.openConnection() as HttpURLConnection
                        conn.requestMethod = "GET"
                        conn.connectTimeout = 5000
                        conn.readTimeout = 5000
                        val code = conn.responseCode
                        if (code == 200) {
                            val br = BufferedReader(InputStreamReader(conn.inputStream))
                            val sb = StringBuilder()
                            var line: String?
                            while (br.readLine().also { line = it } != null) {
                                sb.append(line)
                            }
                            br.close()
                            sb.toString()
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }

                if (result != null) {
                    // Cache to prefs
                    prefs.edit().putString("weather_${finalCity}_cache", result).apply()
                    _weatherState.value = parseWeatherJson(result, false)
                } else {
                    // Try to load cache
                    val cache = prefs.getString("weather_${finalCity}_cache", null)
                    if (cache != null) {
                        _weatherState.value = parseWeatherJson(cache, true)
                    } else {
                        _weatherState.update { it.copy(isLoading = false, error = "Connection failed and no cached data exists.") }
                    }
                }
            } else {
                // Offline load cache
                val cache = prefs.getString("weather_${finalCity}_cache", null)
                if (cache != null) {
                    _weatherState.value = parseWeatherJson(cache, true)
                } else {
                    _weatherState.update { it.copy(isLoading = false, error = "You are offline. No weather cache exists for $city.") }
                }
            }
        }
    }

    private fun parseWeatherJson(jsonString: String, isCache: Boolean): WeatherState {
        try {
            val root = JSONObject(jsonString)
            val currentObj = root.optJSONObject("current_weather") ?: return WeatherState(error = "No current weather payload found.")
            val temp = currentObj.optDouble("temperature", 0.0)
            val windSpeed = currentObj.optDouble("windspeed", 0.0)
            val code = currentObj.optInt("weathercode", 0)

            val dailyObj = root.optJSONObject("daily") ?: return WeatherState(error = "No daily elements found.")
            val timeArr = dailyObj.optJSONArray("time") ?: return WeatherState(error = "No time entries.")
            val codeArr = dailyObj.optJSONArray("weathercode") ?: return WeatherState(error = "No daily weathercodes.")
            val maxArr = dailyObj.optJSONArray("temperature_2m_max") ?: return WeatherState(error = "No max temp entries.")
            val minArr = dailyObj.optJSONArray("temperature_2m_min") ?: return WeatherState(error = "No min temp entries.")
            val precArr = dailyObj.optJSONArray("precipitation_sum") ?: return WeatherState(error = "No precipitation values.")

            val list = mutableListOf<DailyForecast>()
            for (i in 0 until timeArr.length()) {
                val inputDateStr = timeArr.optString(i, "")
                val dayName = getDayNameFromDateStr(inputDateStr)
                list.add(
                    DailyForecast(
                        dayName = dayName,
                        weatherCode = codeArr.optInt(i, 0),
                        maxTemp = maxArr.optDouble(i, 0.0),
                        minTemp = minArr.optDouble(i, 0.0),
                        precipitation = precArr.optDouble(i, 0.0)
                    )
                )
            }

            // Advice Generation Matrix
            val adv = mutableListOf<String>()
            if (code >= 51) {
                adv.add("Pesticide Spray: Highly advised AGAINST chemical or fertilizer spray due to precipitation.")
            } else {
                adv.add("Pesticide Spray: Conditions are dry and stable. Safe for spray cycles.")
            }

            if (code == 0 || code == 1) {
                adv.add("Harvesting Advice: Clear sunny sky overhead. Harvesting cycles are highly favorable.")
            } else {
                adv.add("Harvesting Advice: Clouds or moisture forecasted. Cover harvested crops immediately.")
            }

            var rainForecasted = false
            for (i in 0 until Math.min(3, list.size)) {
                if (list[i].precipitation > 2.0) {
                    rainForecasted = true
                }
            }

            if (rainForecasted) {
                adv.add("Irrigation Advice: Rain expected (> 2mm) in the next 48-72 hours. Skip irrigation cycles to prevent waterlogging.")
            } else {
                adv.add("Irrigation Advice: Dry forecast for the upcoming 3 days. Continue standard hydration schedules.")
            }

            return WeatherState(
                temperature = temp,
                windSpeed = windSpeed,
                weatherCode = code,
                conditionText = getWeatherConditionText(code),
                forecastList = list,
                adviceList = adv,
                isOfflineCache = isCache,
                isLoading = false
            )

        } catch (e: Exception) {
            e.printStackTrace()
            return WeatherState(error = "Parsing failed: ${e.localizedMessage}")
        }
    }

    private fun getDayNameFromDateStr(dateStr: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val date = sdf.parse(dateStr) ?: return dateStr
            val out = SimpleDateFormat("EEE", Locale.ENGLISH)
            out.format(date)
        } catch (e: Exception) {
            dateStr
        }
    }

    private fun getWeatherConditionText(code: Int): String {
        return when (code) {
            0 -> "Clear Sky ☀️"
            1 -> "Mostly Clear 🌤️"
            2 -> "Partly Cloudy ⛅"
            3 -> "Overcast ☁️"
            45, 48 -> "Foggy 🌫️"
            51, 53, 55, 56, 57 -> "Drizzle 🌦️"
            61, 63, 65, 66, 67 -> "Rain 🌧️"
            71, 73, 75, 77 -> "Snow ❄️"
            80, 81, 82 -> "Showers 🌦️"
            95, 96, 99 -> "Thunderstorm ⛈️"
            else -> "Mild Weather"
        }
    }

    // --- Segmented Helper Factory ---
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AppViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class DailyForecast(
    val dayName: String,
    val weatherCode: Int,
    val maxTemp: Double,
    val minTemp: Double,
    val precipitation: Double
)

data class WeatherState(
    val temperature: Double = 0.0,
    val windSpeed: Double = 0.0,
    val weatherCode: Int = 0,
    val conditionText: String = "Clear Sky",
    val forecastList: List<DailyForecast> = emptyList(),
    val adviceList: List<String> = emptyList(),
    val isOfflineCache: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
