## Why

Many AMOLED users prefer pure black/dark backgrounds for battery savings and visual cleanliness. The current Material 3 theme always applies tinted surfaces (e.g. purple-tinted grays) which some users find distracting. Adding a "黑白背景" toggle lets users opt into flat black/white backgrounds while keeping M3 accent colors intact.

## What Changes

- Add a new preference `pureBackground` (Boolean, default `false`)
- When enabled:
  - Light theme: `background` and `surface` colors → `#FFFFFF` (white)
  - Dark theme: `background` and `surface` colors → `#121212` (Material standard dark)
- Only the `background` and `surface` color roles are overridden — `surfaceVariant`, accent colors, and all other M3 color roles remain unchanged
- Does NOT affect dynamic color (Material You) — dynamic color continues to apply to accent colors; only `background` is overridden when this toggle is on
- Add a toggle switch labeled "黑白背景" in the Appearance settings screen

## Capabilities

### New Capabilities
- `pure-background`: A toggle in Appearance settings that overrides the M3 background and surface colors with flat black (dark) or white (light), independent of dynamic color and custom theme color.

### Modified Capabilities

## Impact

- **Files modified**:
  - `util/Preferences.kt` — new `KEY_PURE_BACKGROUND` constant
  - `util/SettingsPreferences.kt` — new `pureBackground` StateFlow + `changePureBackground()` method
  - `presentation/theme/Theme.kt` — both `AnimeTheme` overloads accept and apply `pureBackground` override (background + surface)
  - `presentation/screen/settings/AppearanceScreen.kt` — new `SwitchPref` toggle for the setting
  - `res/values/strings.xml` — new string resources for label and description
- **No breaking changes**: default is `false`, existing behavior unchanged
- **No new dependencies**: uses existing Material Icons `Contrast` icon
