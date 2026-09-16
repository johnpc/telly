@block-channel
Feature: Block channel
  Blocked channels stay listed in the guide and panel with a small lock
  and are PIN-gated to play (ux-spec §3.9): tuning one — panel OK, zap,
  guide OK or a restore — prompts for the parental PIN; a wrong or
  cancelled PIN never tunes. Blocking and unblocking are PIN-gated too
  (setting a PIN first when none exists), and blocked channels are managed
  under Settings -> Parental controls -> Blocked channels.

  Background:
    Given the fixture playlist and EPG are served from "http://10.0.2.2:8090"
    And I completed the add-playlist wizard

  Scenario: Blocking a channel walks PIN setup first and shows the lock indicator
    When I open the channel panel
    And I long-press ok on the row "News One HD"
    And I select "Block channel"
    Then I see "Set a PIN"
    When I enter the PIN "2468"
    Then the "News One HD" row shows a lock indicator

  Scenario: With a PIN configured blocking asks to confirm it and the sheet row flips
    Given the parental PIN is "2468"
    When I open the channel panel
    And I long-press ok on the row "News One HD"
    And I select "Block channel"
    Then I see "Enter PIN"
    When I enter the PIN "2468"
    Then the "News One HD" row shows a lock indicator
    When I long-press ok on the row "News One HD"
    Then I see the menu row "Unblock channel"

  Scenario: Tuning a blocked channel prompts and a wrong PIN stays put
    Given the channel "News One HD" is blocked behind the PIN "2468"
    When I open the channel panel
    And I select the row "News One HD"
    Then I see "Enter PIN"
    When I enter the PIN "1111"
    Then I see "Enter PIN"
    When I press back
    And I press back
    Then no chrome is visible over the video
    And playback goes fullscreen on channel 1 "News One"

  Scenario: The correct PIN tunes the blocked channel
    Given the channel "News One HD" is blocked behind the PIN "2468"
    When I open the channel panel
    And I select the row "News One HD"
    Then I see "Enter PIN"
    When I enter the PIN "2468"
    Then the panel is dismissed and the zap overlay announces channel 2 "News One HD"

  Scenario: Zapping onto a blocked channel is gated too
    Given the channel "News One HD" is blocked behind the PIN "2468"
    When I press channel up
    Then I see "Enter PIN"
    When I enter the PIN "2468"
    Then playback switches to channel 2 "News One HD"

  # Multiview passes the same gate on EVERY pane tune: the entry pane (the
  # last-watched channel), a picker pick and the CH+/- zap all prompt over
  # the multiview layers; a wrong or cancelled PIN never tunes a pane.
  Scenario: Multiview gates its entry pane behind the blocked channel's PIN
    Given the channel "News One" is blocked behind the PIN "2468"
    When I long-press ok
    And I select "Multiview"
    Then I see "Enter PIN"
    And I see 0 multiview panes
    When I enter the PIN "2468"
    Then I see 1 multiview pane
    And the multiview pane "News One" is focused with audio

  Scenario: Picking a blocked channel in multiview is PIN-gated and a wrong PIN never tunes
    Given the channel "News One HD" is blocked behind the PIN "2468"
    And I opened multiview
    When I press ok
    And I select "Add screen"
    And I pick the channel "News One HD" in the multiview picker
    Then I see "Enter PIN"
    When I enter the PIN "1111"
    Then I see "Enter PIN"
    And I see 1 multiview pane
    When I enter the PIN "2468"
    Then I see 2 multiview panes
    And the multiview pane "News One HD" is focused with audio

  Scenario: The parental controls pane unblocks after one PIN check
    Given the channel "News One HD" is blocked behind the PIN "2468"
    When I open Settings
    And I open the "Parental controls" section
    And I activate "Blocked channels"
    Then I see "Enter PIN"
    When I enter the PIN "2468"
    Then I see "News One HD"
    When I activate "News One HD"
    Then I see "No blocked channels"
    And I no longer see "News One HD"
