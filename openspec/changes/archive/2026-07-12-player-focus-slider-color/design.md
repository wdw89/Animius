## Context

The video player has a control overlay (`VideoPlayerControl`) with three visual rows:
1. **Header row**: Back button, title, options (Forward 85s + MoreVert dropdown)
2. **Slider row**: Timeline label + fullscreen toggle, then the progress slider
3. **Playback control row**: Play/pause, next episode, danmaku toggle (left group); speed, resize, episode selection (right group)

Currently there is no structured D-pad focus flow between these rows. The `defaultRemoteControlHandler` on the outer `VideoPlayer` handles basic show/hide and play/pause, but when controls are visible, focus navigation between rows is left to Compose's default spatial algorithm, which can produce wrong targets because row items have varying widths and horizontal positions.

The custom `Slider` already has its own D-pad left/right handling for seeking, but always uses a single `color` parameter (currently set to `inversePrimary`) for the progress track and thumb, providing no visual distinction between focused and unfocused states.

### Constraints
- The `video-player` module is a separate Gradle module from `app` and cannot depend on `app`'s shared focus utilities (`DpadKeyHandler.kt`, `FocusHighlight.kt`)
- The `Slider` component is in the `video-player` module
- The slider must remain usable via touch (drag/tap) regardless of focus state
- No external TV library dependency (consistent with existing focus architecture)

## Goals / Non-Goals

**Goals:**
- Predictable vertical D-pad focus flow: header ↔ slider ↔ playback controls
- Horizontal navigation within each row (left/right for buttons, seeking for slider)
- Visual focus feedback on the slider via color change (primary when focused, dimmer when not)
- Works with existing `defaultRemoteControlHandler` (no regression in remote control behavior)

**Non-Goals:**
- Rewriting the entire player controller UI structure
- Changing the slider's existing D-pad seek step size
- Touch/gesture behavior changes

## Decisions

### Decision 1: Back key hides UI first, exits on second press

**Choice**: When the player control UI is visible, pressing Back hides the UI (calls `playerState.hideControlUi()`). When the UI is already hidden, Back exits the player normally via `onBackPress`.

**Rationale**: This matches standard media player behavior on Android TV (YouTube, Netflix). Users open controls to check progress or adjust settings — a single Back should dismiss the overlay, not exit the player.

**Implementation**: The `BackHandler` in `VideoPlayer` currently calls `onBackPress()` unconditionally. Change it to check `playerState.isControlUiVisible` — if visible, hide controls; otherwise, call `onBackPress`.

### Decision 2: Direction key shortcuts — compact overlay with slider focus for LEFT/RIGHT continuous seek

**Choice**: When controls are hidden and user presses LEFT/RIGHT, a **compact overlay** appears (only slider + timestamp visible; header and playback rows hidden by `isSeeking = true`) AND **focus is placed on the slider**. Because the slider now has focus, subsequent LEFT/RIGHT presses are handled by the slider's own `onKeyEvent` — enabling **continuous seek** via repeated presses or hold. The compact overlay auto-hides via the normal 6-second auto-hide timer (not a custom 1.5s coroutine).

Hidden UI shortcuts:
- **LEFT** → 10-second seek back, show compact overlay, focus slider
- **RIGHT** → 10-second seek forward, show compact overlay, focus slider
- **UP** → show full control UI, focus Forward 85s button
- **DOWN** → show full control UI, focus "选集" button
- **OK/Center** → pause + show full control UI, focus play/pause

Full UI + slider focus (10s seek):
- **LEFT** → slider's own `onKeyEvent` seeks 10s back (continuous)
- **RIGHT** → slider's own `onKeyEvent` seeks 10s forward (continuous)

