## Context

The `tv-focus-inverse-color` change introduced focus highlighting across ~40 interactive components in 13 files. It used two repetition patterns:

- **Pattern B** (`onFocusChanged` + `mutableStateOf`): Used for `IconButton`, `DropdownMenuItem`, clickable `Surface`/`Box`. ~40 instances.
- **Pattern A** (`MutableInteractionSource` + `collectIsFocusedAsState` + `collectIsPressedAsState`): Used for `Button`, `OutlinedButton`, `TextButton`. ~25 instances.

A `rememberIsFocused()` utility already exists in `util/focus/FocusHighlight.kt` (created by `detail-settings-focus-fixes`) and covers Pattern B. `AppearanceScreen` already uses it. All other files still use inline boilerplate.

No utility exists for Pattern A.

## Goals / Non-Goals

**Goals:**
- Add `rememberInteractionFocus()` to `FocusHighlight.kt` covering Pattern A
- Migrate all 13 files from inline focus patterns to the two shared functions
- Delete ~345 lines of duplicated boilerplate
- Zero behavioral or visual change — same focus colors, same pressed-state handling, same Compose APIs

**Non-Goals:**
- Do NOT extract `MediaSmall` Card border (different visual pattern)
- Do NOT extract `NavigationBar` item focus (compound `selected || isFocused` condition)
- Do NOT extract `Slider` D-pad + thumb animation (too specialized)
- Do NOT extract `GridLayoutTab` three-state focus
- Do NOT extract `VideoPlayerControl.AdaptiveIconButton` (already a shared component)
- Do NOT create a `FocusManager` or wrapper composable — stick to modifier-level utilities

## Decisions

### Decision 1: `rememberInteractionFocus()` returns `Pair<Boolean, MutableInteractionSource>`

**Chosen:** `Pair<Boolean, MutableInteractionSource>` — the boolean is `isActive = isFocused || isPressed`, already resolved.

**Rejected:** Returning `Triple<Boolean, Boolean, MutableInteractionSource>` with separate `isFocused` and `isPressed` values. No call site uses them separately — all combine them into `isActive`.

**Rationale:** Every existing use follows the same pattern: `val isActive = isFocused || isPressed`. Returning the pre-combined boolean saves a line at every call site.

### Decision 2: Two separate functions, not one merged

**Chosen:** `rememberIsFocused()` (Pattern B) and `rememberInteractionFocus()` (Pattern A) as separate functions.

**Rejected:** A single function with a boolean parameter (`withPress = true`). This would create two code paths in one function, making it harder to read at call sites.

**Rationale:** The two patterns serve different component families. Pattern A needs `interactionSource` to pass to `Button`/`OutlinedButton`; Pattern B only needs a `Modifier`. Keeping them separate makes intent explicit at the call site.

### Decision 3: What components are NOT migrated

The following have sufficiently unique focus behavior that extracting them would obscure, not clarify:

| Component | Reason to keep inline |
|---|---|
| `MediaSmall` Card | Focus adds a `border()`, not background color — completely different visual pattern |
| `NavigationBar` items | Has `selected \|\| isFocused` compound condition + `NavigationBarItemDefaults.colors()` — combining with `rememberIsFocused()` would add confusion, not reduce it |
| `Slider` (video-player) | Focus triggers D-pad key handling + thumb size animation + white border + `pointerInput` — logic is specific to one component |
| `GridLayoutTab` (HomeScreen) | Three-state: `isFocused` / `selected` / `default` with different colors for each — no other component shares this pattern |
| `VideoPlayerControl.AdaptiveIconButton` | Already a shared internal component with built-in Pattern A — no migration needed |

### Decision 4: Use destructuring `val (x, y)` syntax

**Chosen:** `val (isFocused, focusModifier) = rememberIsFocused()`

**Rejected:** `val focusState = rememberIsFocused()` then `focusState.first` / `focusState.second`. Less readable.

**Rationale:** Kotlin's destructuring makes the intent clear. The function name already signals what it returns; destructuring names reinforce it.

## Risks / Trade-offs

- **[Risk] Merge conflicts with upstream**: The original author may have their own changes in affected files. → **Mitigation**: This is a pure refactor — every change is a mechanical replacement. If conflicts arise, reverting the refactor is trivial: re-apply the inline pattern at the conflicted site.
- **[Risk] Spec requirement change**: The original `tv-focus-inverse-color` spec includes "No new abstraction files". → **Mitigation**: This change includes a MODIFIED requirement replacing it with "Shared focus utilities preferred". The delta spec documents the rationale.
