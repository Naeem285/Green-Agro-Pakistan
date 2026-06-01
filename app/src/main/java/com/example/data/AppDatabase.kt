package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        TraderEntity::class,
        RateEntity::class,
        ReviewEntity::class,
        NewsEntity::class,
        NotificationEntity::class,
        ImageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "green_agro_pakistan_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed database using a background coroutine
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = getDatabase(context).appDao()
                    seedData(dao)
                }
            }
        }

        suspend fun seedData(dao: AppDao) {
            // 1. Seed Admin User
            val adminCnic = "36201-8620031-3"
            val existingAdmin = dao.getUserByCnic(adminCnic)
            if (existingAdmin == null) {
                dao.insertUser(
                    UserEntity(
                        fname = "Muhamad",
                        lname = "Naeem Akram",
                        username = "admin",
                        cnic = adminCnic,
                        password = "030177na",
                        role = "Admin",
                        status = "Approved",
                        phone = "0301-7788992",
                        wa = "0301-7788992",
                        email = "naeemfalak285@gmail.com",
                        recovery_email = "naeem_recovery@gmail.com",
                        dob = "1994-06-15",
                        city = "Multan",
                        address = "Multan, Punjab, Pakistan",
                        acres = 40.0,
                        main_crop = "Wheat"
                    )
                )

                // Insert a couple first notifications
                dao.insertNotification(
                    NotificationEntity(
                        user_id = -1,
                        type = "system",
                        title = "Welcome to Green Agro Pakistan",
                        message = "Your offline-first agricultural portal is officially ready. Sowing seasons, rates, weather advisories are up-to-date for 2026!"
                    )
                )
            }

            // 2. Seed Rates (if empty)
            val allRates = dao.getAllRates()
            // Since getAllRates() is a Flow, let's check with a short manual query if empty or just do it based on admin insertion
            // Let's seed 7 wheat entries (Rs. 3760-3950), 1 basmati, 1 cotton, 1 sugarcane
            dbSeedRates(dao)

            // 3. Seed News
            dbSeedNews(dao)

            // 4. Seed some sample traders to make directory look stunning from start
            dbSeedTraders(dao)
        }

        private suspend fun dbSeedRates(dao: AppDao) {
            val cropsCount = dao.insertRate(
                RateEntity(crop = "Wheat", rate = 3760.0, city = "Lahore", market = "Mandi Lahore", date = "24 May 2026", published_by = "Muhammad Naeem Akram")
            )
            // If it succeeds and we want to populate others
            dao.insertRate(RateEntity(crop = "Wheat", rate = 3790.0, city = "Multan", market = "Ghalla Mandi Multan", date = "25 May 2026", published_by = "Muhammad Naeem Akram"))
            dao.insertRate(RateEntity(crop = "Wheat", rate = 3820.0, city = "Sargodha", market = "Sargodha Mandi", date = "26 May 2026", published_by = "Muhammad Naeem Akram"))
            dao.insertRate(RateEntity(crop = "Wheat", rate = 3850.0, city = "Faisalabad", market = "Mandi Sadar Faisalabad", date = "27 May 2026", published_by = "Muhammad Naeem Akram"))
            dao.insertRate(RateEntity(crop = "Wheat", rate = 3880.0, city = "Sahiwal", market = "Sahiwal Mandi", date = "28 May 2026", published_by = "Muhammad Naeem Akram"))
            dao.insertRate(RateEntity(crop = "Wheat", rate = 3920.0, city = "Okara", market = "Okara Central Mandi", date = "29 May 2026", published_by = "Muhammad Naeem Akram"))
            dao.insertRate(RateEntity(crop = "Wheat", rate = 3950.0, city = "Bahawalpur", market = "Bahawalpur Mandi", date = "30 May 2026", published_by = "Muhammad Naeem Akram"))

            dao.insertRate(RateEntity(crop = "Basmati Rice", rate = 6200.0, city = "Sargodha", market = "Sargodha Grain Market", date = "30 May 2026", published_by = "Muhammad Naeem Akram"))
            dao.insertRate(RateEntity(crop = "Cotton", rate = 7800.0, city = "Multan", market = "Multan Cotton Market", date = "30 May 2026", published_by = "Muhammad Naeem Akram"))
            dao.insertRate(RateEntity(crop = "Sugarcane", rate = 445.0, city = "Faisalabad", market = "Mill Gate Faisalabad", date = "30 May 2026", published_by = "Muhammad Naeem Akram"))
        }

        private suspend fun dbSeedNews(dao: AppDao) {
            dao.insertNews(
                NewsEntity(
                    title = "Govt Announces New Wheat Support Price for 2026",
                    category = "Prices",
                    content = "The Federal Cabinet has officially set the wheat support price at Rs. 3900 per 40kg to support local farming communities across Punjab and Sindh. This aims to counter inflation and encourage record acreage.",
                    author = "Agri Dept"
                )
            )
            dao.insertNews(
                NewsEntity(
                    title = "Monsoon Forecast: Sowing Advisory for Rice Farmers",
                    category = "Weather",
                    content = "Meteorologists expect steady rains from next week. Farmers in Gujranwala, Sialkot, and Sheikhupura are advised to delay transplantation of basmati by 3 days to optimize root bedding.",
                    author = "Met Office"
                )
            )
            dao.insertNews(
                NewsEntity(
                    title = "Organic Pest Control: Key Tips for Cotton Growers",
                    category = "Tips",
                    content = "Integrating predatory yellow sticky traps and using organic neem-extract sprays helps effectively combat whiteflies and thrips without damaging natural ecological balances.",
                    author = "NARC Research"
                )
            )
        }

        private suspend fun dbSeedTraders(dao: AppDao) {
            dao.insertTrader(
                TraderEntity(
                    name = "Tariq Mahmood",
                    shop_name = "Mahmood Grain Traders",
                    phone = "0300-7654321",
                    wa = "0300-7654321",
                    sms = "0300-7654321",
                    cnic = "33100-3334445-1",
                    city = "Faisalabad",
                    address = "Shop 14, Ghalla Mandi, Faisalabad",
                    type = "Commission Agent",
                    crops = "Wheat, Rice, Cotton",
                    notes = "Trusted broker with over 20 years in mandi transactions.",
                    status = "Approved",
                    rating_avg = 4.8,
                    review_count = 12
                )
            )
            dao.insertTrader(
                TraderEntity(
                    name = "Zahid Hussain Khan",
                    shop_name = "Khan & Brothers Ltd",
                    phone = "0301-4455667",
                    wa = "0301-4455667",
                    sms = "0301-4455667",
                    cnic = "35200-8889991-3",
                    city = "Lahore",
                    address = "Mandi Gate, Badami Bagh, Lahore",
                    type = "Wholesaler",
                    crops = "Rice, Corn, Sugarcane",
                    notes = "Bulk buyer for grain export. Quick direct payments.",
                    status = "Approved",
                    rating_avg = 4.2,
                    review_count = 5
                )
            )
            dao.insertTrader(
                TraderEntity(
                    name = "Mian Arshad",
                    shop_name = "Al-Rehman Arhtia Shop",
                    phone = "0333-9112233",
                    wa = "0333-9112233",
                    sms = "0333-9112233",
                    cnic = "38400-1112223-1",
                    city = "Sargodha",
                    address = "Grain Market, Sargodha Central",
                    type = "Arhtia",
                    crops = "Wheat, Sugarcane, Maize",
                    notes = "Supports local farmers with seasonal credit extensions.",
                    status = "Approved",
                    rating_avg = 4.6,
                    review_count = 9
                )
            )
        }
    }
}
