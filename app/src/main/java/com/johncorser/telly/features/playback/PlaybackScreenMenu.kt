package com.johncorser.telly.features.playback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.R
import com.johncorser.telly.core.design.TELLY_MENU_SHEET
import com.johncorser.telly.core.design.TELLY_PLAYBACK_ACCENT
import com.johncorser.telly.core.ui.TellyScreenMenuRow

/**
 * Right-side context-menu sheet (captures 38-40): plain rows plus blue
 * section headers; the first row takes focus like TiviMate's Search row.
 */
@Composable
internal fun PlaybackScreenMenu(
    sections: List<PlayerMenuSection>,
    favorite: Boolean,
    onItem: (PlayerMenuItem) -> Unit,
) {
    val firstFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { firstFocus.requestFocus() }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
        LazyColumn(
            Modifier
                .padding(12.dp)
                .width(240.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(TELLY_MENU_SHEET)),
        ) {
            sections.forEachIndexed { sectionIndex, section ->
                section.header?.let { header ->
                    item { PlaybackScreenMenuHeader(header) }
                }
                itemsIndexed(section.items) { itemIndex, menuItem ->
                    TellyScreenMenuRow(
                        label = menuItem.labelFor(favorite),
                        onClick = { onItem(menuItem) },
                        modifier =
                            if (sectionIndex == 0 && itemIndex == 0) {
                                Modifier.focusRequester(firstFocus)
                            } else {
                                Modifier
                            },
                        icon = menuIcon(menuItem),
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaybackScreenMenuHeader(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 4.dp),
        color = Color(TELLY_PLAYBACK_ACCENT),
        fontSize = 14.sp,
        maxLines = 1,
    )
}

/** Only Search and Settings carry icons in the captured menu (38). */
private fun menuIcon(item: PlayerMenuItem): Int? =
    when (item) {
        PlayerMenuItem.SEARCH -> R.drawable.ic_menu_search
        PlayerMenuItem.SETTINGS -> R.drawable.ic_menu_settings
        else -> null
    }
