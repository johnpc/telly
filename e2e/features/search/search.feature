@search
Feature: Search channels and programmes
  TiviMate's search screen (catalogue §4, captures 49-51): voice orb, query
  bar and gear on top, the search-history landing state, and typed results
  as a Channels shelf plus a chronological Programs list with a detail card.

  Background:
    Given the fixture playlist and EPG are served from "http://10.0.2.2:8090"
    And I completed the add-playlist wizard
    And I press menu
    And I press ok on the quick-bar "Search" slot

  Scenario: The landing screen shows the orb, hint and empty history
    Then the voice orb is focused
    And the query bar shows the hint "Speak to search"
    And I see the "Search history" header with a trash icon
    And the empty state reads "No history"

  Scenario: Typing a query lists matching channels in name order
    When I focus the query bar
    And I type "news"
    Then the "Channels" shelf lists "News One", "News One +1", "News One 2", "News One 24", "News One Extra", "News One HD"
    And every channel card shows its logo, name, current programme and progress

  Scenario: A digits-only query also matches channel numbers by prefix
    When I focus the query bar
    And I type "2"
    Then the "Channels" shelf includes channel 2 "News One HD"

  Scenario: Programme matches are chronological with reference air times
    When I focus the query bar
    And I type "newsroom"
    Then the "Programs" list shows "Newsroom Live" rows ordered by start time
    And rows airing today show a time range like "03:45 — 05:15 PM"
    And rows airing another day are prefixed like "Mon, Sep 14, 12:45 — 01:45 AM"
    And the focused row shows a detail card with title, times and description

  Scenario: OK on a channel result tunes it
    When I focus the query bar
    And I type "news"
    And I press ok on the channel card "News One HD"
    Then playback starts fullscreen on channel 2 "News One HD"

  Scenario: OK on a programme result opens the guide-cell dropdown
    When I focus the query bar
    And I type "newsroom"
    And I press ok on the first programme row
    Then I see the dropdown rows "Remind", "Record", "Custom recording", "Add to My list", "Program description"
    When I select "Remind"
    Then I see the "Unlock Premium" screen

  Scenario: Committed queries land in the history and the trash clears them
    When I focus the query bar
    And I type "news"
    And I press the IME search action
    And I clear the query
    Then the history lists "news"
    When I press ok on the trash icon
    Then the empty state reads "No history"

  Scenario: Back returns to fullscreen playback
    When I press back
    Then no chrome is visible over the video
