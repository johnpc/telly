package com.johncorser.telly.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import com.johncorser.telly.core.settings.Setting
import com.johncorser.telly.core.settings.SettingsRepository

/** One persisted setting observed as Compose state (live, default-seeded). */
@Composable
fun <T> settingState(
    settings: SettingsRepository,
    setting: Setting<T>,
): State<T> = settings.flow(setting).collectAsState(initial = setting.default)
