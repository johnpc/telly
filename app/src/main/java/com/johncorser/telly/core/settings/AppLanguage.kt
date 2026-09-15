package com.johncorser.telly.core.settings

/**
 * Appearance -> Language: picker label -> BCP-47 tag. "System" (the
 * default = today's behavior) returns null, meaning no locale override —
 * the app follows the device language.
 */
object AppLanguage {
    val OPTIONS = listOf("System", "English", "Español", "Français", "Deutsch", "Português", "Italiano")

    fun tagFor(label: String): String? =
        when (label) {
            "English" -> "en"
            "Español" -> "es"
            "Français" -> "fr"
            "Deutsch" -> "de"
            "Português" -> "pt"
            "Italiano" -> "it"
            else -> null
        }
}
