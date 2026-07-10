## Context

`AnimeDetailScreen.kt` and `SearchScreen.kt` already have focus inverse-color effects applied to their buttons using two patterns. The remaining ~40 interactive components across 11 files need the same treatment. The existing spec document (`docs/superpowers/specs/2026-06-27-tv-focus-inverse-color-design.md`) describes both patterns in detail.

Decision: all changes are inline — no shared helper/abstraction files — to minimize review burden for upstream PR acceptance.

## Goals / Non-Goals

**Goals:**
- Every clickable button-like component in the app shows `primary` background + `onPrimary` content when focused
- Uniform behavior across all devices (D-pad, keyboard, TV remote; transparent on touch-only)
- Changes are self-contained in existing files for easy upstream review

**Non-Goals:**
- New abstraction layers, utility composables, or Modifier extensions
- Behavior changes for touch interaction
- Custom focus styling (borders, shadows, scaling) beyond the inverse-color pattern
- Changes to `MediaSmall` cards (already have border-based focus style — intentional)

## Decisions

### 1. Inline vs shared helper → Inline

**Rationale**: This is destined for an upstream PR. Inline changes are self-explanatory in diff review — the reviewer sees the pattern repeated and trusts it. A shared helper file invites design debate ("why this abstraction?") that could block the PR. Each file's change is ~10 lines and trivially revertible per-component.

### 2. Component-to-pattern mapping

| Component | Pattern | Why |
|---|---|---|
| `IconButton` | B (`onFocusChanged`) | No `interactionSource` parameter in Material 3 |
| `Button` / `OutlinedButton` | A (`interactionSource`) | Has `interactionSource` + `colors` parameters |
| `TextButton` | A (`interactionSource`) | Same as `Button` — has `interactionSource` + `colors` |
| `FilledTonalButton` | A (already done) | Has `interactionSource` — already implemented on AnimeDetailScreen |
| `SuggestionChip` | A (already done) | Has `interactionSource` — already implemented on AnimeDetailScreen |
| `DropdownMenuItem` | B (`onFocusChanged`) | No `interactionSource` or `colors` parameter |
| Clickable `Box`/`Row`/`Surface` | B (`onFocusChanged`) | Not a button component — needs manual focus tracking |

### 3. AlertDialog TextButton → also inverse

Even though `AlertDialog` buttons are small and transient, TV users navigate them with D-pad and need clear focus feedback. Same pattern as regular `TextButton`.

### 4. Default unfocused colors → preserve existing

Each component keeps its current default unfocused appearance. The inverse-color effect only activates on focus. This means:
- `BackTopAppBar` IconButton: default `Color.Transparent`, focus → `primary` bg
- `OutlinedButton` in WarningMessage: default outlined style, focus → `primary` filled
- `DropdownMenuItem`: default transparent, focus → `primary` bg with `onPrimary` tinted icon

## Risks / Trade-offs

- **DropdownMenuItem text color**: `DropdownMenuItem` doesn't expose a `colors` parameter. The `text` slot content may need a manual `Text` with conditional color. → Mitigation: wrap text in a custom `Text` that reads the focus state.
- **SegmentedButton** in AppearanceScreen: Material 3 `SegmentedButton` has limited color customization. → If `colors()` API doesn't support focus state, fall back to Pattern B with modifier-based background.
- **Compile-time verification only**: No runtime focus tests exist. → Validate on TV emulator with `android layout` after each screen.
