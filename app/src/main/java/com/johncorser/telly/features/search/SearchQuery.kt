package com.johncorser.telly.features.search

/**
 * Pure query preparation for the search screen (catalogue §4): trims and
 * collapses whitespace, then builds SQLite LIKE patterns with `%`/`_`/`\`
 * escaped so user input can never act as a wildcard. Channel numbers match
 * by prefix, but only when the whole query is digits — "2" finds channel
 * 2x, "news 2" never does.
 */
object SearchQuery {
    /** A LIKE pattern that can only match the empty string — never a number. */
    private const val MATCH_NOTHING = ""

    fun normalize(raw: String): String = raw.trim().replace(WHITESPACE, " ")

    /** Case-insensitive name/title substring pattern (SQLite LIKE semantics). */
    fun nameLike(query: String): String = "%${escape(query)}%"

    /** Channel-number prefix pattern; inert for non-numeric queries. */
    fun numberLike(query: String): String =
        if (query.isNotEmpty() && query.all(Char::isDigit)) "${escape(query)}%" else MATCH_NOTHING

    private fun escape(value: String): String =
        value
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_")

    private val WHITESPACE = Regex("\\s+")
}
