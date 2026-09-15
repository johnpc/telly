package com.johncorser.telly.features.catchup

/**
 * Substitutes the community-standard placeholders of a `catchup-source`
 * template. All values are epoch SECONDS ({offset}/{duration} are spans in
 * seconds); both the `{x}` and `${x}` spellings are accepted.
 */
object CatchupTemplate {
    private const val MS_PER_SECOND = 1_000L

    /** Epoch/duration millis to the whole seconds catch-up URLs carry. */
    fun seconds(ms: Long): Long = ms / MS_PER_SECOND

    fun expand(
        template: String,
        startMs: Long,
        endMs: Long,
        nowMs: Long,
    ): String {
        val start = seconds(startMs)
        val now = seconds(nowMs)
        val values =
            mapOf(
                "utc" to start,
                "start" to start,
                "lutc" to now,
                "now" to now,
                "timestamp" to now,
                "offset" to now - start,
                "duration" to seconds(endMs - startMs),
            )
        // "${x}" first: replacing the inner "{x}" first would strand the "$".
        return values.entries.fold(template) { acc, (key, value) ->
            acc
                .replace("\${$key}", value.toString())
                .replace("{$key}", value.toString())
        }
    }
}
