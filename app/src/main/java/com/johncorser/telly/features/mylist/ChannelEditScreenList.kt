package com.johncorser.telly.features.mylist

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.johncorser.telly.R
import com.johncorser.telly.core.ui.rememberFocusSeed
import com.johncorser.telly.features.playlist.db.ChannelEntity

/** The shared Manage-Favorites / Reorder-channels / names-editor list chrome. */
@Composable
internal fun ChannelEditScreenList(
    title: String,
    rows: List<ChannelEntity>,
    showStars: Boolean,
    onToggle: (ChannelEntity) -> Unit,
    onMove: (ChannelEntity, Int) -> Unit,
    hint: String = stringResource(R.string.channel_edit_move_hint),
) {
    MyListScreenScaffold(title = title, hint = hint) {
        val seed = rememberFocusSeed()
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(start = 40.dp, top = 80.dp, end = 40.dp, bottom = 44.dp)
                .then(seed.modifier()),
        ) {
            items(rows, key = { it.id }) { channel ->
                ChannelEditScreenRow(
                    channel = channel,
                    showStars = showStars,
                    requestFocus = channel == rows.first(),
                    grabYielded = seed.seeded,
                    onToggle = onToggle,
                    onMove = onMove,
                )
            }
        }
    }
}
