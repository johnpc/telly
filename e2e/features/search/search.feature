@search
Feature: Search channels and programmes
  TiviMate's search screen (catalogue §4, captures 49-51 + ref-round6 §D):
  voice orb, query bar and gear on top, the search-history landing state,
  and typed results as a Channels shelf plus a channel-master / airings-
  detail Programs section with a detail card.

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

  Scenario: The Programs section pairs a channel-master lane with the selected channel's airings
    When I focus the query bar
    And I type "newsroom"
    Then the Programs lane lists one card per channel airing "Newsroom Live", in name order
    And the airings pane lists only the selected channel's "Newsroom Live" airings chronologically, repeats included
    And each airing row shows its reference air time
    And the detail card pre-renders the selected channel's first airing

  Scenario: Focusing another channel card swaps the airings pane
    When I focus the query bar
    And I type "newsroom"
    And I focus the Programs channel card "News One +1"
    Then the airings pane lists only the selected channel's "Newsroom Live" airings chronologically, repeats included
    And the detail card pre-renders the selected channel's first airing

  Scenario: Down from the query bar lands on the first channel card
    When I focus the query bar
    And I type "news"
    And I press dpad down
    Then the first channel card "News One" is focused

  Scenario: Down from the query bar restores the last-visited channel card
    When I focus the query bar
    And I type "news"
    And I press dpad down
    And I press dpad right 2 times
    Then "News One 2" has focus
    When I focus the query bar
    And I type "news"
    And I press dpad down
    Then "News One 2" has focus

  Scenario: Down from the query bar without channel matches lands on the first airing row
    When I focus the query bar
    And I type "newsroom"
    And I press dpad down
    Then the first airing row of the selected channel is focused

  Scenario: OK on a Programs channel card tunes it
    When I focus the query bar
    And I type "newsroom"
    And I press ok on the Programs channel card "News One"
    Then playback starts fullscreen on channel 1 "News One"

  Scenario: OK on a channel result tunes it
    When I focus the query bar
    And I type "news"
    And I press ok on the channel card "News One HD"
    Then playback starts fullscreen on channel 2 "News One HD"

  # The dropdown's rows are live like the guide's cell dropdown (shared
  # reminders / DVR / My-list stores); the Remind label flips to "Remove
  # reminder" while one is set, mirroring the guide's reminders scenarios.
  # A later (30+ min out) row keeps the popup from firing mid-scenario.
  Scenario: The programme dropdown's Remind row sets and removes a reminder
    When I focus the query bar
    And I type "newsroom"
    And I press ok on a later programme row
    Then I see the dropdown rows "Remind", "Record", "Custom recording", "Add to My list", "Program description"
    When I select "Remind"
    And I press ok on a later programme row
    Then I see the dropdown rows "Remove reminder", "Record", "Custom recording", "Add to My list", "Program description"
    When I select "Remove reminder"
    And I press ok on a later programme row
    Then I see the dropdown rows "Remind", "Record", "Custom recording", "Add to My list", "Program description"

  Scenario: Committed queries land in the history and the trash clears them
    When I focus the query bar
    And I type "news"
    And I press the IME search action
    And I clear the query
    Then the history lists "news"
    When I press ok on the trash icon
    Then the empty state reads "No history"

  # Settings -> Other -> Search (the reference locks this pane behind
  # premium; telly ships it): save toggle + confirmed clear.
  Scenario: Turning off Save search history stops recording queries
    When I press back
    And I open Settings
    And I open the "Other" section
    And I activate "Search"
    And I activate "Save search history"
    Then the "Save search history" toggle is off
    When I leave settings
    And I press menu
    And I press ok on the quick-bar "Search" slot
    And I focus the query bar
    And I type "news"
    And I press the IME search action
    And I clear the query
    Then the empty state reads "No history"

  Scenario: Clear search history removes committed queries after the confirm
    When I focus the query bar
    And I type "news"
    And I press the IME search action
    And I press back
    And I open Settings
    And I open the "Other" section
    And I activate "Search"
    And I activate "Clear search history"
    Then I see "Clear search history?"
    When I select "Clear"
    And I leave settings
    And I press menu
    And I press ok on the quick-bar "Search" slot
    Then the empty state reads "No history"

  Scenario: Back returns to fullscreen playback
    When I press back
    Then no chrome is visible over the video
