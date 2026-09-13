# TiviMate UX Reference Specification

**Target app:** TiviMate IPTV Player (Android TV), package `ar.tvplayer.tv`, developer **Armobsoft FZE** (AR Mobile Dev).
**Reference version:** 5.3.3 (June 2026, per APKMirror upload 2026-06-23). The 5.x line is a major UI redesign of the classic 4.x UI; where 4.x behavior differs and is still commonly documented, it is called out as **[LEGACY 4.x]**.
**Companion app:** TiviMate Companion, package `ar.tvplayer.companion`, v1.4.5 — "The companion app for TiviMate IPTV player. It is not IPTV player!" Used to purchase/manage Premium on devices without Google Play and to manage activated devices.

**Purpose of this document:** drive a screen-for-screen, gesture-for-gesture open-source clone, later verified against the real app on an Android TV emulator.

**Notation:**
- `[FREE]` — available in the free tier.
- `[PREMIUM]` — requires TiviMate Premium.
- `[VERIFY ON DEVICE]` — could not be pinned down from sources, or sources conflict; must be confirmed on the emulator. Every such tag is also collected in §7.
- "OK" = D-pad center/select. "Back" = Android back. "Menu" = the hamburger/menu key present on some remotes (e.g., Fire TV 3-line button).

---

## 0. Product model (what the app is)

- TiviMate is a **player only**. It ships zero content. All content comes from user-supplied playlists: **M3U URL**, **Xtream Codes** login, or **Stalker Portal**. (Official description, APKMirror, all guides.)
- Designed for **Android TV / large screens / remote navigation**; the listing explicitly says it is "not optimized for touch devices such as phones or tablets." There is a separate mobile-oriented build with a portrait UI; this spec covers the TV app only.
- Free tier is a functional live-TV player with one playlist. Premium unlocks the power features (see §0.2).
- Premium is **not** an in-app purchase on sideloaded devices: the user creates a TiviMate account in the TV app (Settings → Unlock Premium), then pays in the **TiviMate Companion** phone app or on the official website, then activates the TV device against that account (device gets a user-chosen device name; account shows activated devices).

### 0.1 Pricing (historical — affects only the paywall screen copy)
- Legacy pricing (through ~2024/2025, widely documented): **$4.99/year** or **$19.99 lifetime**, up to 5 devices, 5-day trial mentioned by FireStickTricks.
- Current pricing (TROYPOINT, 2026): **$33.99 lifetime** (plus tax), 5 devices, purchase via Companion app or official website (Link payment system or credit card).
- Clone note: paywall copy/pricing is dynamic; treat exact figures as [VERIFY ON DEVICE].

