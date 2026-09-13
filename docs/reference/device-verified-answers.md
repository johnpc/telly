# Device-verified answers to tivimate-ux-spec.md §7 open questions

Verified on TiviMate **5.2.0 free tier**, Android TV emulator (Android 14, 1920×1080),
2026-09-13, with the local fixture backend (30 channels, full XMLTV EPG).
Evidence = screenshot number in `docs/reference/screens/` (uidumps share the number).
Spec targets 5.3.3; treat any 5.2.0/5.3.3 delta as possible but none was observable
from public 5.3.3 changelogs for these areas.

## Navigation / input

- **5.x fullscreen OK: bottom overlay vs side panel** — OK opens the **bottom info
  overlay** (logo tile, title, times, remaining, channel number+name, HD/FPS/audio
  badges, next-programme line, full-width progress bar, shortcut cards "TV guide" +
  "History", down-chevron). There is no legacy left-side channel panel in 5.2.0; the
  panel role is played by the **guide overlay** (timeline + expanded focused row) opened
  from the "TV guide" card or the UP key. Second OK activates the focused shortcut card.
  (34, 35, 47)
- **Default Up/Down during playback** — **DOWN opens the info overlay** (same as OK);
  **UP opens the guide overlay focused on the previous channel row** (wraps from
  channel 1 to 30) with toast "Long press Back button to return to the player". Neither
  zaps by default. The zap-vs-seek remapping lives in Settings → Remote control
  ("Use Down/Up for seeking while watching catch-up", etc.; all premium-locked, all OFF
  except "Use RW/FF/Pause for seeking/pause while watching catch-up" = ON). (36, 37, 64, 65)
- **Right-press in clean fullscreen** — not an EPG strip; no visible reaction (no
  overlay). Program browsing happens in the overlay/guide. (—)
- **Long-press OK menus, exact items**
  - During playback = MENU key menu: Search · Settings · [program]: Open in external
    player · Record · Custom recording · Add to My list · Program description ·
    [channel]: Add to Favorites · Block channel · Hide channel · Assign EPG · Channel
    options · [All channels]: Manage Favorites · Manage blocking · Manage visibility ·
    Reorder channels · Copy channels · Create group · Group options. (38, 39, 40, 46)
  - On a guide cell (short-OK on non-airing program): Remind · Record · Custom
    recording · Add to My list · Program description — anchored dropdown, not modal. (27)
  - Long-OK on a guide cell opens the same full context menu as during playback.
- **Back chain** — playback → guide → (BACK) **app exits immediately to launcher**;
  no groups-column stop and **no exit-confirmation dialog** (that toggle exists at
  Settings → General → "Confirm exit by second press Back", default OFF, premium).
  LEFT (not BACK) shows the groups column; LEFT again shows the nav rail. (25, 70, §2 note)
- **Number-zap** — **does not exist in 5.2.0 free**: digit keys are ignored during
  playback; no overlay renders. (43, 44)
- **CH+/CH− wrap-around / FF-RW semantics / MENU mapping** — CHANNEL_UP shows the info
  overlay instead of zapping on this build; MENU = long-OK menu; FF/RW produced no
  visible paging anywhere. UP-from-playback wraps 1→30, so adjacency wraps. (45, 46, 36)
- **Double-press gestures / previous-channel recall** — none found; recall is the
  "History" card in the overlay. (34)
- **Guide day navigation** — no explicit jump-to-day control found in free tier;
  timeline pans by 30-min cells with LEFT/RIGHT. "Long Left: navigate to past
  programs" is advertised by the hint toast (24) but produced no movement on the
  emulator (75) — likely needs focus at the leftmost "now" edge plus catch-up data.
- **Guide OK semantics** — two-stage: OK #1 on an airing programme tunes it in the
  preview window (row gets blue name + ▶); OK #2 goes fullscreen. OK on a non-airing
  programme opens the action dropdown instead. (32, 27)

## Screens

- **First-run wizard** — no terms dialog, no remote-hint dialog in 5.2.0. Screen titles
  and full field sets captured verbatim: type chooser "M3U playlist / Xtream Codes /
  Stalker Portal"; Xtream = Server address/Username/Password + Include TV channels +
  Include VOD; Stalker = Server address + auto-generated editable MAC (00:1a:79:…) +
  optional Username/Password/Device ID/Device ID 2/Serial number/Signature; M3U =
  Enter URL / Paste from clipboard / Select local playlist → processed summary with
  TV/VOD radio + name → EPG step with url-tvg pre-filled + "Paste playlist URL" +
  "Use default source". Buttons: Next/Back/Done/Cancel. (03–13, 23)
- **Settings tree (top level, exact order)** — Unlock Premium, General, Playlists, EPG,
  Appearance, Playback, Remote control, Parental controls, Other, About. Every
  section's visible items + values captured (54–69); free-tier items are only:
  EPG sources view, Update EPG, Delete playlist, statistics toggle, Privacy policy.
  "Remote control" exists as its own section; no "Profiles" section; General contents
  as listed in the catalogue; Backup/Restore live under General ("Back up data",
  "Restore data"), premium. (18, 52–69)
- **Program detail popup** — replaced in 5.x by (a) the anchored action dropdown on a
  cell and (b) "Program description" menu item (premium in free build); the guide's
  top-right info pane always shows title/times/remaining/description of the focused
  cell. (24, 27)
- **Search** — shelves seen: **Channels**, **Programs** (Movies/TV Shows shelves only
  when the playlist has VOD). Search itself is FREE (reachable, typed query returns
  results); voice orb is default focus; system IME with suggestion chips, digits via
  long-press row. Search history list with trash-clear on the landing screen. (49–51)
