package com.johncorser.telly.features.epg

import com.johncorser.telly.features.epg.db.ProgramDetails

/** A `<channel>` element from an XMLTV document. */
data class XmltvChannel(
    val id: String,
    val displayName: String? = null,
    val iconUrl: String? = null,
)

/** A `<programme>` element with its start/stop resolved to epoch millis. */
data class XmltvProgram(
    val channelId: String,
    val startMs: Long,
    val endMs: Long,
    val details: ProgramDetails,
)

/** Everything parsed from one XMLTV document. */
data class XmltvDocument(
    val channels: List<XmltvChannel> = emptyList(),
    val programs: List<XmltvProgram> = emptyList(),
)
