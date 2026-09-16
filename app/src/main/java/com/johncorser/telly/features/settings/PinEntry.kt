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

    /**
     * OK: hands the dialed PIN to [submit] and blanks the wheels — a
     * prompt kept open by a rejected PIN must start over from 0000 with
     * the first wheel active, not from the stale wrong digits with the
     * cursor parked on the last wheel.
     */
    fun commit(submit: (String) -> Unit): PinEntry {
        submit(value)
        return PinEntry()
    }

    private fun spin(delta: Int): PinEntry =
        copy(digits = digits.mapIndexed { i, d -> if (i == cursor) (d + delta + BASE) % BASE else d })

    companion object {
        const val LENGTH = 4
        private const val BASE = 10
    }
}

/**
 * Pure rules for the keyboard-style PIN entry (PIN input method
 * "Keyboard"): a masked IME text field that accepts digits only and
 * commits as soon as the fourth digit lands. Same verify semantics as the
 * wheel — the committed 4-digit string goes through the same PIN check.
 */
object PinKeyboard {
    fun sanitize(raw: String): String = raw.filter(Char::isDigit).take(PinEntry.LENGTH)

    fun isComplete(value: String): Boolean = value.length == PinEntry.LENGTH

    /**
     * Commit hands the PIN to [submit] and clears the field — a full field
     * left behind by a rejected PIN could never complete again (sanitize
     * caps at four digits), wedging a kept-open prompt for good.
     */
    fun commit(
        value: String,
        submit: (String) -> Unit,
    ): String {
        submit(value)
        return ""
    }
}
