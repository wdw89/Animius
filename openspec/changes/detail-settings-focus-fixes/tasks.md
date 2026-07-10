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
