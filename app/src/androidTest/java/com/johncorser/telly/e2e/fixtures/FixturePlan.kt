package com.johncorser.telly.e2e.fixtures

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Kotlin port of e2e/fixtures/gen-fixtures.mjs: the same 30-channel / 5-group
 * plan over the bundled test-card streams, with playlist.m3u and epg.xml
 * rendered at TEST RUNTIME so "now" programmes always exist. The schedule is
 * deterministic (no RNG) so steps can compute the expected now/next
 * programme for any channel.
 */
object FixturePlan {
    private const val HALF_HOUR_MS = 30 * 60_000L
    private const val PAST_HOURS = 6
    private const val FUTURE_HOURS = 24
    private val durationsMinutes = listOf(30, 45, 60, 75, 90)
    private val suffixes = listOf("", " HD", " +1", " Extra", " 2", " 24")

    private val groups =
        linkedMapOf(
            "News" to GroupPlan("news-one", "News", NEWS_TITLES),
            "Sports" to GroupPlan("sports-arena", "Sports", SPORTS_TITLES),
            "Movies" to GroupPlan("movie-house", "Movie / Drama", MOVIE_TITLES),
            "Kids" to GroupPlan("kids-zone", "Children's / Youth", KIDS_TITLES),
            "Music" to GroupPlan("music-box", "Music / Ballet / Dance", MUSIC_TITLES),
        )

    val channels: List<FixtureChannel> =
        groups.entries.flatMapIndexed { groupIndex, (group, plan) ->
            List(CHANNELS_PER_GROUP) { i ->
                val base = plan.stream.split("-").joinToString(" ") { part -> part.replaceFirstChar(Char::uppercase) }
                FixtureChannel(
                    number = groupIndex * CHANNELS_PER_GROUP + i + 1,
                    tvgId = "${plan.stream}-${i + 1}.fixture",
                    name = base + suffixes[i],
                    group = group,
                    stream = plan.stream,
                )
            }
        }

    fun channelNamed(name: String): FixtureChannel = channels.first { it.name == name }

    /** One deterministic 30-hour schedule per channel around [anchorMs]. */
    fun schedule(anchorMs: Long): List<FixtureProgramme> {
        val windowStart = (anchorMs - PAST_HOURS * 3_600_000L).floorTo(HALF_HOUR_MS)
        val windowEnd = anchorMs + FUTURE_HOURS * 3_600_000L
        return channels.flatMap { channel -> channelSchedule(channel, windowStart, windowEnd) }
    }

    private fun channelSchedule(
        channel: FixtureChannel,
        windowStart: Long,
        windowEnd: Long,
    ): List<FixtureProgramme> {
        val titles = groups.getValue(channel.group).titles
        val out = mutableListOf<FixtureProgramme>()
        var start = windowStart
        var index = 0
        while (start < windowEnd) {
            val minutes = durationsMinutes[(channel.number + index) % durationsMinutes.size]
            val end = start + minutes * 60_000L
            val title = titles[(channel.number * 3 + index) % titles.size]
            out +=
                FixtureProgramme(
                    channelTvgId = channel.tvgId,
                    startMs = start,
                    endMs = end,
                    title = title,
                    subTitle = "$title Special",
                    description = DESCRIPTIONS[(channel.number + index) % DESCRIPTIONS.size],
                )
            start = end
            index += 1
        }
        return out
    }

    fun m3u(baseUrl: String): String =
        buildString {
            append("#EXTM3U url-tvg=\"$baseUrl/epg.xml\"\n")
            channels.forEach { c ->
                append(
                    "#EXTINF:-1 tvg-id=\"${c.tvgId}\" tvg-name=\"${c.name}\" " +
                        "tvg-logo=\"$baseUrl/logos/${c.stream}.png\" group-title=\"${c.group}\",${c.name}\n",
                )
                append("$baseUrl/streams/${c.stream}.ts\n")
            }
        }

