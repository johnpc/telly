@catch-up
Feature: Catch-up playback of already-aired programmes
  Channels can declare catch-up on their #EXTINF line (catchup /
  catchup-source / catchup-days). On such a channel a past programme in the
  guide is playable: OK plays the archived broadcast fullscreen with a seek
  transport, and BACK returns to the guide (ux-spec §2.10/§3.17). The
  fixture playlist marks "News One" catch-up-enabled (2 days); "News One
  HD" has the same deep-past EPG but no catch-up, so its past cells keep
  today's dropdown.

  Background:
    Given the fixture playlist and EPG are served from "http://10.0.2.2:8090"
    And I completed the add-playlist wizard
    And I press back

  Scenario: A past programme on a catch-up channel plays from the guide with a seek transport
    When I long-press dpad left
    Then the guide shows yesterday's programme on channel 1
    When I press ok
    Then catch-up playback starts for yesterday's programme on channel 1
    And the seek transport shows the position readout
    When I press fast-forward
    Then the seek transport shows the position readout
    When I return from catch-up playback
    Then the TV guide opens with the programme grid

  Scenario: A past programme on a channel without catch-up only offers the premium dropdown
    When I long-press dpad left
    And I press dpad down
    Then the guide shows yesterday's programme on channel 2
    When I press ok
    Then a dropdown anchored under the cell lists exactly "Remind", "Record", "Custom recording", "Add to My list", "Program description"
    And the dropdown offers no "Play" action
    When I press back
    Then the programme grid is focused again
