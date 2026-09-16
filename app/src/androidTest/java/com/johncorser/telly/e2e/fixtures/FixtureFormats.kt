package com.johncorser.telly.e2e.fixtures

import com.johncorser.telly.features.playback.ClockStyle
import com.johncorser.telly.features.playback.ProgramTimes

/** The time-range string the app renders for this programme. */
fun FixtureProgramme.rangeText(): String = ProgramTimes.range(startMs, endMs, ClockStyle())
