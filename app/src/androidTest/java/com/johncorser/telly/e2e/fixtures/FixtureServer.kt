package com.johncorser.telly.e2e.fixtures

import androidx.test.platform.app.InstrumentationRegistry
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import java.net.InetAddress

/**
 * Hermetic fixture host: a MockWebServer inside the instrumentation process
 * bound to the emulator's loopback on the same port the dev python server
 * uses (see e2e/fixtures/gen-fixtures.mjs). playlist.m3u / epg.xml are
 * regenerated per scenario so "now" programmes exist; logos and .ts test
 * cards stream from the androidTest assets staged out of e2e/fixtures.
 */
object FixtureServer {
    private const val PORT = 8090
    const val DEV_HOST = "10.0.2.2"
    const val HOST = "127.0.0.1"
    val baseUrl: String get() = "http://$HOST:$PORT"

    private var server: MockWebServer? = null

    @Volatile
    var programmes: List<FixtureProgramme> = emptyList()
        private set

    /** Re-anchors the EPG window (and lazily starts the server). */
    fun reset(anchorMs: Long = System.currentTimeMillis()) {
        programmes = FixturePlan.schedule(anchorMs)
        if (server == null) {
            server =
                MockWebServer().apply {
                    dispatcher = FixtureDispatcher()
                    start(InetAddress.getByName(HOST), PORT)
                }
        }
    }

    /** Rewrites the dev-server host in feature text to this server's. */
    fun mapHost(text: String): String = text.replace(DEV_HOST, HOST)

    fun nowProgramme(
        tvgId: String,
        atMs: Long,
    ): FixtureProgramme = programmes.first { it.channelTvgId == tvgId && it.startMs <= atMs && atMs < it.endMs }

    fun nextProgramme(
        tvgId: String,
        atMs: Long,
    ): FixtureProgramme = programmes.filter { it.channelTvgId == tvgId }.first { it.startMs > atMs }

    private class FixtureDispatcher : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            val path = request.path.orEmpty().substringBefore('?')
            return when {
                path == "/playlist.m3u" -> text(FixturePlan.m3u(baseUrl), "audio/x-mpegurl")
                path == "/epg.xml" -> text(FixturePlan.xmltv(baseUrl, programmes), "application/xml")
                path.startsWith("/logos/") || path.startsWith("/streams/") -> asset(path)
                else -> MockResponse().setResponseCode(HTTP_NOT_FOUND)
            }
        }

        private fun text(
            body: String,
            contentType: String,
        ): MockResponse = MockResponse().setResponseCode(HTTP_OK).setHeader("Content-Type", contentType).setBody(body)

        private fun asset(path: String): MockResponse {
            val assets = InstrumentationRegistry.getInstrumentation().context.assets
            return runCatching {
                assets.open("fixtures$path").use { stream ->
                    val buffer = Buffer().readFrom(stream)
                    MockResponse().setResponseCode(HTTP_OK).setBody(buffer)
                }
            }.getOrElse { MockResponse().setResponseCode(HTTP_NOT_FOUND) }
        }
    }

    private const val HTTP_OK = 200
    private const val HTTP_NOT_FOUND = 404
}
