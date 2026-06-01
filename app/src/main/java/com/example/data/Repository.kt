package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val dao: AppDao) {
    // --- USERS ---
    val allUsers: Flow<List<UserEntity>> = dao.getAllUsers()

    suspend fun insertUser(user: UserEntity): Long {
        return dao.insertUser(user)
    }

    suspend fun updateUser(user: UserEntity) {
        dao.updateUser(user)
    }

    suspend fun getUserByCnic(cnic: String): UserEntity? {
        return dao.getUserByCnic(cnic)
    }

    suspend fun getUserById(id: Int): UserEntity? {
        return dao.getUserById(id)
    }

    suspend fun deleteUserById(id: Int) {
        dao.deleteUserById(id)
    }

    // --- TRADERS ---
    val allTraders: Flow<List<TraderEntity>> = dao.getAllTraders()

    suspend fun insertTrader(trader: TraderEntity): Long {
        return dao.insertTrader(trader)
    }

    suspend fun updateTrader(trader: TraderEntity) {
        dao.updateTrader(trader)
    }

    suspend fun updateTraderStatus(id: Int, status: String) {
        dao.updateTraderStatus(id, status)
    }

    suspend fun updateTraderRating(id: Int, ratingAvg: Double, reviewCount: Int) {
        dao.updateTraderRating(id, ratingAvg, reviewCount)
    }

    suspend fun deleteTraderById(id: Int) {
        dao.deleteTraderById(id)
    }

    // --- RATES ---
    val allRates: Flow<List<RateEntity>> = dao.getAllRates()

    suspend fun insertRate(rate: RateEntity): Long {
        return dao.insertRate(rate)
    }

    suspend fun deleteRateById(id: Int) {
        dao.deleteRateById(id)
    }

    // --- REVIEWS ---
    val allReviews: Flow<List<ReviewEntity>> = dao.getAllReviews()

    fun getReviewsForTrader(traderId: Int): Flow<List<ReviewEntity>> {
        return dao.getReviewsForTrader(traderId)
    }

    suspend fun insertReview(review: ReviewEntity): Long {
        return dao.insertReview(review)
    }

    // --- NEWS ---
    val allNews: Flow<List<NewsEntity>> = dao.getAllNews()

    suspend fun insertNews(news: NewsEntity): Long {
        return dao.insertNews(news)
    }

    suspend fun deleteNewsById(id: Int) {
        dao.deleteNewsById(id)
    }

    // --- NOTIFICATIONS ---
    fun getNotificationsForUser(userId: Int): Flow<List<NotificationEntity>> {
        return dao.getNotificationsForUser(userId)
    }

    suspend fun insertNotification(notification: NotificationEntity): Long {
        return dao.insertNotification(notification)
    }

    suspend fun markAllNotificationsAsRead(userId: Int) {
        dao.markAllNotificationsAsRead(userId)
    }

    suspend fun markNotificationAsRead(id: Int) {
        dao.markNotificationAsRead(id)
    }

    // --- IMAGES ---
    val allImages: Flow<List<ImageEntity>> = dao.getAllImages()

    suspend fun insertImage(image: ImageEntity): Long {
        return dao.insertImage(image)
    }

    suspend fun deleteImageById(id: Int) {
        dao.deleteImageById(id)
    }
}
