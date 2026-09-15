@recording
Feature: Record live TV to disk and manage recordings (DVR)
  telly's premium-style DVR, shipped free (telly has no paywall): "Record"
  on the channel context sheet copies the live stream to a .ts file under
  the app's files dir via a foreground service, "Custom recording" schedules
  a future capture, and the Recordings library (reached from the quick-bar's
  Recordings slot and the guide rail's DVR icon) lists, plays back and
  deletes them. Fixture streams are local .ts files served over HTTP, so a
  short real record is exercised end to end.
  Reference: docs/reference/tivimate-ux-spec.md §2.9 / §3.6.

  Background:
    Given the fixture playlist and EPG are served from "http://10.0.2.2:8090"
    And I completed the add-playlist wizard

  Scenario: The quick-bar's Recordings slot opens an empty library
    When I open the recordings library
    Then the recordings library is empty

  Scenario: Record the playing channel and see it recording in the library
    When I record the playing channel from the context sheet
    And I open the recordings library
    Then the recordings library shows a recording of "News One"
    And the recording shows the REC badge

  Scenario: Stopping a recording keeps it in the library and it plays back
    When I record the playing channel from the context sheet
    And I open the recordings library
    And I select the recording of "News One"
    Then I see "Stop recording?"
    When I select "Stop"
    And I select the recording of "News One"
    Then the recording plays back fullscreen
    When I press back
    Then the recordings library shows a recording of "News One"

  Scenario: Long-pressing OK deletes a recording after a confirm
    When I record the playing channel from the context sheet
    And I open the recordings library
    And I long-press ok on the recording of "News One"
    Then I see "Delete recording?"
    When I select "Delete"
    Then the recordings library is empty

  Scenario: Custom recording schedules a future capture
    When I open the custom recording form for the playing channel
    Then I see "Custom recording"
    When I select "Create"
    And I open the recordings library
    Then the recordings library shows a recording of "News One"
    And the recording shows the "Scheduled" state

  Scenario: The guide rail's DVR icon opens the library
    When I press back
    Then the TV guide opens with the programme grid
    When I press dpad left
    And I select "Recordings"
    Then the recordings library is empty

  Scenario: The Other settings section has a Recording pane
    When I open Settings
    And I open the "Other" section
    And I activate "Recording"
    Then I see "Storage used by recordings"
    And I see "Delete all recordings"
    And I see "Scheduled recordings start only while telly is running"
    When I activate "Delete all recordings"
    Then I see "Delete all recordings?"
