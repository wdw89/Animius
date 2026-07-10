## Why

The focus inverse-color effect (focused button gets `primary` background + `onPrimary` content) is currently only implemented on `AnimeDetailScreen` and `SearchScreen`. On TV/non-touch devices, the remaining ~40 interactive components across the app use default dimming or no visible focus feedback, making D-pad navigation confusing. Users can't easily tell which button is focused.

## What Changes

- Extend focus inverse-color effect to all remaining button-like components across all screens, dialogs, and bottom sheets
- Cover `IconButton`, `Button`, `OutlinedButton`, `TextButton`, `FilledTonalButton`, `SuggestionChip`, `DropdownMenuItem`, and clickable `Box`/`Row` wrappers
- Follow the two established patterns already in `AnimeDetailScreen.kt`: Pattern A (`interactionSource` + `collectIsFocusedAsState`) for components with `interactionSource` support, and Pattern B (`onFocusChanged`) for components without it
- No new abstraction files — all changes are inline in existing composables for easy upstream review

## Capabilities

### New Capabilities
- `tv-focus-inverse-color`: All interactive components (buttons, chips, menu items, clickable surfaces) across the app display inverse colors (`primary` background, `onPrimary` content) when focused via D-pad/keyboard/TV remote

### Modified Capabilities
<!-- No existing specs to modify -->

## Impact

- Affected files: ~11 composable files across `presentation/component/` and `presentation/screen/`
- Shared components (`BackTopAppBar`, `WarningMessage`) — fix once, benefits all screens using them
- No new dependencies, no architecture changes, no API changes
- Touch-only devices: zero impact (focus events never fire during touch interaction)
