@tv-guide
Feature: TV guide
  The signature TiviMate screen (captures 24-27, 32, 74): live preview
  window + focused-programme info pane on top, a 30-min timeline header and
  the virtualized programme grid below, all panning in lockstep.
  Reference: capture catalogue §2 and the device-verified answers.

  Background:
    Given the fixture playlist and EPG are served from "http://10.0.2.2:8090"
    And I completed the add-playlist wizard
    And I press back

  Scenario: The guide renders the grid with fixture EPG titles
    Then I see the preview window playing channel 1 "News One"
    And the info pane shows the focused programme title, time range and description
    And the channel column lists number, logo and name for "News One" and "Sports Arena"
    And the grid shows the current and next programme cells of "News One"
    And channels without EPG show "No information" cells

  Scenario: The timeline header shows half-hour ticks around now
    Then the header clock shows today's date and time
    And the timeline shows labels every 30 minutes
    And the now-line marks the current time in the grid

  Scenario: Focus moves across programmes and time scrolls in lockstep
    When I press dpad right
    Then the next programme cell of channel 1 is focused
    And the info pane shows that programme's title
    When I press dpad down
    Then the focused cell is on channel 2 at roughly the same time
    When I press dpad right 6 times
    Then the timeline header has scrolled forward with the cells

  Scenario: Two-stage OK tunes the preview then goes fullscreen
    When I press dpad down
    And I press ok on the airing programme
    Then the preview window plays channel 2 "News One HD"
    And the channel name of row 2 renders in accent blue with a play marker
    When I press ok
    Then playback goes fullscreen on channel 2 "News One HD"

  Scenario: OK on a future programme opens the premium dropdown
    When I press dpad right
    And I press ok
    Then a dropdown anchored under the cell lists exactly "Remind", "Record", "Custom recording", "Add to My list", "Program description"
    When I select "Remind"
    Then I see the "Unlock Premium" screen
    When I press back
    Then the programme grid is focused again

  Scenario: LEFT at the grid edge opens the groups column
    When I press dpad left
    Then the groups column lists "Favorites", "All channels", "News", "Sports", "Movies", "Kids", "Music"
    When I select "Sports"
    Then the "Sports Arena" row shows number 1
    And the groups column is dismissed

  Scenario: Long-OK on a guide row opens the row context sheet over the grid
    When I long-press ok
    Then a right-side sheet opens with the guide grid still visible behind it
    And I see the menu rows "Search" and "Settings"
    And I see a blue programme section with "Open in external player", "Record", "Custom recording", "Add to My list" and "Program description"
    And I see a blue "News One" section with "Add to Favorites", "Block channel", "Hide channel", "Assign EPG" and "Channel options"
    And I see a blue "All channels" section with "Manage Favorites", "Manage blocking", "Manage visibility", "Reorder channels", "Copy channels", "Create group" and "Group options"
    When I press back
    Then the programme grid is focused again

  Scenario: BACK from the row sheet restores focus to the originating row
    Then the info pane shows the focused programme title, time range and description
    When I press dpad down
    Then the focused cell is on channel 2 at roughly the same time
    When I long-press ok
    Then a right-side sheet opens with the guide grid still visible behind it
    When I press back
    Then the programme grid is focused again
    And the focused cell is on channel 2 at roughly the same time

  Scenario: MENU opens the same row context sheet
    When I press menu
    Then I see the menu rows "Search" and "Settings"

  Scenario: The sheet's favorites toggle relabels and its hide row removes the channel
    When I long-press ok
    And I select "Add to Favorites"
    Then the programme grid is focused again
    When I long-press ok
    Then I see the menu row "Remove from Favorites"
    When I press back
    And I press dpad down
    And I long-press ok
    And I select "Hide channel"
    Then the channel column no longer lists "News One HD"

  Scenario: Premium sheet rows open the Unlock Premium screen and BACK pops back to the sheet
    When I long-press ok
    And I select "Record"
    Then I see the "Unlock Premium" screen
    When I press back
    Then I see the menu rows "Search" and "Settings"

  Scenario: Program description shows the focused programme's synopsis
    When I long-press ok
    And I select "Program description"
    Then the description layer shows the focused programme's title and synopsis
    When I press back
    Then I see the menu rows "Search" and "Settings"

  Scenario: The sheet's Search row opens the search screen
    When I long-press ok
    And I select "Search"
    Then the query bar shows the hint "Speak to search"

  Scenario: The sheet's Settings row opens the settings sheet
    When I long-press ok
    And I select "Settings"
    Then I see "All features are available in Premium version"
    And I see the sections "General, Playlists, EPG, Appearance, Playback, Remote control, Parental controls, Other, About" in order

  Scenario: Channel options pushes the locked premium pane and BACK pops one level
    When I long-press ok
    And I select "Channel options"
    Then a right pane titled "News One" opens
    And I see "All features are available in Premium version"
    And the locked rows list "Channel name", "Restore channel name", "Channel names editor", "Audio decoder", "Video decoder", "Use external player", "EPG time offset, h:min", "Block channel" and "Hide channel"
    When I press back
    Then I see the menu rows "Search" and "Settings"
    When I press back
    Then the programme grid is focused again

  Scenario: BACK at the guide root exits the app without confirmation
    When I press back
    Then telly exits to the launcher
