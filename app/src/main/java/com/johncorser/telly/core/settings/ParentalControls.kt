package com.johncorser.telly.core.settings

import kotlin.random.Random

/**
 * Parental-controls policy over the settings store: a salted-hashed PIN
 * gates locked channel groups (and, optionally, Settings itself) whenever
 * the master toggle is on. Pure logic; PIN dialogs live in the UI slice.
 */
class ParentalControls(
    private val settings: SettingsRepository,
    private val random: Random = Random.Default,
) {
    val isEnabled: Boolean get() = settings.get(TellySettings.PARENTAL_ENABLED)

    val hasPin: Boolean get() = settings.get(TellySettings.PARENTAL_PIN_HASH).isNotEmpty()

    fun setEnabled(enabled: Boolean) {
        settings.set(TellySettings.PARENTAL_ENABLED, enabled)
    }

    /** Stores a fresh salt + hash; the raw PIN is never persisted. */
    fun setPin(pin: String) {
        val salt = PinHasher.newSalt(random)
        settings.set(TellySettings.PARENTAL_PIN_SALT, salt)
        settings.set(TellySettings.PARENTAL_PIN_HASH, PinHasher.hash(pin, salt))
    }

    fun verifyPin(pin: String): Boolean =
        PinHasher.matches(
            pin = pin,
            salt = settings.get(TellySettings.PARENTAL_PIN_SALT),
            expectedHash = settings.get(TellySettings.PARENTAL_PIN_HASH),
        )

    /** True when entering [groupTitle] must be gated behind the PIN. */
    fun isGroupLocked(groupTitle: String): Boolean =
        isEnabled && groupTitle in settings.get(TellySettings.PARENTAL_LOCKED_GROUPS)

    fun setGroupLocked(
        groupTitle: String,
        locked: Boolean,
    ) {
        val current = settings.get(TellySettings.PARENTAL_LOCKED_GROUPS)
        settings.set(TellySettings.PARENTAL_LOCKED_GROUPS, if (locked) current + groupTitle else current - groupTitle)
    }

    /** True when opening Settings must be gated behind the PIN. */
    fun isSettingsLocked(): Boolean = isEnabled && settings.get(TellySettings.PARENTAL_REQUIRE_FOR_SETTINGS)
}
