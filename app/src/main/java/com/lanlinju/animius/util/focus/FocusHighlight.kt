package com.lanlinju.animius.util.focus

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
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

/**
 * Replaces the repetitive `MutableInteractionSource` +
 * `collectIsFocusedAsState()` + `collectIsPressedAsState()` boilerplate
 * used by [androidx.compose.material3.Button], [androidx.compose.material3.OutlinedButton],
 * [androidx.compose.material3.TextButton], etc.
 *
 * Returns a [Pair] of `(isActive: Boolean, interactionSource: MutableInteractionSource)`.
 * `isActive` is `true` when the component is focused or pressed, preventing
 * visual flash during click. Pass `interactionSource` to the button's
 * `interactionSource` parameter.
 *
 * Usage:
 * ```
 * val (isActive, interactionSource) = rememberInteractionFocus()
 * OutlinedButton(
 *     interactionSource = interactionSource,
 *     colors = ButtonDefaults.outlinedButtonColors(
 *         containerColor = if (isActive) MaterialTheme.colorScheme.primary
 *         else MaterialTheme.colorScheme.surface,
 *         contentColor = if (isActive) MaterialTheme.colorScheme.onPrimary
 *         else MaterialTheme.colorScheme.primary
 *     )
 * ) { Text("Retry") }
 * ```
 */
@Composable
fun rememberInteractionFocus(): Pair<Boolean, MutableInteractionSource> {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    return (isFocused || isPressed) to interactionSource
}

