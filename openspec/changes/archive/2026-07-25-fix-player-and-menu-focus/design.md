## Context

The video player and long-press context menus have three TV interaction bugs. The most critical is that on tablet devices (Switch OLED, Pixel Tablet emulator), the Back key requires two presses to dismiss player UI because Compose's `OnBackPressedDispatcher` on non-TV devices does not forward `Key.Back` events when focusable elements are in the tree. The other two are missing focus visuals on long-press menus, and an asymmetric D-pad OK button behavior.

### Key finding from investigation

On the tablet emulator (API 35), logcat confirms that `onPreviewKeyEvent` and root `onKeyEvent` both receive the first `Key.Back` press, but `BackHandler` does NOT fire until the second press. This is a Compose platform difference between TV (where `BackHandler` fires immediately) and tablet (where the focus system intercepts the first Back press before the dispatcher).

## Goals / Non-Goals

**Goals:**
- Single Back press hides player UI on all device types (TV, tablet, phone)
- Long-press menus in download and favorites screens show focus highlight
- D-pad OK/Center toggles play/pause when control UI is hidden, mirroring touch double-tap

**Non-Goals:**
- Changing `hideSystemBars()` behavior (verified unrelated)
- Refactoring the entire focus or BackHandler system
- Adding new utility or abstraction files

## Decisions

### Decision 1: Intercept Back key in root `onKeyEvent` instead of fixing `BackHandler`

**Rationale:** The `BackHandler` (via `OnBackPressedDispatcher`) path is unreliable on tablet Compose. The root `onKeyEvent` already has a hidden-UI shortcut handler that checks UI visibility state. Adding Back key handling there is a natural fit — same location, same state checks.

**Priority when multiple UI layers are visible:**

```
1. Side sheet open?  → close side sheet (speed > resize > episode)
2. Control UI open?  → hide control UI
3. Nothing open?     → pass through (→ BackHandler → exit player)
```

**Alternatives considered:**
- *Override `dispatchKeyEvent` in Activity*: Too invasive, affects all screens, harder to scope to player only.
- *Use `onPreviewKeyEvent` instead*: Would consume before focus system, but harder to coordinate with children.

### Decision 2: Use existing `rememberIsFocused()` from `FocusHighlight.kt`

**Rationale:** The `tv-focus-inverse-color` spec already mandates `rememberIsFocused()` as the single source of truth. The download `PopupMenuListItem` and favorites inline menu simply never adopted it. No new abstraction needed.

### Decision 3: Toggle play/pause in OK handler, matching touch double-tap

**Rationale:** The touch double-tap handler in `VideoPlayer.kt:154-159` already implements symmetric toggle. The D-pad OK handler mirrors this: `if playing → pause + show UI`, `if paused → play only (no UI)` — same as touch double-tap which doesn't show controls.

### Decision 4: Key.Menu triggers context menus, not D-pad long-press

**Rationale:** D-pad long-press is unintuitive on remotes/gamepads and behaves inconsistently across TV vs tablet Compose (FLAG_LONG_PRESS fires on Sony TV but not Switch). The `Key.Menu` keycode (82) is the standard Android mechanism for context/overflow actions. Touch long-press via `combinedClickable.onLongClick` is preserved for touchscreen users. Both paths open the same `DropdownMenu`.

### Decision 5: Delete mode for favorites and download detail

**Rationale:** D-pad users need a discoverable way to delete without relying on popup menus.
A top-bar `OutlinedButton` toggles delete mode. Cards show a centered trash icon
overlay (visual indicator only). Clicking/D-pad OK on the card body triggers delete.
Pressing Back in delete mode exits delete mode (does not navigate away).
Pattern inspired by FongMi/TV (`holder.binding.delete.setVisibility`).

**Behavior:**
- Top-bar shows `OutlinedButton` with `rememberIsFocused()` + `buttonColors` for proper
  inverse-color focus highlighting.
- In delete mode: button shows inverse-color + "Cancel"; each card overlays a centered
  `Icons.Rounded.Delete` icon at 48dp, semi-transparent.
- Click/D-pad OK on card body → delete. The trash icon is NOT a separate click target.
- Back key in delete mode → exits delete mode.
- Animation: trash icon fades in/out with `AnimatedVisibility`.

### Decision 6: Player starts without control UI

**Rationale:** Showing the control overlay on every launch adds unnecessary visual clutter
and an extra interaction step. Removing `showControlUi()` in `LaunchedEffect(url)` lets
playback start with a clean fullscreen view. The control UI still appears on any
D-pad/touch interaction.

### Decision 7: App exit confirmation dialog

When pressing Back on the home/main screen, show an `AlertDialog` with "确认退出?"
and two `OutlinedButton` choices ("Cancel" / "OK"). Buttons use `OutlinedButton` +
`buttonColors` + `rememberIsFocused()` for proper inverse-color focus.

## Risks / Trade-offs

- **Back key consumed in `onKeyEvent` returns `true`**: This means child composables won't see the Back key event. But since the only action needed when UI is visible is "hide UI and move on", this is acceptable. Children that need to handle Back (like text fields) aren't present in the player overlay.
- **Order-of-operations for Back key intercept**: The intercept logic must check visibility states in the same order as `BackHandler` registration (innermost first). Side sheet checks must come before control UI checks.
