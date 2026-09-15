package com.johncorser.telly.core.settings

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Wraps [this] with the picked Appearance -> Language locale, or returns
 * it unchanged for "System" (the default = follow the device language).
 * Used by MainActivity's base context and the live compose provider.
 */
fun Context.withAppLocale(languageLabel: String): Context {
    val tag = AppLanguage.tagFor(languageLabel) ?: return this
    val config = Configuration(resources.configuration)
    config.setLocale(Locale.forLanguageTag(tag))
    return createConfigurationContext(config)
}
