package com.babybloom.data.repository

import com.babybloom.data.local.dao.UserDao
import com.babybloom.data.local.entity.UserEntity
import com.babybloom.util.PasswordHasher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalPasswordStoreTest {
    private val legacyHash = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8"

    @Test
    fun successfulLegacyVerificationUpgradesOnlyThePassword() = runTest {
        val original = UserEntity(id = 1, name = "Parent", email = "parent@example.com", passwordHash = legacyHash)
        val userDao = MemoryUserDao(original)

        assertTrue(LocalPasswordStore(userDao).verify(1, "password"))
        val upgraded = requireNotNull(userDao.user)
        assertFalse(PasswordHasher.needsUpgrade(upgraded.passwordHash))
        assertTrue(PasswordHasher.verify("password", upgraded.passwordHash))
        assertEquals(original, upgraded.copy(passwordHash = original.passwordHash))
    }

    @Test
    fun incorrectPasswordDoesNotModifyTheAccount() = runTest {
        val userDao = MemoryUserDao(UserEntity(id = 1, name = "Parent", email = "parent@example.com", passwordHash = legacyHash))

        assertFalse(LocalPasswordStore(userDao).verify(1, "incorrect"))
        assertEquals(legacyHash, userDao.user?.passwordHash)
        assertEquals(0, userDao.passwordWrites)
    }

    @Test
    fun changingPasswordRequiresTheExistingPassword() = runTest {
        val userDao = MemoryUserDao(UserEntity(id = 1, name = "Parent", email = "parent@example.com", passwordHash = legacyHash))
        val passwordStore = LocalPasswordStore(userDao)

        assertEquals(PasswordChangeResult.INVALID_CREDENTIALS, passwordStore.changePassword(1, "", "NewPassword!9"))
        assertEquals(PasswordChangeResult.INVALID_CREDENTIALS, passwordStore.changePassword(1, "incorrect", "NewPassword!9"))
        assertEquals(legacyHash, userDao.user?.passwordHash)
        assertEquals(0, userDao.passwordWrites)
    }

    @Test
    fun passwordChangeAcceptsTheNewPasswordAndRejectsTheOldOne() = runTest {
        val userDao = MemoryUserDao(UserEntity(id = 1, name = "Parent", email = "parent@example.com", passwordHash = legacyHash))
        val passwordStore = LocalPasswordStore(userDao)

        assertEquals(PasswordChangeResult.SUCCESS, passwordStore.changePassword(1, "password", "NewPassword!9"))
        assertTrue(passwordStore.verify(1, "NewPassword!9"))
        assertFalse(passwordStore.verify(1, "password"))
        assertEquals(PasswordChangeResult.SAME_PASSWORD, passwordStore.changePassword(1, "NewPassword!9", "NewPassword!9"))
        assertEquals(1, userDao.passwordWrites)
    }

    @Test
    fun concurrentPasswordChangeIsNotOverwrittenByLegacyUpgrade() = runTest {
        val userDao = MemoryUserDao(UserEntity(id = 1, name = "Parent", email = "parent@example.com", passwordHash = legacyHash))
        userDao.rejectPasswordUpdate = true

        assertFalse(LocalPasswordStore(userDao).verify(1, "password"))
        assertEquals(0, userDao.passwordWrites)
    }

    @Test
    fun deletedAccountsCannotVerifyOrChangePasswords() = runTest {
        val passwordStore = LocalPasswordStore(MemoryUserDao(null))

        assertFalse(passwordStore.verify(1, "password"))
        assertEquals(PasswordChangeResult.INVALID_CREDENTIALS, passwordStore.changePassword(1, "password", "NewPassword!9"))
    }

    private class MemoryUserDao(var user: UserEntity?) : UserDao {
        var passwordWrites = 0
        var rejectPasswordUpdate = false

        override suspend fun insert(user: UserEntity): Long {
            this.user = user
            return user.id
        }

        override suspend fun update(user: UserEntity) {
            this.user = user
        }

        override suspend fun updatePasswordHash(userId: Long, previousHash: String, newHash: String): Int {
            val current = user ?: return 0
            if (rejectPasswordUpdate || current.id != userId || current.passwordHash != previousHash) return 0
            user = current.copy(passwordHash = newHash)
            passwordWrites++
            return 1
        }

        override suspend fun delete(user: UserEntity) { deleteUser(user.id) }
        override suspend fun getByEmail(email: String): UserEntity? = user?.takeIf { it.email == email }
        override suspend fun getById(id: Long): UserEntity? = user?.takeIf { it.id == id }
        override suspend fun emailExists(email: String): Boolean = getByEmail(email) != null
        override suspend fun findByEmail(email: String): UserEntity? = getByEmail(email)
        override suspend fun findById(id: Long): UserEntity? = getById(id)
        override suspend fun deleteUser(id: Long) {
            if (user?.id == id) user = null
        }
    }
}
