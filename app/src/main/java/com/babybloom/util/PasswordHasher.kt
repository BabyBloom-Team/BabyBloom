package com.babybloom.util

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {
    private const val FORMAT = "pbkdf2-sha256"
    private const val ITERATIONS = 600_000
    private const val SALT_BYTES = 16
    private const val HASH_BYTES = 32
    private val legacyPattern = Regex("[0-9a-f]{64}")

    fun hash(password: String): String {
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val derived = derive(password, salt)
        val encoder = Base64.getEncoder()
        return "$FORMAT:$ITERATIONS:${encoder.encodeToString(salt)}:${encoder.encodeToString(derived)}"
    }

    fun verify(password: String, storedHash: String): Boolean {
        if (needsUpgrade(storedHash)) {
            return MessageDigest.isEqual(
                HashUtils.sha256(password).toByteArray(Charsets.UTF_8),
                storedHash.toByteArray(Charsets.UTF_8)
            )
        }
        if (storedHash.length > 256) return false
        val parts = storedHash.split(':')
        if (parts.size != 4 || parts[0] != FORMAT || parts[1] != ITERATIONS.toString()) return false
        val salt: ByteArray
        val expected: ByteArray
        try {
            salt = Base64.getDecoder().decode(parts[2])
            expected = Base64.getDecoder().decode(parts[3])
        } catch (_: IllegalArgumentException) {
            return false
        }
        if (salt.size != SALT_BYTES || expected.size != HASH_BYTES) return false
        return MessageDigest.isEqual(derive(password, salt), expected)
    }

    fun needsUpgrade(storedHash: String): Boolean = legacyPattern.matches(storedHash)

    private fun derive(password: String, salt: ByteArray): ByteArray {
        val characters = password.toCharArray()
        val specification = PBEKeySpec(characters, salt, ITERATIONS, HASH_BYTES * 8)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(specification).encoded
        } finally {
            specification.clearPassword()
            characters.fill('\u0000')
        }
    }
}
