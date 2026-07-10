## ADDED Requirements

### Requirement: Vertical focus flows through layout rows in order
Interactive elements in vertically stacked layouts (Column or scrollable equivalent) SHALL transfer focus in visual row order when the user presses up/down on D-pad, regardless of horizontal position differences between elements in adjacent rows.

#### Scenario: Episodes row to controls row (detail page)
- **WHEN** D-pad focus is on any episode button in the episodes `LazyRow`
- **THEN** pressing DOWN moves focus to the first focusable item in the `EpisodeListControl` row directly below
- **AND** pressing UP moves focus to the control above the episodes row

#### Scenario: Controls row to related row (detail page)
- **WHEN** D-pad focus is on any control button (channel, reverse, or more episodes)
- **THEN** pressing DOWN moves focus to the first item in the related anime `LazyRow`
- **AND** pressing UP moves focus to an episode button in the row above

#### Scenario: Theme mode to switch rows (settings page)
- **WHEN** D-pad focus is on a theme mode `SegmentedButton` (System, Light, or Dark)
- **THEN** pressing DOWN moves focus to the first focusable item below the color swatch row (the Dynamic Color switch)
- **AND** pressing UP moves focus to an element above the theme mode row

### Requirement: Color swatch row accepts only horizontal focus traversal
The `ColorBall` color swatch `LazyRow` in the appearance settings SHALL only accept horizontal (←/→) focus movement. Vertical (↑/↓) key events originating from within the swatch row SHALL be intercepted and redirected to the logical row above or below.

#### Scenario: Vertical key intercepted inside color swatch row
- **WHEN** D-pad focus is on any color circle inside the `ColorBall` LazyRow
- **THEN** pressing UP moves focus to the theme mode `SegmentedButtonRow` above
- **AND** pressing DOWN moves focus to the Dynamic Color `SwitchPref` below
- **AND** pressing LEFT or RIGHT navigates between color circles within the row (standard behavior)

#### Scenario: Entering the color swatch row from above
- **WHEN** D-pad focus is on the theme mode selector
- **AND** user presses RIGHT (or final → moves focus right)
- **THEN** focus enters the `ColorBall` LazyRow at the first visible color circle

#### Scenario: Exiting the color swatch row to below
- **WHEN** D-pad focus is on the rightmost color circle in the `ColorBall` LazyRow
- **AND** user presses RIGHT
- **THEN** focus moves to the Dynamic Color `SwitchPref` below (or wraps to the next logical control)

### Requirement: Focus highlight uses a shared inverse-color modifier
All interactive elements SHALL use `Modifier.onFocusHighlight()` instead of requiring per-element `var isFocused by remember` + `Modifier.onFocusChanged` boilerplate. The modifier SHALL apply `primary` background and `onPrimary` content color when the element is focused, matching the existing focus style convention.

#### Scenario: Applying focus highlight to a button
- **WHEN** a composable uses `Modifier.onFocusHighlight(containerColor = true, contentColor = true)`
- **AND** the composable receives D-pad focus
- **THEN** the composable's container displays `MaterialTheme.colorScheme.primary`
- **AND** its content displays `MaterialTheme.colorScheme.onPrimary`

#### Scenario: Focus highlight with no color change
- **WHEN** a composable uses `Modifier.onFocusHighlight()` without setting custom colors
- **AND** the composable receives D-pad focus
- **THEN** no visual change occurs (the modifier is a no-op — used as a focus-tracking anchor without UI changes)

### Requirement: D-pad direction keys can be intercepted at layout boundaries
The `Modifier.handleDPadKeyEvents()` extension SHALL allow composables to intercept and handle D-pad direction events (up/down/left/right/enter) before child composables process them, using `onPreviewKeyEvent` priority.

#### Scenario: Intercepting a down press at a vertical boundary
- **WHEN** a composable uses `Modifier.handleDPadKeyEvents(onDown = { customAction() })`
- **AND** the composable (or any child) has focus
- **AND** user presses D-pad DOWN
- **THEN** `customAction()` is invoked
- **AND** the key event is consumed (not propagated to children)

#### Scenario: Passing through a non-intercepted direction
- **WHEN** a composable uses `Modifier.handleDPadKeyEvents(onDown = { ... })` without setting `onLeft`
- **AND** user presses D-pad LEFT
- **THEN** the left key event is NOT consumed and propagates to child composables normally
