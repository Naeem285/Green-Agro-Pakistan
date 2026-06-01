package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fname: String,
    val lname: String,
    val username: String,
    val cnic: String, // format: XXXXX-XXXXXXX-X
    val password: String,
    val role: String, // Admin, Farmer, Trader
    val status: String = "Approved",
    val phone: String,
    val wa: String,
    val email: String,
    val recovery_email: String,
    val dob: String,
    val city: String,
    val address: String,
    val acres: Double = 0.0,
    val main_crop: String = "",
    val shop_name: String = "",
    val trader_type: String = "",
    val img: String? = null, // base64
    val created_at: String = System.currentTimeMillis().toString()
)

@Entity(tableName = "traders")
data class TraderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val shop_name: String,
    val phone: String,
    val wa: String,
    val sms: String,
    val cnic: String = "",
    val city: String,
    val address: String,
    val type: String, // Commission Agent, Arhtia, Wholesaler, Retailer, Exporter
    val crops: String, // Comma separated selected crop names
    val notes: String = "",
    val status: String = "Pending", // Pending, Approved, Rejected
    val rating_avg: Double = 0.0,
    val review_count: Int = 0,
    val created_at: String = System.currentTimeMillis().toString()
)

@Entity(tableName = "rates")
data class RateEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val crop: String,
    val rate: Double,
    val city: String,
    val market: String,
    val date: String,
    val published_by: String,
    val created_at: String = System.currentTimeMillis().toString()
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val trader_id: Int,
    val trader_name: String,
    val shop_name: String,
    val rating: Int,
    val comment: String,
    val user_name: String,
    val created_at: String = System.currentTimeMillis().toString()
)

@Entity(tableName = "news")
data class NewsEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // Prices, Weather, Tips, Policy, Market
    val content: String,
    val author: String,
    val created_at: String = System.currentTimeMillis().toString()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val user_id: Int, // User targeted OR -1 for broadcast
    val type: String, // rate, trader, news, review, system
    val title: String,
    val message: String,
    val is_read: Int = 0, // 0 = unread, 1 = read
    val created_at: String = System.currentTimeMillis().toString()
)

@Entity(tableName = "images")
data class ImageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val filename: String,
    val original_name: String,
    val file_path: String,
    val category: String, // Crop Images, Weather, Traders, News, General
    val title: String,
    val uploaded_by: String,
    val file_size: Long,
    val mime_type: String,
    val created_at: String = System.currentTimeMillis().toString()
)
