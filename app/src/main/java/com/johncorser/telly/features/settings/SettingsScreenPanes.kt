package com.johncorser.telly.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_SETTINGS_HEADER
import com.johncorser.telly.core.design.TELLY_TEXT_PRIMARY
import com.johncorser.telly.core.ui.LocalAccentColor
import com.johncorser.telly.core.ui.rememberFocusSeed

/** Header strip (#333639, 72 dp) + content — both panes share this frame. */
@Composable
internal fun SettingsScreenPane(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier.fillMaxSize()) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(SettingsScreenDims.headerHeight)
                    .background(Color(TELLY_SETTINGS_HEADER)),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = title,
                color = Color(TELLY_TEXT_PRIMARY),
                fontSize = SettingsScreenDims.headerTitleSize,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = SettingsScreenDims.panePadding),
            )
        }
        content()
    }
}

/** The focusable rows of a sheet, with headers/notes rendered statically. */
@Composable
internal fun SettingsScreenRows(
    rows: List<SettingsRow>,
    onActivate: (String) -> Unit,
    initialFocusId: String? = null,
    onRowFocused: (String) -> Unit = {},
) {
    // Once focus lands on ANY row, the initial grab stands down — it must
    // never yank focus back from a row a D-pad move (or the harness) just
    // reached (the Assign-EPG picker steal).
    val seed = rememberFocusSeed()
    LazyColumn(
        modifier = Modifier.fillMaxSize().then(seed.modifier()),
        contentPadding = PaddingValues(vertical = 12.dp),
    ) {
        items(rows, key = { it.id }) { row ->
            when (row) {
                is SettingsRow.Header, is SettingsRow.Note -> SettingsScreenStaticRow(row)
                else ->
                    SettingsScreenRow(
                        row = row,
                        onActivate = onActivate,
                        modifier = Modifier.padding(horizontal = SettingsScreenDims.rowMargin),
                        onFocused = { onRowFocused(row.id) },
                        requestFocus = row.id == initialFocusId,
                        grabYielded = seed.seeded,
                    )
            }
        }
    }
}

/** Blue group headers + the premium/footer notes (non-focusable). */
@Composable
internal fun SettingsScreenStaticRow(row: SettingsRow) {
    val accent = LocalAccentColor.current
    val (text, color, topPadding) =
        when (row) {
            is SettingsRow.Header -> Triple(row.text, accent, 18.dp)
            is SettingsRow.Note -> Triple(row.text, if (row.accent) accent else Color(0xFF919394), 8.dp)
            else -> return
        }
    Text(
        text = text,
        color = color,
        fontSize = SettingsScreenDims.noteSize,
        fontFamily = SettingsScreenDims.fontFamily,
        modifier =
            Modifier.padding(
                start = SettingsScreenDims.panePadding,
                end = SettingsScreenDims.panePadding,
                top = topPadding,
                // The blue premium note sits 12 dp above the first pill
                // (ref/01: note bottom 217 px, pill top 241 px).
                bottom = if (row is SettingsRow.Note) 12.dp else 8.dp,
            ),
    )
}
