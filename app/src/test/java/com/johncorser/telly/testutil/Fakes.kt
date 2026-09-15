package com.johncorser.telly.testutil

import com.johncorser.telly.core.kv.KeyValueStore
import com.johncorser.telly.features.epg.EpgRepository
import com.johncorser.telly.features.epg.db.ProgramDao
import com.johncorser.telly.features.epg.db.ProgramDetails
import com.johncorser.telly.features.epg.db.ProgramEntity
import com.johncorser.telly.features.history.db.WatchHistoryDao
import com.johncorser.telly.features.history.db.WatchHistoryEntity
import com.johncorser.telly.features.player.PlayerEngine
import com.johncorser.telly.features.player.PlayerState
import com.johncorser.telly.features.player.VideoDetails
import com.johncorser.telly.features.playlist.db.ChannelDao
import com.johncorser.telly.features.playlist.db.ChannelEntity
import com.johncorser.telly.features.playlist.db.ChannelFlags
import com.johncorser.telly.features.playlist.db.ChannelGroupCount
import com.johncorser.telly.features.playlist.db.ChannelSource
import com.johncorser.telly.features.search.db.SearchDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory [ChannelDao] so ViewModel tests stay plain-JVM and fast. */
class FakeChannelDao(
    initial: List<ChannelEntity> = emptyList(),
) : ChannelDao {
    val channels = MutableStateFlow(initial)

    override fun observeForPlaylist(playlistId: Long): Flow<List<ChannelEntity>> =
        channels.map { list -> list.filter { it.playlistId == playlistId }.sortedBy { it.number } }

    override fun observeByGroup(
        playlistId: Long,
        groupTitle: String,
    ): Flow<List<ChannelEntity>> =
        channels.map { list ->
            list
                .filter { it.playlistId == playlistId && it.source.groupTitle == groupTitle && !it.flags.hidden }
                .sortedBy { it.number }
        }

    override fun observeGroups(playlistId: Long): Flow<List<ChannelGroupCount>> =
        channels.map { list ->
            list
                .filter { it.playlistId == playlistId && !it.flags.hidden }
                .groupBy { it.source.groupTitle }
                .map { (group, members) -> ChannelGroupCount(group, members.size) }
        }

    override fun observeVisible(): Flow<List<ChannelEntity>> =
        channels.map { list -> list.filter { !it.flags.hidden }.sortedBy { it.number } }

    override suspend fun totalCount(): Int = channels.value.size

    override suspend fun totalGroupCount(): Int = channels.value.mapNotNull { it.source.groupTitle }.distinct().size

    override suspend fun forPlaylist(playlistId: Long): List<ChannelEntity> =
        channels.value.filter { it.playlistId == playlistId }.sortedBy { it.sortIndex }

    override suspend fun deleteForPlaylist(playlistId: Long) {
        channels.update { list -> list.filterNot { it.playlistId == playlistId } }
    }

    override suspend fun insertAll(channels: List<ChannelEntity>) {
        this.channels.update { it + channels }
    }

    override suspend fun update(channel: ChannelEntity) {
        channels.update { list -> list.map { if (it.id == channel.id) channel else it } }
    }
}

/** In-memory [ProgramDao]; only the observing queries matter to these tests. */
class FakeProgramDao(
    initial: List<ProgramEntity> = emptyList(),
) : ProgramDao {
    val programs = MutableStateFlow(initial)

    override fun observeWindow(
        tvgIds: List<String>,
        fromMs: Long,
        toMs: Long,
    ): Flow<List<ProgramEntity>> =
        programs.map { list ->
            list
                .filter { it.channelTvgId in tvgIds && it.endMs > fromMs && it.startMs < toMs }
                .sortedWith(compareBy({ it.channelTvgId }, { it.startMs }))
        }

    override fun observeAiringOrUpcoming(
        tvgIds: List<String>,
        atMs: Long,
    ): Flow<List<ProgramEntity>> =
        programs.map { list ->
            list
                .filter { it.channelTvgId in tvgIds && it.endMs > atMs }
                .sortedWith(compareBy({ it.channelTvgId }, { it.startMs }))
        }

    override suspend fun upsertAll(programs: List<ProgramEntity>) {
        this.programs.update { it + programs }
    }

    override suspend fun deleteFor(tvgIds: List<String>) {
        programs.update { list -> list.filterNot { it.channelTvgId in tvgIds } }
    }

    override suspend fun deleteEndedBefore(beforeMs: Long) {
        programs.update { list -> list.filterNot { it.endMs < beforeMs } }
    }

    override suspend fun count(): Int = programs.value.size
}

