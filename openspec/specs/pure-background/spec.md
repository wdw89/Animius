## ADDED Requirements

### Requirement: Pure background preference
The system SHALL provide a boolean preference `pureBackground` (default `false`) that controls whether the theme background color is overridden with a flat color.

#### Scenario: Default state
- **WHEN** user has never toggled the pure background setting
- **THEN** `pureBackground` SHALL be `false` and the M3 generated background color SHALL be used

#### Scenario: Preference persists across app restarts
- **WHEN** user enables pure background and restarts the app
- **THEN** pure background SHALL remain enabled

### Requirement: Background and surface color override
When `pureBackground` is `true`, the system SHALL override the `background` and `surface` color roles of the active `ColorScheme`.

#### Scenario: Light theme with pure background enabled
- **WHEN** theme mode is Light (or System + device is light) AND `pureBackground` is `true`
- **THEN** `background` and `surface` colors SHALL be `#FFFFFF` (white)

#### Scenario: Dark theme with pure background enabled
- **WHEN** theme mode is Dark (or System + device is dark) AND `pureBackground` is `true`
- **THEN** `background` and `surface` colors SHALL be `#121212`

#### Scenario: Other color roles unchanged
- **WHEN** `pureBackground` is `true`
- **THEN** `primary`, `secondary`, `tertiary`, `surfaceVariant`, and all other non-background/surface color roles SHALL remain unchanged from the active color scheme

### Requirement: Dynamic color compatibility
The pure background setting SHALL NOT interfere with dynamic color (Material You). When both are active, dynamic color determines accent colors while the flat background overrides only the `background` role.

#### Scenario: Dynamic color + pure background
- **WHEN** `dynamicColor` is `true` AND `pureBackground` is `true` on Android 12+
- **THEN** primary/secondary/tertiary colors SHALL come from the wallpaper-derived dynamic scheme AND background/surface SHALL be the flat override color

### Requirement: Appearance settings toggle
The system SHALL display a toggle switch labeled "黑白背景" in the Appearance settings screen with a description explaining the behavior.

#### Scenario: Toggle visibility
- **WHEN** user opens Appearance settings
- **THEN** a "黑白背景" toggle SHALL be visible below the dynamic image color toggle

#### Scenario: Toggle interaction
- **WHEN** user taps the "黑白背景" toggle
- **THEN** `pureBackground` preference SHALL toggle and the theme background SHALL update immediately without restarting the app
