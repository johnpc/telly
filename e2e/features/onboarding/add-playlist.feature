Feature: Add playlist onboarding
  telly ships no content: on first run the user must add an M3U playlist URL
  before anything else can happen. Reference: TiviMate screens 02-12.

  Background:
    Given a fresh install of telly
    And the fixture playlist is served at "http://10.0.2.2:8090/playlist.m3u"

  Scenario: Welcome screen offers the two entry actions
    Then I see "telly doesn't provide any sources of TV channels"
    And I see "To watch TV channels, please add a playlist provided by your IPTV service"
    And "Add playlist" has focus
    When I press dpad right
    Then "Settings" has focus

  Scenario: Adding a valid M3U URL lands on the channels screen
    When I select "Add playlist"
    And I select "M3U playlist"
    And I select "Enter URL"
    And I type "http://10.0.2.2:8090/playlist.m3u"
    And I select "Next"
    Then I see "Channels loaded: 30"

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
