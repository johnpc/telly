# Reference round 6 — TiviMate 5.2.0 re-provisioned, open fidelity items captured

Date: 2026-09-14 · Official TiviMate 5.2.0 (free tier) freshly re-provisioned on the
tv34 AVD (Android 14 TV, 1920×1080, 2 px = 1 dp) with the regenerated fixture backend
(`e2e/fixtures/` served on :8090; playlist `http://10.0.2.2:8090/playlist.m3u`, EPG via
url-tvg; anchor 2026-09-14T20:42Z). All captures are of the REFERENCE app, driven
read-only over adb (local emulator only).

## APK provenance (never re-download)

- Stored at **`~/apks/tivimate-5.2.0.apk`** on this machine (17 160 198 bytes).
- Source: Aptoide open API via the umbrel box (`ssh umbrel@192.168.7.211`, work network
  blocks APK mirrors): `listAppVersions/package_name=ar.tvplayer.tv` → store `pukkatv`,
  vername **5.2.0**, vercode **5208**, malware rank **TRUSTED**, md5
  `6bb187b6691165853643170e0099c100`; download URL
  `https://pool.apk.aptoide.com/pukkatv/ar-tvplayer-tv-5208-73559678-6bb187b6691165853643170e0099c100.apk`.
- md5 verified after download and after scp; `aapt2 dump badging`: package
  `ar.tvplayer.tv`, versionName 5.2.0, versionCode 5208, minSdk 23, targetSdk 34.
- Installed on tv34 with `adb install`; `appops set ar.tvplayer.tv SYSTEM_ALERT_WINDOW allow`
  granted. TiviMate is left installed and configured (fixture playlist + EPG imported).

## A. Long-OK row context sheet — entrance / close motion (ANSWERS motion-punchlist P2)

Method: `adb shell screenrecord` (frames emitted only on change, ~60 fps), frame-stepped
with `ffmpeg -vsync 0` + per-frame `ffprobe` PTS; pixel probes with PIL.
Recordings: `01-sheet-open-close-take1-best.mp4` (both transitions + intermediate
frames), `take2/3` (corroborating). Key frames under `01-frames/`.

- **Entrance is a fast decelerating FADE + small SLIDE-IN FROM THE RIGHT, ~150–280 ms.**
  In take1-best: last sheet-less frame at t=1.763 s; one intermediate frame at
  t=1.904 s with the sheet at **~78–80 % opacity and 9–10 px right of its final x**
  (Search-pill left edge 1418 px vs settled 1408 px; sheet-region mean 49.4 vs settled
  54.2); fully settled by t=2.041 s. Only one mid-frame is ever emitted (none in 5 other
  takes) → the animation is short (~200 ms class) and decelerates (already ~80 % done at
  ~140 ms). The grid dim-scrim (guide bg (15,20,22) → (4,6,6)) lands with/just after the
  settle frame — sheet first, scrim within ~1 frame.
- **Close: the sheet itself is an instant cut** (present at t=5.730 s, gone at
  t=5.904 s, zero intermediate frames across all takes), **then the grid scrim fades
  back out over ~300 ms** (5.904 → 6.203 s). So BACK = sheet pop-off + short scrim fade.
- Evidence frames: `01-frames/open-t*.png`, `01-frames/close-t*.png`.

### Channel options pane push/pop (`03-channel-options-push-pop.mp4`)

- **Push (OK on "Channel options") = cross-fade in place, no slide; the pane REPLACES
  the sheet.** The sheet fades out over ~170 ms (focused-pill probe 219→61 across
  0.832–0.999 s), the pane fades in over ~350 ms (1.067–1.351 s) at its final position
  (frames `01-frames/chopts-push-mid-*.png` show both surfaces translucent, in place).
  The pane is wider than the sheet (starts x≈1200 vs 1408), header strip titled with the
  channel name ("News One"), body = "All features are available in Premium version" +
  Unlock Premium row + locked rows (Channel name / Restore channel name / Channel names
  editor / Audio decoder / Video decoder / Use external player / …).
  `03-channel-options-pane.png/.xml`.
- **Pop (BACK from Channel options) is a quick ~130–150 ms fade that lands directly on
  the GUIDE GRID — NOT back on the sheet.** Because the pane replaced the sheet, there
  is no sheet to restore: one BACK → guide, focus on the originating row (row 1,
  GridView child [0,468][1920,546] focused=true). The question "does BACK restore the
  'Channel options' row in the sheet?" is therefore N/A in 5.2.0 — the reference never
  returns to the sheet at all. `03-after-back-from-channel-options.png/.xml`.

