@watch-and-zap
Feature: Watch live TV and zap between channels
  After setup, telly is a TV: it cold-starts straight into fullscreen
  playback of the last-watched channel and the remote drives everything.
  Reference: TiviMate captures 33-46 and the catalogue's key map (§3), as
  corrected by the round3/round4 on-device verification: OK/DOWN/UP all open
  the info overlay, long-OK/MENU open the quick-bar, and BACK returns to the
  TV guide (the channel panel lives behind the quick-bar's Channels list).

  Background:
    Given the fixture playlist and EPG are served from "http://10.0.2.2:8090"
    And I completed the add-playlist wizard

  Scenario: Setup lands on fullscreen playback of the first channel
    Then playback starts fullscreen on channel 1 "News One"
    And no chrome is visible over the video

  Scenario: The last-watched channel is restored on relaunch
    Given I zapped to channel 2 "News One HD"
    When I relaunch telly
    Then playback starts fullscreen on channel 2 "News One HD"

  Scenario: OK opens the info overlay with programme data
    When I press ok
    Then I see the bottom info overlay
    And I see the channel logo, number 1 and name "News One"
    And I see the current programme title with its time range and progress
    And I see the next programme line
    And I see stream badges like "HD", "25 FPS" and "MONO"
    And I see the "TV guide" and "History" cards

  Scenario: The info overlay auto-hides after its five-second timeout
    When I press ok
    And I wait 7 seconds
    Then no chrome is visible over the video

  Scenario: Channel up and down zap with wrap-around
    When I press channel up
    Then playback switches to channel 2 "News One HD"
    When I press channel down
    And I press channel down
    Then playback switches to channel 30 "Music Box 24"

  Scenario: The quick-bar's Channels list opens the panel focused on the playing channel
    When I long-press ok
    And I select "Channels list"
    Then the channel list panel opens over the dimmed video
    And the focused channel row is number 1 "News One"

  Scenario: Long-press OK opens the quick-bar with live stream slots
    When I long-press ok
    Then I see the quick-bar slots "Search", "Channels list", "Recordings", "Multiview" and "Picture-in-picture"
    And I see the live stream slots "1280 × 720", "Mono", "0 ms" and "Off"

  Scenario: Unbuilt quick-bar slots open a branded placeholder
    When I long-press ok
    And I select "Recordings"
    Then I see "Recordings"
    And I see "Coming soon to telly"
    When I press back
    Then no chrome is visible over the video

  Scenario: Back walks the overlay chain then leaves for the TV guide
    When I press ok
    And I press back
    Then no chrome is visible over the video
    When I press back
    Then the TV guide opens with the programme grid

  # The corrected History model (history-round2; supersedes the capture-48
  # guide-with-History-group reading): the info overlay's shortcut row is
  # TV guide · History · recent-channel cards · Clear. A recent card shows
  # the channel LOGO + its CURRENT programme title; focusing it adds an
  # air-time + title line. OK on the History card opens a DISTINCT
  # full-screen "History" list (title + clear-all trash top-right,
  # "No history" empty state); BACK returns to the fullscreen player.
  # Deliberate deviation: the free reference opens Unlock Premium on a
  # recent card; telly tunes to that channel.

  Scenario: The info overlay shows recent-channel cards and the Clear card
    Given I zapped to channel 2 "News One HD"
    And I zapped to channel 3 "News One +1"
    When I press ok
    Then I see 2 recent-channel cards with the current programmes of "News One HD" and "News One"
    And I see the "Clear" card
    When I focus the first recent-channel card
    Then the focused recent card shows the air time of "News One HD"

  Scenario: OK on a recent-channel card tunes to it
    Given I zapped to channel 2 "News One HD"
    When I press ok
    And I select the first recent-channel card
    Then playback switches to channel 1 "News One"

  Scenario: The Clear card empties the watch history
    Given I zapped to channel 2 "News One HD"
    When I press ok
    And I select the "Clear" card
    Then no recent-channel cards are visible
    When I select the "History" card
    Then the History screen opens
    And I see "No history"

  Scenario: The History card opens the full-screen History list and back returns to the player
    Given I zapped to channel 2 "News One HD"
    And I zapped to channel 3 "News One +1"
    When I press ok
    And I select the "History" card
    Then the History screen opens
    And the History screen lists exactly "News One +1", "News One HD", "News One"
    When I press back
    Then no chrome is visible over the video

  Scenario: History is newest-first and lists each channel once
    Given I zapped to channel 2 "News One HD"
    And I zapped to channel 3 "News One +1"
    And I zapped to channel 2 "News One HD"
    When I press ok
    And I select the "History" card
    Then the History screen lists exactly "News One HD", "News One +1", "News One"

  Scenario: History survives a relaunch
    Given I zapped to channel 7 "Sports Arena"
    When I relaunch telly
    And I press ok
    And I select the "History" card
    Then the History screen lists exactly "Sports Arena", "News One"

  Scenario: OK on a History row tunes that channel
    Given I zapped to channel 2 "News One HD"
    When I press ok
    And I select the "History" card
    And I select the History row "News One"
    Then playback starts fullscreen on channel 1 "News One"

  Scenario: The History screen's clear-all empties the history immediately
    Given I zapped to channel 2 "News One HD"
    When I press ok
    And I select the "History" card
    And I select the History clear-all icon
    Then I see "No history"
