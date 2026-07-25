## 1. Cleanup investigation artifacts

- [x] 1.1 Remove debug logging in `video-player/.../VideoPlayer.kt` (BackDebug log lines in `onPreviewKeyEvent` and `BackHandler`)
- [x] 1.2 Remove debug logging in `app/.../videoplayer/VideoPlayerScreen.kt` (BackDebug log line in root `onKeyEvent`)
- [x] 1.3 Remove unused `Key`, `key`, `type` imports added to `VideoPlayer.kt`

## 2. Fix issue #2 — Back key intercept in player

- [x] 2.1 Add `Key.Back` case in root `onKeyEvent` handler (`VideoPlayerScreen.kt`) to intercept Back key when any UI is visible
- [x] 2.2 Implement priority-based Back handling: side sheet open → close side sheet; control UI open → hide control UI; nothing open → pass through (return `false`)
- [ ] 2.3 Verify Back key behavior on tablet emulator: single press hides UI, no focus loss before hide
- [ ] 2.4 Verify Back key behavior on TV emulator: no regression (still single press hides UI)
- [ ] 2.5 Verify Back key exits player when all UI is hidden

## 3. Fix issue #1 — PopupMenu inverse color + Key.Menu support

- [x] 3.1 Add `rememberIsFocused()` to `DropdownMenuItem` in `PopupMenuListItem.kt`
- [x] 3.2 Add `rememberIsFocused()` to delete `DropdownMenuItem` in `FavouriteScreen.kt`
- [x] 3.3 Remove D-pad long-press from `PopupMenuListItem.kt`; keep `Key.Menu` + touch long-press
- [x] 3.4 Remove D-pad long-press from `FavouriteScreen.kt`; keep `Key.Menu` + touch long-press
- [ ] 3.5 Verify: TV Menu key opens popup; touch long-press still works

## 4. Fix issue #3 — D-pad OK toggle + player starts without UI

- [x] 4.1 D-pad OK: playing → pause + show UI; paused → play only
- [ ] 4.2 Remove `showControlUi()` from initial-load `LaunchedEffect` in `VideoPlayer.kt`
- [ ] 4.3 Verify: player starts fullscreen, no control overlay; OK/D-pad still shows controls

## 5. Fix issue #4 — Download episode picker auto-focus

- [x] 5.1 Add `FocusRequester` to first `SuggestionChip` in `EpisodeBottomSheet`
- [x] 5.2 Add `LaunchedEffect` with 200ms delay for auto-focus
- [ ] 5.3 Verify: download episode picker auto-focuses first chip on TV

## 6. Fix issue #5 — Delete mode + focus highlight polish

- [ ] 6.1 Replace `TextButton` with `OutlinedButton` + `buttonColors` + `rememberIsFocused()` for delete button in `FavouriteScreen`
- [ ] 6.2 Replace `TextButton` with `OutlinedButton` + `buttonColors` + `rememberIsFocused()` for delete button in `DownloadDetailScreen`
- [ ] 6.3 Fix "anime detail" button: unfocused color white, use `OutlinedButton` + `rememberIsFocused()`
- [ ] 6.4 `FavouriteScreen`: delete mode → each card overlays centered red `Icons.Rounded.Delete` icon (48dp, `AnimatedVisibility`)
- [ ] 6.5 `DownloadDetailScreen`: delete mode → each row overlays centered red delete icon
- [ ] 6.6 `FavouriteScreen`: Back key in delete mode → exits delete mode
- [ ] 6.7 `DownloadDetailScreen`: Back key in delete mode → exits delete mode
- [ ] 6.8 Verify: trash icon visible in delete mode, hidden otherwise; click card deletes

## 7. Fix issue #6 — App exit confirmation dialog

- [ ] 7.1 Add `AlertDialog` to `MainScreen` (home): "确认退出?" with Cancel/OK `OutlinedButton`s
- [ ] 7.2 Buttons use `rememberIsFocused()` + `buttonColors` for inverse-color focus
- [ ] 7.3 Verify: Back on home → dialog appears; OK exits app, Cancel dismisses

## 8. Final verification

- [ ] 8.1 Build debug APK and install on all test devices
- [ ] 8.2 Manual smoke test all fixes on each device
