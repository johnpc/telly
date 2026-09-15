@reminders
Feature: Reminders
  TiviMate-style programme reminders (premium in the reference; ux-spec:
  a popup appears shortly before the programme starts and can switch to
  the channel). The guide future-cell dropdown's Remind row schedules one
  and relabels to "Remove reminder" while it is set; Settings -> Other ->
  Reminders lists the schedule with the lead-time picker and an OK ->
  delete confirm. The popup itself is wall-clock-driven (the engine ticks
  per minute), so firing cannot be driven deterministically here — the
  due/exactly-once/lead-time logic is JVM-tested in ReminderEngineTest;
  these scenarios cover set, relabel, list and delete.

  Background:
    Given the fixture playlist and EPG are served from "http://10.0.2.2:8090"
    And I completed the add-playlist wizard
    And I press back

  Scenario: Remind toggles a reminder from the guide dropdown
    When I open the dropdown on a later programme of channel 1
    Then I see "Remind"
    When I select "Remind"
    Then the programme grid is focused again
    When I press ok
    Then I see "Remove reminder"
    When I select "Remove reminder"
    Then the programme grid is focused again
    When I press ok
    Then I see "Remind"

  Scenario: A scheduled reminder is listed in settings and deleted through the confirm
    When I open the dropdown on a later programme of channel 1
    And I select "Remind"
    Then the programme grid is focused again
    When I long-press ok
    And I select "Settings"
    And I open the "Other" section
    And I activate "Reminders"
    Then the reminders pane lists the remembered programme on "News One"
    When I activate the remembered reminder
    Then I see "Delete reminder?"
    When I select "Delete"
    Then I see "No reminders"

  Scenario: The reminder lead time defaults to 5 minutes and persists a new choice
    When I long-press ok
    And I select "Settings"
    And I open the "Other" section
    And I activate "Reminders"
    Then I see "No reminders"
    And the "Show reminder before, min" row shows "5"
    When I activate "Show reminder before, min"
    And I choose "10"
    Then the "Show reminder before, min" row shows "10"
