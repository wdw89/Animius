## 1. Shared Components (fix once, benefits all screens)

- [x] 1.1 `BackTopAppBar.kt` — Add focus inverse-color to navigation `IconButton` (Pattern B)
- [x] 1.2 `WarningMessage.kt` — Add focus inverse-color to retry `OutlinedButton` (Pattern A)

## 2. VideoPlayerScreen

- [x] 2.1 Add focus inverse-color to all `IconButton` instances (Pattern B)
- [x] 2.2 Add focus inverse-color to all `OutlinedButton` instances (Pattern A)
- [x] 2.3 Add focus inverse-color to `AdaptiveTextButton` instances (Pattern A)
- [x] 2.4 Add focus inverse-color to side-sheet `Button` (Pattern A)
- [x] 2.5 Build, install, verify with `android layout`

## 3. WeekScreen

- [x] 3.1 Add focus inverse-color to `AppBarAction` `IconButton` instances (Pattern B)
- [x] 3.2 Add focus inverse-color to source title `Surface` (Pattern B)
- [x] 3.3 Add focus inverse-color to `DropdownMenuItem` items (Pattern B)
- [x] 3.4 Add focus inverse-color to `AlertDialog` `TextButton` instances in `VersionUpdateDialog`, `SourceSwitchDialog`, `SettingsDialog`, `DomainChangeDialog` (Pattern A)
- [x] 3.5 Build, install, verify with `android layout`

## 4. SearchScreen

- [x] 4.1 Add focus inverse-color to captcha `AlertDialog` `TextButton` instances (Pattern A)
- [x] 4.2 Build, install, verify with `android layout`

## 5. HistoryScreen

- [x] 5.1 Add focus inverse-color to `DeleteHistoryButton` `IconButton` (Pattern B)
- [x] 5.2 Add focus inverse-color to `DeleteAllHistoriesDialog` `AlertDialog` `TextButton` instances (Pattern A)
- [x] 5.3 Build, install, verify with `android layout`

## 6. CrashScreen

- [x] 6.1 Add focus inverse-color to `Button` and `OutlinedButton` in `InfoScreen` (Pattern A)
- [x] 6.2 Build, install, verify with `android layout`

## 7. HomeScreen

- [x] 7.1 Add focus inverse-color to media type selector `IconButton` instances (Pattern B)
- [x] 7.2 Build, install, verify with `android layout`

## 8. DanmakuSettingsScreen

- [x] 8.1 Add focus inverse-color to navigation `IconButton` (Pattern B)
- [x] 8.2 Add focus inverse-color to `ResetButton` `Button` (Pattern A)
- [x] 8.3 Build, install, verify with `android layout`

## 9. AppearanceScreen

- [x] 9.1 Add focus inverse-color to navigation `IconButton` (Pattern B)
- [x] 9.2 Add focus inverse-color to `SegmentedButton` (Pattern B — `SegmentedButtonDefaults.colors()`)
- [x] 9.3 Add focus inverse-color to `SwitchPref` clickable rows (Pattern B — skipped, Switch handles focus natively)
- [x] 9.4 Build, install, verify with `android layout`

## 10. Final Verification

- [x] 10.1 Full build and manual walkthrough on TV emulator — navigate all screens with D-pad
- [x] 10.2 Verify no regressions on touch (phone layout) — tap all buttons, behavior unchanged

## 11. AnimeDetailScreen DropdownMenu

- [x] 11.1 Add focus inverse-color to DropdownMenu items (Pattern B, reference WeekScreen)

## 12. Dialogs — Missing Buttons

- [x] 12.1 SourceSwitchDialog: add dismissButton "取消" + focus to radio rows
- [x] 12.2 ChannelSelectorDialog: simplify to single close button + focus to channel items
- [x] 12.3 LoadingIndicationDialog: add dismissButton "取消"

## 13. Switch/Toggle Unification

- [x] 13.1 Refactor SettingsItem (WeekScreen) to use ListItem + focus
- [x] 13.2 Refactor SwitchPref (AppearanceScreen) to use ListItem + focus
- [x] 13.3 SwitchItem (DanmakuSettingsScreen) inherits from SwitchPref

## 14. AppearanceScreen Gaps

- [x] 14.1 ColorBall: add focus border (primary stroke, 3dp)
- [x] 14.2 SliderItem: add focus inverse-color to ListItem rows
