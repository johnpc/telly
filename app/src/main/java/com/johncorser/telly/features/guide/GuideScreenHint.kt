package com.johncorser.telly.features.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_GUIDE_TOAST

/**
 * The first-open hint toast at the guide's bottom right (capture 24):
 * light grey card, black text, bold key names.
 */
@Composable
internal fun GuideScreenHintToast(
    controller: GuideController,
    modifier: Modifier = Modifier,
) {
    val visible by controller.hint.collectAsState()
    if (!visible) return
    Text(
        text =
            buildAnnotatedString {
                appendHintLine("Long OK", " open menu\n")
                appendHintLine("Left", " show groups\n")
                appendHintLine("Long Left", " navigate to past programs")
            },
        modifier =
            modifier
                .padding(end = 16.dp, bottom = 16.dp)
                .background(Color(TELLY_GUIDE_TOAST), RoundedCornerShape(4.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
        color = Color.Black,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    )
}

private fun androidx.compose.ui.text.AnnotatedString.Builder.appendHintLine(
    key: String,
    rest: String,
) {
    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("$key:") }
    append(rest)
}

/**
 * The "Confirm exit by second press Back" warning, bottom-center in the
 * hint-toast treatment (the reference's own is premium-locked/uncapturable).
 */
@Composable
internal fun GuideScreenExitToast(
    controller: GuideController,
    modifier: Modifier = Modifier,
) {
    val visible by controller.exit.warning.collectAsState()
    if (!visible) return
    Text(
        text = ExitConfirm.MESSAGE,
        modifier =
            modifier
                .padding(bottom = 24.dp)
                .background(Color(TELLY_GUIDE_TOAST), RoundedCornerShape(4.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
        color = Color.Black,
        fontSize = 15.sp,
    )
}
