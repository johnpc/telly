package com.johncorser.telly.features.player

/**
 * Settings-driven audio behavior injected into [Media3PlayerEngine.create]:
 * "Select surround audio track by default" (read live per tracks change)
 * and "Audio passthrough" (read once at player creation — each visit to a
 * playback surface builds a fresh engine, so a change applies on the next
 * tune-in). Both captured defaults are off = today's behavior.
 */
class PlayerAudioPrefs(
    val surroundByDefault: () -> Boolean = { false },
    val passthrough: () -> Boolean = { false },
)
