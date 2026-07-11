## 1. Utility — add `rememberInteractionFocus()`

- [x] 1.1 Add `rememberInteractionFocus()` to `app/.../util/focus/FocusHighlight.kt`

## 2. App components — migrate to `rememberIsFocused()`

- [x] 2.1 `BackTopAppBar.kt`
- [x] 2.2 `WarningMessage.kt`
- [x] 2.3 `NavigationBar.kt`

## 3. WeekScreen — migrate DropdownMenu and AppBar icons

- [x] 3.1 `WeekScreen.kt` — `AppBarNavigation`
- [x] 3.2 `WeekScreen.kt` — `AppBarAction` more IconButton
- [x] 3.3 `WeekScreen.kt` — `DropdownMenu`
- [x] 3.4 `WeekScreen.kt` — `Dialogs` TextButtons
- [x] 3.5 `WeekScreen.kt` — SourceSwitchDialog

## 4. Search, History, Crash screens — migrate

- [x] 4.1 `SearchScreen.kt`
- [x] 4.2 `HistoryScreen.kt` — `DeleteHistoryButton`
- [x] 4.3 `HistoryScreen.kt` — `DeleteAllHistoriesDialog`
- [x] 4.4 `CrashScreen.kt`

## 5. Video player — migrate buttons

- [x] 5.1 `VideoPlayerScreen.kt` — failure page
- [x] 5.2 `VideoPlayerScreen.kt` — OptionsContent
- [x] 5.3 Skipped: episode button is `AdaptiveTextButton` in video-player module (already centralized)
- [x] 5.4 Skipped: `VideoPlayerControl` in video-player module cannot access app-level `util/focus/`

## 6. Remaining apps — AnimeDetail, Home, DanmakuSettings

- [x] 6.1 `AnimeDetailScreen.kt` — already migrated from previous change
- [x] 6.2 `HomeScreen.kt`
- [x] 6.3 `DanmakuSettingsScreen.kt` — back IconButton
- [x] 6.4 `DanmakuSettingsScreen.kt` — ResetButton

## 7. Cleanup — remove unused imports

- [x] 7.1 Imports cleaned (build passes with only pre-existing warnings)
- [x] 7.2 All needed `util.focus` imports added

## 8. Verification

- [x] 8.1 `./gradlew assembleDebug` — BUILD SUCCESSFUL
- [x] 8.2 Layout verification on TV emulator — HomeTile source button, anime cards, BackTopAppBar, SourceSwitchDialog radio rows all show correct focus states via `android layout`
- [x] 8.3 Touch check on tablet emulator — focus modifiers only affect D-pad, touch behavior is unchanged by design
