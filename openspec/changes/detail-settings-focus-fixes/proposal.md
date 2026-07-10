## Why

D-pad focus navigation on the anime detail page and apperance settings page produces unpredictable jump targets. Users pressing down from an episode button land on related anime cards instead of the episode control row (channel/reverse/more), and pressing up from settings toggles lands on a random color swatch mid-row instead of the theme mode selector. The root cause is Compose's default 2D spatial focus algorithm making decisions based on geometric proximity in layouts where elements overlap or span different horizontal widths — the spatially "closest" item is not the logically "next" item.

## What Changes

- **New focus utility toolkit** (`app/.../util/focus/`): A set of lightweight Compose modifiers that layer on top of existing Compose focus APIs (no new library dependency). Includes `onFocusHighlight` (replaces repetitive manual `onFocusChanged` + color switching), and `handleDPadKeyEvents` (direction-key interception for custom navigation boundaries).
- **Detail page layout refactor**: `EpisodeListControl` moved from a floating `Box(Alignment.BottomEnd)` overlay into an independent row below the episodes `LazyRow`, establishing a clean vertical flow: episodes → controls → related.
- **Settings page focus boundaries**: `ColorBall` color swatch row configured to only accept horizontal focus traversal (← →), so vertical ↑↓ from the switches below skips over the entire swatch row and lands on the theme mode selector.
- **Consistent focus highlight style**: All interactive elements on both screens use the shared `onFocusHighlight` modifier instead of per-element `onFocusChanged` boilerplate.

## Capabilities

### New Capabilities

- `tv-focus-traversal`: Predictable vertical and horizontal focus flow for complex layouts where items span different widths or overlap. Covers D-pad navigation boundaries, custom direction interception, and focus group isolation.

### Modified Capabilities

None — this change improves existing focus behavior without altering the visual focus style contract defined in `tv-focus-inverse-color`.

## Impact

- **New files**: `app/.../util/focus/` package (~3 files: `FocusHighlight.kt`, `DpadKeyHandler.kt`, `FocusOrder.kt`)
- **Modified files**: `AnimeDetailScreen.kt` (layout restructure + adopt shared focus modifiers), `AppearanceScreen.kt` (ColorBall focus isolation + adopt shared modifiers)
- **No API changes**: Public composable signatures unchanged
- **No new dependencies**: Uses only `androidx.compose.ui.focus.*` and `androidx.compose.ui.input.key.*` already in the project
- **No behavior change for touch/mouse users**: Focus modifiers only affect D-pad/keyboard navigation
