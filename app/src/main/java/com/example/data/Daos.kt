package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- USERS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE cnic = :cnic LIMIT 1")
    suspend fun getUserByCnic(cnic: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): UserEntity?

    @Query("SELECT * FROM users ORDER BY id DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: Int)

    // --- TRADERS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrader(trader: TraderEntity): Long

    @Update
    suspend fun updateTrader(trader: TraderEntity)

    @Query("SELECT * FROM traders ORDER BY rating_avg DESC, id DESC")
    fun getAllTraders(): Flow<List<TraderEntity>>

    @Query("DELETE FROM traders WHERE id = :id")
    suspend fun deleteTraderById(id: Int)

    @Query("UPDATE traders SET status = :status WHERE id = :id")
    suspend fun updateTraderStatus(id: Int, status: String)

    @Query("UPDATE traders SET rating_avg = :ratingAvg, review_count = :reviewCount WHERE id = :id")
    suspend fun updateTraderRating(id: Int, ratingAvg: Double, reviewCount: Int)

    // --- RATES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRate(rate: RateEntity): Long

    @Query("SELECT * FROM rates ORDER BY created_at DESC")
    fun getAllRates(): Flow<List<RateEntity>>

    @Query("DELETE FROM rates WHERE id = :id")
    suspend fun deleteRateById(id: Int)

    // --- REVIEWS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity): Long

    @Query("SELECT * FROM reviews ORDER BY created_at DESC")
    fun getAllReviews(): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE trader_id = :traderId ORDER BY created_at DESC")
    fun getReviewsForTrader(traderId: Int): Flow<List<ReviewEntity>>

    // --- NEWS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(news: NewsEntity): Long

    @Query("SELECT * FROM news ORDER BY created_at DESC")
    fun getAllNews(): Flow<List<NewsEntity>>

    @Query("DELETE FROM news WHERE id = :id")
    suspend fun deleteNewsById(id: Int)

    // --- NOTIFICATIONS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("SELECT * FROM notifications WHERE user_id = :userId OR user_id = -1 ORDER BY created_at DESC")
    fun getNotificationsForUser(userId: Int): Flow<List<NotificationEntity>>

    @Query("UPDATE notifications SET is_read = 1 WHERE user_id = :userId OR user_id = -1")
    suspend fun markAllNotificationsAsRead(userId: Int)

    @Query("UPDATE notifications SET is_read = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Int)

    // --- IMAGES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: ImageEntity): Long

    @Query("SELECT * FROM images ORDER BY created_at DESC")
    fun getAllImages(): Flow<List<ImageEntity>>

    @Query("DELETE FROM images WHERE id = :id")
    suspend fun deleteImageById(id: Int)
}
