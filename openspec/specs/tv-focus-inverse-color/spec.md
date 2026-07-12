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
Focus inverse-color implementations SHALL use shared utility functions from `util/focus/FocusHighlight.kt` rather than inline per-element boilerplate. New interactive components SHALL adopt `rememberIsFocused()` (for `IconButton`, `DropdownMenuItem`, clickable surfaces) or `rememberInteractionFocus()` (for `Button`, `OutlinedButton`, `TextButton`) instead of re-implementing `onFocusChanged` + `mutableStateOf` or `MutableInteractionSource` + `collectIsFocusedAsState` + `collectIsPressedAsState`.

The two utility functions (`rememberIsFocused()` and `rememberInteractionFocus()`) SHALL be the single source of truth for focus state tracking. When a bug is found in focus state logic, it SHALL be fixed once in the utility file, not in each screen.

#### Scenario: Adding a new IconButton with focus
- **WHEN** a developer adds a new `IconButton` to any screen that needs D-pad focus highlighting
- **THEN** they SHALL use `val (isFocused, focusModifier) = rememberIsFocused()` and attach `focusModifier` to the button, reading `isFocused` for `containerColor` and `contentColor`
- **AND** they SHALL NOT write a new `mutableStateOf(false)` + `onFocusChanged` block

#### Scenario: Adding a new TextButton with focus
- **WHEN** a developer adds a new `TextButton` to any screen that needs D-pad focus highlighting
- **THEN** they SHALL use `val (isActive, interactionSource) = rememberInteractionFocus()` and pass `interactionSource` to the button, reading `isActive` for colors
- **AND** they SHALL NOT write a new `MutableInteractionSource` + `collectIsFocusedAsState` + `collectIsPressedAsState` block

#### Scenario: Code review of a single screen
- **WHEN** an upstream reviewer inspects the diff of any single screen file
- **THEN** the focus inverse-color logic SHALL reference `rememberIsFocused()` or `rememberInteractionFocus()` from `util/focus/FocusHighlight.kt`, which is self-documenting and reviewable once

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

### Requirement: Slider focus indicated by thumb animation
The `Slider` component SHALL indicate D-pad focus through thumb animations (size increase + white border). The track colors (play progress, buffered, unplayed) SHALL remain unchanged when focus changes.

#### Scenario: Slider receives D-pad focus
- **WHEN** D-pad navigation focuses the slider
- **THEN** the thumb grows from 15dp to 20dp
- **AND** a 3dp white border appears around the thumb
- **AND** the track colors (play progress, buffered, unplayed) remain unchanged

#### Scenario: Slider loses D-pad focus
- **WHEN** D-pad focus moves away from the slider
- **THEN** the thumb shrinks from 20dp to 15dp
- **AND** the white border disappears
- **AND** the track colors remain unchanged

#### Scenario: Touch interaction on slider
- **WHEN** user touches or drags the slider on a touch-only device
- **THEN** the slider does NOT show the focus thumb animation
- **AND** the standard touch interaction (drag/tap) works normally

### Requirement: Side sheet selected item is visually prominent
The currently selected item in the speed and resize side sheets SHALL display in `primary` color AND bold font weight to clearly distinguish it from unselected items.

#### Scenario: Selected speed option is bold
- **WHEN** the speed side sheet is open
- **AND** a speed option (e.g., "1.0X") is the currently active value
- **THEN** that item's text is displayed in `primary` color AND `FontWeight.Bold`

#### Scenario: Selected resize option is bold
- **WHEN** the resize side sheet is open
- **AND** a resize option (e.g., "适应") is the currently active value
- **THEN** that item's text is displayed in `primary` color AND `FontWeight.Bold`

