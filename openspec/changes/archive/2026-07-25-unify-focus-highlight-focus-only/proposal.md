## Why

`rememberInteractionFocus()` returns `isActive = isFocused || isPressed`, causing touch interactions to trigger primary color inversion on all M3 buttons (IconButton, TextButton, OutlinedButton, Button). This conflicts with Material3's intended touch behavior (ripple only, no color change). The function is used across 12 call sites in 8 files, while the rest of the project already correctly uses `rememberIsFocused()` (focus-only, ~50 call sites). This creates inconsistent UX between player controls, dialog buttons, and main app buttons.

## What Changes

- Delete `rememberInteractionFocus()` from `FocusHighlight.kt`
- Replace all 12 `rememberInteractionFocus` calls with `rememberIsFocused`
- Fix `AdaptiveIconButton` in video-player: remove inline `isPressed` tracking, use `rememberIsFocused`
- Fix `AdaptiveTextButton` in video-player: remove inline `isPressed` tracking, use `rememberIsFocused`
- For `Button`/`OutlinedButton`/`TextButton` call sites: remove custom `interactionSource` param (M3 uses its own default source for ripple; focus comes from `rememberIsFocused` modifier instead)
- Clean up unused `collectIsPressedAsState` imports

## Capabilities

### New Capabilities

- `player-button-press-highlight`: All focusable components use focus-only highlight — touch preserves Material3 default ripple, D-pad focus triggers primary color inversion. Single utility function (`rememberIsFocused`) for the entire project.

### Modified Capabilities

None — this is a new capability establishing a project-wide convention.

## Impact

- **`FocusHighlight.kt`**: delete 1 function (~12 lines)
- **`VideoPlayerControl.kt`**: `AdaptiveIconButton` + `AdaptiveTextButton` — remove `isPressed`, use `rememberIsFocused` (~15 lines changed)
- **7 app files**: replace `rememberInteractionFocus` → `rememberIsFocused`, remove `interactionSource` param from M3 buttons (~12 call sites, mostly mechanical find-and-replace)
- **Imports**: remove `collectIsPressedAsState` where it becomes unused
- **No API or dependency changes**
