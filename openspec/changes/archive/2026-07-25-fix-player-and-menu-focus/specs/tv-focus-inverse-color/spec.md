## MODIFIED Requirements

### Requirement: DropdownMenuItem receives focus
All `DropdownMenuItem` instances across the app, including those in long-press context menus (download screen, favorites screen), SHALL display inverse-color focus feedback: `primary` background with `onPrimary`-tinted icon and text when focused via D-pad, keyboard, or TV remote.

#### Scenario: DropdownMenuItem in download long-press menu receives focus
- **WHEN** D-pad navigation focuses a `DropdownMenuItem` in the download screen's `PopupMenuListItem` long-press menu
- **THEN** the item displays `MaterialTheme.colorScheme.primary` background and `MaterialTheme.colorScheme.onPrimary` text color

#### Scenario: DropdownMenuItem in favorites long-press menu receives focus
- **WHEN** D-pad navigation focuses the delete `DropdownMenuItem` in the favorites screen long-press menu
- **THEN** the item displays `primary` background with `onPrimary` text color

#### Scenario: DropdownMenuItem in player options menu receives focus (existing)
- **WHEN** D-pad navigation focuses a `DropdownMenuItem` in the video player options menu
- **THEN** the item displays `primary` background with `onPrimary`-tinted icon and text (unchanged, already implemented)