## B. Guide-sheet BACK focus restore from the 5th row (ANSWERS visual-round5 logged row)

`04-row5-focused-before.xml`: focused row = channel **5, News One 2**, bounds
**[0,702][1920,780]**. Sheet opened via long-OK (`04-row5-sheet-open.png`), one BACK →
`04-row5-after-back.xml`: focused row = **the same 5th row, [0,702][1920,780]** (texts
'5', 'News One 2'). **TiviMate restores focus to the originating guide row** (the row
also keeps a white outline while the sheet is open).

## C. PIN entry (ANSWERS visual-round5 "PIN wheel" logged row — negatively)

**Parental controls cannot be enabled in the 5.2.0 FREE build — the PIN entry UI does
not exist to capture.** Every row in Settings → Parental controls is premium-locked
(dimmed + padlock) AND not focusable: from Unlock Premium, DOWN skips the whole section
(uidump shows focus falling to the scroll container, `09-parental-toggle-activate-result.png`);
OK never reaches the Off toggle, so no PIN was ever set and the app is not left locked.
Observable free-tier facts only (`09-parental-controls-section.png/.xml`,
`09-parental-belowfold.png/.xml`): rows Off (toggle) · Change PIN · PIN input method =
**"Picker"** (default) · Don't require PIN after unlocking = "Always require" · Don't
require for channels only (toggle) · "Require PIN for": Settings, Settings | Playlists,
Settings | EPG, … This matches device-verified-answers ("parental PIN UI" in the
premium-unverifiable list). telly's PIN-wheel visual diff has NO free-build reference
target; the only anchor is the locked "PIN input method: Picker" label.

## D. Search live probes (ANSWERS search-punchlist P3 open items)

Query "news" on the fixture (34 airing/upcoming title matches across 6 News channels;
ground truth from `e2e/fixtures/epg.xml`).

### Grouping rule — one row per AIRING, master–detail per CHANNEL

The Programs section is a **two-pane master–detail**, not a flat list
(`06-search-news-results.png/.xml`, `08-programs-card-*.png`):

- **Left lane = vertical list of channel cards**, one per channel with ≥1 matching
  programme, ordered by channel **name** (case-insensitive): News One, News One +1,
  News One 2, News One 24, News One Extra, News One HD — same order as the Channels
  shelf, NOT zap order.
- **Right lane = ONLY the selected card's airings**, chronological, **one row per
  airing with NO title dedupe**: News One shows "Newsroom Live: Newsroom Live Special"
  twice (11:00 PM and Tue 2:45 AM); News One 2 shows it three times. Verified 1:1
  against EPG ground truth for News One (4 rows), News One +1 (4), News One 2 (8),
  News One HD (4).
- Same-titled programmes airing on several channels are **not merged across
  channels** — each channel card carries its own copy.
- Navigation: DOWN inside the rows stops dead at the selected channel's last airing;
  LEFT jumps to the card lane; DOWN there selects the next channel card and **swaps the
  rows pane** to that channel's airings, preselecting its first row into the right-side
  detail card (`08-programs-card-newsone-plus1-selected.png/.xml`).
- This explains the round-5 observation "live TiviMate collapsed the news list to 2
  rows": that was one selected channel with 2 matching airings, not a per-title
  collapse. **telly's flat soonest-first per-airing list with shared channel cards per
  run is a structural mismatch** — the reference is a channel-master/airings-detail
  layout.

### DOWN from the query bar

`07-search-down-from-querybar.png/.xml`: with "news" typed (IME dismissed), one DOWN
from the query field focuses the **FIRST channel card** ("News One", bounds
[32,298][312,506]) — the leftmost card of the Channels shelf, NOT the geometrically
nearest card under the bar's center. telly's current nearest-candidate behaviour
(4th card) is a deviation.

Bonus: first Search open in a fresh install raises the system RECORD_AUDIO permission
dialog ("Allow TiviMate to record audio?"); granted "While using the app" on this AVD.

## End state

TiviMate 5.2.0 remains installed and configured on tv34 with the fixture playlist,
imported EPG and watch data intact; parental controls untouched (never enableable).
Fixture server was `python3 -m http.server 8090` in `e2e/fixtures/` (not persistent —
restart it before driving TiviMate again, or its playlist/EPG updates will fail
harmlessly).
