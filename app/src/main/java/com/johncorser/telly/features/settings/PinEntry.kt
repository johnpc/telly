package com.johncorser.telly.features.settings

/**
 * Pure state for the picker-style PIN entry (PIN input method "Picker" per
 * capture 67): four digit wheels, LEFT/RIGHT moves, UP/DOWN spins.
 * The captured reference PIN screen itself is premium-locked, so the exact
 * dialog visuals are VERIFY-ON-DEVICE; the digit count follows convention.
 */
data class PinEntry(
    val digits: List<Int> = List(LENGTH) { 0 },
    val cursor: Int = 0,
) {
    val value: String get() = digits.joinToString(separator = "")

    fun up(): PinEntry = spin(+1)

    fun down(): PinEntry = spin(-1)

    fun left(): PinEntry = copy(cursor = (cursor - 1).coerceAtLeast(0))

    fun right(): PinEntry = copy(cursor = (cursor + 1).coerceAtMost(LENGTH - 1))

    private fun spin(delta: Int): PinEntry =
        copy(digits = digits.mapIndexed { i, d -> if (i == cursor) (d + delta + BASE) % BASE else d })

    companion object {
        const val LENGTH = 4
        private const val BASE = 10
    }
}