/** In-memory [SearchDao] over the channel/programme fakes. */
class FakeSearchDao(
    private val channelDao: FakeChannelDao,
    private val programDao: FakeProgramDao,
) : SearchDao {
    override suspend fun channels(
        nameLike: String,
        numberLike: String,
    ): List<ChannelEntity> =
        channelDao.channels.value
            .filter { !it.flags.hidden }
            // The real DAO matches `' ' || name` so patterns anchor to word starts.
            .filter { sqlLike(nameLike, " " + it.source.name) || sqlLike(numberLike, it.number.toString()) }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.source.name })

    override suspend fun programs(
        titleLike: String,
        atMs: Long,
        limit: Int,
    ): List<ProgramEntity> =
        programDao.programs.value
            .filter { it.endMs > atMs && sqlLike(titleLike, " " + it.details.title) }
            .sortedWith(compareBy({ it.startMs }, { it.channelTvgId }))
            .take(limit)
}

/** Minimal SQLite LIKE (ESCAPE '\') emulation so fakes match the real DAOs. */
fun sqlLike(
    pattern: String,
    value: String,
): Boolean {
    val regex =
        buildString {
            var i = 0
            while (i < pattern.length) {
                when (val c = pattern[i]) {
                    '\\' -> {
                        append(Regex.escape(pattern[i + 1].toString()))
                        i++
                    }
                    '%' -> append(".*")
                    '_' -> append(".")
                    else -> append(Regex.escape(c.toString()))
                }
                i++
            }
        }
    return Regex(regex, RegexOption.IGNORE_CASE).matches(value)
}

/** Recording [PlayerEngine] fake: no Media3, just observable state. */
class FakePlayerEngine : PlayerEngine {
    override val state = MutableStateFlow<PlayerState>(PlayerState.Idle)
    override val video = MutableStateFlow<VideoDetails?>(null)
    override val tracks = FakeTrackFacade()
    val loaded = mutableListOf<String>()
    var stops = 0
    var released = false
    private var mutedState = false
    val muted: Boolean get() = mutedState

    override fun load(streamUrl: String) {
        loaded += streamUrl
        state.value = PlayerState.Buffering
    }

    override fun stop() {
        stops += 1
        state.value = PlayerState.Idle
    }

    override fun release() {
        released = true
    }

    override fun setMuted(muted: Boolean) {
        mutedState = muted
    }
}

/** In-memory [WatchHistoryDao] mirroring the real one's ordering and trim. */
class FakeWatchHistoryDao : WatchHistoryDao {
    val events = MutableStateFlow<Map<String, Long>>(emptyMap())

    private fun ordered(map: Map<String, Long>): List<String> =
        map.entries
            .sortedWith(compareByDescending<Map.Entry<String, Long>> { it.value }.thenBy { it.key })
            .map { it.key }

    override suspend fun upsert(event: WatchHistoryEntity) {
        events.update { it + (event.channelKey to event.watchedAtMs) }
    }

    override fun observeEvents(): Flow<List<WatchHistoryEntity>> =
        events.map { map -> ordered(map).map { key -> WatchHistoryEntity(key, map.getValue(key)) } }

    override suspend fun trimTo(cap: Int) {
        events.update { map -> ordered(map).take(cap).associateWith(map::getValue) }
    }

    override suspend fun clear() {
        events.value = emptyMap()
    }
}

/** Map-backed [KeyValueStore]. */
class FakeKeyValueStore : KeyValueStore {
    val values = mutableMapOf<String, Long>()

    override fun getLong(key: String): Long? = values[key]

    override fun putLong(
        key: String,
        value: Long,
    ) {
        values[key] = value
    }
}

fun testChannel(
    id: Long,
    number: Int,
    name: String,
    group: String? = "News",
    tvgId: String? = "tvg-$id",
): ChannelEntity =
    ChannelEntity(
        id = id,
        playlistId = 1,
        number = number,
        sortIndex = number - 1,
        source =
            ChannelSource(
                name = name,
                groupTitle = group,
                logoUrl = "http://logo/$id.png",
                streamUrl = "http://s/$id.ts",
                tvgId = tvgId,
            ),
        flags = ChannelFlags(),
    )

fun ChannelEntity.asFavorite(): ChannelEntity = copy(flags = flags.copy(favorite = true))

fun testProgram(
    tvgId: String,
    startMs: Long,
    endMs: Long,
    title: String,
    episode: String? = null,
): ProgramEntity =
    ProgramEntity(
        channelTvgId = tvgId,
        startMs = startMs,
        endMs = endMs,
        details = ProgramDetails(title = title, episode = episode, description = "Description of $title"),
    )

fun ProgramEntity.describedAs(text: String): ProgramEntity = copy(details = details.copy(description = text))

/** [EpgRepository] against the fake DAO; the parser is never touched here. */
fun testEpgRepository(programDao: ProgramDao): EpgRepository =
    EpgRepository(programDao = programDao, newParser = { error("no XML parsing in this test") })
