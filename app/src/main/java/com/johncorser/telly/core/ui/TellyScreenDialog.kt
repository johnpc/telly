package com.johncorser.telly.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text

/**
 * Small centered dialog over playback chrome: a rounded column with a title
 * row, shared by the custom-recording form and the quick-bar track pickers.
 */
@Composable
fun TellyScreenDialog(
    title: String,
    width: Dp,
    fill: Color,
    corner: Dp,
    verticalPadding: Dp,
    titleSize: TextUnit,
    titlePadding: PaddingValues,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            Modifier
                .width(width)
                .background(fill, RoundedCornerShape(corner))
                .padding(vertical = verticalPadding, horizontal = 8.dp),
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = titleSize,
                modifier = Modifier.padding(titlePadding),
            )
            content()
        }
    }
}
