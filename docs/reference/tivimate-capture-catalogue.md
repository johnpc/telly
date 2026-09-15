# TiviMate 5.2.0 device capture catalogue

Captured 2026-09-13 on the Android TV emulator (emulator-5554, Android 14, 1920×1080),
package `ar.tvplayer.tv` v5.2.0 (free tier, no premium account). Fixture backend:
`e2e/fixtures/` served at `http://10.0.2.2:8090` (30 channels / 5 groups / XMLTV EPG).

Artifacts:
- Screenshots: `docs/reference/screens/NN-name.png` (76 files)
- uiautomator dumps (exact bounds, resource-ids, text): `docs/reference/uidumps/NN-name.xml` (38 files)

Conventions below: "OK" = DPAD_CENTER, "long-OK" = long-press DPAD_CENTER.
All bounds quoted from uidumps are px at 1920×1080 (2 px = 1 dp on this xhdpi config).

## Global visual tokens (pixel-sampled from captures)

| Token | Value | Sampled from |
|---|---|---|
| App background | `#131619` | 24 guide bg, 02 welcome bg |
| Guide cell (unfocused, future/current) | `#1B1E21` | 24 |
| Focused cell / focused list row | `#DEE0E2`–`#FFFFFF` white pill, black text | 24, 34 |
| Settings panel background | `#232629` | 18 |
| Settings panel header strip | `#333639` (title white 34px at y≈74) | 18 |
| Settings focused row | `#E0E2E4` rounded pill | 18 |
| Accent (progress fill, playing-channel name, section headers in menus, "All features…" note) | `#2196F3` (Material Blue 500) | 34, 18, 24 |
| Guide date/clock header text | `#90CAF9` (Material Blue 200) | 24 |
| Now-line | 2px vertical rule, ~`#384C5C` over bg (low-alpha light blue) with a small dot at the timeline row | 24 |
| Overlay shortcut card bg | ~~`#1B1D21`; focused card `#FFFFFF`~~ **CORRECTED (round3):** resting `#181E20` (24,30,32), focused `#252A2D` (37,42,45) with WHITE icon/text — the focused card is NOT the white pill; focus shows as the lighter fill + ~13% growth (280×208 vs 248×184 px) | 34; round3-ref/02 |
| Progress track | grey ~`#666` (thin, 4px); fill `#2196F3` with round thumb | 34 |
| Typeface | Roboto throughout (system) | all |

Focus model everywhere: white rounded pill + black text/icon. Premium-locked rows are
dimmed grey with a padlock icon replacing their normal icon; toggles of locked rows render
but are inert.

---

## 1. First run & add-playlist wizard (GuidedStep style: left icon+title pane, middle form column, right actions column)

### 00–02 launcher / first launch / welcome
- `02-welcome.png` + dump. Dark screen `#131619`. Icon pane left (TV icon). Title
  "Welcome to TiviMate" with body "TiviMate doesn't provide any sources of TV channels".
  Right actions: **[Add playlist]** (focused white pill), **[Settings]**.
  Action rows are 88px tall, corner radius 8px.
- BACK here exits the app.

### 03 playlist type chooser (`03-add-playlist-type-chooser.png` + dump)
- Reached: welcome → OK on Add playlist.
- Left pane: "Playlist type". Middle column options (top→bottom):
  **M3U playlist** (default focus), **Xtream Codes**, **Stalker Portal**. Right: **Cancel**.
- BACK = Cancel → welcome.

### 04 Xtream Codes form (`04-xtream-codes-form.png` + dump)
- Fields: **Server address** ("Enter URL" hint), **Username**, **Password**;
  checkboxes **Include TV channels** (checked), **Include VOD** (checked).
- Actions: **Next** (disabled/grey until fields valid, has → glyph), **Back**.

