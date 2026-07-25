package com.lanlinju.animius.util.focus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged

/**
 * Replaces the repetitive `var isFocused by remember { mutableStateOf(false) }`
 * + `Modifier.onFocusChanged { isFocused = it.isFocused }` boilerplate.
 *
 * Returns a [Pair] of `(isFocused: Boolean, focusModifier: Modifier)`.
 * Attach the modifier to your composable; read `isFocused` for colors.
 *
 * Usage:
 * ```
 * val (isFocused, focusModifier) = rememberIsFocused()
 * IconButton(
 *     modifier = Modifier.then(focusModifier),
 *     colors = IconButtonDefaults.iconButtonColors(
 *         containerColor = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent
 *     )
 * ) {
 *     Icon(tint = if (isFocused) MaterialTheme.colorScheme.onPrimary else Color.White)
 * }
 * ```
 */
@Composable
fun rememberIsFocused(): Pair<Boolean, Modifier> {
    var isFocused by remember { mutableStateOf(false) }
    return isFocused to Modifier.onFocusChanged { isFocused = it.isFocused }
}

