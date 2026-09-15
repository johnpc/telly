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
        return channels.flatMap { channel -> channelSchedule(channel, windowStart, windowEnd) } +
            channels.filter { it.tvgId in deepPastTvgIds }.flatMap { deepPastSchedule(it, windowStart) }
    }

    /** Backwards from the main window so existing now/next phases never shift. */
    private fun deepPastSchedule(
        channel: FixtureChannel,
        windowStart: Long,
    ): List<FixtureProgramme> {
        val titles = groups.getValue(channel.group).titles
        val floor = windowStart - (DEEP_PAST_HOURS - PAST_HOURS) * 3_600_000L
        val out = mutableListOf<FixtureProgramme>()
        var end = windowStart
        var index = 1
        while (end > floor) {
            val start = end - durationsMinutes[(channel.number + index) % durationsMinutes.size] * 60_000L
            val title = "Past " + titles[(channel.number * 3 + index) % titles.size]
            val description = DESCRIPTIONS[(channel.number + index) % DESCRIPTIONS.size]
            out += FixtureProgramme(channel.tvgId, start, end, title, "$title Special", description)
            end = start
            index += 1
        }
        return out
    }

    // One channel ships WITHOUT EPG so "No information" cells are real:
    // Sports Arena (7) is visible in the guide's initial 7 rows but outside
    // the news family the search scenarios assert complete cards for.
    val noEpgTvgIds: Set<String> = setOf("sports-arena-1.fixture")

    // Catch-up e2e: News One is the ONLY catch-up-enabled channel (standard
    // #EXTINF attributes; the template resolves to the same fixture .ts —
    // query params are ignored by the servers). News One + News One HD also
    // carry EPG a full extra day into the past so a −24 h guide day jump
    // lands on real "Past …" programmes; News One HD stays catch-up-free so
    // the past dropdown behavior is assertable. Mirrors gen-fixtures.mjs 1:1.
    const val CATCHUP_TVG_ID = "news-one-1.fixture"
    const val CATCHUP_DAYS = 2
    private val deepPastTvgIds = setOf("news-one-1.fixture", "news-one-2.fixture")
    private const val DEEP_PAST_HOURS = 30

    // epg-alt.xml (custom-EPG-source scenarios): covers ONE channel epg.xml
    // misses (Sports Arena) plus ONE it also covers (News One) with distinct
    // "Alt "-prefixed titles, so merge and per-channel precedence are both
    // observable. Mirrors e2e/fixtures/gen-fixtures.mjs 1:1.
    private val altTvgIds = setOf("news-one-1.fixture", "sports-arena-1.fixture")

    /** The deterministic schedule served at /epg-alt.xml around [anchorMs]. */
    fun altSchedule(anchorMs: Long): List<FixtureProgramme> {
        val windowStart = (anchorMs - PAST_HOURS * 3_600_000L).floorTo(HALF_HOUR_MS)
        val windowEnd = anchorMs + FUTURE_HOURS * 3_600_000L
        return channels
            .filter { it.tvgId in altTvgIds }
            .flatMap { channel -> altChannelSchedule(channel, windowStart, windowEnd) }
    }

    private fun altChannelSchedule(
        channel: FixtureChannel,
        windowStart: Long,
        windowEnd: Long,
    ): List<FixtureProgramme> {
        val titles = groups.getValue(channel.group).titles
        val out = mutableListOf<FixtureProgramme>()
        var start = windowStart
        var index = 0
        while (start < windowEnd) {
            val end = start + durationsMinutes[(channel.number + index) % durationsMinutes.size] * 60_000L
            val title = "Alt " + titles[(channel.number * 3 + index) % titles.size]
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

    private fun channelSchedule(
        channel: FixtureChannel,
        windowStart: Long,
        windowEnd: Long,
    ): List<FixtureProgramme> {
        if (channel.tvgId in noEpgTvgIds) return emptyList()
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

    // VOD entries (mp4 stream URLs -> classified as Movies, never channels);
    // mirrors e2e/fixtures/gen-fixtures.mjs VOD_ITEMS 1:1.
    val vodItems: List<FixtureVodItem> =
        listOf(
            FixtureVodItem(tvgId = "vod-big-buck.fixture", name = "Big Buck Bunny", group = "Cinema"),
            FixtureVodItem(tvgId = "vod-sintel.fixture", name = "Sintel", group = "Cinema"),
        )

    fun m3u(baseUrl: String): String =
        buildString {
            append("#EXTM3U url-tvg=\"$baseUrl/epg.xml\"\n")
            channels.forEach { c ->
                append(
                    "#EXTINF:-1 tvg-id=\"${c.tvgId}\" tvg-name=\"${c.name}\" " +
                        "tvg-logo=\"$baseUrl/logos/${c.stream}.png\"${catchupAttributes(c, baseUrl)} " +
                        "group-title=\"${c.group}\",${c.name}\n",
                )
                append("$baseUrl/streams/${c.stream}.ts\n")
            }
            vodItems.forEach { v ->
                append(
                    "#EXTINF:-1 tvg-id=\"${v.tvgId}\" tvg-name=\"${v.name}\" " +
                        "tvg-logo=\"$baseUrl/logos/movie-house.png\" group-title=\"${v.group}\",${v.name}\n",
                )
                append("$baseUrl/streams/vod-sample.mp4\n")
            }
        }

    private fun catchupAttributes(
        c: FixtureChannel,
        baseUrl: String,
    ): String =
        if (c.tvgId == CATCHUP_TVG_ID) {
            " catchup=\"default\" catchup-source=\"$baseUrl/streams/${c.stream}.ts" +
                "?utc={utc}&lutc={lutc}&d={duration}\" catchup-days=\"$CATCHUP_DAYS\""
        } else {
            ""
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

data class FixtureVodItem(
    val tvgId: String,
    val name: String,
    val group: String,
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
