## Why

The video player control overlay currently has no structured D-pad focus navigation between its rows (header, slider, playback controls). When the user presses up/down, focus jumps unpredictably. Additionally, the progress slider always uses the `primary` color regardless of focus state, making it hard to tell when the slider is focused — a problem for TV users who rely on clear focus indicators.

## What Changes

- **Player control focus routing**: Add D-pad vertical navigation boundaries between the three control rows (header, slider, playback), so up/down moves predictably between rows while left/right navigates within each row.
- **Slider focus-aware color**: The `Slider` component will indicate D-pad focus via the thumb only — the thumb grows larger (15→20dp) and gains a 3dp white border when focused. The progress track and buffered track colors remain unchanged (defaulting to `primary`), consistent with both phone and TV usage. The `focusedColor` parameter is available for callers that want focused track color differentiation, but defaults to `primary` (no track change). A critical fix moves `.onFocusChanged` before `.focusable()` in the modifier chain so the focus callback actually fires.
- **Direction key shortcuts when UI hidden**: When the control overlay is hidden: pressing LEFT/RIGHT seeks 10 seconds, shows a compact overlay (slider + timestamp only), AND focuses the slider so subsequent LEFT/RIGHT presses continuously seek via the slider's own key handler; pressing UP shows UI and focuses the Forward 85s button; pressing DOWN shows UI and focuses the "选集" button; pressing OK/Center pauses playback, shows UI, and focuses the play/pause button.
- **Unified 10-second seek step**: Both the hidden-UI quick seek and the focused-slider D-pad seek use 10 seconds, so the first press and subsequent continuous presses feel consistent.
- **Default focus on load**: When the player first opens with the control UI visible, focus defaults to the play/pause button so D-pad navigation is immediately available.
- **Back key hides UI**: When the control overlay is visible, pressing Back hides the UI instead of exiting the player. Pressing Back again (with UI hidden) exits the player.
- **Speed/resize side sheet focus**: When the speed or resize side sheet opens, focus is automatically placed on the first selectable item. Pressing Back closes the side sheet and returns focus to the main control UI. The currently selected item uses bold text in addition to `primary` color for clear visual distinction.
- **Auto-hide timer respects D-pad**: The player UI auto-hide timer resets on ANY D-pad key event (including navigation keys), not just button clicks. This prevents the UI from disappearing while the user is browsing with the remote.

## Capabilities

### New Capabilities
- `player-focus-navigation`: Structured D-pad focus flow between the three control rows in the video player overlay — header (back + options) → slider → playback controls (play/pause, next, danmaku, speed, resize, episode). Up/down moves between rows; left/right navigates within each row.

### Modified Capabilities
- `tv-focus-inverse-color`: The Slider component SHALL use `primary` color only when focused. When unfocused, it uses a dimmer color (the `color` parameter), consistent with the inverse-color pattern used by buttons.

## Impact

- `video-player/src/.../component/Slider.kt`: Fix `onFocusChanged` modifier order (before `.focusable()`); change D-pad seek step to 10s; use `onClick` instead of `onValueChange`+`onValueChangeFinished` in `onKeyEvent`; `focusedColor` parameter retained (defaults to `primary`, no track color change on focus)
- `video-player/src/.../VideoPlayerControl.kt`: Add `onKeyEvent` boundaries; expose FocusRequesters; hide fullscreen button on TV; Slider call site simplified (no need for `progressLineColor`)
- `video-player/src/.../VideoPlayer.kt`: Back key hides UI first; `onPreviewKeyEvent` resets auto-hide timer
- `video-player/src/.../VideoPlayerSate.kt`: `hideControlUi()` resets `isSeeking = false`; `rememberVideoPlayerState` seek increments changed to 10s
- `app/.../videoplayer/VideoPlayerScreen.kt`: `defaultRemoteControlHandler` — LEFT/RIGHT hidden=10s seek + compact overlay + focus slider (continuous seek), UP/DOWN/OK show UI+focus targets; `LaunchedEffect` for default play/pause focus on load; Speed/Resize side sheet FocusRequester + BackHandler
