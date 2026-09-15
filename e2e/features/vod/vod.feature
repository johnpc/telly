@vod
Feature: VOD movies
  Playlist entries with video-file extensions (.mp4/.mkv/.avi/.mov) are VOD
  "Movies", not live channels (device-verified: TiviMate classifies .mp4
  stream URLs as VOD — that's why the live fixtures are .ts). VOD items get
  their own browser behind the guide rail's film icon, seekable fullscreen
  playback, and resume positions ("Continue watching").

  Background:
    Given the fixture playlist and EPG are served from "http://10.0.2.2:8090"
    And I completed the add-playlist wizard

  Scenario: VOD entries never become guide channels
    Then the playlist imported 30 live channels and 2 VOD items
    When I open the guide's groups column
    Then I see "Movies"
    And the groups column does not list "Cinema"

  Scenario: The rail's Movies icon opens the VOD browser
    When I open the VOD browser from the guide rail
    Then I see "Cinema"
    And I see the VOD item card "Big Buck Bunny"
    And I see the VOD item card "Sintel"

  Scenario: OK on an item plays it fullscreen with the seek transport
    When I open the VOD browser from the guide rail
    And I play the VOD item "Big Buck Bunny"
    Then VOD playback starts with the transport visible
    And the transport shows the title "Big Buck Bunny"

  Scenario: A partially watched item shows progress and offers Resume
    When I open the VOD browser from the guide rail
    And I play the VOD item "Big Buck Bunny"
    And VOD playback starts with the transport visible
    And I wait 4 seconds
    And I press back
    Then the VOD item card "Big Buck Bunny" shows watch progress
    When I play the VOD item "Big Buck Bunny"
    Then I see "Resume playback?"
    And I see "Start over"
    When I select "Resume"
    Then VOD playback starts with the transport visible