- **Favorites vs "My list"** — separate concepts. Favorites = channel group (Favorites
  appears as first group in the groups column; managed via premium menu items).
  **My list** = rail section with tabs **My TV programs** and **My reminders**
  (programme watchlist + reminder management). "Add to My list" is premium. (25, 76, 77)
- **Recordings section** — free tier shows a flat empty state "No recordings"; no tabs
  visible without premium/recordings. (72)
- **Reminders** — management list = My list → My reminders. In the FREE build even
  setting a reminder is premium-gated (Remind → paywall), contradicting older
  community docs. Bell glyph unverifiable (cannot set one). (27→31, 77)
- **Multiview** — no entry point exists anywhere in the free 5.2.0 UI (not in the
  long-OK menu). Premium-only feature; unverifiable without an account.
- **History** — reachable from the overlay's History card; opens the guide-style
  overlay (History group materializes only after watching; retention/clear action not
  exposed in free UI). Not shown as a group in the groups column in 5.2.0. (47, 48, 25)
- **PiP trigger** — Settings → General → "Switch to picture-in-picture mode on press
  Home" (toggle, premium, default off). External player enable = Settings → Playback →
  "Use external player" (No) and per-channel in Channel options. (54, 60, 41)
- **Sleep timer** — NOT present in 5.2.0 (no timer item in any menu). The 15–240 min
  timer documented by FireStickTricks is not in this build. (38–40)
- **Catch-up** — options are distributed: per-playlist catch-up settings not visible in
  free per-playlist panel; Remote control section is dominated by catch-up seeking
  toggles. Archive browser unreachable without premium + catch-up source. (64, 65, 20)
- **Channel context menu (full list)** — Add to Favorites, Block channel, Hide channel,
  Assign EPG, Channel options (+ per-channel items inside Channel options: rename,
  restore name, names editor, audio/video decoder, external player, EPG time offset,
  block, hide). No "Move"/"Change group"/"Change logo" wording in 5.2.0. (38–42)
- **Parental PIN** — PIN input method default "Picker"; "Don't require PIN after
  unlocking" = "Always require"; lockable surfaces list starts Settings,
  Settings | Playlists (rest below fold, locked). Digit count unverifiable (premium). (67, 69)
- **Paywall copy** — captured verbatim (bullets + "limit of 5 devices" + subscription
  w/ 7-day free trial + one-time payment); price figures render as "?" without Play
  billing, confirming pricing is fetched dynamically. (28, 29)

## Visual

- **Exact hexes** — bg `#131619`; cells `#1B1E21`; focused `#DEE0E2`/`#FFFFFF`;
  settings panel `#232629`, header `#333639`; accent `#2196F3` (Material Blue 500);
  guide clock `#90CAF9`; now-line ≈`#384C5C` (alpha-blue over bg); overlay card
  `#1B1D21`. The default theme reads as "Color theme: Dark • Blue" (59). It is a
  Material-palette app, not custom colors.
- **Focus** — flat white pill, black content, no scale/glow. Overlays: bottom overlay
  fades/slides up; settings panels slide in from right; groups/rail slide from left.
- **Fonts** — Roboto; guide/program title 34px (~17sp*2? at xhdpi 2px=1dp ⇒ 17dp);
  cell text 28px; header/timeline 28px; settings rows 30px with 24px sub-line.
- **Current-cell elapsed fill** — none; progress is conveyed only by the now-line and
  the dash-pill in info panes. Record/reminder/catch-up glyphs unverifiable (premium).
- **Buffering** — no spinner observed (fixture starts fast); video-details readout not
  present in overlay beyond HD/FPS/audio badges. (34)
- **Clock** — `Sun, Sep 13, 2:45 PM` 12-hour with weekday+date, in both guide header
  and overlay top-right; no 24h toggle found in free settings.

## Behavior

- **EPG update interval values** — field exists ("Update interval, hours", default
  "None") but the value list is premium-locked; same for playlist auto-update. "Update
  on playlists change" toggle exists (default off). (57)
- **Channel numbering** — sequential playlist order in "All channels"; **renumbered
  from 1 inside each group**; numbering restart on group switch. Multi-playlist
  numbering unverifiable (premium). (24, 74)
- **Free-tier gates (exhaustive observed list)** — every program action (remind,
  record, custom recording, My list, description), every channel action (favorite,
  block, hide, assign EPG, all channel options), all group management, playlist
  add/edit/update/sort, EPG add-source/intervals/keep-days, all Appearance, all
  Playback, all Remote control, all Parental, Other subsections, General items,
  backup/restore. Free is: watch, guide, groups, search, history, My-list tabs
  (empty), EPG sources view, Update EPG, delete/re-add the single playlist. Prompt =
  full-screen Unlock Premium GuidedStep (identical copy each time). (28 et al.)
- **Pause/timeshift free tier** — MEDIA_PLAY_PAUSE not testable meaningfully on the
  60s looping fixture; no timeshift UI appeared; stream rejoins live on loop restart.
- **Startup default (free)** — always the TV guide, All-channels group, no auto-tune
  (verified twice after process restarts). "Turn on last channel on app start" is the
  premium toggle covering the alternative. (relaunch evidence; 54)

## Unverifiable without a premium account / real provider

Recording flows and tabs, catch-up playback + archive browser + catch-up types,
multiview, favorites group behavior, reminders bell + auto-switch, PiP, external
player handoff, backup/restore scope, parental PIN UI, Appearance sub-screens
(TV guide/Player/Groups/Logos), Remote control sub-screens, per-playlist update
intervals, real prices on the paywall, TiviMate-account flows past the login form.
