# TV Focus Inverse Color

## Purpose

Ensure all interactive components across the app provide clear visual feedback when focused via D-pad, keyboard, or TV remote, using an inverse-color pattern: `primary` background with `onPrimary` content.

## Requirements

### Requirement: Focused button inverse color
All interactive button-like components across the app SHALL display `primary` background with `onPrimary` content color when focused via D-pad, keyboard, or TV remote.

#### Scenario: IconButton receives focus
- **WHEN** D-pad navigation focuses an `IconButton`
- **THEN** the button displays `MaterialTheme.colorScheme.primary` as container color and `MaterialTheme.colorScheme.onPrimary` as content (icon) color

#### Scenario: FilledTonalButton receives focus
- **WHEN** D-pad navigation focuses a `FilledTonalButton`
- **THEN** the button displays `primary` background with `onPrimary` text/icon

#### Scenario: SuggestionChip receives focus
- **WHEN** D-pad navigation focuses a `SuggestionChip`
- **THEN** the chip displays `primary` background with `onPrimary` label

#### Scenario: TextButton receives focus
- **WHEN** D-pad navigation focuses a `TextButton` (including in `AlertDialog`)
- **THEN** the button displays `primary` container color with `onPrimary` content color

#### Scenario: OutlinedButton receives focus
- **WHEN** D-pad navigation focuses an `OutlinedButton`
- **THEN** the button displays `primary` filled background with `onPrimary` content, overriding its default outlined appearance

#### Scenario: DropdownMenuItem receives focus
- **WHEN** D-pad navigation focuses a `DropdownMenuItem`
- **THEN** the item displays `primary` background with `onPrimary`-tinted icon and text

#### Scenario: Clickable Box/Row receives focus
- **WHEN** D-pad navigation focuses a clickable `Box`, `Row`, or `Surface`
- **THEN** the container displays `primary` background with `onPrimary`-tinted content

### Requirement: Focus state includes press state
For components using Pattern A (`interactionSource`), the active focus visual SHALL include the pressed state to prevent visual flash during click.

#### Scenario: Button pressed while focused
- **WHEN** user presses (clicks) a focused button
- **THEN** the button SHALL retain the inverse-color appearance (not revert to default) until the press action completes

### Requirement: Touch interaction unaffected
Focus inverse-color effects SHALL NOT activate during normal touch interaction on touch-only devices.

#### Scenario: Touch tap on phone
- **WHEN** user taps a button on a touch-only device (phone, tablet without keyboard/D-pad)
- **THEN** the button SHALL NOT show the focus inverse-color effect; standard Material 3 ripple/press behavior SHALL be preserved

### Requirement: No new abstraction files
All focus inverse-color implementations SHALL be inline in existing composable files. No new shared helper composable, Modifier extension, or utility file SHALL be introduced.

#### Scenario: Code review of a single screen
- **WHEN** an upstream reviewer inspects the diff of any single screen file
- **THEN** the focus inverse-color logic SHALL be self-contained and understandable within that file without referencing new abstraction layers

### Requirement: ListItem focus styling
When `ListItem`-based components (settings toggles, slider rows) receive focus, the entire row SHALL highlight with `surfaceVariant` background. The headline and supporting text SHALL transition to `primary` color, except where `primary` would conflict with embedded controls (e.g., Switch toggles, Slider tracks).

#### Scenario: Settings toggle row receives focus
- **WHEN** D-pad navigation focuses a `SwitchPref` or `SettingsItem` row
- **THEN** the row background becomes `surfaceVariant`, headline text becomes `primary`, and the Switch control remains its default coloring

### Requirement: Slider D-pad navigation
All `Slider` components SHALL respond to D-pad left/right key events. The step increment SHALL be configurable per slider via a `dpadStep` parameter; when not specified, the slider SHALL use a sensible default based on its range and step count.

#### Scenario: D-pad right on a slider
- **WHEN** a Slider has focus and the user presses D-pad Right
- **THEN** the slider value increases by the configured step amount

#### Scenario: D-pad left on a slider
- **WHEN** a Slider has focus and the user presses D-pad Left
- **THEN** the slider value decreases by the configured step amount
