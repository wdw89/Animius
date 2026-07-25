## Requirements

### Requirement: All focusable components use focus-only highlight

All buttons and interactive components in the app SHALL only highlight (primary color inversion) when focused via D-pad or controller. Touch/press interactions SHALL use the default Material3 ripple without triggering primary color inversion.

#### Scenario: Touch preserves Material3 ripple

- **WHEN** any focusable button (IconButton, TextButton, OutlinedButton, Button) is touched
- **THEN** the button SHALL show the default Material3 ripple on its current background
- **AND** the button SHALL NOT change its container color or content color on press

#### Scenario: D-pad focus triggers color inversion

- **WHEN** any focusable button receives D-pad focus (via TV remote or controller)
- **THEN** the button background SHALL turn `MaterialTheme.colorScheme.primary`
- **AND** the icon/text color SHALL invert to `MaterialTheme.colorScheme.onPrimary`

#### Scenario: Consistent behavior across all screens

- **WHEN** any button is used in the app (player overlay, dialogs, error pages, settings, detail page, home screen, etc.)
- **THEN** its highlight behavior SHALL be identical: focus drives color, touch drives ripple only
- **AND** there SHALL be only one focus-tracking utility function used throughout the project

## Implementation

### Root cause

`rememberInteractionFocus()` returns `isActive = isFocused || isPressed`, causing ALL component types (IconButton, TextButton, OutlinedButton, Button) to invert colors on touch. This is incorrect — touch should only show Material3 ripple.

### Fix

1. **Replace all `rememberInteractionFocus` calls with `rememberIsFocused`** across 8 files (12 call sites)
2. **Delete `rememberInteractionFocus`** from `FocusHighlight.kt`
3. **Fix `AdaptiveIconButton`** in video-player: use `rememberIsFocused` instead of inline `isPressed` tracking
4. **Fix `AdaptiveTextButton`** in video-player: use `rememberIsFocused` instead of inline `isPressed` tracking
5. **For `Button`/`OutlinedButton`/`TextButton`**: remove custom `interactionSource` parameter — M3 uses its own default source for ripple; focus state comes from `rememberIsFocused` modifier

### Files affected

| File | Change |
|------|--------|
| `FocusHighlight.kt` | Delete `rememberInteractionFocus()` |
| `VideoPlayerControl.kt` | `AdaptiveIconButton` + `AdaptiveTextButton`: use `rememberIsFocused` |
| `VideoPlayerScreen.kt` | 2× `rememberInteractionFocus` → `rememberIsFocused`, remove `interactionSource` param |
| `WarningMessage.kt` | 1× same |
| `CrashScreen.kt` | 3× same |
| `HistoryScreen.kt` | 2× same |
| `SearchScreen.kt` | 2× same |
| `DanmakuSettingsScreen.kt` | 1× same |
| `WeekScreen.kt` | 3× same |
