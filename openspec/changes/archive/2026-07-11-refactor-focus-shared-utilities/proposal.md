## Why

~450 lines of identical focus boilerplate copied across 13 files — two patterns (`onFocusChanged` + `mutableStateOf` and `MutableInteractionSource` + `collectIsFocusedAsState` + `collectIsPressedAsState`) repeated ~65 times. A `rememberIsFocused()` utility already exists in `util/focus/FocusHighlight.kt` but only `AppearanceScreen` uses it. The Button-family pattern (`rememberInteractionFocus()`) has no shared utility at all. Every new button added to any screen copies the same 4-8 lines of boilerplate, risking typos and inconsistency.

The "No new abstraction files" constraint from the original `tv-focus-inverse-color` spec was well-intentioned (keep diffs self-contained for upstream PR review) but has produced the exact opposite outcome: every file carries a large, repetitive diff that obscures the actual UI changes. A 10-line shared utility reduces each screen's diff to 1-2 lines, making upstream review easier, not harder.

## What Changes

- Add `rememberInteractionFocus()` to `util/focus/FocusHighlight.kt` — replaces the `MutableInteractionSource` + `collectIsFocusedAsState` + `collectIsPressedAsState` + `isActive` chain in Button-family components
- Migrate all 13 files from inline focus patterns to `rememberIsFocused()` / `rememberInteractionFocus()`
- Remove ~345 lines of duplicated boilerplate
- Replace "No new abstraction files" spec requirement with "Shared focus utilities preferred"

## Capabilities

### Modified Capabilities

- `tv-focus-inverse-color`: Replace "No new abstraction files" requirement with "Shared focus utilities preferred" — the visual behavior (primary/onPrimary on focus) is unchanged, but the implementation pattern shifts from per-file inline boilerplate to shared `util/focus/FocusHighlight.kt` utilities.

## Impact

- **Modified files**: 14 files — 1 util file (+10 lines) + 13 screen/component files (~-345 lines net)
- **Behavior change**: None. Same Compose APIs, same runtime focus → color mapping
- **No new dependencies**: Uses existing `androidx.compose.foundation.interaction` and `androidx.compose.ui.focus` APIs already in the project
- **Upstream PR**: Per-screen diffs shrink significantly; shared utility is self-documenting and reviewable once
