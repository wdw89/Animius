package com.lanlinju.animius.util.focus

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

/**
 * Intercepts D-pad direction keys at the composable boundary before children
 * process them. Useful for custom focus navigation at layout boundaries where
 * the default 2D spatial algorithm produces wrong targets.
 *
 * Each direction callback is optional. If a callback is null, that direction
 * key falls through to child composables for normal handling.
 *
 * Usage:
 * ```
 * Box(
 *     modifier = Modifier.handleDPadKeyEvents(
 *         onDown = { focusRequester.requestFocus() },
 *         onUp = { backFocusRequester.requestFocus() }
 *     )
 * ) { ... }
 * ```
 */
fun Modifier.handleDPadKeyEvents(
    onUp: (() -> Unit)? = null,
    onDown: (() -> Unit)? = null,
    onLeft: (() -> Unit)? = null,
    onRight: (() -> Unit)? = null,
    onEnter: (() -> Unit)? = null,
): Modifier = onPreviewKeyEvent { keyEvent: KeyEvent ->
    if (keyEvent.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

    when (keyEvent.key) {
        Key.DirectionUp -> {
            onUp?.invoke()
            onUp != null
        }

        Key.DirectionDown -> {
            onDown?.invoke()
            onDown != null
        }

        Key.DirectionLeft -> {
            onLeft?.invoke()
            onLeft != null
        }

        Key.DirectionRight -> {
            onRight?.invoke()
            onRight != null
        }

        Key.DirectionCenter -> {
            onEnter?.invoke()
            onEnter != null
        }

        Key.Enter -> {
            onEnter?.invoke()
            onEnter != null
        }

        else -> false
    }
}
