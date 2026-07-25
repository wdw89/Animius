## 1. Fix video-player module

- [x] 1.1 Rewrite `AdaptiveIconButton` — tracks focus internally via `onFocusChanged`, no press tracking
- [x] 1.2 Rewrite `AdaptiveTextButton` — tracks focus internally, controls text color directly
- [x] 1.3 Wire `rememberIsFocused` at all `AdaptiveIconButton` call sites — not needed (focus is internal now)
- [x] 1.4 Wire `rememberIsFocused` at all `AdaptiveTextButton` call sites — not needed (focus is internal now)
- [x] 1.5 Clean up unused `collectIsPressedAsState`, `collectIsFocusedAsState`, `MutableInteractionSource` imports in `VideoPlayerControl.kt`

## 2. Migrate rememberInteractionFocus call sites

- [x] 2.1 `VideoPlayerScreen.kt` — 2× replace with `rememberIsFocused`, remove `interactionSource` param from `OutlinedButton`
- [x] 2.2 `WarningMessage.kt` — 1× replace, remove `interactionSource` from `OutlinedButton`
- [x] 2.3 `CrashScreen.kt` — 3× replace, remove `interactionSource` from `Button`/`OutlinedButton`
- [x] 2.4 `HistoryScreen.kt` — 2× replace, remove `interactionSource` from `TextButton`
- [x] 2.5 `SearchScreen.kt` — 2× replace, remove `interactionSource` from `TextButton`
- [x] 2.6 `DanmakuSettingsScreen.kt` — 1× replace, remove `interactionSource` from `Button`
- [x] 2.7 `WeekScreen.kt` — 4× replace, remove `interactionSource` from `TextButton`

## 3. Delete rememberInteractionFocus

- [x] 3.1 Delete `rememberInteractionFocus()` function from `FocusHighlight.kt`
- [x] 3.2 Remove `collectIsPressedAsState`, `collectIsFocusedAsState`, `MutableInteractionSource` imports from `FocusHighlight.kt`
- [x] 3.3 Remove `rememberInteractionFocus` imports from all 7 migrated files + clean up unused `collectIsPressedAsState`/`collectIsFocusedAsState`/`MutableInteractionSource` imports

## 4. Verify

- [x] 4.1 Build: `./gradlew :app:assembleDebug` passes
- [x] 4.2 Verify no remaining references to `rememberInteractionFocus` in project (zero matches); `collectIsPressedAsState` remains only in files that use it for other purposes
