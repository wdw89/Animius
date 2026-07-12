## Why

Two focus-related bugs in the video player cause crashes and broken UI states during D-pad navigation: (1) pressing UP/DOWN while in compact seek mode (after LEFT/RIGHT with hidden UI) crashes because Slider's `focusProperties` point to `FocusRequester`s that are not in the composition tree during compact mode, and (2) pressing UP/DOWN while a side sheet (episode selector, speed, resize) is open incorrectly triggers the hidden-UI shortcut handler, showing the full control overlay on top of the still-visible side sheet — creating a confusing double-overlay state.

## What Changes

- **Bug 1 fix**: Prevent UP/DOWN crash during compact seek mode by having the Slider handle UP/DOWN key events to exit compact mode gracefully, instead of relying on `focusProperties` that point to removed composables.
- **Bug 2 fix**: Guard the hidden-UI D-pad shortcut handler in `VideoPlayerScreen` to not intercept keys when any side sheet (episode/speed/resize) is visible, allowing side sheets to handle their own focus navigation.

## Capabilities

### New Capabilities

- `player-focus-dpad-safety`: Crash-free D-pad navigation during compact seek mode and correct side-sheet-aware key routing

### Modified Capabilities

- `player-focus-navigation`: Hidden-UI shortcut handler must skip UP/DOWN/CENTER when side sheets are visible, and Slider must safely handle UP/DOWN during compact seek mode instead of crashing via dangling focusProperties

## Impact

- `VideoPlayerScreen.kt`: outer `.onKeyEvent` handler (add side-sheet visibility guard)
- `Slider.kt` or `VideoPlayerControl.kt`: handle UP/DOWN during compact mode (slider's key event or its `focusProperties`)
- No API changes, no dependency changes, no breaking changes