### 05–06 Stalker Portal form (`05…png` + dump, `06…scrolled.png`)
- Fields: **Server address**, **MAC address** — auto-generated, editable
  (captured value `00:1a:79:8b:29:a2`, i.e. the classic 00:1A:79 STB prefix), then an
  "Optional fields" group: **Username**, **Password**, **Device ID**, **Device ID 2**,
  **Serial number**, **Signature**. Actions: Next (disabled), Back.

### 07–10 M3U URL step
- `07-m3u-url-form.png` + dump: options **Enter URL** (focused), **Paste from clipboard**,
  **Select local playlist**; actions Next (disabled) / Back.
- `08-m3u-url-edit-keyboard.png` + dump: OK on Enter URL turns the row into an inline
  EditText and raises the **system Gboard TV IME** (dark, QWERTY, suggestion strip) —
  the wizard does NOT use a custom keyboard.
- `09-m3u-url-typed.png`: URL typed; Next becomes enabled (white) live while typing.
- `10-m3u-after-url-commit.png`: ENTER commits the field, keyboard dismisses, focus
  jumps automatically to **Next**.

### 11–12, 23 processing + summary
- `11-m3u-processing.png`: "Processing playlist" with an indeterminate spinner replacing
  the actions column.
- `12-m3u-processing-done.png` + dump (first run with `.mp4` URLs): title
  **"Playlist is processed"**, summary line **"Movies: 30"**, radio choice
  **TV playlist** (selected) / **VOD playlist**, editable **Playlist name** field
  (auto-filled from host: "10.0.2.2"). Actions: **Next**, **Back**.
- `23-playlist-processed-channels.png` (re-run with `.ts` URLs): summary
  **"Channels: 30"**, same layout.
- IMPORTANT parser behavior: stream URLs ending in `.mp4`/`.mkv` are classified as VOD
  ("Movies") even when `group-title` is set, and then the TV guide stays EMPTY even with
  "TV playlist" selected. `.ts` URLs classify as live channels. Fixtures must use `.ts`.

### 13 EPG step (`13-epg-url-step.png` + dump)
- Title "EPG". The `url-tvg` attribute from the M3U header is **pre-filled** into
  **Enter URL** (shows `http://10.0.2.2:8090/epg.xml`). Other options:
  **Paste from clipboard**, **Paste playlist URL** (hint: use when EPG is embedded),
  **Use default source** (greyed). Actions: **Done**, **Back**.
- OK on Done → lands directly on the TV guide (`14-after-epg-done.png`); EPG downloads
  in the background and rows populate within seconds (`24`). No terms dialog and no
  remote-navigation hint dialog appeared anywhere in this build's flow.

---

## 2. TV guide (main screen)

### 24 populated guide (`24-tv-guide-populated.png` + dump) — canonical anatomy
- Top-left **preview window** 32,24→670,384 (16:9), live video (black until a channel
  is tuned; nothing auto-plays on cold start in free tier).
- Top-right **info pane**: program title 34px semibold ("Business Hour: Episode 7. S1 E7"
  — title renders as `Title: Subtitle`), second line `02:30 — 03:45 PM` + dash-progress
  pill + `61 min` (remaining), description grey 2 lines, top-right a **star outline**
  icon (favorite) and below it the **group name** ("News").
- **Header row** y≈408-464: date+clock `Sun, Sep 13, 2:44 PM` in `#90CAF9` at x=32;
  timeline labels every 30 min (`02:30 PM`,`03:00 PM`…) width 320px per 30 min.
- **Now-line**: thin vertical rule + dot at header, color ~#384C5C.
- **Wrap (verified round4, read-only drive):** DOWN at the last channel row
  (30) wraps focus to row 1, and UP at row 1 wraps to row 30.
