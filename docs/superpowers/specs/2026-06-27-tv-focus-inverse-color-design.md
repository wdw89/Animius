# TV Focus Inverse Color — Implementation Spec

## Problem

On Android TV, buttons in the anime detail page used a default dimming effect when focused via remote control, making it hard to distinguish the focused element. The goal was to replace this with a clear inverse-color effect (like Compose for TV buttons): focused button gets `primary` background with `onPrimary` content.

## Affected Components

| Component | Composable | Button type |
|---|---|---|
| Back button | `TopAppBar.navigationIcon` | `IconButton` |
| More menu | `TopAppBar.actions` | `IconButton` |
| Favorite heart | `FavouriteIcon` | `IconButton` |
| Episode pills | `AnimeEpisodes` | `FilledTonalButton` |
| Reverse list | `EpisodeListControl` | `Text` → wrapped in `Box` |
| More episodes | `EpisodeListControl` | `Row` |
| Channel selector | `EpisodeListControl` | `Text` → wrapped in `Box` |
| Episode chips (bottom sheet) | `EpisodeBottomSheet` | `SuggestionChip` |

## Implementation Patterns

### Pattern A: Components with `interactionSource` support (`FilledTonalButton`, `SuggestionChip`)

Use the component's built-in `interactionSource` parameter + `collectIsFocusedAsState()` / `collectIsPressedAsState()`.

```
val interactionSource = remember { MutableInteractionSource() }
val isFocused by interactionSource.collectIsFocusedAsState()
val isPressed by interactionSource.collectIsPressedAsState()
val isActive = isFocused || isPressed

Component(
    interactionSource = interactionSource,
    colors = ...(
        containerColor = when {
            isAndroidTV && isActive -> MaterialTheme.colorScheme.primary
            else -> defaultColor
        }
    )
)
```

Do NOT add explicit `.onFocusChanged` or `.focusable()` modifiers — the component handles these internally via `interactionSource`.

**Why `isActive = isFocused || isPressed`**: Prevents a visual flash when the user clicks — if only `isFocused` were used, the color would revert to default during the press-down phase before navigation fires.

### Pattern B: Components without `interactionSource` (`IconButton`, `Box`/`Row` wrappers)

Use `.onFocusChanged` modifier + `mutableStateOf`.

```
var isFocused by remember { mutableStateOf(false) }

Component(
    modifier = Modifier
        .onFocusChanged { if (isAndroidTV) isFocused = it.isFocused }
        .clickable(onClick = ...),
    colors = ...(
        containerColor = when {
            isAndroidTV && isFocused -> MaterialTheme.colorScheme.primary
            else -> defaultColor
        }
    )
)
```

**On non-TV devices**: `onFocusChanged` is harmless — it simply never fires without focus navigation.

### Color Rules

All components follow the same color mapping:

| State | Background | Content (icon/text) |
|---|---|---|
| Not focused (default) | `surfaceVariant.copy(alpha = 0.45f)` or component default | `primary` or component default |
| Focused / Pressed (TV only) | `MaterialTheme.colorScheme.primary` | `MaterialTheme.colorScheme.onPrimary` |

### Background Addition

Components that originally had no background (`Text` with `clickable`:
- Reverse list, More episodes, Channel selector) were wrapped in a `Box` (or kept as `Row`) with:
  - `.clip(RoundedCornerShape(8.dp))`
  - `.background(...)` with the conditional color
  - `.padding(horizontal = 12.dp, vertical = 3.dp)` for compact sizing
  - Vertical offset increased from `large_padding + 6.dp` to `large_padding + 16.dp` to prevent overlap with episode buttons above

### Pitfalls Encountered

1. **`.let {}` with `Modifier`**: Using `.let { if (cond) it.focusable() else it }` broke click handling. The plain `Modifier` returned from the else branch caused unexpected behavior. Fixed by using direct modifier calls or conditional `.onFocusChanged` that's always applied but guarded inside the lambda.

2. **Redundant `.focusable()`**: Adding explicit `.focusable()` before `.clickable()` can interfere with `clickable`'s internal focus handling. Since `clickable` (and button components) already add `focusable()`, never add it manually.

3. **`onFocusChanged` vs `interactionSource`**: For components that support `interactionSource` (buttons, chips), prefer `interactionSource.collectIsFocusedAsState()` over `onFocusChanged` — it's synchronized with the component's own state machine and avoids timing issues.

4. **Missing `interactionSource` parameter**: Creating a `MutableInteractionSource` and using `collectIsFocusedAsState()` on it is useless if the component doesn't receive that `interactionSource` parameter. Always pass it explicitly.

## TV Detection

```
val isAndroidTV = com.lanlinju.animius.util.isAndroidTV(context)
// Returns true on Android TV devices, false on phones/tablets
```

All focus inverse color effects are gated on `isAndroidTV` so phone/tablet behavior is unchanged.
