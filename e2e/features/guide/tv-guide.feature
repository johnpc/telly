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

  Scenario: OK plays the focused channel and jumps straight to fullscreen
    When I press dpad down
    And I press ok on the airing programme
    Then playback goes fullscreen on channel 2 "News One HD"

  # The dropdown moved to long-OK (a regular OK now plays the channel). Play
  # channel is the top row and is live; Remind is live (reminders slice,
  # e2e/features/reminders); the rest open the coming-soon placeholder.
  Scenario: Long-OK on a programme opens the cell dropdown
    When I press dpad right
    And I long-press ok
    Then a dropdown anchored under the cell lists exactly "Play channel", "Remind", "Record", "Custom recording", "Add to My list", "Program description"
    When I select "Program description"
    Then I see "Coming soon to telly"
    When I press back
    Then the programme grid is focused again

  Scenario: LEFT at the grid edge opens the groups column
    When I press dpad left
    Then the groups column lists "Favorites", "All channels", "News", "Sports", "Movies", "Kids", "Music"
    When I select "Sports"
    Then the "Sports Arena" row shows number 1
    And the groups column is dismissed

  Scenario: MENU on a guide row opens the row context sheet over the grid
    When I press menu
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
    When I press menu
    Then a right-side sheet opens with the guide grid still visible behind it
    When I press back
    Then the programme grid is focused again
    And the focused cell is on channel 2 at roughly the same time

  Scenario: The sheet's favorites toggle relabels and its hide row removes the channel
    When I press menu
    And I select "Add to Favorites"
    Then the programme grid is focused again
    When I press menu
    Then I see the menu row "Remove from Favorites"
    When I press back
    And I press dpad down
    And I press menu
    And I select "Hide channel"
    Then the channel column no longer lists "News One HD"

  # The sheet's Record row is live DVR (recording slice): it instant-records
  # the focused row's channel — fixture News One is a recordable .ts stream —
  # and the sheet closes back onto the grid. The library flows live in
  # e2e/features/recording; this asserts the guide sheet's own behavior.
  Scenario: The sheet's Record row starts recording the focused channel
    When I press menu
    And I select "Record"
    Then the programme grid is focused again
    And a recording of "News One" is in progress

  Scenario: Program description shows the focused programme's synopsis
    When I press menu
    And I select "Program description"
    Then the description layer shows the focused programme's title and synopsis
    When I press back
    Then I see the menu rows "Search" and "Settings"

  Scenario: The sheet's Search row opens the search screen
    When I press menu
    And I select "Search"
    Then the query bar shows the hint "Speak to search"

  Scenario: The sheet's Settings row opens the settings sheet
    When I press menu
    And I select "Settings"
    Then I see the sections "General, Playlists, EPG, Appearance, Playback, Remote control, Parental controls, Other, About" in order

  Scenario: Channel options replaces the sheet and BACK lands directly on the guide grid
    When I press menu
    And I select "Channel options"
    Then a right pane titled "News One" opens
    And the pane lists the rows "Channel name", "Channel names editor", "Audio decoder", "Video decoder", "Use external player", "EPG time offset, h:min", "Block channel" and "Hide channel"
    When I press back
    Then the programme grid is focused again
    And the focused cell is on channel 1 at roughly the same time

  # The rail walks six focus stops by index (search, live TV, bookmark,
  # Movies, DVR, gear); a second LEFT lands on the gear, so Live TV sits
  # four UP presses away.
  Scenario: The rail's live-TV icon is a focus stop whose OK returns to the guide
    When I press dpad left
    Then the groups column lists "Favorites", "All channels", "News", "Sports", "Movies", "Kids", "Music"
    When I press dpad left
    And I press dpad up 4 times
    Then the "Live TV" rail icon has focus
    When I press ok
    Then focus returns to the groups column

  Scenario: Create group adds a custom group to the guide's groups column
    When I press menu
    And I select "Create group"
    And I type "My Picks"
    Then the programme grid is focused again
    When I press dpad left
    Then the groups column lists "Favorites", "All channels", "News", "Sports", "Movies", "Kids", "Music", "My Picks"

  Scenario: Assign EPG fills an EPG-less guide row from the picked id
    When I press dpad down 6 times
    And I press menu
    And I select "Assign EPG"
    Then I see "Auto (tvg-id)"
    When I select "news-one-1.fixture" in the tool sheet
    Then the guide row of "Sports Arena" shows the current programme of EPG id "news-one-1.fixture"

  # BACK mirrors LEFT: from the grid it opens the groups column, again moves
  # to the settings gear, and from there it exits the app.
  Scenario: BACK walks to the groups column then the gear before exiting
    When I press back
    Then the groups column lists "Favorites", "All channels", "News", "Sports", "Movies", "Kids", "Music"
    When I press back
    Then the "Settings" rail icon has focus
    When I press back
    Then telly exits to the launcher

  Scenario: Confirm exit by second press Back warns first, then exits
    Given confirm exit on second BACK is enabled
    When I press back
    And I press back
    Then the "Settings" rail icon has focus
    When I press back
    Then I see "Press BACK again to exit"
    And telly is still running
    When I press back
    Then telly exits to the launcher

  Scenario: The 24-hour clock format drives the guide clocks and persists across relaunch
    Given the clock format is "24-hour"
    When I relaunch telly
    And I press back
    Then the header clock shows today's date and a 24-hour time
    And the timeline shows 24-hour labels every 30 minutes
