<p align="center">
  <img src="assets/banner.png" alt="telly banner" width="640" />
</p>

# telly

**telly — a beautiful, open-source live-TV experience for Android TV.**

telly is a native Android TV IPTV player in the spirit of TiviMate: bring your own
M3U playlist and XMLTV guide, get a fast, gorgeous, remote-first live-TV UI.

> telly is an independent open-source project. It is **not affiliated with, endorsed
> by, or connected to TiviMate** or its developers in any way.

## Features

| Feature | Status |
| --- | :---: |
| Live playback (Media3, HLS/TS) | ✅ |
| Channel list panel | ✅ |
| Search (channels + programmes) | ✅ |
| EPG guide (XMLTV) | ⬜ |
| Archive / catch-up | ⬜ |
| Recording | ⬜ |
| Multi-playlist support | ⬜ |
| Favorites | ⬜ |
| Channel groups | ⬜ |
| Picture-in-guide preview | ⬜ |
| Settings | ✅ |
| Parental controls (PIN) | ✅ |
| Backup / restore | ✅ |

## Install

Grab the latest APK from [GitHub Releases](../../releases) and sideload it onto your
Android TV device (via `adb install telly-*.apk` or a sideload app like Downloader).
Supports Android 6.0+ (minSdk 23), including older TV boxes.

## Architecture

- **Native Kotlin + Jetpack Compose for TV** (`androidx.tv:tv-material`) — no webviews,
  no cross-platform layer. Leanback launcher integration, Compose UI.
- **Media3/ExoPlayer** (with HLS) for playback.
- **No backend.** Your playlist/EPG URLs stay on your device; data is fetched directly
  (OkHttp + kotlinx.serialization) and cached locally (Room).
- **Vertical feature slices:** `com.johncorser.telly.features.<slice>` (playlist, epg,
  player, settings, ...) with shared design tokens in `core/`.

## Quality gates

Every commit must pass the full gate (`./scripts/quality.sh`, enforced by a pre-commit
hook and CI): ktlint + detekt, ≤100 lines per logic file, per-method CRAP ≤ 15,
per-function Halstead difficulty ≤ 20, zero code duplication (jscpd), ≥80% line
coverage, and a green build + unit tests. Thresholds are never loosened — the code
gets fixed instead. See [CLAUDE.md](CLAUDE.md) for the full charter.

## Development

```sh
./scripts/install-hooks.sh   # one-time: enable the pre-commit quality gate
./scripts/quality.sh         # run every gate locally
./gradlew assembleDebug      # build a debug APK
```

Requires JDK 17, the Android SDK (platform 36), and Node.js for the gate scripts.

## License

Open source — license file to land with the first tagged release.
