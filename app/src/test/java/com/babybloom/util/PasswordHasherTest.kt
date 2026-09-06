package com.babybloom.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {
    @Test
    fun newHashesAreSaltedAndVerifyOnlyTheCorrectPassword() {
        val firstHash = PasswordHasher.hash("ParentPassword!9")
        val secondHash = PasswordHasher.hash("ParentPassword!9")

        assertNotEquals(firstHash, secondHash)
        assertFalse(PasswordHasher.needsUpgrade(firstHash))
        assertTrue(PasswordHasher.verify("ParentPassword!9", firstHash))
        assertFalse(PasswordHasher.verify("WrongPassword!9", firstHash))
    }

    @Test
    fun legacyPasswordsRemainVerifiableUntilTheyAreUpgraded() {
        val legacyHash = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8"

        assertTrue(PasswordHasher.needsUpgrade(legacyHash))
        assertTrue(PasswordHasher.verify("password", legacyHash))
        assertFalse(PasswordHasher.verify("incorrect", legacyHash))
    }

    @Test
    fun multibytePasswordsAreNotTruncatedAtSeventyTwoBytes() {
        val sharedPrefix = "أ".repeat(40)
        val storedHash = PasswordHasher.hash(sharedPrefix + "A")

        assertTrue(PasswordHasher.verify(sharedPrefix + "A", storedHash))
        assertFalse(PasswordHasher.verify(sharedPrefix + "B", storedHash))
    }

    @Test
    fun malformedAndUnsupportedHashesFailClosed() {
        val invalidHashes = listOf(
            "",
            "not-a-hash",
            "pbkdf2-sha256:2147483647:AAAA:AAAA",
            "pbkdf2-sha256:600000:!:!",
            "pbkdf2-sha256:600000:AAAA:AAAA",
            "x".repeat(257)
        )

        invalidHashes.forEach { assertFalse(PasswordHasher.verify("password", it)) }
    }
}
