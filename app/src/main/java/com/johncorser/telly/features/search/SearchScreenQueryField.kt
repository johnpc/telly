package com.johncorser.telly.features.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.R
import com.johncorser.telly.features.search.SearchScreenDims as Dims

/**
 * The light-grey "Speak to search" bar (captures 49/50): a StreamingTextView
 * stand-in that accepts key input directly; the IME search action commits
 * the query into the history.
 */
@Composable
internal fun SearchScreenQueryField(
    query: String,
    viewModel: SearchViewModel,
    firstResult: FocusRequester?,
    modifier: Modifier,
) {
    Box(
        modifier
            .height(Dims.barHeight)
            .clip(RoundedCornerShape(Dims.barCorner))
            .background(Dims.barFill)
            .padding(horizontal = Dims.barTextPad),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (query.isEmpty()) {
            Text(text = stringResource(R.string.search_hint), color = Dims.barHint, fontSize = 20.sp)
        }
        // Compose text fields consume DPAD_DOWN as a cursor move, which
        // would trap D-pad focus in the bar (device-verified); hand the key
        // to the focus manager so DOWN reaches the results like TiviMate.
        val focusManager = LocalFocusManager.current
        BasicTextField(
            value = query,
            onValueChange = viewModel::onQueryChange,
            singleLine = true,
            textStyle = TextStyle(color = Dims.barText, fontSize = 20.sp),
            cursorBrush = SolidColor(Dims.barText),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { viewModel.commit(query) }),
            modifier =
                Modifier.fillMaxWidth().onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && event.key == Key.DirectionDown) {
                        moveDownFromBar(firstResult, focusManager)
                    } else {
                        false
                    }
                },
        )
    }
}

/**
 * DOWN with results up lands on the FIRST channel card, not Compose's
 * geometrically nearest candidate (ref-round6 07-search-down-from-querybar);
 * the history landing keeps the plain spatial move.
 */
private fun moveDownFromBar(
    firstResult: FocusRequester?,
    focusManager: FocusManager,
): Boolean {
    if (firstResult == null) return focusManager.moveFocus(FocusDirection.Down)
    return try {
        firstResult.requestFocus()
        true
    } catch (ignored: IllegalStateException) {
        // The first card scrolled out of composition; fall back to spatial.
        focusManager.moveFocus(FocusDirection.Down)
    }
}
