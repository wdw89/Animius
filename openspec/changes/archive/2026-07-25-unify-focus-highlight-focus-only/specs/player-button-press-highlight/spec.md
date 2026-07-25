## ADDED Requirements

### Requirement: All focusable components use focus-only highlight

All buttons and interactive components in the app SHALL only highlight (primary color inversion) when focused via D-pad or controller. Touch/press interactions SHALL use the default Material3 ripple without triggering primary color inversion.

#### Scenario: Touch preserves Material3 ripple

- **WHEN** any focusable button (IconButton, TextButton, OutlinedButton, Button) is touched
- **THEN** the button SHALL show the default Material3 ripple on its current background
- **AND** the button SHALL NOT change its container color or content color on press

#### Scenario: D-pad focus triggers color inversion

- **WHEN** any focusable button receives D-pad focus (via TV remote or controller)
- **THEN** the button background SHALL turn MaterialTheme.colorScheme.primary
- **AND** the icon/text color SHALL invert to MaterialTheme.colorScheme.onPrimary

#### Scenario: Consistent behavior across all screens

- **WHEN** any button is used in the app (player overlay, dialogs, error pages, settings, detail page, home screen, etc.)
- **THEN** its highlight behavior SHALL be identical: focus drives color, touch drives ripple only
- **AND** there SHALL be only one focus-tracking utility function used throughout the project
