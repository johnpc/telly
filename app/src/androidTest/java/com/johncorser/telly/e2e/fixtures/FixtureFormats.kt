package com.johncorser.telly.e2e.fixtures

import com.johncorser.telly.features.playback.ProgramTimes
import java.util.TimeZone

/** The time-range string the app renders for this programme. */
fun FixtureProgramme.rangeText(): String = ProgramTimes.range(startMs, endMs, TimeZone.getDefault())
