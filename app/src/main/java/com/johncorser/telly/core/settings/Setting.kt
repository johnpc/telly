package com.johncorser.telly.core.settings

/**
 * A typed settings key: storage key, default, and the string codec used by
 * the [KeyValueStore]. Instances live in [TellySettings].
 */
class Setting<T>(
    val key: String,
    val default: T,
    val encode: (T) -> String,
    val decode: (String) -> T,
)

fun boolSetting(
    key: String,
    default: Boolean,
): Setting<Boolean> = Setting(key, default, Boolean::toString, String::toBooleanStrictOrNull.then(default))

fun intSetting(
    key: String,
    default: Int,
): Setting<Int> = Setting(key, default, Int::toString, { raw: String -> raw.toIntOrNull() }.then(default))

fun stringSetting(
    key: String,
    default: String,
): Setting<String> = Setting(key, default, { it }, { it })

/** Sets are stored newline-joined; names never contain newlines in practice. */
fun stringSetSetting(key: String): Setting<Set<String>> =
    Setting(
        key = key,
        default = emptySet(),
        encode = { it.joinToString(separator = "\n") },
        decode = { raw -> raw.lines().filter(String::isNotEmpty).toSet() },
    )

private fun <T> ((String) -> T?).then(default: T): (String) -> T = { this(it) ?: default }
