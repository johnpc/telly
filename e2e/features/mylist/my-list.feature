@my-list
Feature: My list and favorites management
  The formerly-premium TiviMate rows telly ships for real (ux-spec §3, no
  captures exist for the free tier): "Add to My list" on the guide dropdown
  and the shared context sheet, the My List screen behind the guide rail's
  bookmark icon, and the Manage Favorites / Reorder channels screens.

  Background:
    Given the fixture playlist and EPG are served from "http://10.0.2.2:8090"
    And I completed the add-playlist wizard
    And I press back

  Scenario: Adding a future programme from the dropdown flips its row label
    When I press dpad right
    And I press ok
    Then I see "Add to My list"
    When I select "Add to My list"
    Then the programme grid is focused again
    When I press ok
    Then I see "Remove from My list"
    When I press back
    Then the programme grid is focused again

  Scenario: The rail bookmark opens My List and OK on an airing entry tunes
    When I press dpad down
    And I long-press ok
    And I select "Add to My list"
    Then the programme grid is focused again
    When I press dpad up
    And I press dpad right
    And I press ok
    And I select "Add to My list"
    Then the programme grid is focused again
    # first LEFT returns to the airing cell, the second reaches the rail
    When I press dpad left
    And I press dpad left
    And I select "My list"
    Then the My List rows are the upcoming programme of "News One" then the current programme of "News One HD"
    When I select the My List row "News One HD"
    Then playback goes fullscreen on channel 2 "News One HD"

  Scenario: A future entry shows its description and long-OK removes it
    When I press dpad right
    And I press ok
    And I select "Add to My list"
    Then the programme grid is focused again
    # first LEFT returns to the airing cell, the second reaches the rail
    When I press dpad left
    And I press dpad left
    And I select "My list"
    Then I see "My list"
    When I select the My List row "News One"
    Then I see the description of the upcoming programme of "News One"
    When I press back
    And I long-press ok
    Then I see "No programs"

  # LEFT/RIGHT moves the focused row (TiviMate's documented favorites
  # management); the Favorites group in the guide honors the saved order.
  Scenario: Manage Favorites toggles favorites and reorders them persistently
    When I long-press ok
    And I select "Manage Favorites"
    Then I see "Manage Favorites"
    When I select "News One"
    Then the channel row "News One" is marked favorite
    When I select "News One HD"
    Then the channel row "News One HD" is marked favorite
    When I focus the channel row "News One"
    And I press dpad right
    Then the channel row "News One HD" is listed above "News One"
    When I press back
    Then the programme grid is focused again
    When I press dpad left
    And I select "Favorites"
    Then the "News One HD" row shows number 1
    And the "News One" row shows number 2

  Scenario: Reorder channels moves a channel within the current group
    When I long-press ok
    And I select "Reorder channels"
    Then I see "Reorder channels"
    When I focus the channel row "News One"
    And I press dpad right
    Then the channel row "News One HD" is listed above "News One"
    When I press back
    Then the programme grid is focused again
    And the channel column lists "News One HD" above "News One"
