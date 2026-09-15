@epg-data
Feature: EPG data foundation
  Channels and guide data live in Room, not in memory: playlists survive a
  restart and the guide is populated from the playlist's url-tvg EPG source.
  Reference: TiviMate ux-spec §2.5 (guide), §5 (EPG updates, 24 h default).

  Background:
    Given the fixture playlist is served at "http://10.0.2.2:8090/playlist.m3u"
    And the fixture EPG is served at "http://10.0.2.2:8090/epg.xml"
    And an alternative fixture EPG is served at "http://10.0.2.2:8090/epg-alt.xml"

  Scenario: Channels persist across an app restart
    Given I added the playlist "http://10.0.2.2:8090/playlist.m3u"
    And playback starts fullscreen on channel 1 "News One"
    When I relaunch telly
    Then playback starts fullscreen on channel 1 "News One"

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

  # TiviMate manages EPG sources under Settings -> EPG -> EPG sources
  # (captures 57-58). The free reference locks "Add source"; telly ships it
  # unlocked by product directive, following TiviMate's documented premium
  # flow (ux-spec 3.10: EPG sources -> add source URL). Merge rule: sources
  # are fetched auto-detected first, then custom in the order they were
  # added; the last source that covers a channel owns that channel's
  # schedule, so custom sources take precedence per channel.
  Scenario: A custom EPG source is added in settings and merged into the guide data
    Given I added the playlist "http://10.0.2.2:8090/playlist.m3u"
    And the EPG update completes
    When I open Settings
    And I open the "EPG" section
    And I activate "EPG sources"
    And I activate "Add source"
    And I type "http://10.0.2.2:8090/epg-alt.xml"
    Then I see "http://10.0.2.2:8090/epg-alt.xml"
    When I press back
    And I activate "Update EPG"
    # epg-alt.xml covers Sports Arena, which url-tvg's epg.xml leaves empty...
    Then the guide data contains program titles from "epg-alt.xml" for channel "Sports Arena"
    # ...and it also covers News One, overriding the url-tvg schedule.
    And the guide data contains program titles from "epg-alt.xml" for channel "News One"

  # Wizard EPG step (capture 13): the url-tvg URL is pre-filled into
  # "Enter URL" and the committed value is what telly fetches after Done.
  Scenario: The EPG URL entered in the wizard overrides the playlist's url-tvg
    Given a fresh install of telly
    When I select "Add playlist"
    And I select "M3U playlist"
    And I select "Enter URL"
    And I type "http://10.0.2.2:8090/playlist.m3u"
    And I select "Next"
    Then I see "Playlist is processed"
    When I select "Next"
    Then I see "EPG URL"
    When I select "Enter URL"
    And I type "http://10.0.2.2:8090/epg-alt.xml"
    And I select "Done"
    And the EPG update completes
    Then the guide data contains program titles from "epg-alt.xml" for channel "News One"
