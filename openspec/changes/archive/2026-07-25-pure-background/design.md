## Context

Animius uses Material 3 theming with three color sources: custom seed color (Catppuccin palette), dynamic color (Material You wallpaper-based), and hardcoded fallback. The `AnimeTheme` composable in `Theme.kt` selects the color scheme based on user preferences, then applies it to `MaterialTheme`. Surface/background colors are generated automatically by M3's tonal palette system, producing tinted grays that some users find undesirable on AMOLED screens.

Current preference flow: `SettingsPreferences` (singleton with `MutableStateFlow` + `SharedPreferences`) → `AnimeTheme` collects state → selects `ColorScheme` → passes to `MaterialTheme`.

## Goals / Non-Goals

**Goals:**
- Add a toggle that overrides the `background` and `surface` color roles with a flat color (white for light, #121212 for dark)
- Keep all other M3 color roles (primary, secondary, surfaceVariant, etc.) untouched
- Maintain full compatibility with dynamic color and custom seed color
- Use existing `SwitchPref` composable and preference infrastructure

**Non-Goals:**
- Override `surfaceVariant`, `surfaceContainer`, or other surface-level container colors (future enhancement if requested)
- Implement AMOLED pure black (#000000) — using #121212 per Material Design dark theme standard
- Add per-source or per-screen background customization

## Decisions

### 1. Override at `ColorScheme` level via `.copy(background = ..., surface = ...)`

**Decision**: After the color scheme is determined (dynamic or custom seed), apply `scheme.copy(background = ..., surface = ...)` if `pureBackground` is true.

**Rationale**: Clean, composable approach. The override happens at the same level as the scheme selection, keeping the logic centralized. Uses `.let {}` to chain the override after the `when` block.

**Alternatives considered**:
- Override in a custom `LocalBackgroundColor` composition local → over-engineered for a single color role
- Modify `getSchemeFromSeed()` to accept a background override → mixes concerns

### 2. Dark background = `#121212` (not pure `#000000`)

**Decision**: Use Material Design's standard dark surface color `0xFF121212`.

**Rationale**: Pure black causes black smearing on OLED panels during scrolling. `#121212` is the established Material Design dark background and is widely recognized as "dark mode" while avoiding visual artifacts.

### 3. No interaction with dynamic color

**Decision**: `pureBackground` only overrides `background`, regardless of whether dynamic color is active. Dynamic color continues to control primary/secondary/tertiary and all other roles.

**Rationale**: Users may want both — Material You accent colors from wallpaper AND a flat background. Forcing mutual exclusion would reduce flexibility.

### 4. Preference stored as Boolean with default `false`

**Decision**: Single boolean toggle, off by default. Named `pureBackground` in code, "黑白背景" in UI.

**Rationale**: Simple on/off matches the feature scope. Default `false` preserves existing behavior for all users.

## Risks / Trade-offs

- **Only `background` and `surface` are overridden** → Cards, chips, and other container elements using `surfaceVariant`, `surfaceContainer*` will still have M3 tinted colors. This may look inconsistent to some users. Mitigation: can be extended to override more surface roles later if requested.
- **#121212 vs #000000 debate** → Some AMOLED enthusiasts want pure black. Mitigation: #121212 avoids black smearing; can add a "pure AMOLED" option later.
- **No `onBackground` override** → If the background changes to white/black, the text color (`onBackground`) might not have optimal contrast against M3-generated onBackground. In practice, M3's onBackground is already near-black/near-white, so this should be fine.
