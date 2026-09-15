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
