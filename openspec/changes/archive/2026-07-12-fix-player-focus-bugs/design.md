## Context

The video player's focus navigation system was built for the spec-driven `player-focus-navigation` change. It has two code paths:

1. **UI visible** (`isControlUiVisible=true`): Focus flows through the control overlay rows (header → slider → playback) using explicit `focusProperties`.
2. **UI hidden** (`isControlUiVisible=false`): An outer `.onKeyEvent` handler in `VideoPlayerScreen.kt` intercepts D-pad keys for shortcuts: LEFT/RIGHT → timed seek + compact overlay, UP/DOWN/CENTER → show full UI with targeted focus.

Two bugs exist at the interaction boundary between these paths and other UI states (compact seek mode, side sheets).

## Goals / Non-Goals

**Goals:**
- Fix the crash when pressing UP/DOWN during compact seek mode (after LEFT/RIGHT with hidden UI)
- Fix the side-sheet-overlap issue: UP/DOWN should NOT show control UI when episode/speed/resize sheets are open
- Minimal changes — no new parameters on public APIs unless necessary

**Non-Goals:**
- Redesigning the focus system architecture
- Changing the seek behavior or auto-hide timer logic
- Adding new D-pad shortcuts

## Decisions

### Decision 1: Slider UP/DOWN callback for compact mode exit

**Problem**: During compact seek mode (`isSeeking=true`), `ControlHeader` and `PlaybackControl` are not composed, but the Slider's `focusProperties { up = backFocusRequester; down = playPauseFocusRequester }` still points to those removed `FocusRequester`s. When the user presses UP/DOWN, Compose's focus system tries to navigate to non-existent nodes → crash.

**Options considered**:

| Option | Description | Verdict |
|--------|-------------|---------|
| A — Conditional `focusProperties` | Only set `up`/`down` when `!isSeeking` | Rejected: doesn't prevent the focus system from attempting spatial fallback navigation, which could still fail silently |
| B — Handle in parent `onKeyEvent` | Wrap Slider in Box that intercepts UP/DOWN | Rejected: the Slider's own `onKeyEvent` fires first for UP/DOWN (returning `false`), and the focus system's navigation fires before parent key handlers |
| C — Add `onDpadUpDown` callback to Slider | Slider consumes UP/DOWN and invokes callback to `showControlUi()` | **Chosen** |

**Rationale**: Option C catches the event at the source (Slider's `onKeyEvent`), consumes it before the focus system gets involved, and triggers `showControlUi()` to exit compact mode gracefully. The callback is a simple `(() -> Unit)?` parameter with a default of `null` — no breaking change to existing Slider callers.

### Decision 2: Side-sheet visibility guard on outer key handler

**Problem**: When a side sheet (episode selector, speed, resize) is opened via `showEpisodeUi()`/`showSpeedUi()`/`showResizeUi()`, these methods call `hideControlUi()` internally → `isControlUiVisible = false`. The outer `.onKeyEvent` handler in `VideoPlayerScreen` only checks `isControlUiVisible`, so it intercepts D-pad keys that should go to the side sheet, calling `showControlUi()` which shows the full control overlay on top of the still-open side sheet.

**Fix**: Add a simple OR guard:

```kotlin
if (playerState.isControlUiVisible.value ||
    playerState.isEpisodeUiVisible.value ||
    playerState.isSpeedUiVisible.value ||
    playerState.isResizeUiVisible.value) return@onKeyEvent false
```

This lets keys pass through to the side sheet composables, which handle their own focus navigation and Back key dismissal.

**No alternatives considered**: This is a straightforward missing-condition bug. The check simply needed to account for all UI-visible states, not just `isControlUiVisible`.

## Risks / Trade-offs

- **[Low] Slider API change**: Adding `onDpadUpDown` parameter to the `Slider` composable is backward-compatible (default parameter). Caller in `BottomControlBar` is the only usage in the project.
- **[Low] Side sheet guard may mask future states**: If new overlay states are added, the guard must be updated. Consider extracting an `isAnyOverlayVisible` computed property in the future.
