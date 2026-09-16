@add-playlist
Feature: Add playlist onboarding
  telly ships no content: on first run the user must add an M3U playlist URL
  before anything else can happen. Reference: TiviMate screens 02-12.

  # Fixture URLs are written for the dev python server (10.0.2.2:8090). The
  # harness serves the same fixtures from inside the instrumentation process
  # and rewrites the host, so the scenarios stay dev-runnable verbatim.
  Background:
    Given a fresh install of telly
    And the fixture playlist is served at "http://10.0.2.2:8090/playlist.m3u"

  Scenario: Welcome screen offers the two entry actions
    Then I see "telly doesn't provide any sources of TV channels"
    And I see "To watch TV channels, please add a playlist provided by your IPTV service"
    And "Add playlist" has focus
    When I press dpad right
    Then "Settings" has focus

  Scenario: Adding a valid M3U URL processes it and lands on fullscreen playback
    When I select "Add playlist"
    And I select "M3U playlist"
    And I select "Enter URL"
    And I type "http://10.0.2.2:8090/playlist.m3u"
    And I select "Next"
    Then I see "Playlist is processed"
    And I see "Channels: 31"
    And I see "Playlist name"
    And I see "10.0.2.2"
    And I see "TV playlist"
    When I select "Next"
    # EPG step (capture 13): the url-tvg from the M3U header is pre-filled.
    Then I see "EPG URL"
    And I see "Enter EPG URL for the playlist. You can add or change it later in the settings. XMLTV format is only supported."
    And I see "http://10.0.2.2:8090/epg.xml"
    And I see "Paste playlist URL"
    And I see "Use default source"
    When I select "Done"
    Then playback starts fullscreen on channel 1 "News One"

  Scenario: A non-http URL is rejected on the URL step
    When I select "Add playlist"
    And I select "M3U playlist"
    And I select "Enter URL"
    And I type "not-a-url"
    And I select "Next"
    Then I see "Enter a valid http(s) URL"

  Scenario: An unreachable playlist reports a load error
    When I select "Add playlist"
    And I select "M3U playlist"
    And I select "Enter URL"
    And I type "http://10.0.2.2:8090/does-not-exist.m3u"
    And I select "Next"
    Then I see "Could not load the playlist. Check the URL and try again."

  Scenario: Back walks the wizard one step at a time
    When I select "Add playlist"
    And I select "M3U playlist"
    Then I see "Enter URL"
    When I press back
    Then I see "Playlist type"
    When I press back
    Then I see "telly doesn't provide any sources of TV channels"
