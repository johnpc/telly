package com.johncorser.telly.core.settings

import java.security.MessageDigest
import kotlin.random.Random

/** Salted SHA-256 hashing for the parental-controls PIN. */
object PinHasher {
    private const val SALT_BYTES = 16
    private const val HEX_RADIX = 16
    private const val BYTE_MASK = 0xFF

    fun newSalt(random: Random = Random.Default): String = random.nextBytes(SALT_BYTES).toHex()

    fun hash(
        pin: String,
        salt: String,
    ): String =
        MessageDigest
            .getInstance("SHA-256")
            .digest("$salt:$pin".toByteArray(Charsets.UTF_8))
            .toHex()

    fun matches(
        pin: String,
        salt: String,
        expectedHash: String,
    ): Boolean = expectedHash.isNotEmpty() && hash(pin, salt) == expectedHash

    private fun ByteArray.toHex(): String =
        joinToString(separator = "") { byte ->
            (byte.toInt() and BYTE_MASK).toString(HEX_RADIX).padStart(2, '0')
        }
}
