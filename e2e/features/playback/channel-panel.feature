Feature: Channel list panel
  The panel is the 5.x guide-overlay stand-in (capture 47): groups column on
  the left, channel rows with now-playing programme and progress on the
  right, over the dimmed live video.

  Background:
    Given the fixture playlist and EPG are served from "http://10.0.2.2:8090"
    And I completed the add-playlist wizard
    And I press dpad up

  Scenario: The panel shows groups and channels with now-playing info
    Then the groups column lists "Favorites", "All channels", "News", "Sports", "Movies", "Kids", "Music"
    And "All channels" is the selected group
    And every channel row shows its number, logo, name and current programme with progress
    And the focused row expands into a detail card with times and description

  Scenario: Channel numbers restart from 1 inside a group
    When I press dpad left
    And I select "Sports"
    Then the "Sports Arena" row shows number 1

  Scenario: OK on a channel row tunes it and dismisses the panel
    When I select the row "News One HD"
    Then no chrome is visible over the video
    And playback switches to channel 2 "News One HD"

  Scenario: The favorite toggle persists across a restart
    When I long-press ok on the row "News One"
    And I select "Add to Favorites"
    And I press dpad left
    And I select "Favorites"
    Then the channels column lists exactly "News One"
    When I relaunch telly
    And I press dpad up
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
