# Settings on-device side-by-side — first pass

Date: 2026-09-14 · telly (main, post-guide-round2) vs TiviMate 5.2.0 live on
emulator-5554. Matched pairs in `settings-round1/{ref,telly}`. Geometry in px
on the 1920x1080 emulator (2 px = 1 dp).

## Structural verdict (the flagged question)

TiviMate settings is a **right-sheet stack**, NOT a two-pane shell. Drive-through
findings (read-only, `settings-round1/ref`):

- Opening Settings slides in ONE ~360 dp sheet at the screen's right edge over
  the current screen (guide/playback), which stays rendered under a ~60%-black
  scrim. Header strip #333639 (72 dp) with a 20 sp title; body panel #232629.
- The root sheet lists: premium note, Unlock Premium, then the nine sections
  (General, Playlists, EPG, Appearance, Playback, Remote control, Parental
  controls, Other, About) — uidump `ref/01`.
- OK on a section **replaces the root sheet content in place** with that
  section's sheet (same frame; a ~300 ms content crossfade, sheet frame static —
  frame-scan of a screenrecord). The section list is NOT kept visible.
- Sub-sheets (playlist detail, EPG sources, pickers, PIN, rename) push the same
  way; BACK **pops one sheet at a time**, and BACK at the root closes settings.
- Focus never leaves the sheet for the underlay; BACK is the only way out.

telly was a two-pane shell (left section list always visible driving a right
pane). **Rebuilt to the right-sheet stack** (`refactor(settings): right-sheet
shell`): `SettingsUiState` is now a `panes` stack (null = root list); the shell
is one `SettingsScreenSheet` keyed on the active pane over the live base route
(RootScreen renders Settings as an overlay over the screen beneath it, not as a
crossfade replacement). `SettingsViewModel`/store/row builders kept; section
rows became root-sheet rows (`section:` ids) that push a Section pane. Entry
point matches TiviMate: the guide's nav-rail gear (LEFT from the groups column
to the gear, OK opens the sheet).

## Screen-by-screen deltas

All P0/P1 fixed this pass; matched pairs captured. Row model unchanged from the
prior blind build except where noted.

| Sev | Screen | Delta | Resolution |
| --- | --- | --- | --- |
| P1 | Shell | wrong navigation model (two-pane vs sheet stack) | rebuilt (above); `ref/01` vs `telly/01-root` — section list + push verified |
| P1 | All sheets | rows were edge-to-edge; ref pills inset 8 dp with 16 dp inner pad | `rowMargin` 8 dp + `rowPadding` 16 dp; root rows now land at ref pitch (355/445/535… px) |
| P1 | Header | title 17 sp vs ref 20 sp regular (glyph band 36 px) | `headerTitleSize` 20 sp, `FontWeight.Medium`, default font (was condensed) |
| P1 | Focus | D-pad could escape the sheet into the dimmed guide underlay | single `exit=Cancel` focus trap around the whole surface |
| P1 | Rename / pickers / PIN | overlay text field / wheels never took focus (row behind kept it) — the old VERIFY-ON-DEVICE gap | overlay sheets that replace the section sheet drop it from composition while up, so the overlay owns focus; text field retries requestFocus across frames + `TextFieldValue` caret at end |
| P2 | Playlist list + EPG sources | selected row showed a trailing check; ref shows a LEADING accent circle-check | `checkIcon` leading icon (`ic_settings_check_circle`), accent at rest / near-black under focus |
| P2 | Delete confirm | said "Delete playlist" + name; ref is "Delete playlist?" + "All channels from the playlist \"…\" will no longer be available" with a warning triangle | rewritten (`ic_settings_warning`); `telly/08` vs `ref/08` |
| P2 | Guided steps (paywall/confirm) | guidance pane too narrow (470 dp), body 13 sp | pane 592 dp, body 16 sp/19.5 dp, action pills 328 dp — matches uidump 28 |
| P3 | Multi-line rows | fixed 45 dp height clipped 2-line titles | title column pads 12 dp vertical; icon/switch stay centered |

## Premium-lock treatment (matches TiviMate free)

Confirmed live: in free TiviMate, premium rows are **dimmed + padlock and skip
focus** (Appearance TV guide/Player/Groups/Logos/Language/Font size; EPG Past
days/Store descriptions/Add source; Playlists Add playlist/Update all;
Parental — every row; Playback AFR/external player). telly replicates the
padlock + focus-skip. **Accepted deviation (unchanged from the blind build):**
telly keeps a handful of these rows *live and functional* because it can honor
them without premium — Color theme (accent picker), EPG update interval, Add
playlist (gated to the 1-playlist free cap → paywall), playlist Update/Delete/
rename/enable, Back up/Restore, EPG update-now. So telly's sheets show fewer
padlocks and (without the padlock indent) slightly less title wrapping than the
ref. Logged as intentional per the charter; not a bug.

Residual (deferred, P3): TiviMate's parental PIN dialog is premium-locked and
uncapturable, so telly's picker-wheel Change PIN dialog is style-matched, not
pixel-matched (opened TiviMate's, CANCELLED it — it is the same 4-wheel picker).

## Functional checks

| Check | Result | Evidence |
| --- | --- | --- |
| Toggle persists across telly restart | PASS | toggled "Confirm exit" on → force-stop+relaunch → still on (`telly/f1-toggle-persist`); prefs `general_confirm_exit` round-tripped |
| Playlist rename persists | PASS | renamed 10.0.2.2 → "Living room", restart, Playlists still lists "Living room"; renamed back to 10.0.2.2 |
| EPG interval change → RefreshScheduler | PASS (unit) | `RefreshSchedulerTest` (custom intervals, provider re-read each isDue, None=only-never-fetched) + settings.feature scenario |
| Parental PIN set/lock/unlock (telly only) | PASS | on-device: master toggle on → forced Change PIN → set 2468 via picker → committed; prefs hold salted `parental_pin_hash`+`parental_pin_salt`. Lock/unlock gate is logic-only in telly → `ParentalControlsTest` (gate only while master on; verifyPin accepts only the right PIN). Left master OFF. |
| Backup export → JSON file | PASS | "Back up data" wrote `files/telly-backup.json` (adb-pulled to `telly/telly-backup.json`): version 1, settings map, playlist identity {name,url,epgUrl} |
| Restore round-trips | PASS (unit) | `SettingsBackupManagerTest` (export carries every setting+playlist; import restores settings and re-adds playlists channel-less pending Update) |

## End state

telly force-stopped; parental left disabled; the fixture playlist survived
(renamed back to 10.0.2.2). TiviMate values unchanged — nothing was ever
committed there (only read; the delete-PIN/paywall dialogs opened were CANCELLED
or BACKed out).
