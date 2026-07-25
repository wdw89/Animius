## Context

The project has two focus-tracking utilities in `FocusHighlight.kt`:

| Function | Returns | Touch behavior |
|----------|---------|---------------|
| `rememberIsFocused()` | `(isFocused, Modifier)` | No inversion (correct) |
| `rememberInteractionFocus()` | `(isFocused \|\| isPressed, InteractionSource)` | Inverts color on press (incorrect) |

`rememberInteractionFocus` was created to "prevent visual flash during click" — the idea being that if `containerColor` jumps from `transparent` to `primary` on focus change, pressing first smooths the transition. But this solves a non-problem: M3 buttons already animate color transitions, and `IconButton` has no such flash. The real cost is that touch interactions look wrong — buttons turn primary color on every press instead of showing a ripple.

The main app's `IconButton` usage (HomeScreen, DetailScreen, Settings, etc.) exclusively uses `rememberIsFocused`. Only `rememberInteractionFocus` call sites and the video-player module's inline implementations have this issue.

## Goals / Non-Goals

**Goals:**
- Eliminate primary color inversion on touch for ALL buttons
- Single focus-tracking utility: `rememberIsFocused`
- Delete `rememberInteractionFocus` (no callers remain)
- Fix `AdaptiveIconButton` and `AdaptiveTextButton` to match

**Non-Goals:**
- No change to ripple behavior, focus traversal, or D-pad key handling
- No new utility functions (we already have the right one)
- No change to `VideoPlayerScreen.kt` failure page button behavior — those M3 `OutlinedButton` call sites already correctly pass `interactionSource`; they just need their `isPressed` logic removed

## Decisions

### Decision 1: Delete `rememberInteractionFocus`, don't deprecate

**Chosen:** Direct deletion after all call sites migrated.

**Rationale:** The function has 12 call sites. After migration, zero callers remain. Deprecation adds noise with no benefit. The function is internal, not a public API.

### Decision 2: Remove `interactionSource` param from M3 buttons

**Chosen:** For `Button`/`OutlinedButton`/`TextButton` call sites, remove the custom `interactionSource`. Use `rememberIsFocused` modifier for focus state.

**Rationale:** M3 buttons create their own default `MutableInteractionSource` when none is passed. The custom source was only needed because `rememberInteractionFocus` exposed `collectIsPressedAsState()` on it. Without press tracking, there's no reason to pass a custom source. The `rememberIsFocused` modifier handles focus tracking independently.

**Alternative considered:** Keep passing `interactionSource` but stop reading `isPressed` from it. Rejected — adds unnecessary complexity; M3's default source works identically for ripple.

### Decision 3: `AdaptiveIconButton` uses `rememberIsFocused` modifier, not inline state

**Chosen:** Add a `focusModifier` parameter to `AdaptiveIconButton`, wired to `rememberIsFocused` at each call site.

**Rationale:** `AdaptiveIconButton` currently tracks focus and press inline. Replacing with `rememberIsFocused` unifies with the rest of the project. The modifier approach (`.onFocusChanged`) is simpler than `MutableInteractionSource` + `collectIsFocusedAsState()`.

**Alternative considered:** Keep `interactionSource` but stop reading `isPressed`. Rejected — adds `MutableInteractionSource` creation + `collectIsFocusedAsState` boilerplate when `Modifier.onFocusChanged` is sufficient.

## Risks / Trade-offs

- **[Risk] M3 Button color jump on focus change without press smoothing** → Mitigation: M3 buttons use `animateColorAsState` internally for smooth transitions. The "flash" concern was theoretical; no actual jank observed in practice with `rememberIsFocused`.
- **[Trade-off] ~14 files changed for a behavior fix** → The change is mechanical (find-and-replace) across most files. Risk of merge conflicts is low since each file only touches one utility function call.