- **Channel rows** 78px pitch: number (grey, blue when playing), logo tile 56×48
  (rounded, shows the channel logo), name (white; **playing channel name is #2196F3 and
  gets a blue ▶ at the row's right edge**), then program cells.
- **Cells**: rounded 8px, fill #1B1E21, 4px gaps, single-line ellipsized text
  `Title: Subtitle`; focused cell = white; **past programs render dimmer**; text greys
  out for past. Cell width proportional to duration (320px/30min).
- First-run hint toast (bottom-right, grey rounded): "**Long OK**: open menu /
  **Left**: show groups / **Long Left**: navigate to past programs".

### 25 groups column (`25-guide-groups-column.png` + dump)
- Reached: LEFT from grid. Column slides in left of the guide (guide shifts right):
  **Favorites, All channels (focus), News, Sports, Movies, Kids, Music** — plain text
  rows, focused = white pill. No "History" group appears in 5.2.0.
- OK on a group filters the guide; **channel numbers restart from 1 within the group**
  (`74-guide-movies-group.png`: Movie House channels numbered 1–6).
- Group selection resets to All channels after app restart.
- LEFT again from groups column → **nav rail** (see §5). RIGHT → back to grid.

### 26–27 OK on a future program (`26`, `27-program-detail-popup.png` + dump)
- OK on a non-airing program opens a **dropdown menu anchored under the cell** (not a
  modal): **Remind** (focus), **Record**, **Custom recording**, **Add to My list**,
  **Program description**. Menu bg dark, focused row white pill.
- FREE-TIER GATE: every one of these five items opens the **Unlock Premium** screen
  (verified for Remind and Program description; `31-remind-result.png`).

### 32 OK on the current program (`32-guide-first-ok-preview-tune.png`)
- First OK on an airing program **tunes it in the preview window** (stays in guide;
  ▶ appears at the row; channel name turns blue). Second OK → fullscreen playback.

### 75 long-press LEFT (`75-guide-past-programs.png`)
- No visible change in this environment (window did not page into the past) —
  inconclusive on the emulator despite 6h of past EPG.

### BACK chain
- With groups column open, RIGHT-of-grid focus etc.: BACK from the guide grid at root
  **exits the app immediately to the launcher — no exit-confirmation dialog**
  ("Confirm exit by second press Back" is OFF by default and premium-locked).

---

## 3. Fullscreen playback

### 33 clean playback (`33-fullscreen-playback.png`)
- Zero chrome. (Fixture NEWS ONE card fills the screen.)

### 34–35 OK → info overlay (`34-playback-ok-panel.png` + dump, `35` after DOWN)
- Bottom overlay over a subtle bottom scrim; ALSO a top scrim row: group name
  ("News") top-left, date+clock top-right — both white.
- Anatomy: channel logo tile 158×158 with blue focus border (left); program title 34px;
  line 2: `02:30 — 03:45 PM` + dash-progress + `61 min` + `1  News One` (number+name,
  bold) + badges **HD**, **25 FPS**, **MONO** (grey rounded pills, white caps text);
  line 3: `03:45 — 05:15 PM  Newsroom Live: Episode 8. S1 E8` (next programme).
- Full-width progress bar y≈771: track grey, fill `#2196F3`, round thumb at now.
- Shortcut row of cards 150×110: **TV guide** (grid icon) then **History**
  (clock-with-arrow icon); LEFT/RIGHT moves focus and the row scrolls; a centered
  **down-chevron** at bottom (no further rows expand in free tier — DOWN is a no-op here).

> **CORRECTED (history-round2, 2026-09-15)** — with REAL accumulated watch
> history the shortcut row is longer than 33/34 showed (those installs had
> no history). Full row, left→right: **TV guide** · **History** · **recent-
> channel cards** (one per recently-watched channel, newest first: channel
> **logo** + the channel's **current programme title** in accent blue —
> NOT the channel name/number; focusing one adds a bottom `air-time +
> programme title` line) · **Clear** (trash icon). Row is a GridView
> [0,792][1920,1012]; TV guide tile 280×208 (first/focused), the rest
> ~248×184. **OK on a recent-channel card → shared Unlock Premium** (free
> tier; verified on both a Music- and a News-group card — universal, not a
> zap; BACK → player, no channel change). See §48 for the History card and
> the Clear card. Evidence: `sidebyside/history-round2/`.
- BACK hides the overlay.

### Key map discovered during playback (free 5.2.0)

> **CORRECTED (round3, re-verified twice on the live device):** the UP and
> long-OK/MENU rows below were captured from a different pre-state and are
> wrong at BARE fullscreen. The verified map is:
> - **UP at bare fullscreen opens the INFO overlay** (same as OK/DOWN), not
>   the guide overlay at the previous channel. A **second UP** moves focus
>   into a transport row (00:16/45:00, ⏮ ⏪ ⏸ ⏩ ⏭, LIVE badge, record dot).
>   Evidence: round3-ref/03-panel-up.png, 03b-panel-up-up.png, 03c-panel-up-x3.png.
> - **long-OK / MENU at bare fullscreen opens a bottom icon quick-bar**
>   (Search · Channels list · Recordings · Multiview · Picture-in-picture ·
>   1280 × 720 · Mono · 0 ms · Off(CC)); focus = white circle on Search,
>   auto-hides ~5 s. The right-side sheet documented below belongs to
>   long-OK **on a row inside the guide overlay/panel**.
>   Evidence: round3-ref/07-player-ctx-menu.png/.xml, 08-player-menu-key.png.
> - Channel change shows a **compact zap overlay** (logo, title,
>   time/progress/remaining, number+name, description line, next programme)
>   while the OLD video keeps playing until the new stream is ready
>   (~1.5–2 s); it auto-hides ~5.5 s. Evidence: round3-ref/10-zap-*.

| Key | Result | Evidence |
|---|---|---|
| OK | opens bottom info overlay | 34 |
| DOWN | opens the same bottom info overlay | 37 |
| UP | ~~opens the **guide overlay** focused on the previous channel row (wraps 1→30) + toast~~ **CORRECTED (round3): opens the info overlay; second UP focuses the transport row** | ~~36~~ round3-ref/03* |
| long-OK | ~~context menu (see below)~~ **CORRECTED (round3): bottom icon quick-bar** | ~~38~~ round3-ref/07 |
| MENU | same as long-OK (quick-bar) | 46, round3-ref/08 |
| CHANNEL_UP | opens the info overlay (no zap on this build/emulator; flagged unreliable — the clone zaps directly with the compact zap overlay) | 45 |
| digits 0–9 | **no-op** — no number-zap overlay exists in 5.2.0 free | 43, 44 |
| long-BACK (from any guide overlay) | return to fullscreen player | 36 toast, verified |
| BACK | guide overlay → player? No: from clean playback BACK returns to the **TV guide** | round3-ref/06-back3 |

### 38–40 long-OK / MENU context menu (`38`,`39`,`40` + dump 38)

> **CORRECTED (round3):** this right-side sheet is NOT the bare-fullscreen
> long-OK menu — capture 38's background shows the guide overlay with the
> preview window, i.e. it was taken from the **guide/panel row context**.
> Long-OK on a panel/guide row opens this full sheet with the panel still
> visible behind it (round3-ref/05-panel-row-longok.png/.xml); long-OK at
> bare fullscreen opens the bottom icon quick-bar instead (round3-ref/07).
> Sheet metrics (round3): 512 px wide, 16 px off the screen's top/right,
> 80 px row pitch, focus pill inset 16 px from the sheet edges.

Right-side sheet, sectioned, verbatim (top→bottom):
- **Search** (row with magnifier, focused by default), **Settings** (gear)
- blue header = program title ("Business Hour: Episode 7. S1 E7"):
  **Open in external player**, **Record**, **Custom recording**, **Add to My list**,
  **Program description**
- blue header = channel name ("News One"):
  **Add to Favorites**, **Block channel**, **Hide channel**, **Assign EPG**,
  **Channel options**
- blue header **All channels**:
  **Manage Favorites**, **Manage blocking**, **Manage visibility**, **Reorder channels**,
  **Copy channels**, **Create group**, **Group options**
- Note: opening this menu while fullscreen simultaneously reveals the guide overlay
  behind it (current row highlighted). No sleep timer, aspect-ratio, CC or audio-track
  item exists anywhere in this build's menus.

### 41–42 Channel options (`41`,`42` + dump 41)
Right panel titled with the channel name ("News One"), all rows premium-locked:
**Channel name** (News One), **Restore channel name**, **Channel names editor** (Off),
**Audio decoder** (Hardware), **Video decoder** (Hardware), **Use external player** (No),
**EPG time offset, h:min** (0:00), **Block channel**, **Hide channel**.

### 47 "TV guide" card → guide overlay (`47-playback-guide-overlay.png` + dump)
- Full-screen guide over the video (video visible dimmed behind): timeline header at
  top, the focused channel row is **expanded** into a detail card (big logo 140×140,
  title, times + progress + description, star icon, group name at right), rows of other
  channels below with cells. This is the 5.x "channel list panel".
- OK on History card opens the same overlay (with History as source group when it
  exists). Long-BACK returns to the player.

> **CORRECTED (history-round2):** the History card does NOT open this guide
> overlay. See §48.

### 48 (`48-history-screen.png` + dump) — captured overlay state after History press.

> **CORRECTED (history-round2, 2026-09-15) — SUPERSEDES the capture-48
> reading AND the interim "EPG schedule browser" reading.** With real
> accumulated history, **OK on the History card opens a DISTINCT full-screen
> "History" list surface** — NOT bare playback (capture-48 was on a
> history-empty install), NOT the guide overlay, NOT an EPG schedule
> browser. It is a leanback VerticalGrid screen: title **"History"**
> top-RIGHT (`TextView [1568,40][1744,116]`) + a **clear-all trash icon**
> (`ImageView [1792,40][1872,120]`), dimmed live video behind. This session
> it showed the empty state **"No history"** (`TextView [887,519]`) even
> though the info-row recent-channel cards were populated — the standalone
> History log and the recent quick-cards are different sources; when
> populated the screen would show a grid of channel cards. **BACK →
> fullscreen player.** The info-row **Clear** card (trash + "Clear",
> rightmost) clears the recent-channel row — NOT activated (read-only round,
> must not clear accumulated history), so its confirm behavior is
> uncaptured. Method note: BACK from the info overlay lands on the
> guide/channel-list panel, so reach the History card via long-BACK →
> bare fullscreen → OK → RIGHT → OK in one burst (the overlay auto-hides
> ~5 s and resets focus). Evidence: `sidebyside/history-round2/`.

---

## 4. Search

### 49 search screen (`49-search-screen.png` + dump)
- Reached: context menu → Search (or nav rail Search).
- Leanback SearchFragment: round **mic orb** left (focus default — OK starts voice
  input), **"Speak to search"** StreamingTextView bar (light grey), gear icon right,
  below: "**Search history**" header + trash icon, empty state "**No history**".
  Video keeps playing dimmed behind.
- Typing: RIGHT moves focus to the text bar; it accepts key input directly
  (system IME appears bottom-right with suggestion chips).

### 50–51 results (`50` + dump, `51` without keyboard)
- Query "news" → shelves: **Channels** (landscape cards: logo tile, channel name,
  current programme title in blue, thin progress bar) and **Programs** (rows: logo
  card + title + time range; future items show a date `Mon, Sep 14, 12:45 — 01:45 AM`).
  Focused program shows a right-side detail card (title, times, description).
- No Movies/TV Shows shelves appear when the playlist has no VOD.

---

## 5. Nav rail & sections

### 16/17/70 rail (`16` collapsed, `17` + dump expanded, `70` rail+groups+guide)
- Collapsed rail: wordmark "**tv**" (blue), icons: magnifier, TV screen (selected),
  DVR badge, bookmark, gear at bottom. Expanded (focus enters rail): wordmark
  "**tivimate**" ("tivi" blue #2196F3, "mate" white), labeled rows: **Search**, **TV**,
  **Recordings**, **My list**, **Settings** (gear, pinned at bottom).
- **Sections are dynamic**: a **Movies** row (clapperboard) existed while the playlist
  had VOD items (17); after re-adding as pure live channels it disappeared. No
  "TV Shows" row with our fixtures.
- Selected section = dark grey pill; focused = white pill.

### 71–72 Recordings (`72-recordings-screen.png`)
- Empty state: centered "**No recordings**" text; no tabs visible in free tier.

### 76–77 My list (`76`,`77`)
- Two tabs (left column): **My TV programs** / **My reminders**; empty states
  "No programs" / "No reminders". (This is where reminders are managed.)
- NOTE: the first attempt to open My list crashed the app to the launcher
  (process died, no ANR dialog); the retry worked. Treat as flaky, not blocking.

---

## 6. Settings

### 18/52 root (`18-settings-root.png` + dump, `52` scrolled)
Right-side sheet (width 720px, bg #232629), header "Settings", blue note
"**All features are available in Premium version**" above the first row.
Rows: **Unlock Premium** (key icon) · **General** · **Playlists** · **EPG** ·
**Appearance** · **Playback** · **Remote control** · **Parental controls** ·
**Other** · **About**. Sub-panels slide in from the right; BACK pops one level.
Every section repeats the blue note + "Unlock Premium" as its first row.

### 54–56 General (all locked): Auto start app on boot (off) · Auto start app on wake
up from sleep mode (off; sub "May not work on all devices") · Turn on last channel on
app start (off) · Switch to picture-in-picture mode on press Home (off) · Confirm exit
by second press Back (off) · User-Agent (Not set) · UDP proxy (address:port) (Not set)
· Back up data · Restore data.

### 19–22 Playlists (`19` + dump, `20`,`21` per-playlist, `22` delete confirm)
- List: the playlist ("10.0.2.2", sub "Channels: 30", blue check) then LOCKED:
  **Playlists sorting** (By name), **Add playlist**, **Update all playlists**.
  Free tier = exactly one playlist; adding another is the gate.
- Per-playlist (`20`,`21`): LOCKED **Enable playlist** (on), **Playlist name**,
  **Playlist URL**, **EPG sources** (1 source), **User-Agent** (Not set),
  **Manage groups**; header **Update options**: **Update interval, hours** (None),
  **Update on app start** (off), **Update playlist**; the ONLY free action is
  **Delete playlist** → GuidedStep confirm ("Delete playlist / 10.0.2.2" with
  **Delete** / **Cancel**) (`22`).

### 57–58 EPG (`57` + dump; `58` EPG sources)
- Rows: **EPG sources** (free), LOCKED: **Past days to keep EPG** (7),
  **Store program descriptions** (toggle on), header **Update options**:
  **Update interval, hours** (None), **Update on app start** (off),
  **Update on playlists change** (off); **Update EPG** (free action, bottom).
- EPG sources (`58`): row "**10.0.2.2 (default)**" with URL sub-line and blue check;
  LOCKED **Add source**; footer note "EPG sources should be assigned in the playlist
  settings".

### 59 Appearance (all locked): sub-screens **TV guide**, **Player**, **Groups**,
**Logos**; then **Language** (System), **Font size** (Medium),
**Color theme** (Dark • Blue). (No transparency/timeout items visible at this level —
they presumably live inside the locked sub-screens.)

### 60–62 Playback (all locked): **Buffer size** (Small), **Audio decoder** (Hardware),
**Video decoder** (Hardware), **Auto frame rate (AFR)** (Off), **Select surround audio
track by default** (off), **Audio passthrough** (off), **Use external player** (No),
**Skip steps**.

### 64–65 Remote control: sub-screens **TV guide**, **Player** (locked); header
**Seeking options** (all locked): **Use RW/FF/Pause for seeking/pause while watching
catch-up** (ON default), **Use RW to rewind live stream with catch-up** (off),
**Use Left/Right for seeking while watching catch-up** (off), **Use Left to rewind live
stream with catch-up** (off), **Use Down/Up for seeking while watching catch-up** (off),
**Use Down to rewind live stream with catch-up** (off).

### 67/69 Parental controls (all locked): **Off** (master toggle), **Change PIN**,
**PIN input method** (Picker), **Don't require PIN after unlocking** (Always require),
**Don't require for channels only** (off); header **Require PIN for**: **Settings**,
**Settings | Playlists**, … (list continues below the fold; unscrollable in free tier
because locked rows are not focusable).

### 68 Other: locked sub-screens **Search**, **Reminders**, **Recording**, **VOD**.

### 53 About (`53` + dump): **Unlock Premium**, **Send anonymous statistics to improve
the app** (toggle, ON, FREE — the only free toggle in Settings), **Privacy policy**,
**Version / 5.2.0**.

---

## 7. Premium / paywall

### 28 Unlock Premium (`28-unlock-premium-paywall.png` + dump)
Full-screen GuidedStep. Left: big unlocked-padlock icon + title "Unlock Premium" +
bullet list verbatim: "You will get access to:" • Support for multiple playlists •
Favorites management • Catch-up • Customizable EPG update intervals • Customizable
panels transparency and timeout • Manual channels sorting • Turning on last channel on
app start • Auto frame rate (AFR) • and much more. Actions: **Next** (focus), **Cancel**.

### 29 subscription info (`29` + dump)
Text (verbatim): "TiviMate Premium is available with a limit of 5 devices and one of
the next payment options:" • "Subscription ? per year and 7-day free trial" •
"One-time payment ?" — prices render as "?" because Play billing is unreachable on the
emulator — plus "You need to have TiviMate account to proceed." Actions: **Account**,
**Back**.

### 30 account form (`30` + dump)
Centered dark card: **Email** field (teal/cyan focus underline), **Password**,
buttons **LOG IN** / **SIGN UP**; IME auto-opens. BACK×3 returns to the app.

### Gate points confirmed in free tier
- ALL items of the program cell menu (Remind, Record, Custom recording, Add to My list,
  Program description) → paywall.
- All channel/all-channels management items in the long-OK menu are premium
  (each opens the paywall or a fully locked panel).
- Nearly every Settings row is locked (padlock) except: EPG sources (view),
  Update EPG, Delete playlist, Add playlist (only when no playlist exists),
  anonymous-statistics toggle, About/Privacy policy.

---

## 8. Notable behaviors & quirks (for the clone)

1. `.mp4` URL extension ⇒ VOD classification (guide left empty); `.ts` ⇒ live channel.
2. `url-tvg` from the M3U auto-populates the wizard EPG step; EPG fetched right after
   Done; logos fetched lazily from `tvg-logo`.
3. When a 60s fixture stream ends, TiviMate immediately reconnects — looks like a loop
   with the on-screen timer restarting; useful for e2e.
4. Startup (free): always lands on the TV guide, group reset to "All channels",
   nothing auto-tunes, preview stays black until first tune. Cold-start look
   (round3): system task-snapshot as starting window → guide skeleton (header +
   black preview box, no rows) → rows populate ~2 s later → focus pill ~2.5 s.
   No splash, no welcome interstitial (round3-ref/09-coldstart-*).
5. Info overlay badges are derived from the stream: HD, 25 FPS, MONO for the fixtures.
6. Number keys, CH+/CH− zap, FF/RW paging: none of these do anything observable in
   free 5.2.0 on the emulator.
7. The app exits on BACK from guide root without confirmation (setting locked+off).
8. One crash observed opening My list (process death → launcher); non-reproducible.
9. Voice search orb is the default focus on the Search screen; text entry works only
   after moving focus onto the text bar itself.
10. Blue section headers inside menus (program title / channel name / "All channels")
    are `#2196F3`, 28px.
