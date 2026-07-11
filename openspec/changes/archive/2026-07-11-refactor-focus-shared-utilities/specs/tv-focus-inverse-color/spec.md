## MODIFIED Requirements

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
