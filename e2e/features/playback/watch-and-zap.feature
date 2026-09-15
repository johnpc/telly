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

  # The History card (capture 34: second 150x110 card, clock-with-arrow
  # icon). Capture 47's note: OK on it "opens the same overlay (with History
  # as source group when it exists)". Capture 48's uidump — taken right
  # after the press — is bare playback (zero text nodes, one focused
  # full-screen ViewGroup): 5.2.0 free has no History screen and no
  # persistent History group (capture 25 lists only Favorites / All
  # channels / News / Sports / Movies / Kids / Music). telly honors the
  # documented intent: the card lands on the guide with a synthetic,
  # recently-watched History source group.

  Scenario: The History card opens the guide on the recently-watched channels
    Given I zapped to channel 2 "News One HD"
    And I zapped to channel 3 "News One +1"
    When I press ok
    And I select the "History" card
    Then the TV guide opens with the programme grid
    And "History" is the selected group
    And the channels column lists exactly "News One +1", "News One HD", "News One"

  Scenario: History is newest-first and lists each channel once
    Given I zapped to channel 2 "News One HD"
    And I zapped to channel 3 "News One +1"
    And I zapped to channel 2 "News One HD"
    When I press ok
    And I select the "History" card
    Then the channels column lists exactly "News One HD", "News One +1", "News One"

  Scenario: History channel numbers restart from 1
    Given I zapped to channel 7 "Sports Arena"
    When I press ok
    And I select the "History" card
    Then the "Sports Arena" row shows number 1
    And the "News One" row shows number 2

  Scenario: History survives a relaunch
    Given I zapped to channel 7 "Sports Arena"
    When I relaunch telly
    And I press ok
    And I select the "History" card
    Then the channels column lists exactly "Sports Arena", "News One"

  Scenario: The groups column shows History only while it is the source group
    When I press ok
    And I select the "TV guide" card
    And I press dpad left
    Then the groups column lists "Favorites", "All channels", "News", "Sports", "Movies", "Kids", "Music"
    And the groups column does not list "History"
    When I press back
    And I press ok
    And I select the "History" card
    And I press dpad left
    Then the groups column lists "History", "Favorites", "All channels", "News", "Sports", "Movies", "Kids", "Music"
    When I select "Sports"
    Then the "Sports Arena" row shows number 1

  Scenario: Back from the History guide exits the app, like any guide root
    When I press ok
    And I select the "History" card
    And I press back
    Then telly exits to the launcher

  # Multiview (multiview-round captures + multiview-spec.md). The free
  # reference is a single-pane teaser whose every add/change selection
  # opens Unlock Premium; telly ships the multi-pane grid working (charter
  # precedent: no premium tier). Audio ownership is asserted via the pane
  # semantics ("audio"/"muted"), never actual sound.

  Scenario: The quick-bar's Multiview slot opens the single-pane teaser
    When I long-press ok
    And I select "Multiview"
    Then I see 1 multiview pane
    And the multiview pane "News One" is focused with audio
    And I see "Press OK to show menu"
    And I see "Your IPTV provider may limit the number of concurrent connections"

  Scenario: OK on the pane opens its menu over the pane view
    Given I opened multiview
    When I press ok
    Then I see the multiview menu rows "Add screen", "Search and add" and "Change channel"
    When I select "Add screen"
    Then the multiview channel picker shows the list, schedule and detail panes

  Scenario: Adding a second screen builds the side-by-side grid; the new pane owns audio
    Given I opened multiview
    When I press ok
    And I select "Add screen"
    And I pick the channel "News One HD" in the multiview picker
    Then I see 2 multiview panes
    And the multiview pane "News One HD" is focused with audio
    And the multiview pane "News One" is muted

  Scenario: D-pad focus moves between panes and audio follows the focused pane
    Given I opened multiview with a second screen "News One HD"
    When I press dpad left
    Then the multiview pane "News One" is focused with audio
    And the multiview pane "News One HD" is muted

  Scenario: Change channel swaps the focused pane through the picker
    Given I opened multiview
    When I press ok
    And I select "Change channel"
    And I pick the channel "Sports Arena" in the multiview picker
    Then I see 1 multiview pane
    And the multiview pane "Sports Arena" is focused with audio

  Scenario: Remove screen collapses the grid back to a single pane
    Given I opened multiview with a second screen "News One HD"
    When I press ok
    And I select "Remove screen"
    Then I see 1 multiview pane
    And the multiview pane "News One" is focused with audio

  Scenario: Back walks picker to panes to fullscreen playback
    Given I opened multiview
    When I press ok
    And I select "Change channel"
    And I press back
    Then I see "Press OK to show menu"
    When I press back
    Then no chrome is visible over the video
    And playback starts fullscreen on channel 1 "News One"
