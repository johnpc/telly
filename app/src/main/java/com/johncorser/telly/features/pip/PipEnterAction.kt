package com.johncorser.telly.features.pip

/**
 * The quick-bar's Picture-in-picture slot: drop whatever overlay is up
 * first (the quick-bar itself, usually) so the tiny window shows clean
 * video, then hand over to the activity's real enterPictureInPictureMode
 * call.
 */
class PipEnterAction(
    private val clearChrome: () -> Unit,
    private val enter: () -> Unit,
) {
    operator fun invoke() {
        clearChrome()
        enter()
    }
}
