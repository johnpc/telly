package com.johncorser.telly.core.design

// Design tokens shared across feature slices, as ARGB Long values so they stay
// plain-JVM testable. Feature UI converts them with androidx Color(Long).
const val TELLY_BACKGROUND = 0xFF0E1116
const val TELLY_SURFACE = 0xFF161B22
const val TELLY_ACCENT = 0xFF2EC4B6
const val TELLY_TEXT_PRIMARY = 0xFFF5F7FA
const val TELLY_TEXT_SECONDARY = 0xFF8B949E

// Onboarding + wizard palette, sampled with a color picker from the TiviMate
// reference screenshots (docs/reference/screens 02, 03, 07 and 10 at 1920x1080).
const val TELLY_ONBOARDING_BACKGROUND = 0xFF131619
const val TELLY_GUIDANCE_PANE = 0xFF232629
const val TELLY_FOCUS_FILL = 0xFFDEE0E2
const val TELLY_FOCUS_TEXT = 0xFF000000
const val TELLY_BUTTON_RESTING = 0xFF272A2D
const val TELLY_TEXT_MUTED = 0xFFA7A8A9
const val TELLY_TEXT_DISABLED = 0xFF4D5052
const val TELLY_TEXT_FAINT = 0xFF5D5F61
const val TELLY_TEXT_FAINT_FOCUSED = 0xFF6F7071
const val TELLY_TEXT_GUIDANCE_MUTED = 0xFF919394
const val TELLY_PANE_DIVIDER = 0xFF222527
const val TELLY_FIELD_UNDERLINE = 0xFF3D4042
const val TELLY_ERROR_TEXT = 0xFFE57373
