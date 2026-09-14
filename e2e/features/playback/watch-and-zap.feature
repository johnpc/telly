Feature: Watch live TV and zap between channels
  After setup, telly is a TV: it cold-starts straight into fullscreen
  playback of the last-watched channel and the remote drives everything.
  Reference: TiviMate captures 33-46 and the catalogue's key map (§3).

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

  Scenario: The info overlay auto-hides after five seconds
    When I press ok
    And I wait 6 seconds
    Then no chrome is visible over the video

  Scenario: Channel up and down zap with wrap-around
    When I press channel up
    Then playback switches to channel 2 "News One HD"
    When I press channel down
    And I press channel down
    Then playback switches to channel 30 "Music Box 24"

  Scenario: Up opens the channel panel focused on the previous channel
    When I press dpad up
    Then the channel list panel opens over the dimmed video
    And the focused channel row is number 30 "Music Box 24"

  Scenario: Long-press OK opens the player context menu verbatim
    When I long-press ok
    Then I see the menu rows "Search" and "Settings"
    And I see a blue programme section with "Record" and "Program description"
    And I see a blue channel section with "Add to Favorites" and "Hide channel"
    And I see a blue "All channels" section with "Manage Favorites" and "Group options"

  Scenario: Unbuilt menu rows open a branded placeholder
    When I long-press ok
    And I select "Record"
    Then I see "Record"
    And I see "Coming soon to telly"
    When I press back
    Then no chrome is visible over the video

  Scenario: Back walks the overlay chain then leaves for the TV guide
    When I press ok
    And I press back
    Then no chrome is visible over the video
    When I press back
    Then the TV guide opens with the programme grid
