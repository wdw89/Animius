## 1. Preference Infrastructure (已完成)

- [x] 1.1 Add `KEY_PURE_BACKGROUND` constant to `util/Preferences.kt`
- [x] 1.2 Add `pureBackground` StateFlow and `changePureBackground()` to `util/SettingsPreferences.kt`

## 2. Theme Layer (已完成)

- [x] 2.1 Add `pureBackground` parameter to parameterized `AnimeTheme` in `Theme.kt`
- [x] 2.2 Apply `.copy(background = ..., surface = ...)` override when `pureBackground` is true (white for light, #121212 for dark)
- [x] 2.3 Collect `pureBackground` state in no-arg `AnimeTheme` overload and pass to parameterized version

## 3. String Resources (已完成)

- [x] 3.1 Add `pure_background` and `pure_background_description` strings to `res/values/strings.xml`

## 4. Appearance Settings UI (待完成)

- [x] 4.1 Add `Icons.Outlined.Contrast` import to `AppearanceScreen.kt`
- [x] 4.2 Add `SwitchPref` toggle for "黑白背景" in `AppearanceScreen` below the dynamic image color toggle, wired to `SettingsPreferences.pureBackground` / `changePureBackground()`

## 5. Verification

- [x] 5.1 Build the project with `./gradlew assembleDebug` to verify compilation
- [ ] 5.2 Test: enable toggle in light mode → background should be white
- [ ] 5.3 Test: enable toggle in dark mode → background should be #121212
- [ ] 5.4 Test: enable toggle + dynamic color → accent colors from wallpaper, flat background
- [ ] 5.5 Test: toggle off → M3 tinted background restored
