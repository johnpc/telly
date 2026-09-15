package com.johncorser.telly.features.panel

import com.johncorser.telly.features.playlist.db.ChannelEntity

/**
 * One channel row of the panel, pre-formatted for rendering: number, channel,
 * the airing programme and its progress. Channel numbers restart from 1
 * inside a group (catalogue §2, capture 74); "All channels" keeps the
 * playlist numbering.
 */
data class PanelRow(
    val channel: ChannelEntity,
    val displayNumber: Int,
    val nowTitle: String?,
    val nowRange: String?,
    /** Raw airing span, for actions that persist the programme (My list). */
    val nowStartMs: Long? = null,
    val nowEndMs: Long? = null,
    val remaining: String?,
    val description: String?,
    val nextTitle: String?,
    val progressPermille: Int,
)
