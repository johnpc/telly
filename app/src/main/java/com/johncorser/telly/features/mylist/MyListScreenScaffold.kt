package com.johncorser.telly.features.mylist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.johncorser.telly.core.design.TELLY_ONBOARDING_BACKGROUND
import com.johncorser.telly.core.ui.TellyScreenMutedText
import com.johncorser.telly.core.ui.TellyScreenWhiteText

/**
 * The mylist screens' shared chrome (the History-screen idiom): flat app
 * background, 28 sp title top-right, an optional muted hint bottom-left,
 * with [content] composed on top so overlays can cover the chrome.
 */
@Composable
internal fun MyListScreenScaffold(
    title: String,
    hint: String?,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(TELLY_ONBOARDING_BACKGROUND)),
    ) {
        Box(Modifier.align(Alignment.TopEnd).padding(top = 20.dp, end = 24.dp)) {
            TellyScreenWhiteText(text = title, fontSize = 28.sp)
        }
        hint?.let {
            Box(Modifier.align(Alignment.BottomStart).padding(start = 40.dp, bottom = 16.dp)) {
                TellyScreenMutedText(text = it, fontSize = 13.sp)
            }
        }
        content()
    }
}
