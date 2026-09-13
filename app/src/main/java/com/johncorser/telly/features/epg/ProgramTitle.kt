package com.johncorser.telly.features.epg

import com.johncorser.telly.features.epg.db.ProgramDetails

/**
 * THE programme-title formatter (round3 P0 item 1): every surface that names
 * a programme — info/zap overlays, panel rows, detail cards, menu headers —
 * renders through here. TiviMate 5.2.0 form (round3-ref 02/04/05 uidumps):
 * `Title: Sub-title. S1 E10`, dropping the parts that are missing
 * ("Global Update: Global Update Special", "Global Update. S1 E10",
 * "Global Update").
 */
object ProgramTitle {
    fun of(details: ProgramDetails): String {
        val subTitle = details.subTitle?.takeIf { it.isNotBlank() }
        val episode = details.episode?.takeIf { it.isNotBlank() }
        val head = subTitle?.let { "${details.title}: $it" } ?: details.title
        return episode?.let { "$head. $it" } ?: head
    }
}
