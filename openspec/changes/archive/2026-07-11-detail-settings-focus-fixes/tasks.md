## 1. Focus utility toolkit

- [x] 1.1 Create `app/.../util/focus/` package directory
- [x] 1.2 Implement `DpadKeyHandler.kt` — `Modifier.handleDPadKeyEvents(onUp, onDown, onLeft, onRight, onEnter)` using `onPreviewKeyEvent` with `KeyEventType.KeyUp` filter
- [x] 1.3 Implement `FocusHighlight.kt` — `Modifier.onFocusHighlight()` that tracks `isFocused` via `MutableInteractionSource.collectIsFocusedAsState()` and toggles `primary`/`onPrimary` colors on the composable
- [x] 1.4 Implement `FocusOrder.kt` — thin helpers for configuring `focusProperties {}` (e.g., `blockVertical()`, `blockHorizontal()`)

## 2. Detail page layout refactor

- [x] 2.1 Replace `Box { AnimeEpisodes + EpisodeListControl(BottomEnd) }` with `Column { AnimeEpisodes + EpisodeListControl(right-aligned Row) }` in `AnimeDetailScreen.kt`
- [x] 2.2 Remove `Modifier.offset(y = ...)` from `EpisodeListControl` (no longer needed in Column layout)
- [x] 2.3 Adopt `FocusHighlight` modifier on `AnimeEpisodes` buttons, `EpisodeListControl` items, `TopAppBar` buttons, and `FavouriteIcon` — replacing manual `onFocusChanged` + color state boilerplate

## 3. Settings page focus boundaries

- [x] 3.1 Wrap `ColorBall` LazyRow in a `Box` with `handleDPadKeyEvents(onUp, onDown)` that routes vertical focus to `ThemeModeSettings` (above) and Dynamic Color `SwitchPref` (below) via `FocusRequester`
- [x] 3.2 Add `FocusRequester` targets to `ThemeModeSettings` (first SegmentedButton) and Dynamic Color `SwitchPref` for the color swatch boundary to route to
- [x] 3.3 Adopt `FocusHighlight` modifier on `ThemeModeSettings` SegmentedButtons, `ColorBall` color circles, and `SwitchPref` ListItems — replacing manual `onFocusChanged` boilerplate

## 4. Verification

- [ ] 4.1 Manual test on TV/emulator: detail page — verify ↓ from any episode lands on EpisodeListControl, ↓ from control lands on related, ↑ from related lands on control, ↑ from control lands on episodes
- [ ] 4.2 Manual test on TV/emulator: settings page — verify ↓ from ThemeModeSettings lands on ColorBall (not skip), ↓ from ColorBall goes to Dynamic Color, ↑ from Dynamic Color lands on ColorBall, ↑ from ColorBall goes to ThemeModeSettings
- [x] 4.3 Visual regression: confirm focus highlight colors (primary/onPrimary) appear identical to current behavior on both screens
- [x] 4.4 Touch/mouse regression: confirm click behavior unchanged on all interactive elements

## 5. Detail page remaining focus fixes

- [x] 5.1 Fix `lastPlayedFocusRef` index calculation for reversed list: compute `reversedIndex = episodes.size - 1 - lastPosition` when `reverseList` is true
- [x] 5.2 Route FavouriteIcon ↓ to last played episode via `handleDPadKeyEvents(onDown = { lastPlayedFocusRef.value?.requestFocus() })`
- [x] 5.3 Fix `LaunchedEffect` key from `Unit` to `displayList.size, reverseList` and add `key = { episode.url }` to `itemsIndexed` to prevent stale FocusRequester after reverse
- [x] 5.4 Show reverse state: toggle text between "列表倒序" and "列表正序" based on `reverseList`
- [x] 5.5 Split `LaunchedEffect` into two: one for updating `lastPlayedFocusRef` (key: `displayList.size, reverseList`), one for initial `requestFocus()` (key: `Unit`), to avoid stealing focus from reverse button
- [x] 5.6 Fix reverse-then-UP crash: clear `lastPlayedFocusRef.value = null` synchronously on reverse toggle; use `SideEffect` instead of `LaunchedEffect` for ref updates to eliminate async timing gap
- [x] 5.7 **v4 重构**: 删除 `lastPlayedFocusRef` 机制；`onUpFocusRequest` 改为 `focusManager.moveFocus(FocusDirection.Up)`；`FavouriteIcon.onDown` 同样改为 `moveFocus(Down)`；`AnimeEpisodes` 初始 focus 用函数级 `LaunchedEffect(Unit)` 滚动到 `lastPosition`
