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

    /**
     * True when entering [groupTitle] to WATCH must be gated behind the PIN.
     * "Don't require for channels only" exempts watching from the PIN (the
     * settings gates below still apply) — TiviMate's captured semantics.
     */
    fun isGroupLocked(groupTitle: String): Boolean =
        isEnabled &&
            !settings.get(TellySettings.PARENTAL_CHANNELS_ONLY) &&
            groupTitle in settings.get(TellySettings.PARENTAL_LOCKED_GROUPS)

    fun setGroupLocked(
        groupTitle: String,
        locked: Boolean,
    ) {
        val current = settings.get(TellySettings.PARENTAL_LOCKED_GROUPS)
        settings.set(TellySettings.PARENTAL_LOCKED_GROUPS, if (locked) current + groupTitle else current - groupTitle)
    }

    /** True when opening Settings must be gated behind the PIN. */
    fun isSettingsLocked(): Boolean = isEnabled && settings.get(TellySettings.PARENTAL_REQUIRE_FOR_SETTINGS)

    /** True when opening Settings -> Playlists must be gated behind the PIN. */
    fun isPlaylistsLocked(): Boolean = isEnabled && settings.get(TellySettings.PARENTAL_REQUIRE_FOR_PLAYLISTS)

    /** True when PIN prompts use the masked keyboard entry, not the wheels. */
    val usesKeyboardPin: Boolean
        get() = settings.get(TellySettings.PARENTAL_PIN_INPUT_METHOD).equals("Keyboard", ignoreCase = true)

    /**
     * True when every gated surface must re-prompt after an unlock — the
     * captured default. "Don't require PIN after unlocking" set to
     * [RELOCK_UNTIL_RESTART] keeps one successful unlock for the session.
     */
    fun relocksAfterUnlock(): Boolean = settings.get(TellySettings.PARENTAL_RELOCK) != RELOCK_UNTIL_RESTART

    companion object {
        /** The relock picker option that keeps an unlock until app restart. */
        const val RELOCK_UNTIL_RESTART = "Until app restart"
    }
}
