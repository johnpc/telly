package com.johncorser.telly.features.onboarding

import android.content.Context
import android.graphics.Typeface
import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import com.johncorser.telly.core.design.TELLY_TEXT_FAINT

/**
 * The raw EditText behind [WizardScreenFieldEditor]: 12 sp Roboto Condensed
 * `#5D5F61` value text (reference 09), IME action Next commits the value and
 * hides the keyboard, and the field grabs focus + raises the IME on attach.
 */
internal fun wizardScreenEditText(
    context: Context,
    initialValue: String,
    uriInput: Boolean,
    onValueChange: (String) -> Unit,
    onCommit: () -> Unit,
): EditText =
    EditText(context).apply {
        background = null
        isSingleLine = true
        inputType =
            InputType.TYPE_CLASS_TEXT.let {
                if (uriInput) it or InputType.TYPE_TEXT_VARIATION_URI else it
            }
        imeOptions = EditorInfo.IME_ACTION_NEXT
        // After inputType: setInputType() resets the typeface (ref 09 renders
        // the value in Roboto Condensed with glyphs 16 px below the label box).
        val density = resources.displayMetrics.density
        setPadding(
            0,
            (WizardScreenDims.EDITOR_TEXT_PAD_TOP_DP * density).toInt(),
            0,
            (WizardScreenDims.EDITOR_TEXT_PAD_BOTTOM_DP * density).toInt(),
        )
        textSize = WizardScreenDims.EDITOR_TEXT_SIZE_SP
        typeface = Typeface.create("sans-serif-condensed", Typeface.NORMAL)
        setTextColor(TELLY_TEXT_FAINT.toInt())
        setText(initialValue)
        setSelection(text.length)
        doAfterTextChanged { onValueChange(it?.toString().orEmpty()) }
        setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                context.getSystemService(InputMethodManager::class.java)
                    ?.hideSoftInputFromWindow(windowToken, 0)
                onCommit()
                true
            } else {
                false
            }
        }
        post {
            requestFocus()
            context.getSystemService(InputMethodManager::class.java)?.showSoftInput(this, 0)
        }
    }
