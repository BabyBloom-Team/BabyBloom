package com.babybloom.data.repository

import com.babybloom.data.local.dao.UserDao
import com.babybloom.util.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class PasswordChangeResult {
    SUCCESS,
    INVALID_CREDENTIALS,
    SAME_PASSWORD
}

class LocalPasswordStore @Inject constructor(private val userDao: UserDao) {
    suspend fun verify(userId: Long, password: String): Boolean = withContext(Dispatchers.Default) {
        val user = userDao.getById(userId) ?: return@withContext false
        if (!PasswordHasher.verify(password, user.passwordHash)) return@withContext false
        if (PasswordHasher.needsUpgrade(user.passwordHash)) {
            return@withContext userDao.updatePasswordHash(
                userId, user.passwordHash, PasswordHasher.hash(password)
            ) == 1
        }
        true
    }

    suspend fun changePassword(
        userId: Long,
        currentPassword: String,
        newPassword: String
    ): PasswordChangeResult = withContext(Dispatchers.Default) {
        val user = userDao.getById(userId)
            ?: return@withContext PasswordChangeResult.INVALID_CREDENTIALS
        if (!PasswordHasher.verify(currentPassword, user.passwordHash)) {
            return@withContext PasswordChangeResult.INVALID_CREDENTIALS
        }
        if (PasswordHasher.verify(newPassword, user.passwordHash)) {
            return@withContext PasswordChangeResult.SAME_PASSWORD
        }
        val updated = userDao.updatePasswordHash(
            userId, user.passwordHash, PasswordHasher.hash(newPassword)
        )
        if (updated == 1) PasswordChangeResult.SUCCESS else PasswordChangeResult.INVALID_CREDENTIALS
    }
}