### 0.2 Premium feature list (consolidated from TROYPOINT v5.3.3 guide + Optimedia + FireStickTricks)
All of the following are **[PREMIUM]**:
1. Multiple playlists (free = 1 playlist only).
2. Recording (all modes) + scheduled recordings + custom recurring recordings.
3. Catch-up / archive playback.
4. Favorites management (free app shows an "unlock premium" prompt when you try to favorite; Cancel returns you to the app).
5. Manual channel sorting; group/channel customization (hide, reorder, rename).
6. UI customization (Appearance options incl. background/accent color, panel transparency, panel timeout, customizable TV-guide update intervals).
7. Parental controls.
8. Picture-in-picture.
9. External video player support.
10. Backup/restore of settings.
11. Auto frame rate (AFR).
12. "Resume last channel on start" / startup-channel control.
13. Reminders that auto-switch ("auto-play") to a channel.
14. Multiview (simultaneous channels).
15. Auto-update playlist (per Optimedia's free/premium table).

Free tier retains: playback, TV guide with EPG, one playlist, search, basic reminders (notification only) [VERIFY ON DEVICE — whether basic reminders are free], sleep timer, subtitle/audio selection.

---

## 1. App-wide navigation model (D-pad / remote semantics)

This is the soul of the app. TiviMate's signature is that **fullscreen video is the home state**, and everything else is an overlay you summon with the D-pad. The app never shows persistent chrome over video unless summoned.

### 1.1 Context: fullscreen playback (live channel playing, no overlay)

| Input | Behavior |
|---|---|
| **OK (short press)** | Opens the **playback info overlay / channel panel**. In 5.x this is the bottom **info bar + shortcut row** (see §2.3): program artwork card, title, times, progress bar, then a horizontal row: "TV guide", "History", then recent/adjacent channel cards. In **[LEGACY 4.x]** OK opened the left-side channel-list panel directly. [VERIFY ON DEVICE — whether 5.x OK opens the bottom overlay, the side channel list, or both in sequence] |
| **OK (long press)** | Opens the **player options menu** (right-side or popup list): Add to Favorites, Closed captions (CC), audio track, video/audio settings, sleep timer, multi-view, open in external player, audio sync offset, etc. (TROYPOINT: "Long-press Select during playback opens options — favorites, CC, etc."; Guru99 reaches Multi-view and Record from long-press OK.) Exact item order [VERIFY ON DEVICE]. |
| **Up / Down** | Default: opens/scrolls the **quick channel list** without leaving playback (TROYPOINT: "Down button during playback opens the channel list without returning to the EPG"). A Playback setting can instead make Up/Down **zap channels directly** (channel surf). Default assignment [VERIFY ON DEVICE]. |
| **Left / Right** | With the info overlay hidden: Right opens **program details / EPG strip for the current channel** (browse previous/next programs of the playing channel; TROYPOINT: "Left/Right: previous/next program"). [VERIFY ON DEVICE — 5.x may require the info bar to be open first] |
| **Back** | Leaves fullscreen playback and returns to the **TV guide** (main screen) with the current channel continuing in the preview window / behind the guide. Pressing Back again from the guide root triggers the **exit-confirmation dialog** if enabled ("Are you sure you want to exit?" style; the confirm-on-exit toggle exists in settings). Chain: playback → guide → (confirm) → home screen. |
| **Channel Up / Channel Down** (dedicated keys) | Zap to next/previous channel in the current group. [VERIFY ON DEVICE — wrap-around at ends] |
| **Number keys (0–9)** | Direct channel entry: typed digits appear in a small overlay (top-right/top-left); after a ~2 s timeout or pressing OK, the app tunes to that channel number. Invalid number → overlay dismisses with no change. [VERIFY ON DEVICE — overlay position, timeout length, OK-to-commit] |
| **Play/Pause** | Pause/resume the stream. On live streams without timeshift the stream re-joins live on resume; with catch-up/timeshift-capable sources it resumes from pause point. [VERIFY ON DEVICE — free-tier pause behavior] |
| **Fast-forward / Rewind** | In catch-up/recording playback: seek. During live playback: [VERIFY ON DEVICE — no-op vs. channel-list paging]. |
| **Menu key** (remotes that have it) | Opens the player options menu (same as long-press OK). [VERIFY ON DEVICE] |

Overlay auto-hide: the info overlay dismisses itself after a timeout (~5 s; "panel timeout" is a Premium-customizable Appearance setting). Back also dismisses any overlay without leaving playback.

### 1.2 Context: playback info overlay open (bottom bar, 5.x)

Observed directly in official screenshot #1:
- Layout: program **artwork card** (left), program **title**, **time range** ("07:30 — 10:00 AM"), a small dash-progress pill + "**35 min**" (minutes remaining), **HD** and **STEREO** badges; a full-width thin **blue progress bar** with a round thumb; below it a horizontal **shortcut row**: `TV guide` (grid icon) card, `History` (clock-with-arrow icon) card, then channel cards (logo + thin progress bar under each) for recent/nearby channels; a **down-chevron** centered below hints that pressing Down reveals more.
- Left/Right: move focus across the shortcut/channel row. OK on a channel card: tune to it. OK on "TV guide": open guide. OK on "History": open history list.
- Down: expands to more rows / full channel list [VERIFY ON DEVICE].
- Up: focus the progress bar / program info for seek (catch-up) [VERIFY ON DEVICE].
- Back: hide overlay, stay fullscreen.

### 1.3 Context: channel-list side panel (quick zap panel)

**[LEGACY 4.x]** (still the mental model for most community documentation; 5.x keeps an equivalent panel reachable from playback):
- Two-column overlay on the left of the video: **Groups column** (further left) and **Channels column**; video keeps playing behind (panel is translucent; transparency is an Appearance setting).
- Up/Down: move channel selection. OK: tune to selected channel and close panel (or first press shows the channel's EPG detail; see "panel OK behavior" [VERIFY ON DEVICE]).
- Left: focus moves to the **groups column**; Up/Down selects group; Right or OK returns to channels of that group.
- Right (on a channel): opens the **program info column** for that channel (current + upcoming program list) — the "press right for EPG detail" signature. Right again / OK on a program: program detail popup.
- Back: close panel (return to clean fullscreen).
- Rewind / Fast-forward: **page-scroll the channel list** up/down without changing channel (TROYPOINT).
- Long-press OK on a channel: **channel context menu** — Add/remove favorite, Hide channel, Move (manual sort) [PREMIUM], Change group, Rename, Change logo, Block (parental), Open in external player, Add to Multiview. Exact items and order [VERIFY ON DEVICE].
- The panel shows for each channel: number, logo, name, current program title + progress bar. Panel auto-hides after timeout (Premium-customizable).

### 1.4 Context: TV guide (main screen)

| Input | Behavior |
|---|---|
| **Up/Down** | Move channel row selection (guide scrolls vertically; selected row highlighted). |
| **Left/Right** | Move horizontally across program cells in the timeline (earlier/later). Moving left of "now" enters past programs (catch-up territory, dimmed cells). Holding scrolls fast. |
| **OK on a program cell (current program)** | Tune to that channel; if pressed on the channel that is already playing, switches to fullscreen playback. First-OK vs second-OK semantics [VERIFY ON DEVICE]. |
| **OK on a future program** | Opens the **program detail popup** (with actions: Set reminder, Record, etc.). |
| **OK on a past program (catch-up available)** | Plays the archived broadcast [PREMIUM] or opens detail popup with a Play action. |
| **Long-press OK** | Context menu on the program/channel: Record / Custom recording (TROYPOINT: "long-press Select in the guide → 'Custom Recording'"), Set reminder, Add to favorites, channel options. |
| **Back** | Focus jumps from the program grid to the **groups/nav column** on the left (TROYPOINT: "Back: shows groups"); Back again → exit-confirm dialog (if enabled) → leave app. |
| **Left at leftmost cell** | Also lands on the groups column / left nav rail. |
| **Rewind / Fast-forward** | Page the guide vertically (channel list) without changing channel (TROYPOINT). [VERIFY ON DEVICE — in 5.x these may page by day/timeline instead] |
| **Channel Up/Down** | Page the guide vertically. [VERIFY ON DEVICE] |
| **Menu key** | Opens guide options: "Group options" (Manage groups / Manage positions), bulk favorites add (TROYPOINT). |
| **Number keys** | Jump/tune to channel number. [VERIFY ON DEVICE] |
| **Play/Pause** | [VERIFY ON DEVICE — likely tunes selected channel or no-op] |

Day navigation: the guide header shows date + clock ("Fri, 14 Nov, 10:35 PM" in accent color, screenshot #3). Scrolling right/left crosses midnight into next/previous days; a **jump-to-day / jump-to-time** control exists (the catch-up screen shows a dedicated day column — screenshot #6; whether the main guide has an explicit day-picker popup is [VERIFY ON DEVICE]).

### 1.5 Context: settings

Standard Android-TV list navigation: Up/Down moves items, OK enters/toggles, Back goes up one level. Two-pane layout (sections left, items right) [VERIFY ON DEVICE — 5.x settings may be a single navigable column with sub-screens].

### 1.6 Global keys

- **Double-press behaviors:** community docs do not describe any double-press gesture; assume none. [VERIFY ON DEVICE — e.g., double-Back to exit]
- **Previous-channel recall:** the 5.x info overlay's channel row and the "History" entry serve last-channel recall. A dedicated "previous channel" key/gesture [VERIFY ON DEVICE].
- **Remote-key remapping:** a settings area lets users re-assign some buttons (see §2.13 Remote control settings) [VERIFY ON DEVICE — extent].

---

## 2. Screen catalogue

### 2.1 First run / welcome + add-playlist wizard [FREE]

1. **Terms/usage acceptance** on first launch (Guru99 mentions accepting Terms and Conditions). Single dialog, OK to accept. [VERIFY ON DEVICE — exact copy]
2. **Welcome screen** — near-empty dark screen, TiviMate logo, single focused button/action: **"Add playlist"**.
3. **Playlist type chooser** — three options (screenshot #3 badges confirm naming): **M3U**? / **Xtream Codes** / **Stalker Portal**. On-screen labels historically: "M3U playlist", "Xtream Codes login", "Stalker Portal". Exact 5.x strings [VERIFY ON DEVICE].
4. **M3U path:** text field for **playlist URL** (provider-emailed, credentials usually embedded as query params). "Next". The app fetches and parses; shows **channel count** (and VOD counts in 5.x: movies/series). User can **rename the playlist** and select which sub-playlists (TV / VOD) to include (FireStickTricks). "Next".
5. **EPG step (M3U only):** field "Enter URL" for the XMLTV EPG address (`http://server/xmltv.php?username=X&password=Y` format for Xtream-backed M3U). Optional; skippable. "Next"/"Done".
6. **Xtream path:** fields: playlist **name**, **server URL incl. port** (e.g., `http://host:80`), **username**, **password** → "Add"/"Next". EPG auto-configures from the Xtream server (Optimedia: EPG loads automatically, ~7 days, refreshes in background each morning).
7. **Stalker path:** portal URL + MAC address entry. [VERIFY ON DEVICE — field list, whether MAC is auto-generated/editable]
8. **Remote-navigation hint dialog** — after adding the first playlist, TiviMate shows a one-time screen explaining remote navigation; user presses "OK" to finish (FireStickTricks).
9. First load: parsing channels + EPG takes 1–5 min with a progress/status indicator in the status bar; then lands on the **TV guide**.

Failure modes: connection errors surface a retry dialog (Optimedia: verify `http://` prefix and port; VPN can interfere with adding a service).

### 2.2 Main structure / navigation rail (5.x) [FREE]

Observed in screenshots #3/#4:
- A **left icon rail** (collapsed) that expands to a labeled nav column: **Search** (magnifier), **TV** (screen icon), **Movies** (clapperboard), **TV Shows** [VERIFY ON DEVICE — icon], **Recordings** (DVR badge icon), **My list** (bookmark icon). FireStickTricks names the seven sections: "Search, TV, Movies, TV Shows, DVR, Favorites, and Settings". Settings gear lives at the bottom of the rail [VERIFY ON DEVICE — position].
- Expanded nav shows the **tivimate** wordmark top-left ("tivi" in teal/cyan, "mate" in white).
- Selected nav item: filled pill in the **accent color** (blue default) with white icon+label. Focused item: white pill, black text.
- Next to the rail, the **groups column**: playlist name headers (collapsible, chevron up/down — e.g., "John's playlist", "Family playlist"), each expanding to its groups: "All channels", then provider categories (Comedy, Entertainment, Nature, …) plus special groups **History** and **Favorites** [VERIFY ON DEVICE — whether History/Favorites appear inside the group list; screenshot #3 shows "History" as a group under the playlist header].
- Multiple playlists merge into this one view [PREMIUM for >1 playlist].

### 2.3 Main playback screen (fullscreen) [FREE]

- Clean video, zero chrome by default.
- Summoned bottom overlay (screenshot #1): scrim gradient from bottom; program artwork card ~160×90; title 28–32 px semi-bold white; time range + remaining minutes in secondary grey; **HD**/**STEREO** metadata badges (small dark pills, uppercase); full-width 4 px blue progress bar with round thumb; shortcut row of rounded cards (~150×110): icon + label ("TV guide", "History"), then channel logo cards each with a thin progress bar of the channel's current program; centered down-chevron.
- Buffering indicator: centered spinner over video [VERIFY ON DEVICE — style]; stream stats (quality, FPS, sound) shown in the video details area per FireStickTricks ("Video details: shows quality, frame rate (FPS), and sound quality by default").
- Channel-number zap overlay (digits) [VERIFY ON DEVICE — position/style].

### 2.4 Channel-list side panel [FREE]

See §1.3 for interaction. Layout:
- Translucent dark panel over left portion of video (transparency/timeout Premium-tunable).
- Groups column: text list; selected group highlighted.
- Channels column rows: **number** • **logo tile** (rounded dark-grey square/rect with logo centered) • **name** • current program title (secondary line) • thin progress bar.
- Right-side program info column (when opened with Right): list of current + upcoming programs with times for the highlighted channel; OK opens program detail popup.
- [VERIFY ON DEVICE — exact 5.x panel composition; screenshots show the guide-style presentation, and community docs describe the 4.x panel.]

### 2.5 Full TV guide (EPG grid) [FREE]

Observed in screenshot #3:
- **Top-left preview window** (~1/3 width) playing the current channel live; **top-right program info**: title (e.g., "The Midnight Hour"), time range "10:15 — 11:45 PM", small progress dash + "31 min" remaining, then 2–3 lines of program description (grey).
- **Guide header row**: left cell shows date+time "Fri, 14 Nov, 10:35 PM" in **accent blue**; timeline labels every 30 min ("10:00 PM  10:30 PM  11:00 PM").
- **Now-line**: thin vertical light-blue line across all rows at the current time.
- **Channel column**: channel **number** (grey), **logo tile** (rounded dark rect), **channel name** (white; the currently-playing channel's name renders in accent blue with a small ▶ play triangle at the row's right edge).
- **Program cells**: rounded rects; base fill dark grey-blue; **past programs dimmer/darker**; **focused cell lighter grey**; text single-line ellipsized. Cell width proportional to duration; 30-min grid.
- Left of the grid: groups column / nav rail (§2.2).
- Row height ~64 px at 1080p; 5–7 rows visible under the preview area.
- Progress shading: the current program cell shows elapsed portion via the now-line position (no per-cell fill visible in screenshots) [VERIFY ON DEVICE — whether current-program cells also get a subtle elapsed-fill].
- Icons in cells: record dot for scheduled recordings, bell for reminders, rewind/clock glyph for catch-up-able programs [VERIFY ON DEVICE — exact glyphs].

### 2.6 Program detail popup [FREE]

- Invoked by OK on a non-playing program (future/past) or from the program info area.
- Contents: title, time range, description, artwork if provided by EPG; action buttons: **Play** (if live/catch-up), **Set reminder** (future), **Record** [PREMIUM], **Custom recording** [PREMIUM], **Cancel**/Back to dismiss.
- [VERIFY ON DEVICE — exact button set and order; whether it is a centered modal or a side sheet.]

### 2.7 Search [FREE with premium aspects — FireStickTricks lists "Powerful search" under premium; VERIFY]

Observed in screenshot #5:
- Full-screen: top **search field** (rounded, light) with magnifier icon at left and inline cursor; typed text underlined.
- Results grouped in horizontal shelves with section headers: **"Movies"** (portrait poster cards, caption strip at bottom of card), **"Channels"** (landscape logo cards with thin progress bar), presumably **"TV Shows"** and **"Programs"** shelves as well [VERIFY ON DEVICE].
- **Custom on-screen keyboard** (TiviMate-drawn, not the system IME): suggestion row of rounded word chips above the keys; QWERTY layout with digit superscripts on the top row (long-press or secondary for digits), keys as dark rounded squares, focused key white with black glyph; bottom row: `123?`, cursor-left, cursor-right, space, `-`, `_`, and a blue **search/magnifier** key.
- Keyboard appears as an overlay in the lower-right; results remain visible behind/above.

### 2.8 Favorites management [PREMIUM]

- Adding: (a) player options (long-press OK during playback) → "Add to Favorites"; (b) TV guide → Menu key → bulk-add UI; (c) channel context menu in lists.
- A **Favorites** group appears in the groups column; TROYPOINT recommends moving it to top via "Manage positions".
- 5.x additionally has **"My list"** in the nav rail (bookmark icon) — for VOD/shows watchlist; distinct from channel Favorites [VERIFY ON DEVICE — semantics of My list vs Favorites].
- Free-tier behavior: attempting to favorite triggers the **unlock-premium prompt**; Cancel dismisses.

### 2.9 Recordings / DVR [PREMIUM]

- Nav section **Recordings** (DVR icon). Sub-areas: active/scheduled recordings list, completed recordings library, and **custom recurring recordings**. [VERIFY ON DEVICE — tab names]
- **Storage setup:** Settings → Recordings → folder path via **"Select Folder"** (internal or USB; FAT32 USB per TROYPOINT; Guru99: local or external e.g. USB hub).
- **Ways to record:**
  1. Instant: player options / "Record" button while watching (Guru99, FireStickTricks).
  2. From guide: select/long-press a current or future program → Record → creates a scheduled recording for that program's time slot.
  3. **Custom recording:** long-press in guide → "Custom Recording" → "New Recording" → pick channel, date/time window, repeat (recurring) options (TROYPOINT).
- Multiple simultaneous recordings supported.
- Recording playback: from Recordings section; transport controls with FF/RW seek; resume position [VERIFY ON DEVICE].
- Conflicts/margins: pre/post padding settings [VERIFY ON DEVICE — existence and defaults].

### 2.10 Catch-up / archive playback [PREMIUM]

Observed in screenshot #6 (single-channel archive browser):
- Layout: **program list** (left, wide): rows "HH:MM AM/PM  Title"; **section headers** for days in accent blue ("Yesterday"); past-day items greyed by recency; focused row is a **white pill with black text and a ▶ icon** at right.
- **Day column** (middle-right): vertical list "Tue 28 Oct", "Wed 29 Oct", "Thu 30 Oct", "Fri 31 Oct" (focused = white card), "Sat 1 Nov" — selecting a day jumps the list.
- **Detail card** (right): preview image, program title, time range ("00:00 AM — 2:45 AM") in a dark rounded card.
- Entry points: guide (select past program on a catch-up channel), channel panel, or program detail popup.
- Catch-up availability is provider-dependent; per-playlist **catch-up type** configuration exists in playlist settings (community-documented values: Default, Append, Shift, Flussonic, Xtream Codes + days) [VERIFY ON DEVICE — exact option list].
- Playback: full transport (seek/FF/RW/pause). APKMirror 5.3.3 changelog: "fixed missing catch-up for some m3u playlists".

### 2.11 Reminders [FREE basic / PREMIUM auto-switch]

- Set from program detail popup or long-press on a future program → "Set reminder".
- A bell indicator marks reminded programs in the guide [VERIFY ON DEVICE].
- At program start: popup/notification appears; **Premium**: reminder can automatically switch ("auto-play") to the channel (TROYPOINT premium list).
- Reminder management list (view/delete all) [VERIFY ON DEVICE — location, possibly under a nav section or settings].

### 2.12 Multiview [PREMIUM]

Observed in screenshot #2:
- Multiple video tiles on screen (2 shown; up to 4 in a 2×2 grid per community knowledge [VERIFY ON DEVICE — max tiles]).
- Focused tile has a context menu (dark rounded panel; focused row = white pill): **"Add screen"**, **"Search and add"**, **"Change channel"**, **"Play"**, **"Enlarge screen"**, **"Full screen"**, **"Remove screen"** (exact labels from screenshot).
- Audio follows the focused/selected tile; other tiles muted [VERIFY ON DEVICE].
- Entry: player options → "Multi-view mode" → "Add screens" (Guru99).
- Exit: "Full screen" on a tile returns to normal playback.

### 2.13 Settings tree

Top-level Settings entries (merged from Guru99/TROYPOINT/FireStickTricks/Optimedia + community knowledge; exact 5.x order [VERIFY ON DEVICE]):

1. **Playlists** [FREE for 1; PREMIUM for >1]
   - List of configured playlists; OK on one opens per-playlist settings:
     - Name (text)
     - URL / credentials (text; re-enter)
     - **Update playlist** now (action)
     - Auto-update interval / update on app start (list) [PREMIUM per Optimedia "auto-update playlist"]
     - **Catch-up type** (list: Default / Append / Shift / Flussonic / Xtream Codes) + catch-up days (number) [VERIFY ON DEVICE]
     - Numbering / channel-number origin (list) [VERIFY ON DEVICE]
     - EPG time offset / time shift (list of hour offsets) [VERIFY ON DEVICE]
     - User agent (text) [VERIFY ON DEVICE]
     - Included sub-playlists: Live TV / Movies / Series toggles (5.x smart view) [VERIFY ON DEVICE]
     - Delete playlist (action + confirm)
   - "Add playlist" (action → wizard §2.1)
   - "Category settings" — enable/disable provider categories (Optimedia: Settings → "Playlist → Category Settings")
   - "Refresh"/"Update" action (Optimedia: fixes missing channels, re-sorts categories)
2. **EPG** [FREE]
   - **EPG sources** list → "Add EPG source" → "Enter the URL" or "Paste from clipboard" → OK (Guru99); per-source enable toggle; **"Default source"** toggle (FireStickTricks: Settings → EPG → enable "Default source")
   - **Update interval** (list; TROYPOINT recommends 24 hours; customizable intervals are [PREMIUM])
   - **Update on app start** (toggle; TROYPOINT recommended Off)
   - **Update on playlist change** (toggle; TROYPOINT recommended On)
   - **Past days to keep EPG** (number; TROYPOINT recommends 1)
   - **Update EPG now** (action; Optimedia "Update EPG Now")
3. **Appearance** [mostly PREMIUM]
   - Accent color (list with color dots: Pink, Purple, Indigo, **Blue** (default), Cyan, Teal, Green, Lime, Yellow, Amber — screenshot #4; check-circle on the selected color)
   - Background color (Guru99; premium per Guru99 note)
   - Font size (list: small/normal/large) (Guru99)
   - Text color (Guru99) [VERIFY ON DEVICE — scope]
   - UI transparency / panel transparency (slider/list) [PREMIUM]
   - Panel timeout (list, seconds) [PREMIUM]
   - Sub-areas per Guru99: **Logos** (channel logo options: show/hide, background shape), **TV Guide** (rows/density, hours visible, show progress), **Player** (overlay elements), **Language** [VERIFY ON DEVICE — item-level detail]
   - Clock format / show clock [VERIFY ON DEVICE]
4. **Playback** [FREE core]
   - **Buffer size** (list) (Guru99)
   - **Playback speed** (list) (Guru99) [VERIFY ON DEVICE — live vs VOD scope]
   - Video decoder (Auto / Hardware / Software) [VERIFY ON DEVICE]
   - **Auto frame rate (AFR)** (toggle + options) [PREMIUM]
   - Aspect ratio default (list: Fit/Fill/16:9/4:3/Stretch) [VERIFY ON DEVICE — names]
   - Up/Down button action during playback (zap vs open panel) [VERIFY ON DEVICE — exact setting name/location]
   - Audio: passthrough, tunneled playback, volume normalization [VERIFY ON DEVICE]
   - Subtitles defaults ("Show closed captions for all channels if available" — Guru99)
   - Network/HTTP options [VERIFY ON DEVICE]
5. **Recordings** [PREMIUM]
   - Recording folder ("Select Folder" — internal/USB)
   - Pre/post recording margins [VERIFY ON DEVICE]
   - Filename template [VERIFY ON DEVICE]
6. **Remote control** [VERIFY ON DEVICE — section existence/name in 5.x]
   - Button remapping (e.g., what OK/Up/Down/colored keys do)
   - Long-press assignments
7. **Parental controls** [PREMIUM]
   - Set/change **PIN**
   - Require PIN for: app access, playlists, TV Guide, group options, settings (TROYPOINT: "PIN required for playlists, TV Guide, group options, etc.")
   - Per-channel blocking is done from the channel's own context menu ("Block"/channel settings)
   - Guru99 path note: Settings → "Preferences" → "Parental Controls" [VERIFY ON DEVICE — whether nested under a Preferences group]
8. **General** [VERIFY ON DEVICE — section name]
   - **Auto start on boot** (toggle; Guru99 "Auto Start App on Boot")
   - **Exit confirmation** (toggle; TROYPOINT)
   - Startup behavior: resume last channel [PREMIUM] / start on guide
   - Time format (12/24 h) [VERIFY ON DEVICE]
   - Language
9. **Backup and restore** [PREMIUM]
   - Backup settings to file/folder; Restore from file; includes playlists/settings/favorites [VERIFY ON DEVICE — scope and cloud option]
10. **TiviMate Companion** (Optimedia: Settings → "TiviMate Companion → Enable Server") — toggle that runs a local server so the phone Companion app can find the TV for pairing/casting/keyboard.
11. **Unlock Premium** — account/activation flow (§3.11).
12. **Profiles** [VERIFY ON DEVICE — multi-profile support and its UI, referenced in community docs for 4.7+]
13. **About** — version, device ID [VERIFY ON DEVICE].

Item types: toggles (switch on right), lists (open radio dialog), actions (immediate), text fields (open TiviMate keyboard §2.7).

### 2.14 Unlock Premium / account screens

- Settings → **"Unlock Premium"** → informational page → **"Next"** → **"Account"** → email + password fields → **"Sign up"** (new) with re-entered password → **"Create account"**; or **"Log in"** (existing).
- After payment (Companion/website): back on TV → "Next" → device name field → **"Activate"** → **"OK"** (TROYPOINT exact flow).
- Website account page lists activated devices; 5-device limit.

### 2.15 History [FREE]

- Reachable from the playback overlay shortcut row ("History" card) and as a group in the groups column (screenshot #3 shows "History" under the playlist header).
- Chronological list of recently watched channels/content. [VERIFY ON DEVICE — retention length, clear-history action]

### 2.16 Movies / TV Shows (VOD) [FREE; 5.x]

- Nav sections **Movies** and **TV Shows** expose the playlist's VOD catalogue: category rows/grid of portrait poster cards with caption strip.
- Detail page per title with Play/Resume; series → season/episode lists. [VERIFY ON DEVICE — layout details; this spec's clone scope may exclude VOD]
- FireStickTricks: content appears within a few minutes of playlist setup; sub-sections mirror provider VOD categories.

---

## 3. Flows (step-by-step)

### 3.1 First launch → playlist added → channels loaded [FREE]
1. Launch → accept terms dialog.
2. Welcome screen → OK on "Add playlist".
3. Choose type (M3U / Xtream Codes / Stalker Portal).
4. Enter URL/credentials (TiviMate keyboard) → Next.
5. App downloads playlist → shows counts (channels / movies / series) → optionally rename, toggle included content → Next.
6. (M3U) EPG URL step → enter or skip → Next/Done.
7. One-time remote-navigation hint → OK.
8. Land on TV guide; EPG populates over the next minutes (status-bar progress); channel logos load lazily.

### 3.2 Change channel — via list [FREE]
1. Watching fullscreen → press OK (or Down) → panel/overlay appears, video keeps playing.
2. Up/Down to highlight another channel (RW/FF to page).
3. OK → tunes; overlay auto-hides.
4. To change group: Left to groups column → pick group → Right/OK back into channels.

### 3.3 Change channel — zap by number [FREE]
1. During playback press digits, e.g. `2`, `4`.
2. Digits render in the zap overlay; after ~2 s (or OK) tunes to channel 24. [VERIFY ON DEVICE — timeout, cancel via Back]

### 3.4 Change channel — CH+/CH− or Up/Down-zap [FREE]
1. Press Channel Up/Down (or D-pad Up/Down if configured to zap) → immediate tune to adjacent channel in the current group, brief info overlay flashes with the new channel's info, then auto-hides.

### 3.5 Browse guide while watching [FREE]
1. From fullscreen press Back (or OK → "TV guide" card) → guide opens; the current channel keeps playing in the preview window (TROYPOINT: guide reopens "with your current channel playing in the background").
2. Navigate rows/timeline; program info pane updates as focus moves.
3. OK on a live program → tunes and (second OK / OK on playing channel) returns fullscreen.
4. Back → groups column; Back again → exit prompt.

### 3.6 Set a recording [PREMIUM]
1. Ensure storage: Settings → Recordings → Select Folder (USB FAT32 or internal).
2. Instant: while watching → long-press OK → Record. Recording badge appears; recording continues in background.
3. From guide: focus a program → OK → detail popup → Record; or long-press OK → Record.
4. Custom/recurring: guide long-press → "Custom Recording" → "New Recording" → channel, start/end, repeat pattern → save.
5. View/manage under nav → Recordings (scheduled list, completed list). Multiple simultaneous recordings OK.

### 3.7 Set a reminder [FREE basic / PREMIUM auto-switch]
1. Guide → focus a future program → OK → detail popup → "Set reminder".
2. Bell icon marks the cell. At start time a prompt appears; with Premium auto-switch enabled the app tunes automatically.

### 3.8 Add a favorite [PREMIUM]
1. While watching → long-press OK → "Add to Favorites". (Free build: premium prompt appears here; Cancel to dismiss.)
2. Bulk: TV guide → Menu key → favorites/bulk-add UI → toggle channels.
3. Favorites group appears in groups column; reorder group to top via Menu → "Group options" → "Manage positions".

### 3.9 Reorder / hide channels and groups [PREMIUM]
1. Groups: TV guide → Menu → "Group options" → "Manage groups" → toggle visibility per group; "Manage positions" → move groups up/down.
2. Channels: channel context menu (long-press in list/guide) → Move / Hide / Change group / Rename / Change logo. [VERIFY ON DEVICE — exact labels]

### 3.10 Edit playlist & EPG source management [FREE/PREMIUM]
1. Settings → Playlists → select playlist → edit fields (name/URL), Update now, delete.
2. Settings → EPG → EPG sources → add source URL ("Enter the URL" / "Paste from clipboard"), toggle "Default source".
3. Set update interval (24 h recommended), "Update on app start" Off, "Update on playlist change" On, past days 1 (TROYPOINT recommendations).
4. Force refresh: "Update EPG now".

### 3.11 Unlock Premium / activate device
1. Settings → Unlock Premium → Next → Account → Sign up (email, password ×2) → Create account.
2. On phone: install TiviMate Companion → Account → log in → choose plan → pay (Link / credit card). (Or pay on the official website.)
3. Back on TV: Next → enter device name → Activate → OK. Premium features unlock immediately (~30 s per Optimedia).

### 3.12 Parental lock [PREMIUM]
1. Settings → Parental controls → set PIN.
2. Choose protected surfaces (playlists / TV Guide / group options / settings).
3. Block individual channel: channel context menu → Block (PIN required to view thereafter).
4. PIN dialog: 4-digit entry via D-pad [VERIFY ON DEVICE — digit count/UI].

### 3.13 Backup / restore [PREMIUM]
1. Settings → Backup and restore → Backup → choose location (local folder/USB) → file written.
2. Restore → pick backup file → app restores playlists/settings/favorites → restart [VERIFY ON DEVICE — restart behavior and backup scope].

### 3.14 Subtitles / audio / sync [FREE]
1. Long-press OK during playback → **CC** → switch "Off" → "Closed captions 1" (per-channel availability).
2. Audio track selection from the same options menu [VERIFY ON DEVICE — menu label].
3. Audio sync: press OK during playback and adjust offset up/down until lips match (TROYPOINT).

### 3.15 Sleep timer [FREE]
1. Player options → stopwatch/timer icon (near subtitle controls per Guru99).
2. Right-side menu with durations **15–240 minutes** (FireStickTricks) → select → app stops playback/exits when timer fires [VERIFY ON DEVICE — action at expiry, increments].

### 3.16 Multiview [PREMIUM]
1. Player options → "Multi-view mode".
2. Tile menu: Add screen / Search and add → pick channel per tile.
3. Focus a tile → OK → menu (Change channel / Play / Enlarge screen / Full screen / Remove screen).
4. "Full screen" exits multiview to normal playback.

### 3.17 Catch-up viewing [PREMIUM]
1. Guide: navigate left into past cells of a catch-up-enabled channel → OK → plays archive; or
2. Open the channel's archive browser (screenshot #6): pick day in day column → pick program → OK (focused row shows ▶) → playback with full seek.

### 3.18 PiP and external player [PREMIUM]
1. PiP: from fullscreen playback invoke PiP (player options / home-press behavior) → video shrinks to system PiP while other apps run. [VERIFY ON DEVICE — trigger]
2. External player: enable in settings ("use external player"), then channel context menu → "open in external player" (TROYPOINT/Guru99).

---

## 4. Visual design tokens (from official 1920×1080 screenshots)

> Hexes are sampled-by-eye approximations from Play Store screenshots; confirm with a screen-grab color picker on the emulator.

### 4.1 Color palette (dark theme, 5.x)
- **App background:** near-black desaturated navy, ~`#0B0F14` – `#10141B`.
- **Surface / panels (menus, cards, keyboard):** `#1D232D` – `#252C38` (dark blue-grey), rounded corners ~8–12 px.
- **Surface variant (program cells):** base `#2A303C`; past programs darker/dimmer `#1E242E`; focused cell `#3E4654`.
- **Focus highlight (lists/menus/keyboard):** **solid white `#FFFFFF` rounded pill/rect with black `#0D0D0D` text/icons.** This is the universal focus treatment (menu rows, day cards, program rows, keys, group pills).
- **Accent (default Blue):** `#2F80ED`–`#3B82F6` — used for: selected nav pill fill, progress-bar fill, now-line (lighter, ~`#7FB2F0`), date/section headers ("Yesterday", guide date), playing-channel name + ▶ glyph, checkmark circles, search key.
- **Accent options:** Pink, Purple, Indigo, Blue (default), Cyan, Teal, Green, Lime, Yellow, Amber.
- **Primary text:** `#FFFFFF`. **Secondary text:** `#98A2B3` (grey-blue). **Disabled/past:** ~55% white.
- **Scrims:** bottom gradient scrim over video for overlays, black at ~85% fading to 0% by mid-screen; side-panel scrim similar from left. [VERIFY ON DEVICE — exact alphas]
- **Badges (HD/STEREO):** dark grey pill `#3A414D` with white 10–11 px uppercase text; slight rounding.
- **Wordmark:** "tivi" teal/cyan `#20C5C8` + "mate" white. [VERIFY ON DEVICE]

### 4.2 Typography
- Family: system sans (Roboto or very close). No serif anywhere.
- Scale (at 1080p, approx): program title in overlay/info 28–32 px semi-bold; section headers ("Movies", "Accent color") 24–26 px medium; list rows / program cells 20–22 px regular; secondary metadata 18–20 px regular; badges 10–12 px caps.
- Line style: single-line ellipsis in cells; times use en-dash format "07:30 — 10:00 AM"; remaining time as "35 min".

### 4.3 Spacing & density
- Guide rows ~60–68 px tall; 30-min timeline columns; 4–8 px gaps between cells (cells read as separate rounded blocks, not a contiguous grid).
- Cards in shortcut row ~150×110 px, 12 px gaps.
- Panel padding ~16–24 px; menu rows ~56 px tall.

### 4.4 Focus & motion
- Focus = white fill (no border/glow, no scale-up visible in stills). Transitions are fast fades/slides; overlays slide from bottom (playback bar) or left (panel). [VERIFY ON DEVICE — animation durations]

### 4.5 Progress bars & indicators
- Thin 3–4 px rounded track `#3A4150`, blue fill, optional round thumb (playback bar).
- Channel cards carry a mini progress bar of the live program.
- Now-line: 2 px light-blue vertical rule spanning the grid.

### 4.6 Channel logos
- Rendered inside rounded-rect tiles (dark grey `#262D38`), logo centered with padding, preserving aspect; tile ~ 84×56 px in guide rows. Logos with transparency sit on the tile background (Appearance has logo-background options per Guru99 "Logos").

---

## 5. Known behaviors & quirks

- **EPG updates:** default/customary interval 24 h (custom intervals [PREMIUM]); "update on app start" and "update on playlist change" toggles; "past days to keep" trims history. Xtream sources auto-provide ~7 days and refresh in the background each morning (Optimedia). Full first load 2–5 min with status-bar progress.
- **Playlist caching:** channels cache locally after first load so subsequent launches are instant (Optimedia).
- **Channel numbering:** numbers come from playlist order by default; visible as the left column in guide/panel; zap-by-number uses them. Renumbering rules and per-playlist numbering options [VERIFY ON DEVICE].
- **Group handling:** provider categories become groups; "All channels" is always present; groups can be hidden/reordered [PREMIUM]; special groups: Favorites, History; multiple playlists each contribute their own group sets under collapsible playlist headers (5.x).
- **All-channels sorting:** 5.3.3 changelog fixed channel sorting in the "All channels" group — sorting there follows playlist/number order [VERIFY ON DEVICE].
- **Buffering indicator:** spinner over video; "video details" readout shows resolution/FPS/audio info (FireStickTricks). Buffer size adjustable in Playback settings.
- **Audio track / subtitles:** selected via player options (CC entry lists "Off", "Closed captions 1", …); per-channel availability; optional "show closed captions for all channels if available" setting.
- **Aspect ratio:** cycle/choose from player options; default set in Playback settings [VERIFY ON DEVICE — option names].
- **Playback speed:** adjustable per Guru99 (Playback settings); primarily meaningful for catch-up/recordings [VERIFY ON DEVICE].
- **Sleep timer:** 15–240 min from the player timer icon.
- **Auto frame rate (AFR):** [PREMIUM] switches display refresh to match content; device support required.
- **VPN interplay:** adding a playlist can fail while a VPN is connected (FireStickTricks tip: disconnect → add → reconnect); otherwise VPN-agnostic.
- **Exit confirmation:** optional dialog so chained Back presses don't quit accidentally.
- **Fake-app warning:** many scam "TiviMate premium mod" APKs and fake "TiviMate subscriptions" exist; real app sells no content (relevant only to docs/README of the clone).
- **Companion pairing:** TV-side "Enable Server" setting; Companion (per Optimedia) then offers phone-remote with keyboard, search-and-cast to TV, and wireless APK push. (APKCombo's listing documents only premium-unlock + device management — treat extra Companion capabilities as [VERIFY].)
- **Recording storage:** USB must be FAT32 (TROYPOINT); recordings land in the chosen folder; multiple parallel recordings supported.
- **Free-tier favorite prompt:** favoriting in free build opens the premium upsell — an intentional discoverability quirk to replicate for fidelity (with the clone's own upsell disabled, presumably).

---

## 6. Sources

| # | Source | Contribution |
|---|---|---|
| 1 | Official Play Store screenshots (6, via APKCombo mirror of play-lh.googleusercontent.com, fetched 2026-09-13, 1920×1080) | §2 layouts, §4 all visual tokens, multiview menu labels, accent-color list, guide anatomy, search + keyboard, catch-up browser, playback overlay anatomy |
| 2 | TROYPOINT — "How to Install TiviMate IPTV Player V5.3.3" (troypoint.com/tivimate-iptv-player, June 2026) | Premium feature list, pricing $33.99/5 devices, remote button map (Select/Back/L-R/U-D/RW-FF/Menu/long-press), groups management flow, favorites flows, EPG recommended settings + defaults, recording modes + FAT32, premium activation wizard step-by-step, parental control scope, subtitle flow, audio-sync flow |
| 3 | FireStickTricks — "TiviMate IPTV Player guide" (firesticktricks.com/tivimate-iptv-player.html) | 7 nav sections (Search/TV/Movies/TV Shows/DVR/Favorites/Settings), add-playlist wizard steps incl. rename + TV/VOD selection + EPG URL + remote-hint dialog, legacy pricing $4.99yr/$19.99 lifetime + 5-day trial, sleep timer 15–240 min, multiview entry, video-details readout, premium purchase via Companion, VPN quirk |
| 4 | Guru99 — "How to Install TiviMate on Firestick" (guru99.com/how-to-install-tivimate-on-firestick.html) | Terms acceptance, settings sections (Playback buffer/speed; Appearance: Logos/TV Guide/Player/Language/Background Color/Font Size/Text Color/UI Transparency; EPG sources add-URL/paste; Recording folder select; Parental controls under Preferences; Auto start on boot), multi-view path, CC values |
| 5 | Optimedia — "TiviMate 2026 — Download, Setup & Complete Guide" (optimedia.tv/blog/tivimate-setup-guide-firestick-android) | Xtream wizard fields, EPG auto/7-days/morning refresh, M3U EPG URL format, free-vs-premium table (1 playlist free, auto-update premium), Companion features (remote/keyboard/cast/APK push), "Enable Server" setting, caching, troubleshooting quirks |
| 6 | APKMirror — TiviMate (Android TV) listing (apkmirror.com/apk/armobsoft-fze/tivimate-iptv-player-android-tv) | Version history (5.3.2/5.3.3 mid-2026), 5.3.3 changelog (All-channels sorting fix, m3u catch-up fix), official description (grid EPG, playlist formats, not-touch-optimized, player-only disclaimer), developer name Armobsoft FZE |
| 7 | APKCombo — TiviMate Companion listing (apkcombo.com/tivimate-companion/ar.tvplayer.companion) | Companion purpose verbatim ("unlock Premium… on devices without Google Play", "managing your activated devices"), version 1.4.5, Android 12+ |
| 8 | tivimate.com (fetched; JS-only shell) | Only tagline retrievable: "TiviMate is a media player. Add your own playlists to watch live TV, movies and shows" (via search snippets) — site itself not scrapeable |
| 9 | Model background knowledge of TiviMate 4.x community documentation (r/TiviMate, YouTube walkthroughs) | Classic panel model (OK→channel panel, Right→EPG detail), catch-up types, channel context-menu items — everything from this source is tagged [VERIFY ON DEVICE] where not corroborated above |

Not reachable during research: reddit.com (blocked for fetching), Google Play pages (empty render), DuckDuckGo (CAPTCHA), APK binaries for string extraction (Cloudflare on APKPure/APKMirror/apkcombo download endpoints), troypoint.com/how-to-record-on-tivimate (404).

---

## 7. Open questions — MUST verify on emulator

Navigation / input:
- [ ] 5.x fullscreen OK: bottom overlay vs side channel panel vs both (sequence). Exact overlay composition on second OK press.
- [ ] Default Up/Down action during playback (open list vs zap) and the setting that switches it (name, section, default).
- [ ] Right-press during clean fullscreen: opens current-channel EPG strip? Behavior in 5.x.
- [ ] Long-press OK menus: exact item lists + order (a) during playback, (b) on a channel row, (c) on a guide cell.
- [ ] Back chain from every screen (esp. guide → groups column → exit prompt) and the exit-confirm default state + exact dialog copy.
- [ ] Number-zap: overlay position/style, commit timeout, OK-to-commit, Back-to-cancel, behavior on invalid number.
- [ ] CH+/CH− wrap-around; FF/RW semantics in each context (paging vs seeking); Menu-key mapping on remotes without it.
- [ ] Any double-press gestures; previous-channel recall shortcut.
- [ ] Guide day navigation: explicit jump-to-day/time control? Paging keys?
- [ ] Guide OK semantics: single OK tune vs first-OK-select/second-OK-fullscreen.

Screens:
- [ ] First-run: exact wizard screen titles, field labels, button labels for all three playlist types; Stalker portal field set (MAC auto-gen?); terms dialog copy.
- [ ] Settings: exact top-level section list, order, per-item labels/types/defaults for every section (this spec's §2.13 is a scaffold, not ground truth). Especially: Remote control section existence, Profiles, General section contents, Backup scope.
- [ ] Program detail popup: modal shape, button set/order, artwork handling.
- [ ] Search: shelf set beyond Movies/Channels; is any part of search premium-gated; keyboard digit entry method.
- [ ] Favorites vs "My list": separate concepts? My list contents/behavior.
- [ ] Recordings section: tab names (Scheduled/Completed/Recurring?), item cards, conflict handling, padding settings, filename pattern.
- [ ] Reminders: management list location; free vs premium split (is notification-only reminder free?); guide bell glyph.
- [ ] Multiview: max tiles, layouts, audio-focus rule, how first entered from player options (exact label).
- [ ] History: retention, clear action, ordering.
- [ ] PiP trigger; external-player enable setting location.
- [ ] Sleep timer: increments, end-of-timer action (stop vs app close), countdown visibility.
- [ ] Catch-up: per-playlist catch-up type option values; free-tier catch-up limitation specifics; archive browser entry points.
- [ ] Channel context menu: full item list (Hide/Move/Change group/Rename/Change logo/Block/External player?).
- [ ] Parental PIN: digit count, entry UI, lockable surfaces list.
- [ ] Paywall screen: current price/copy, trial availability.

Visual:
- [ ] Exact hex values (sample from emulator screenshots): background, surfaces, cell states, accent variants, scrim alphas, secondary text.
- [ ] Focus animation (scale? fade duration), overlay slide animations, panel auto-hide timeout default.
- [ ] Fonts: confirm Roboto vs custom; exact sp sizes.
- [ ] Current-program cell elapsed-fill existence; guide glyphs for record/reminder/catch-up.
- [ ] Buffering spinner style; video-details overlay layout.
- [ ] Clock display in guide header format; 12/24 h behavior.

Behavior:
- [ ] EPG update interval option values; playlist auto-update interval values; what "update on playlist change" precisely does.
- [ ] Channel numbering rules with multiple playlists (continuous vs per-playlist restart) and renumbering UI.
- [ ] Free-tier: exact gate points (which actions raise the premium prompt) and prompt copy.
- [ ] Pause/timeshift behavior on plain live streams in free tier.
- [ ] Startup behavior default (guide vs last channel) in free build.

---

## Appendix A — Verbatim UI string inventory (confirmed sightings)

Strings below were directly observed in official screenshots (S#) or quoted by a source (T=TROYPOINT, F=FireStickTricks, G=Guru99, O=Optimedia, M=APKMirror). Use these as exact-match assertions in emulator verification; everything else in this spec is paraphrase.

Navigation / sections:
- "Search" (S4, F) · "TV" (S4, F) · "Movies" (S4, S5, F) · "TV Shows" (F) · "Recordings" (S4) / "DVR" (F) · "My list" (S4) · "Favorites" (F) · "Settings" (F)
- "All channels" (S3, M changelog: "All channels" group) · "History" (S3, S1)
- Group examples rendered as plain text list items: "Comedy", "Entertainment", "Nature", "Shows", "Culture" (S3, S4)

Playback overlay (S1):
- "TV guide" · "History" · time format "07:30 — 10:00 AM" · remaining "35 min" · badges "HD", "STEREO"

Multiview menu (S2, exact order top→bottom):
- "Add screen" · "Search and add" · "Change channel" · "Play" · "Enlarge screen" · "Full screen" · "Remove screen"

Accent color list (S4, order as shown): "Pink", "Purple", "Indigo", "Blue", "Cyan", "Teal", "Green", "Lime", "Yellow", "Amber" — header "Accent color"

Guide (S3): header "Fri,  14 Nov, 10:35 PM" · timeline "10:00 PM", "10:30 PM", "11:00 PM" · info "10:15 — 11:45 PM", "31 min"

Catch-up browser (S6): section header "Yesterday" · day cells "Tue 28 Oct" … "Sat 1 Nov" · detail "00:00 AM — 2:45 AM"

Playlist types (S3 badges): "M3U" · "Xtream Codes" · "Stalker Portal"

Wizard / settings / flows (source-quoted, not screenshot-confirmed):
- "Add playlist" (F, O) · "Enter URL" (F) · "Paste from clipboard" (G) · "Next", "Done", "OK" (F)
- "Unlock Premium" (T, G) · "Account" (T) · "Sign up" (T) · "Create account" (T) · "Activate" (T) · device-name prompt (T)
- "Group options" (T) · "Manage groups" (T) · "Manage positions" (T)
- "Add to Favorites" (T, G) · "Custom Recording" / "New Recording" (T) · "Record" (F)
- "Closed captions 1" (T, G) · "CC" (T, F)
- "Default source" (F) · "Update EPG Now" (O) · "Add EPG Source" (O)
- "Select Folder" (G) · "Multi-view mode" / "Add screens" (G)
- "Install unknown apps" etc. are Fire OS strings, not TiviMate's.

Official description fragments (M/Play): "the effortless way to watch your playlists" · "TiviMate doesn't provide any sources of TV channels. It is a player only." · "not optimized for touch devices such as phones or tablets"
Companion (APKCombo, verbatim): "The companion app for TiviMate IPTV player. It is not IPTV player!" · "This app is intended to unlock Premium functionality of TiviMate IPTV player on devices without Google Play." · "It allows managing your activated devices also."

## Appendix B — Key × context matrix (consolidated)

Legend: ✔ = source-corroborated, ○ = community knowledge [VERIFY ON DEVICE], — = expected no-op/unknown.

| Key | Fullscreen playback | Info overlay open | Channel panel | TV guide | Settings |
|---|---|---|---|---|---|
| OK short | ✔ open overlay/panel | ✔ activate focused card | ○ tune selected (close) | ✔ tune / detail popup (future) | ✔ enter/toggle |
| OK long | ✔ player options menu | ○ item context menu | ○ channel context menu | ✔ record/reminder menu ("Custom Recording") | — |
| Up/Down | ✔ open/scroll channel list (or zap, per setting ○) | ○ move between rows | ✔ move channel selection | ✔ move channel rows | ✔ move items |
| Left/Right | ✔ prev/next program info | ✔ move across cards | ✔ Left→groups, Right→program info | ✔ timeline earlier/later | ✔ adjust/list |
| Back | ✔ to TV guide | ✔ hide overlay | ✔ close panel | ✔ to groups col → exit prompt | ✔ up one level |
| RW/FF | ○ seek (catch-up/DVR only) | ○ — | ✔ page channel list | ✔ page channel list | — |
| CH+/CH− | ○ zap adjacent channel | ○ zap | ○ page | ○ page | — |
| 0–9 | ○ number zap | ○ number zap | ○ jump to number | ○ jump/tune | — |
| Play/Pause | ✔ pause/resume | ○ same | — | ○ tune selected | — |
| Menu | ○ player options | ○ options | ○ channel options | ✔ "Group options"/bulk favorites | — |

Row-level citations: OK-long in guide, Back-to-groups, RW/FF paging, Menu-in-guide, Left/Right program, Down-opens-list — TROYPOINT remote map. OK activations and overlay anatomy — screenshots. Everything marked ○ goes on the §7 checklist.
