## 1. Fix Slider UP/DOWN crash during compact seek

- [x] 1.1 Add `onDpadUpDown: (() -> Unit)? = null` parameter to Slider in `video-player/.../component/Slider.kt`
- [x] 1.2 Add UP/DOWN/UP handling in Slider's `onKeyEvent`: consume event and invoke `onDpadUpDown`
- [x] 1.3 In `BottomControlBar` (`VideoPlayerControl.kt`), pass `state.showControlUi()` as `onDpadUpDown` callback to Slider

## 2. Fix side sheet overlapping control UI

- [x] 2.1 In `VideoPlayerScreen.kt` outer `.onKeyEvent`, add `isEpisodeUiVisible || isSpeedUiVisible || isResizeUiVisible` to the guard condition so keys pass through when any side sheet is open

## 3. Build & verify

- [x] 3.1 Build and install on TV emulator (`./gradlew :app:installDebug`)
- [x] 3.2 Verify Bug 1 fix: play video, hide UI, press LEFT to enter compact seek, then press UP/DOWN — no crash, full UI appears
- [x] 3.3 Verify Bug 2 fix: open episode selector, press UP/DOWN — side sheet handles keys, control UI does NOT appear
- [x] 3.4 Verify Bug 2 fix: open speed/resize sheet, press UP/DOWN — same behavior (no control UI)
- [x] 3.5 Verify regression: normal focus navigation (show UI, navigate header↔slider↔playback) still works correctly