**Rationale**: The previous design used a 1.5s auto-hide coroutine and did not focus the slider, so subsequent LEFT/RIGHT presses had no handler (the outer Box's `onKeyEvent` returns false when `isControlUiVisible` is true, and the slider had no focus). By focusing the slider on the first press, continuous seek works naturally — the slider's own `onKeyEvent` handles every subsequent press. Reusing the normal 6s auto-hide timer (reset by `onUserInteraction()` on every key via `onPreviewKeyEvent`) means the overlay stays visible while the user keeps pressing, and disappears 6s after the last press. `isSeeking = true` keeps the overlay compact (hides header + playback rows); it is reset to `false` in `hideControlUi()` so the next `showControlUi()` renders the full UI.

**Implementation**: `onTimedSeek(skipMs)` is a new method on `VideoPlayerState` (implemented in `VideoPlayerStateImpl`):
1. If UI not visible, `showControlUi()` — starts the auto-hide timer (resets `isSeeking=false` + `hideAfterMs=6s`)
2. `isSeeking.value = true` — overrides `showControlUi`'s reset, hides header + playback rows (compact overlay)
3. `hideAfterMs = seekHideAfterMs` (1500ms) — overrides `showControlUi`'s 6s with a shorter 1.5s timer
4. `controlUiLastInteractionMs = 0` — reset timer so 1.5s counts from this seek
5. Compute new position, update `videoProgress`, call `player.seekTo(newPos)`

In `VideoPlayerScreen.kt`, the outer Box's `onKeyEvent` calls `playerState.onTimedSeek(±10000)` then sets `pendingFocusTarget = FocusTarget.SLIDER` for the deferred focus queue.

**Critical: `isSeeking` lifecycle** — `isSeeking` is NOT reset in `hideControlUi()` or `onSeeked()`. It stays true during fade-out so the compact overlay fades out directly (no full UI flash). It is reset to `false` only in `showControlUi()`, so the next time the UI is shown (tap or UP/DOWN/OK), it starts in full UI mode.

**Short seek timer**: A new `hideAfterMs` variable (default 6s, set to 1.5s during seeking) controls the auto-hide delay. The polling loop in `showControlUi()` checks `controlUiLastInteractionMs >= hideAfterMs`. `onUserInteraction()` (called on every D-pad key via `onPreviewKeyEvent`) resets `controlUiLastInteractionMs = 0`, so continuous D-pad pressing keeps resetting the 1.5s timer. The overlay hides 1.5s after the last key press.

**Touch drag**: `onSeeking()` reorders to call `showControlUi()` before setting `isSeeking=true` (so `showControlUi`'s `isSeeking=false` reset doesn't clobber it). `onSeeked()` no longer resets `isSeeking` — it sets `hideAfterMs = 1.5s` and resets the timer, so the compact overlay hides 1.5s after drag release without flashing to full UI.

### Decision 3: Default focus on initial load

**Choice**: After `LaunchedEffect(url)` calls `showControlUi()`, use `LaunchedEffect(Unit)` with `runCatching` to request focus on the play/pause button.

**Rationale**: `showControlUi()` is called on initial playback start but no FocusRequester is ever invoked programmatically. The user sees controls but D-pad is dead until they press a direction key. Defaulting to play/pause gives an immediate, natural starting point.

### Decision 4: Fullscreen button hidden on TV

**Choice**: The `TimelineControl` composable SHALL use `LocalContext.current.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)` to detect TV devices. On TV, the fullscreen toggle button is not rendered at all.

**Rationale**: The fullscreen toggle is a phone-only feature — it switches between landscape fullscreen and portrait 16:9. On TV, both orientation changes are skipped (`isAndroidTV` / `isWideScreen` guards return early), and the screen is already ~16:9. Hiding it entirely removes an unnecessary dead control from the UI and eliminates it from the focus flow automatically.

### Decision 5: Explicit focusProperties routing + focusable anchor

**Choice**: Two mechanisms work together:

1. **Outer Box** gets `.focusable()` + `onKeyEvent` handler (`handlePlayerKeys`). When the control UI is hidden, this Box holds focus — providing a Compose focus node so key events are dispatched. `handlePlayerKeys` intercepts LEFT/RIGHT (KeyDown, 10s seek + compact overlay + `pendingFocusTarget = SLIDER` to focus the slider) and UP/DOWN/OK (KeyUp, show controls + focus target). After the first LEFT/RIGHT, the slider has focus and handles subsequent presses itself.

2. **Row-level `focusProperties`** on the back button, slider, and play/pause button explicitly declare direction targets using `FocusRequester` references. This bypasses Compose's spatial search entirely — which cannot work because the outer `focusable` Box at `fillMaxSize` always appears "closest" to every candidate.

| Component | `focusProperties` |
|---|---|
| Back button (IconButton) | `down = sliderFocusRequester` |
| Slider (Box) | `up = backFocusRequester`; `down = playPauseFocusRequester` |
| PlayPause button (AdaptiveIconButton) | `up = sliderFocusRequester` |

**Rationale**: After attempting spatial search, `moveFocus()`, `requestFocus()` safety nets, and `canFocus = false`, the root issue is that the outer Box MUST be focusable (for hidden-UI key dispatch) but MUST NOT participate in spatial search (it's `fillMaxSize` and always wins). `focusProperties` with explicit FocusRequester targets is the only mechanism that satisfies both constraints. The Compose focus skill advises: "Use this sparingly. Too many hard-coded links create stale focus graphs when layouts change. Prefer natural focus order unless the design requires a specific jump or trap." This is that case — the outer Box is an unavoidable trap that requires explicit routing.

**Implementation**:
- `VideoPlayerScreen.kt`: Outer Box gets `.focusable() + onKeyEvent(handlePlayerKeys)`
- `VideoPlayerControl.kt`: Back `IconButton` gets `Modifier.focusProperties { down = sliderFocusRequester }`; `Slider` Box gets `Modifier.focusProperties { up = backFocusRequester; down = playPauseFocusRequester }`; `PlayPauseButton` gets `Modifier.focusProperties { up = sliderFocusRequester }`
- New FocusRequester: `backFocusRequester`
- `Slider.kt`: expose `focusProperties` via a `focusPropertiesModifier` parameter or apply it in `BottomControlBar`

### Decision 6: Slider focus feedback — thumb-only, no track color change

**Choice**: The slider signals D-pad focus through thumb animations only: thumb grows from 15dp to 20dp and gains a **3dp white border**. The track color (play progress, buffered, unplayed) remains unchanged on focus. The `focusedColor` parameter exists for callers who explicitly want focused track differentiation but defaults to `primary` (same as `color`).

| Parameter | Default | Meaning |
|---|---|---|
| `color` | `MaterialTheme.colorScheme.primary` | Play progress + thumb (unfocused) |
| `focusedColor` | `MaterialTheme.colorScheme.primary` | Play progress + thumb (focused) — same as unfocused by default |
| `trackColor` | `Color.LightGray.copy(alpha = 0.38f)` | Unplayed track (unchanged) |
| `secondTrackColor` | `Color.LightGray.copy(alpha = 0.78f)` | Buffered track (unchanged) |

**Rationale**: The default `color` is the app's brand `primary` — changing it to white would alter the phone UX for the sake of TV focus, which is the wrong trade-off. The thumb's size increase (15→20dp) and white border (0→3dp) already provide clear, unambiguous focus feedback without touching track colors at all. This keeps phone and TV experiences unified: the slider looks the same on both form factors, with TV adding only the thumb animation overlay.

**Critical fix — `onFocusChanged` order**: The thumb animation was not working because `.onFocusChanged { ... }` was placed **after** `.focusable()` in the modifier chain. In Compose, `onFocusChanged` must be placed **before** `.focusable()` to observe the focusable node's own focus state. When placed after, it observes a child node and `isFocused` never becomes true. See Decision 11 for details.

**Slider D-pad seek step**: Changed to 10 seconds (`10000f / durationMs`). 10s is the standard TV seek step (YouTube TV, Netflix). This unifies the hidden-UI quick seek and the focused-slider seek to the same step size, so the first press and subsequent presses feel consistent.

### Decision 7: No shared utility dependency for video-player module

**Choice**: Use inline `onKeyEvent` for row boundaries and inline focus state tracking in `VideoPlayerControl.kt`, consistent with the module's existing self-contained approach (`AdaptiveIconButton`, `AdaptiveTextButton`).

**Rationale**: The `video-player` module already has its own focus patterns using `MutableInteractionSource` and `onFocusChanged`. Adding a dependency on `app` module or creating a shared utility module would be architectural overhead for a single consumer. The module's self-contained focus helpers (`AdaptiveIconButton`, `AdaptiveTextButton`) already work well — we just extend the pattern to row navigation.

### Decision 8: Focus routing target selection

**Choice**: When pressing DOWN from the header row, move focus to the slider. When pressing DOWN from the slider, move focus to the first button in the playback control row (play/pause). UP reverses the flow.

**Rationale**: This follows the visual layout: header is topmost, slider is middle, playback controls are bottom. The slider is the most important control for seeking, so DOWN from header goes there first. UP from slider goes to back button in header (the most prominent header control).

### Decision 9: Speed/resize side sheets receive auto-focus and Back support

**Choice**: Add `FocusRequester` + `LaunchedEffect(Unit)` to the first item in `SpeedSideSheet` and `ResizeSideSheet`. Add `BackHandler` to both side sheets to close on Back key press.

**Rationale**: Currently `showSpeedUi()` calls `hideControlUi()`, which removes the entire `VideoPlayerControl` composable tree from composition — including whichever button had focus (e.g., the "倍速" button). When the side sheet appears via `AnimatedVisibility`, no element requests focus, so the D-pad has no target. This is why side sheet items appear "unfocusable."

The `AdaptiveTextButton` composable already has full inverse-color focus handling (`collectIsFocusedAsState` + `collectIsPressedAsState`), so once focus enters the side sheet, highlighting works automatically. No color changes needed in `AdaptiveTextButton`.

**Implementation**:
- First `AdaptiveTextButton` in each side sheet gets `Modifier.focusRequester(focusRequester)`
- `LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }` places focus on the first item
- `BackHandler { onDismissRequest() }` closes the sheet on Back key
- On dismiss, `VideoPlayerControl` reappears; existing slider focus behavior resumes

### Decision 10: Auto-hide timer resets on any D-pad key event

**Choice**: Add a `Modifier.onPreviewKeyEvent` on the inner `VideoPlayer` Box that calls `playerState.onUserInteraction()` — which resets `controlUiLastInteractionMs` to 0 — and returns `false` (does not consume the event). Add `onUserInteraction()` to the `VideoPlayerState` interface.

**Rationale**: The current auto-hide timer (`controlUiLastInteractionMs`) only resets on button clicks (`play()`, `pause()`, `onSeeking()`, `onClickSlider()`, etc.) and `showControlUi()`. D-pad navigation keys (UP/DOWN/LEFT/RIGHT/OK) do NOT reset the timer. This means:
- When browsing DropdownMenu items with D-pad, the timer keeps counting.
- When navigating between control buttons, the timer keeps counting.
- The UI can disappear mid-navigation.

Using `onPreviewKeyEvent` at the `VideoPlayer` Box level (priority before any child) and returning `false` ensures:
- Every D-pad key resets the timer.
- No keys are consumed — child composables (slider seeking, dropdown menu, etc.) continue to work normally.

**Implementation**:
- `VideoPlayerState` interface gets `fun onUserInteraction()` (default no-op for backwards compatibility)
- `VideoPlayerStateImpl` implements it as `controlUiLastInteractionMs = 0`
- Inner `VideoPlayer` Box gets `.onPreviewKeyEvent { playerState.onUserInteraction(); false }`

### Decision 11: Fix `onFocusChanged` modifier order in Slider.kt

**Choice**: Move `.onFocusChanged { isFocused = it.isFocused }` to **before** `.focusable()` in the Slider's modifier chain.

**Rationale**: This was the root cause of Bug 1 (thumb never animating in full UI). In Compose, modifier order matters for focus: `onFocusChanged` observes the focus state of the node created by the **next** modifier in the chain. When `onFocusChanged` is placed after `focusable()`, it observes a child of the focusable node, and `it.isFocused` never becomes true. The correct order is:

```kotlin
modifier
    .focusRequester(focusRequester)
    .onFocusChanged { isFocused = it.isFocused }  // BEFORE focusable
    .focusable()
    .onKeyEvent { ... }
```

This is documented in the Compose focus API: "onFocusChanged should be placed before focusable/focusGroup modifiers to observe their focus state."

### Decision 12: Unify hidden-UI seek with slider focus (continuous seek)

**Choice**: When the user presses LEFT/RIGHT with the UI hidden, the compact overlay appears AND the slider receives focus (`pendingFocusTarget = FocusTarget.SLIDER`). Subsequent LEFT/RIGHT presses are then handled by the slider's own `onKeyEvent` — enabling continuous seek via repeated presses or hold.

**Rationale**: The previous design did not focus the slider on the first LEFT/RIGHT press. After `showControlUi()` set `isControlUiVisible = true`, the outer Box's `onKeyEvent` returned false (because `if (isControlUiVisible) return false`), and the slider had no focus — so subsequent LEFT/RIGHT presses went nowhere. The user observed "无UI时按左右键有放大加白边但是依然无法长按或多次按键来持续快退快进" — the thumb appeared to animate (because `isSeeking = true` drove `isActive`), but continuous seek did not work. By focusing the slider on the first press, the slider's own `onKeyEvent` takes over and continuous seek works naturally.

**Implementation**:
- Add `FocusTarget.SLIDER` to the enum
- In the outer Box's `onKeyEvent`, LEFT/RIGHT KeyDown: after `onTimedSeek(...)`, set `pendingFocusTarget = FocusTarget.SLIDER`
- The existing `LaunchedEffect(pendingFocusTarget)` deferred focus queue handles the `sliderFocusRequester.requestFocus()` call after `AnimatedVisibility` composes

### Decision 13: Slider D-pad seek uses `onClick` instead of `onValueChange` + `onValueChangeFinished`

**Choice**: The slider's `onKeyEvent` for LEFT/RIGHT calls `onClick(progress)` (which maps to `state.onClickSlider` — updates `videoProgress` + seeks the player directly) instead of `onValueChange(progress)` + `onValueChangeFinished()` (which map to `state.onSeeking` + `state.onSeeked` — sets `isSeeking = true` then `false`).

**Rationale**: `onSeeking` sets `isSeeking = true` and `onSeeked` sets `isSeeking = false`. If the slider's `onKeyEvent` used this path, each D-pad seek press would flip `isSeeking` true→false, causing the compact overlay (which relies on `isSeeking = true` to hide header + playback rows) to flash back to full UI on each press. By using `onClick` (which does NOT toggle `isSeeking`), the compact overlay stays compact during continuous seek. The thumb animation is driven by `isFocused` (Decision 11 fix), not `isSeeking`, so it remains visible throughout.

**Implementation** in `Slider.kt`:
```kotlin
.onKeyEvent { event ->
    if (event.type == KeyEventType.KeyDown) {
        val stepFraction = if (durationMs > 0) 10000f / durationMs else 0.02f
        when (event.key) {
            Key.DirectionRight -> { onClick((value + stepFraction).coerceIn(0f, 1f)); true }
            Key.DirectionLeft -> { onClick((value - stepFraction).coerceIn(0f, 1f)); true }
            else -> false
        }
    } else false
}
```

### Decision 14: Unify seek step to 10 seconds

**Choice**: Both the hidden-UI quick seek (`onTimedSeek`) and the focused-slider D-pad seek use **10 seconds** (`10000ms` / `10000f / durationMs`).

**Rationale**: The previous design used 5s for hidden-UI and 30s for focused-slider. Now that hidden-UI seek focuses the slider (Decision 12), the first press (10s via `onTimedSeek`) and subsequent presses (via slider's `onKeyEvent`) must use the same step — otherwise the first press jumps 5s and subsequent presses jump 30s, which feels jarring. 10s is the standard TV seek step (YouTube TV, Netflix, Android TV system media controls). The `ExoPlayer.Builder` config in `rememberVideoPlayerState` also sets `setSeekForwardIncrementMs(15 * 1000)` / `setSeekBackIncrementMs(15 * 1000)` — these should be updated to 10s as well for consistency, though they are only used by `control.forward()` / `control.rewind()` which are not currently called by D-pad handlers.

**Implementation**:
- `Slider.kt`: `stepFraction = 10000f / durationMs`
- `VideoPlayerScreen.kt` `onTimedSeek`: `skipMs = ±10000`
- `VideoPlayerSate.kt` `rememberVideoPlayerState`: `setSeekForwardIncrementMs(10 * 1000)`, `setSeekBackIncrementMs(10 * 1000)`

## Risks / Trade-offs

- **Risk**: `onKeyEvent` at row level may interfere with the slider's own left/right key handling.
  - **Mitigation**: Use `onKeyEvent` (not `onPreviewKeyEvent`) so child composables consume keys first. Only intercept Up/Down which the slider doesn't handle anyway.

- **Risk**: The player control's `AnimatedVisibility` enter/exit may cause `FocusRequester` timing issues (similar to the reverse list crash on detail page).
  - **Mitigation**: Use `runCatching` for `requestFocus()` calls and avoid storing `FocusRequester` across composable lifecycles.

- **Trade-off**: The `focusedColor` addition makes the Slider API slightly larger, but it's a single optional parameter with a sensible default.
