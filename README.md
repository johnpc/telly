<p align="center">
  <img src="assets/icon.png" alt="telly icon" width="128" />
</p>

<h1 align="center">telly</h1>

<p align="center"><b>An open-source, TiviMate-style IPTV player for Android TV.</b></p>

<p align="center">
  <img src="docs/media/guide.png" alt="telly TV guide" width="800" />
</p>

telly is a native Android TV live-TV app built with Kotlin and Jetpack Compose for
TV. Bring your own M3U playlist and XMLTV EPG and you get a fast, remote-first TV
guide, instant channel zapping, search, and a familiar channel-surfing feel. There is
no backend and there are no accounts: everything is fetched on-device and cached
locally.

> telly is an independent open-source project inspired by TiviMate's UX. It is
> **not affiliated with, endorsed by, or connected to TiviMate** or its developers
> in any way.

## Features

### TV guide

A full timeline grid with live preview, programme details, channel groups, and
day-jumping. Pan the timeline with the D-pad; OK tunes the preview, OK again goes
fullscreen.

<img src="docs/media/guide-nav.gif" alt="Guide navigation: panning the timeline and tuning a channel" width="800" />

### Instant zapping

CH+/CH− (and number entry) zap directly with a TiviMate-style overlay showing the
channel number, current programme and what's next.

<img src="docs/media/zap.gif" alt="Channel zapping with the zap overlay" width="800" />

### Playback info overlay

OK during playback shows the programme info: times, progress, now/next, and stream
details (resolution, FPS, audio).

<img src="docs/media/playback-info.png" alt="Fullscreen playback with the info overlay" width="800" />

### Channel list panel

The quick-bar's Channels list opens groups + channels over the dimmed video, with
now-playing info, progress, and an expanded detail card on the focused row.

<img src="docs/media/channel-panel.png" alt="Channel list panel over live video" width="800" />

### Search

Search channels by name or number and programmes by title, with a two-pane
programme browser and search history.

<img src="docs/media/search.gif" alt="Typing a search and getting live results" width="800" />

### Context menu everywhere

Long-press OK on any channel row for the context sheet: search, settings,
favorites, hide channel, programme description and more.

<img src="docs/media/context-sheet.gif" alt="Opening the channel context sheet" width="800" />

### Settings

A TiviMate-style two-pane settings shell: playlists, EPG, appearance (accent
themes), playback, parental controls (PIN), and JSON backup/restore.

<img src="docs/media/settings.png" alt="Settings sheet" width="800" />

### Simple onboarding

First run walks you through adding your playlist URL — that's the whole setup.

<img src="docs/media/welcome.png" alt="Onboarding welcome screen" width="800" />

## Install

Grab the latest APK from [GitHub Releases](../../releases) (latest: `v0.1.0-29`) and
sideload it onto your Android TV device:

```sh
adb install telly-*.apk
```

or use a sideloading app like Downloader. Supports Android 6.0+ (minSdk 23),
including older TV boxes.

Or build from source:

```sh
./gradlew assembleDebug   # APK lands in app/build/outputs/apk/debug/
```

## Getting started

1. Launch telly. On first run it asks for a playlist.
2. Choose **Add playlist → M3U playlist → Enter URL** and type the M3U URL from
   your IPTV provider.
3. If your playlist carries a `url-tvg` hint, the XMLTV EPG is picked up
   automatically; otherwise add the EPG URL under **Settings → EPG**.
4. That's it — playback starts on channel 1 and BACK brings up the TV guide.

Your URLs never leave the device. Playlist and guide data are fetched directly
(OkHttp) and cached in a local Room database.

## Architecture

- **Native Kotlin + Jetpack Compose for TV** (`androidx.tv:tv-material`) — no
  webviews, no cross-platform layer. Leanback launcher integration, Compose UI.
- **Media3/ExoPlayer** (with HLS) for playback.
- **No backend.** Device-side fetching + Room caching only.
- **Vertical feature slices:** `com.johncorser.telly.features.<slice>` (playlist,
  epg, player, guide, search, settings, ...) with shared design tokens in `core/`.

## Development

Start with [CLAUDE.md](CLAUDE.md) — the project charter, conventions and decisions
log live there.

```sh
./scripts/install-hooks.sh   # one-time: enable the pre-commit quality gate
./scripts/quality.sh         # run every gate locally (authoritative)
./gradlew assembleDebug      # build a debug APK
```

Requires JDK 17, the Android SDK, and Node.js for the gate scripts.

Every commit must pass the full gate (`./scripts/quality.sh`, enforced by the
pre-commit hook): ktlint + detekt, ≤100 logic lines per file, per-method CRAP ≤ 15,
per-function Halstead difficulty ≤ 20, zero duplication (jscpd), ≥80% line coverage,
and a green build + unit tests. Thresholds are never loosened — the code gets fixed
instead.

Every feature also ships as an executable Gherkin spec under `e2e/features/`
(cucumber-android driving the real app with D-pad events on an Android TV emulator,
against a hermetic in-process fixture server).

## License

No license file yet — one will land with an upcoming tagged release. Until then the
code is source-available on GitHub without an explicit grant.
