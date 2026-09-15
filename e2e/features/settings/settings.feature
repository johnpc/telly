@settings
Feature: Settings
  The TiviMate-style settings sheet stack (device-verified 2026-09-13): a
  single 360 dp right sheet over the dimmed underlying screen. The root
  sheet lists the captured sections; OK replaces it in place with that
  section's sheet and BACK pops one sheet at a time. Rows render the
  captured defaults; not-yet-built rows render dimmed with a padlock (no
  premium wording — telly is fully open source with no premium tier).
  Reference: capture catalogue §6 (screens 18-22, 52-69) + live drive
  (docs/reference/sidebyside/settings-round1/ref).

  Background:
    Given the fixture playlist is served at "http://10.0.2.2:8090/playlist.m3u"
    And I added the playlist "http://10.0.2.2:8090/playlist.m3u"
    And I open Settings

  Scenario: The section list renders the captured sections in order
    Then I see the sections "General, Playlists, EPG, Appearance, Playback, Remote control, Parental controls, Other, About" in order

  Scenario: A toggle persists across an app restart
    When I open the "General" section
    And I activate "Confirm exit by second press Back"
    Then the "Confirm exit by second press Back" toggle is on
    When I relaunch telly
    And I open Settings
    And I open the "General" section
    Then the "Confirm exit by second press Back" toggle is on

  Scenario: Changing the EPG update interval changes the refresh policy
    When I open the "EPG" section
    Then the "Update interval, hours" row shows "None"
    When I activate "Update interval, hours"
    And I choose "6"
    Then the "Update interval, hours" row shows "6"
    And EPG data older than 6 hours is due for refresh
    And EPG data fresher than 6 hours is not due for refresh

  Scenario: A parental PIN locks a channel group
    When I open the "Parental controls" section
    And I activate "Off"
    And I set the PIN to "2468"
    And the group "Movies" is locked
    Then opening the group "Movies" requires the PIN
    And entering the PIN "2468" unlocks it
    And opening the group "News" does not require the PIN

  Scenario: Renaming a playlist persists
    When I open the "Playlists" section
    And I activate the playlist "10.0.2.2"
    And I activate "Playlist name"
    And I type "Living room"
    Then the playlists section lists "Living room"
    When I relaunch telly
    And I open Settings
    And I open the "Playlists" section
    Then the playlists section lists "Living room"

  # telly has no premium tier: the reference gated a second playlist behind
  # Unlock Premium; telly just opens the add-playlist wizard.
  Scenario: Adding a second playlist opens the add-playlist wizard
    When I open the "Playlists" section
    And I activate "Add playlist"
    Then I see "Playlist type"
    And I see "M3U playlist"

  Scenario: Locked rows render dimmed with a padlock and skip focus
    When I open the "Playback" section
    Then the row "Auto frame rate (AFR)" is locked
    And the row "Skip steps" is locked
    And the row "Buffer size" is not locked

  Scenario: The Appearance rows are all unlocked
    When I open the "Appearance" section
    Then the row "TV guide" is not locked
    And the row "Player" is not locked
    And the row "Groups" is not locked
    And the row "Logos" is not locked
    And the row "Language" is not locked
    And the row "Font size" is not locked
    And the row "Color theme" is not locked

  # Playback extras (AFR / external player / skip steps): the reference
  # locks these premium rows; telly has no premium tier and ships them as
  # live pickers whose picks persist across the sheet closing and a restart.
  Scenario: The playback extras rows are unlocked and their picks persist
    When I open the "Playback" section
    Then the row "Auto frame rate (AFR)" is not locked
    And the row "Use external player" is not locked
    And the row "Skip steps" is not locked
    And the "Auto frame rate (AFR)" row shows "Off"
    And the "Use external player" row shows "Off"
    And the "Skip steps" row shows "10s / 30s / 1m / 5m"
    When I activate "Auto frame rate (AFR)"
    And I choose "On (also switch refresh rate on stop)"
    Then the "Auto frame rate (AFR)" row shows "On (also switch refresh rate on stop)"
    When I activate "Use external player"
    And I choose "On"
    Then the "Use external player" row shows "On"
    When I activate "Skip steps"
    And I choose "30s / 1m / 5m / 10m"
    Then the "Skip steps" row shows "30s / 1m / 5m / 10m"
    When I relaunch telly
    And I open Settings
    And I open the "Playback" section
    Then the "Auto frame rate (AFR)" row shows "On (also switch refresh rate on stop)"
    And the "Use external player" row shows "On"
    And the "Skip steps" row shows "30s / 1m / 5m / 10m"

  # telly has no premium tier: the reference locks the playlist URL, UA,
  # groups and update rows behind Unlock Premium; telly ships them unlocked.
  Scenario: The playlist detail premium rows are unlocked
    When I open the "Playlists" section
    And I activate the playlist "10.0.2.2"
    Then the row "Playlist URL" is not locked
    And the row "User-Agent" is not locked
    And the row "Manage groups" is not locked
    And the row "Update interval, hours" is not locked
    And the row "Update on app start" is not locked

  # The URL is the playlist's identity: editing it re-keys the stored row
  # in place (channels and per-playlist settings survive) and re-fetches.
  Scenario: Editing the playlist URL re-keys the playlist and keeps its channels
    When I open the "Playlists" section
    And I activate the playlist "10.0.2.2"
    And I activate "Playlist URL"
    And I type "http://10.0.2.2:8090/playlist.m3u?edited=1"
    Then the "Playlist URL" row shows "http://10.0.2.2:8090/playlist.m3u?edited=1"
    And the playlists section lists "10.0.2.2"
    And I see "Channels: 30"

  Scenario: A per-playlist User-Agent persists across a relaunch
    When I open the "Playlists" section
    And I activate the playlist "10.0.2.2"
    Then the "User-Agent" row shows "Not set"
    When I activate "User-Agent"
    And I type "telly-e2e-agent"
    Then the "User-Agent" row shows "telly-e2e-agent"
    When I relaunch telly
    And I open Settings
    And I open the "Playlists" section
    And I activate the playlist "10.0.2.2"
    Then the "User-Agent" row shows "telly-e2e-agent"

  Scenario: The playlist update options persist
    When I open the "Playlists" section
    And I activate the playlist "10.0.2.2"
    Then the "Update interval, hours" row shows "None"
    When I activate "Update interval, hours"
    And I choose "8"
    Then the "Update interval, hours" row shows "8"
    When I activate "Update on app start"
    Then the "Update on app start" toggle is on

  Scenario: Disabling a group hides it from the guide's group list
    When I open the "Playlists" section
    And I activate the playlist "10.0.2.2"
    And I activate "Manage groups"
    And I activate "Music"
    And I leave settings
    And I open the channel panel
    And I press dpad left
    Then the groups column does not list "Music"
    And the groups column lists "News" and "Movies"
    And the channels column no longer lists "Music Box"

  Scenario: The TV guide sub-pane persists a density choice
    When I open the "Appearance" section
    And I activate "TV guide"
    Then the "Number of visible channels" row shows "7"
    When I activate "Number of visible channels"
    And I choose "9"
    Then the "Number of visible channels" row shows "9"

  Scenario: The Player sub-pane persists the panels timeout
    When I open the "Appearance" section
    And I activate "Player"
    Then the "Panels timeout, sec" row shows "5"
    When I activate "Panels timeout, sec"
    And I choose "8"
    Then the "Panels timeout, sec" row shows "8"

  Scenario: Hiding the Favorites group removes it from the channel panel
    When I open the "Appearance" section
    And I activate "Groups"
    And I activate "Show 'Favorites' group"
    Then the "Show 'Favorites' group" toggle is off
    And the channel panel group list does not include "Favorites"

  Scenario: The Logos sub-pane persists a background choice
    When I open the "Appearance" section
    And I activate "Logos"
    Then the "Logo background" row shows "Default"
    When I activate "Logo background"
    And I choose "Dark"
    Then the "Logo background" row shows "Dark"

  Scenario: The Language picker persists
    When I open the "Appearance" section
    Then the "Language" row shows "System"
    When I activate "Language"
    And I choose "Español"
    Then the "Language" row shows "Español"

  Scenario: Font size persists across an app relaunch
    When I open the "Appearance" section
    And I activate "Font size"
    And I choose "Large"
    Then the "Font size" row shows "Large"
    When I relaunch telly
    And I open Settings
    And I open the "Appearance" section
    Then the "Font size" row shows "Large"

  # The reference locks these two sub-screens behind premium; telly ships
  # them by the no-premium-tier directive. Defaults render the
  # device-verified key map.
  Scenario: Remote control opens the TV guide and Player key sub-screens
    When I open the "Remote control" section
    Then the row "TV guide" is not locked
    And the row "Player" is not locked
    When I activate "Player"
    Then the "OK button" row shows "Show info panel"
    And the "Up/Down buttons" row shows "Show info panel"
    And the "Left/Right buttons" row shows "Nothing"
    And the "Long press OK" row shows "Open quick menu"

  Scenario: A TV guide key remap persists across an app restart
    When I open the "Remote control" section
    And I activate "TV guide"
    Then the "Left/Right buttons" row shows "Move by programme"
    When I activate "Left/Right buttons"
    And I choose "Move by page"
    Then the "Left/Right buttons" row shows "Move by page"
    When I relaunch telly
    And I open Settings
    And I open the "Remote control" section
    And I activate "TV guide"
    Then the "Left/Right buttons" row shows "Move by page"

  Scenario: Remapping the OK button to the channels list takes effect at fullscreen playback
    When I open the "Remote control" section
    And I activate "Player"
    And I activate "OK button"
    And I choose "Open channels list"
    And I leave settings for fullscreen playback
    And I press ok
    Then the channel list panel opens over the dimmed video