    fun xmltv(
        baseUrl: String,
        programmes: List<FixtureProgramme>,
    ): String =
        buildString {
            append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<tv generator-info-name=\"telly-fixtures\">\n")
            channels.forEach { c ->
                append("  <channel id=\"${c.tvgId}\">\n")
                append("    <display-name>${c.name.escapeXml()}</display-name>\n")
                append("    <icon src=\"$baseUrl/logos/${c.stream}.png\" />\n  </channel>\n")
            }
            programmes.forEach { p ->
                append("  <programme start=\"${p.startMs.xmltvStamp()}\" stop=\"${p.endMs.xmltvStamp()}\" ")
                append("channel=\"${p.channelTvgId}\">\n")
                append("    <title lang=\"en\">${p.title.escapeXml()}</title>\n")
                append("    <sub-title lang=\"en\">${p.subTitle.escapeXml()}</sub-title>\n")
                append("    <desc lang=\"en\">${p.description.escapeXml()}</desc>\n")
                append("  </programme>\n")
            }
            append("</tv>\n")
        }

    private fun Long.floorTo(step: Long): Long = this / step * step

    private fun Long.xmltvStamp(): String {
        val format = SimpleDateFormat("yyyyMMddHHmmss", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
        return "${format.format(Date(this))} +0000"
    }

    private fun String.escapeXml(): String =
        replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;")

    private const val CHANNELS_PER_GROUP = 6
}

data class GroupPlan(
    val stream: String,
    val category: String,
    val titles: List<String>,
)

data class FixtureChannel(
    val number: Int,
    val tvgId: String,
    val name: String,
    val group: String,
    val stream: String,
)

data class FixtureProgramme(
    val channelTvgId: String,
    val startMs: Long,
    val endMs: Long,
    val title: String,
    val subTitle: String,
    val description: String,
) {
    /** What ProgramTitle renders for this programme ("Title: Sub-title"). */
    val displayTitle: String get() = "$title: $subTitle"
}

private val NEWS_TITLES =
    listOf(
        "Morning Report",
        "World News Now",
        "The Daily Brief",
        "Politics Tonight",
        "Business Hour",
        "Weather Watch",
        "Global Update",
        "Newsroom Live",
    )
private val SPORTS_TITLES =
    listOf(
        "Match of the Day",
        "Championship Live",
        "Sports Center",
        "The Halftime Show",
        "Boxing Classics",
        "Motorsport Weekly",
        "Tennis Masters",
        "Extreme Games",
    )
private val MOVIE_TITLES =
    listOf(
        "The Long Road Home",
        "Midnight in Berlin",
        "Chasing Shadows",
        "The Last Stand",
        "Summer of '89",
        "Iron Harvest",
        "The Quiet Hour",
        "City of Glass",
    )
private val KIDS_TITLES =
    listOf(
        "Puppy Patrol",
        "Space Cadets",
        "The Magic Treehouse",
        "Dino Explorers",
        "Robot Friends",
        "Fairy Tale Theater",
        "Junior Chefs",
        "Ocean Adventures",
    )
private val MUSIC_TITLES =
    listOf(
        "Top 40 Countdown",
        "Classic Rock Block",
        "Jazz After Dark",
        "Electronic Sessions",
        "Country Roads",
        "Hip Hop Nation",
        "Symphony Hall",
        "Indie Discoveries",
    )
private val DESCRIPTIONS =
    listOf(
        "An in-depth look at the stories shaping our world today, with expert analysis and live reports.",
        "Join our hosts for the latest updates, interviews and highlights you won't want to miss.",
        "A fan-favorite returns with more surprises, bigger moments and unforgettable characters.",
        "Critics call it 'a triumph' - experience the acclaimed hit everyone is talking about.",
        "All-new episode featuring special guests and exclusive behind-the-scenes footage.",
        "The definitive guide to what's happening now, presented by our award-winning team.",
    )
