package com.johncorser.telly.core.design

/**
 * Accent choices behind Settings -> Appearance -> "Color theme" (captured
 * value "Dark  •  Blue"; the premium-locked picker list itself was not
 * capturable, so the set is the Material-500 palette — VERIFY-ON-DEVICE).
 * The default Blue is the sampled TiviMate accent #2196F3.
 */
object AccentPalette {
    val DEFAULT = "Blue" to 0xFF2196F3

    val OPTIONS: List<Pair<String, Long>> =
        listOf(
            DEFAULT,
            "Red" to 0xFFF44336,
            "Pink" to 0xFFE91E63,
            "Purple" to 0xFF9C27B0,
            "Indigo" to 0xFF3F51B5,
            "Cyan" to 0xFF00BCD4,
            "Teal" to 0xFF009688,
            "Green" to 0xFF4CAF50,
            "Amber" to 0xFFFFC107,
            "Orange" to 0xFFFF9800,
        )

    fun argbFor(name: String): Long = OPTIONS.firstOrNull { it.first == name }?.second ?: DEFAULT.second

    fun names(): List<String> = OPTIONS.map { it.first }
}
