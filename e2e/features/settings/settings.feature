@settings
Feature: Settings
  The TiviMate-style settings sheet stack (device-verified 2026-09-13): a
  single 360 dp right sheet over the dimmed underlying screen. The root
  sheet lists the captured sections; OK replaces it in place with that
  section's sheet and BACK pops one sheet at a time. Rows render the
  captured defaults; free-tier premium locks are replicated exactly.
  Reference: capture catalogue §6 (screens 18-22, 52-69) + live drive
  (docs/reference/sidebyside/settings-round1/ref).

  Background:
    Given the fixture playlist is served at "http://10.0.2.2:8090/playlist.m3u"
    And I added the playlist "http://10.0.2.2:8090/playlist.m3u"
    And I open Settings

  Scenario: The section list renders the captured sections in order
    Then I see "All features are available in Premium version"
    And I see "Unlock Premium"
    And I see the sections "General, Playlists, EPG, Appearance, Playback, Remote control, Parental controls, Other, About" in order

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

  Scenario: Adding a second playlist hits the captured premium gate
    When I open the "Playlists" section
    And I activate "Add playlist"
    Then I see "Unlock Premium"
    And I see "Support for multiple playlists"

  Scenario: Premium-locked rows render dimmed with a padlock and skip focus
    When I open the "Appearance" section
    Then the row "TV guide" is locked
    And the row "Language" is locked
    And the row "Color theme" is not locked
