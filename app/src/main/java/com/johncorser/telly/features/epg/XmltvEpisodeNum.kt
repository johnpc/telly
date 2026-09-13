package com.johncorser.telly.features.epg

/**
 * Converts an XMLTV `<episode-num>` element into TiviMate's display form.
 * xmltv_ns numbers are zero-based "season.episode.part" (each field optional,
 * possibly "index/total"): `0.9.` renders as "S1 E10" (round3-ref 02/04).
 * Other systems (onscreen et al.) already carry display text and pass through.
 */
object XmltvEpisodeNum {
    private const val XMLTV_NS = "xmltv_ns"
    private const val SEASON_FIELD = 0
    private const val EPISODE_FIELD = 1

    /** The renderable episode string, or null when nothing displayable exists. */
    fun display(
        system: String?,
        text: String?,
    ): String? =
        text?.trim()?.takeIf { it.isNotEmpty() }?.let { value ->
            if (system?.trim() == XMLTV_NS) fromXmltvNs(value) else value
        }

    private fun fromXmltvNs(value: String): String? {
        val fields = value.split('.')
        val season = oneBased(fields.getOrNull(SEASON_FIELD))
        val episode = oneBased(fields.getOrNull(EPISODE_FIELD))
        return listOfNotNull(season?.let { "S$it" }, episode?.let { "E$it" })
            .joinToString(" ")
            .ifEmpty { null }
    }

    /** "9/20" or " 9 " -> 10 (xmltv_ns is zero-based); junk -> null. */
    private fun oneBased(field: String?): Int? =
        field
            ?.substringBefore('/')
            ?.trim()
            ?.toIntOrNull()
            ?.takeIf { it >= 0 }
            ?.plus(1)
}
