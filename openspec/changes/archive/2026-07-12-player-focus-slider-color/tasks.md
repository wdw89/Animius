## 1. Slider D-pad seek step and focus fix

- [x] 1.1 `focusedColor` parameter already added (defaults to `primary` — no track color change, thumb-only focus feedback)
- [x] 1.2 Change D-pad seek step to 10000ms (10s) in `Slider.kt` (`10000f / durationMs`)
- [x] 1.3 Change thumb focus border from 2dp to 3dp in `Slider.kt`
- [x] 1.4 **Fix `onFocusChanged` modifier order**: move `.onFocusChanged { isFocused = it.isFocused }` to BEFORE `.focusable()` in `Slider.kt` modifier chain (root cause of thumb not animating)
- [x] 1.5 **Change slider `onKeyEvent` to use `onClick`** instead of `onValueChange` + `onValueChangeFinished` (avoids `isSeeking` true→false cycle that flashes compact overlay to full UI)
- [ ] 1.6 Verify: thumb grows + 3dp white border on focus, track colors unchanged, drag/tap works

## 2. Player control focus navigation (focusProperties explicit routing)

- [x] 2.1 Hide fullscreen button in `TimelineControl` when running on TV (`FEATURE_LEANBACK`)
- [x] 2.2 Add `backFocusRequester` to `ControlHeader` back button; add `focusProperties { down = sliderFocusRequester }`
- [x] 2.3 Add `focusProperties { up = backFocusRequester; down = playPauseFocusRequester }` to Slider in `BottomControlBar`
- [x] 2.4 Add `focusProperties { up = sliderFocusRequester }` to `PlayPauseButton`
- [x] 2.5 Outer Box in `VideoPlayScreen`: `.focusable()` + `handlePlayerKeys` onKeyEvent (hidden-UI shortcuts)

## 3. Key shortcuts when UI hidden (10s seek, compact overlay + slider focus)

- [x] 3.1 LEFT/RIGHT KeyDown → 10s seek via `playerState.onTimedSeek(±10000)` (compact overlay + 1.5s short hide timer) + `pendingFocusTarget = SLIDER` (focus slider for continuous seek)
- [x] 3.2 Add `forwardFocusRequester` to `OptionsContent`; wire through
- [x] 3.3 Add `playPauseFocusRequester` to `PlayPauseButton`; wire through
- [x] 3.4 Add `episodeFocusRequester` to "选集" `AdaptiveTextButton`; wire through
- [x] 3.5 Pending focus queue: `pendingFocusTarget` state + `LaunchedEffect(50ms delay)` for deferred `requestFocus()` after `AnimatedVisibility` composes
- [x] 3.6 Add `FocusTarget.SLIDER` to enum; wire `sliderFocusRequester.requestFocus()` in the deferred focus queue
- [x] 3.7 UP → show UI + focus forward; DOWN → show UI + focus episode; OK → pause + show UI + focus play/pause (in `handlePlayerKeys` onKeyEvent)
- [x] 3.8 `hideControlUi()` does NOT reset `isSeeking` (compact overlay fades out directly, no full UI flash); `showControlUi()` resets `isSeeking=false` (full UI when shown again)
- [x] 3.9 `onTimedSeek` moved to `VideoPlayerState` interface + impl; uses `hideAfterMs=1.5s` (short timer); `onSeeked()` no longer resets `isSeeking` (sets short timer instead)

## 4. Initial default focus

- [x] 4.1 Add `LaunchedEffect(Unit) { delay(100); runCatching { playPauseFocusRequester.requestFocus() } }` in `VideoPlayScreen`

## 5. Back key hides UI

- [x] 5.1 Modify `BackHandler` in `VideoPlayer` to check `playerState.isControlUiVisible`: if visible → hide UI; else → call `onBackPress`

## 6. Side sheet focus

- [x] 6.1 Add `FocusRequester` to first `AdaptiveTextButton` in `SpeedSideSheet`; `LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }`
- [x] 6.2 Same for `ResizeSideSheet`
- [x] 6.3 Add `BackHandler { onDismissRequest() }` to both `SpeedSideSheet` and `ResizeSideSheet`
- [x] 6.4 Make selected item in Speed/Resize side sheets bold (`FontWeight.Bold`) in addition to `primary` color

## 7. Auto-hide timer key reset and seek step unification

- [x] 7.1 Add `fun onUserInteraction()` to `VideoPlayerState` interface
- [x] 7.2 Implement `onUserInteraction()` in `VideoPlayerStateImpl` to set `controlUiLastInteractionMs = 0`
- [x] 7.3 Add `Modifier.onPreviewKeyEvent { playerState.onUserInteraction(); false }` to inner `VideoPlayer` Box
- [x] 7.4 Change `setSeekForwardIncrementMs` / `setSeekBackIncrementMs` from 15s to 10s in `rememberVideoPlayerState` config (consistency with D-pad seek step)

## 8. Verification

- [x] 8.1 Build and run on TV/emulator: verify vertical D-pad flow header ↔ slider ↔ playback controls
- [ ] 8.2 Verify LEFT/RIGHT hidden → 10s seek + compact overlay (slider + timestamp), slider focused, auto-hides after 1.5s (no full UI flash)
- [ ] 8.3 Verify LEFT/RIGHT hidden → continuous seek works (subsequent presses seek 10s each, overlay stays compact)
- [ ] 8.4 Verify LEFT/RIGHT focused slider → 10s seek (continuous press works, overlay stays compact)
- [ ] 8.5 Verify slider thumb: 15→20dp + 3dp white border on focus (after `onFocusChanged` order fix)
- [ ] 8.6 Verify UP/DOWN/OK shortcuts show UI + focus correct target (no crash)
- [ ] 8.7 Verify Back hides UI first, then exits player
- [ ] 8.8 Verify default focus on play/pause button on initial load
- [ ] 8.9 Verify touch drag seek → compact overlay shows, releases → hides after 1.5s (no full UI flash)
- [ ] 8.10 Verify auto-hide timer resets on D-pad navigation
- [ ] 8.11 Verify side sheet selected item displays bold + primary color
