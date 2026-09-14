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
import androidx.compose.ui.graphics.SolidColor
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
        BasicTextField(
            value = query,
            onValueChange = viewModel::onQueryChange,
            singleLine = true,
            textStyle = TextStyle(color = Dims.barText, fontSize = 20.sp),
            cursorBrush = SolidColor(Dims.barText),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { viewModel.commit(query) }),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
