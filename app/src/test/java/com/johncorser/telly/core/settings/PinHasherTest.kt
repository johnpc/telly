package com.johncorser.telly.core.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class PinHasherTest {
    @Test
    fun `hashing is deterministic for the same pin and salt`() {
        assertEquals(PinHasher.hash("1234", "salt"), PinHasher.hash("1234", "salt"))
    }

    @Test
    fun `different salts or pins produce different hashes`() {
        assertNotEquals(PinHasher.hash("1234", "a"), PinHasher.hash("1234", "b"))
        assertNotEquals(PinHasher.hash("1234", "a"), PinHasher.hash("4321", "a"))
    }

    @Test
    fun `matches verifies the original pin only`() {
        val salt = PinHasher.newSalt(Random(seed = 7))
        val hash = PinHasher.hash("0912", salt)
        assertTrue(PinHasher.matches("0912", salt, hash))
        assertFalse(PinHasher.matches("0913", salt, hash))
    }

    @Test
    fun `an empty stored hash never matches`() {
        assertFalse(PinHasher.matches("1234", "salt", ""))
    }

    @Test
    fun `salts are 32 hex chars and seeded-random deterministic`() {
        val salt = PinHasher.newSalt(Random(seed = 1))
        assertEquals(32, salt.length)
        assertTrue(salt.matches(Regex("[0-9a-f]{32}")))
        assertEquals(salt, PinHasher.newSalt(Random(seed = 1)))
        assertNotEquals(salt, PinHasher.newSalt(Random(seed = 2)))
    }
}
