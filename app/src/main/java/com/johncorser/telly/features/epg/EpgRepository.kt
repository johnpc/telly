package com.johncorser.telly.features.epg

import com.johncorser.telly.features.epg.db.ProgramDao
import com.johncorser.telly.features.epg.db.ProgramEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import java.io.IOException
import java.io.Reader
import java.io.StringReader

/**
 * Fetches XMLTV over HTTP (the playlist's url-tvg), streams it through
 * [XmltvParser] and upserts programmes into Room. The parser factory is
 * injected: kxml2 in JVM tests, android.util.Xml on devices.
 */
class EpgRepository(
    private val programDao: ProgramDao,
    private val newParser: () -> XmlPullParser,
    private val client: OkHttpClient = OkHttpClient(),
    private val storeDescriptions: () -> Boolean = { true },
) {
    /** Downloads + stores the EPG at [epgUrl]; returns the programme count. */
    suspend fun refresh(epgUrl: String): Int =
        withContext(Dispatchers.IO) {
            val request = Request.Builder().url(epgUrl).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("HTTP ${response.code} while downloading EPG")
                }
                store(XmltvParser.parse(newParser(), bodyReader(response.body?.charStream())))
            }
        }

    /** Programmes overlapping [fromMs, toMs) for [tvgIds] — the guide window. */
    fun programsFor(
        tvgIds: List<String>,
        fromMs: Long,
        toMs: Long,
    ): Flow<List<ProgramEntity>> = programDao.observeWindow(tvgIds, fromMs, toMs)

    /** Per-channel now/next at the injected instant [atMs]. */
    fun nowNext(
        tvgIds: List<String>,
        atMs: Long,
    ): Flow<Map<String, NowNext>> =
        programDao
            .observeAiringOrUpcoming(tvgIds, atMs)
            .map { NowNextResolver.resolve(it, atMs) }

    /** Trims history; honors Settings -> EPG -> "Past days to keep EPG". */
    suspend fun trimEndedBefore(cutoffMs: Long) = programDao.deleteEndedBefore(cutoffMs)

    /** Every EPG channel id with stored data — the Assign EPG picker. */
    fun channelIds(): Flow<List<String>> = programDao.observeChannelIds()

    /** Replace semantics per refresh: old rows of the refreshed channels go away. */
    private suspend fun store(document: XmltvDocument): Int {
        val keepDescriptions = storeDescriptions()
        val entities =
            document.programs.map { program ->
                ProgramEntity(
                    channelTvgId = program.channelId,
                    startMs = program.startMs,
                    endMs = program.endMs,
                    details = if (keepDescriptions) program.details else program.details.copy(description = null),
                )
            }
        programDao.deleteFor(entities.map { it.channelTvgId }.distinct())
        programDao.upsertAll(entities)
        return entities.size
    }

    private fun bodyReader(reader: Reader?): Reader = reader ?: StringReader("")
}
