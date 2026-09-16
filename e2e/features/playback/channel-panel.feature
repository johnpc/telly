@channel-panel
Feature: Channel list panel
  The panel is the 5.x guide-overlay stand-in (capture 47): groups column on
  the left, channel rows with now-playing programme and progress on the
  right, over the dimmed live video. BACK at bare playback opens it
  (device-verified round3 key map).

  Background:
    Given the fixture playlist and EPG are served from "http://10.0.2.2:8090"
    And I completed the add-playlist wizard
    And I open the channel panel

  Scenario: The panel shows groups and channels with now-playing info
    Then the groups column lists "Favorites", "All channels", "News", "Sports", "Movies", "Kids", "Music"
    And "All channels" is the selected group
    And each visible channel row shows its number, logo, name and current programme with progress
    And the focused row expands into a detail card with times and description

  Scenario: Channel numbers restart from 1 inside a group
    When I press dpad left
    And I select "Sports"
    Then the "Sports Arena" row shows number 1

  Scenario: OK on a channel row tunes it and dismisses the panel
    When I select the row "News One HD"
    Then the panel is dismissed and the zap overlay announces channel 2 "News One HD"
    When I wait 7 seconds
    Then no chrome is visible over the video

  Scenario: Long-press OK on a channel row opens the context menu verbatim
    When I long-press ok on the row "News One"
    Then I see the menu rows "Search" and "Settings"
    And I see a blue programme section with "Record" and "Program description"
    And I see a blue channel section "News One" with "Add to Favorites" and "Hide channel"
    And I see a blue "All channels" section with "Manage Favorites" and "Group options"

  Scenario: The sheet's Search row opens the search screen
    When I long-press ok on the row "News One"
    And I select "Search"
    Then the query bar shows the hint "Speak to search"

  Scenario: The sheet's Settings row opens the settings sheet
    When I long-press ok on the row "News One"
    And I select "Settings"
    Then I see the sections "General, Playlists, EPG, Appearance, Playback, Remote control, Parental controls, Other, About" in order

  # telly has no premium tier: the reference's paywalled rows share the
  # branded coming-soon placeholder with the uncaptured rows.
  Scenario: Formerly-premium sheet rows open the coming-soon placeholder and BACK pops back to the sheet
    When I long-press ok on the row "News One"
    And I select "Record"
    Then I see "Coming soon to telly"
    When I press back
    Then I see the menu rows "Search" and "Settings"

  Scenario: Program description shows the row's airing programme synopsis
    When I long-press ok on the row "News One"
    And I select "Program description"
    Then the description layer shows the focused programme's title and synopsis
    When I press back
    Then I see the menu rows "Search" and "Settings"

  Scenario: Channel options replaces the sheet and BACK lands directly on the panel
    When I long-press ok on the row "News One"
    And I select "Channel options"
    Then a right pane titled "News One" opens
    And the pane lists the rows "Channel name", "Channel names editor", "Audio decoder", "Video decoder", "Use external player", "EPG time offset, h:min", "Block channel" and "Hide channel"
    When I press back
    Then the groups column lists "Favorites", "All channels", "News", "Sports", "Movies", "Kids", "Music"
    And "All channels" is the selected group

  Scenario: Renaming a channel from the pane shows everywhere and survives a restart
    When I long-press ok on the row "News One"
    And I select "Channel options"
    And I select "Channel name"
    And I type "News Uno"
    Then a right pane titled "News Uno" opens
    And the pane lists the rows "Restore channel name"
    When I press back
    Then the "News Uno" row shows number 1
    When I relaunch telly
    And I open the channel panel
    Then the "News Uno" row shows number 1

  Scenario: Restore channel name brings the playlist name back
    When I long-press ok on the row "News One"
    And I select "Channel options"
    And I select "Channel name"
    And I type "News Uno"
    And I select "Restore channel name"
    Then a right pane titled "News One" opens
    When I press back
    Then the "News One" row shows number 1

  Scenario: The channel names editor renames from the bulk list
    When I long-press ok on the row "News One"
    And I select "Channel options"
    And I select "Channel names editor"
    Then I see "Press OK to rename a channel"
    When I select "News One HD"
    And I type "News One Deluxe"
    Then I see "News One Deluxe"

  Scenario: An EPG time offset shifts the channel's programmes in the panel
    When I long-press ok on the row "News One"
    And I select "Channel options"
    And I select "EPG time offset, h:min"
    And I select "1:00"
    Then the "EPG time offset, h:min" row shows "1:00"
    When I press back
    Then the "News One" row shows the programme airing 60 minutes earlier

  Scenario: The decoder pickers persist a per-channel override
    When I long-press ok on the row "News One"
    And I select "Channel options"
    And I select "Audio decoder"
    And I select "Software"
    Then the "Audio decoder" row shows "Software"

  Scenario: Block channel from the pane walks the same PIN gate as the sheet row
    When I long-press ok on the row "News One HD"
    And I select "Channel options"
    And I select "Block channel"
    Then I see "Set a PIN"
    When I enter the PIN "2468"
    And I open the channel panel
    Then the "News One HD" row shows a lock indicator

  Scenario: Hide channel from the pane removes the row like the sheet's
    When I long-press ok on the row "News One HD"
    And I select "Channel options"
    And I select "Hide channel"
    And I open the channel panel
    Then the channels column no longer lists "News One HD"

  Scenario: Uncaptured sheet rows stay on the branded placeholder
    When I long-press ok on the row "News One"
    And I select "Assign EPG"
    Then I see "Coming soon to telly"
    When I press back
    Then I see the menu rows "Search" and "Settings"

  Scenario: The favorite toggle persists across a restart
    When I long-press ok on the row "News One"
    And I select "Add to Favorites"
    And I press dpad left
    And I select "Favorites"
    Then the channels column lists exactly "News One"
    When I relaunch telly
    And I open the channel panel
    And I press dpad left
    And I select "Favorites"
    Then the channels column lists exactly "News One"

  Scenario: Hiding a channel removes it from the panel
    When I long-press ok on the row "News One HD"
    And I select "Hide channel"
    Then the channels column no longer lists "News One HD"

  Scenario: Focus position is remembered per group
    When I focus the row "News One Extra"
    And I press dpad left
    And I select "Sports"
    And I press dpad left
    And I select "All channels"
    Then the focused channel row is number 4 "News One Extra"
