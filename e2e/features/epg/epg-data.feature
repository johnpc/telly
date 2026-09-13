Feature: EPG data foundation
  Channels and guide data live in Room, not in memory: playlists survive a
  restart and the guide is populated from the playlist's url-tvg EPG source.
  Reference: TiviMate ux-spec §2.5 (guide), §5 (EPG updates, 24 h default).

  Background:
    Given the fixture playlist is served at "http://10.0.2.2:8090/playlist.m3u"
    And the fixture EPG is served at "http://10.0.2.2:8090/epg.xml"

  Scenario: Channels persist across an app restart
    Given I added the playlist "http://10.0.2.2:8090/playlist.m3u"
    And I see "Channels loaded: 30"
    When I relaunch telly
    Then I see "Channels loaded: 30"

  Scenario: The EPG referenced by url-tvg is fetched and stored
    Given I added the playlist "http://10.0.2.2:8090/playlist.m3u"
    When the EPG update completes
    Then the guide data contains program titles from "epg.xml" for channel "News One"
    And the program for "News One" airing now has a start time, an end time and a description

  Scenario: Channel numbers follow playlist order
    Given I added the playlist "http://10.0.2.2:8090/playlist.m3u"
    Then channel "News One" has number 1
    And channel "Music Box 24" has number 30

  Scenario: Favorites survive a playlist refresh
    Given I added the playlist "http://10.0.2.2:8090/playlist.m3u"
    And I marked channel "News One" as a favorite
    When the playlist is refreshed from the same URL
    Then channel "News One" is still a favorite
