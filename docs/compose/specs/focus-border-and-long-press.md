# Focus Border & Long Press Spec

## MediaSmall Focus Border

### Component
`MediaSmall` in `app/src/main/java/com/lanlinju/animius/presentation/component/MediaSmall.kt`

### Behavior
When a `MediaSmall` card receives D-pad focus, a **3dp white rounded-corner border** appears around the card. When focus leaves, the border disappears (0dp/transparent).

### Implementation
```
Card(onClick) → modifier.onFocusChanged → modifier.border(Color.White, 3dp, cardShape)
```

- `Card(onClick)` makes the card focusable by default (no extra `focusable()` needed)
- `onFocusChanged` tracks `isFocused` state
- `border` conditionally renders 3dp white border when focused, 0dp transparent when not
- Card retains default Material3 elevation, ripple, and dimming on focus

### Key discovery
`LazyRow` and `LazyVerticalGrid` do NOT forward D-pad focus to individual items by default. Must add `Modifier.focusGroup()` to the container:

- `MediaSmallRow` → `LazyRow(modifier = Modifier.focusGroup())`
- HomeScreen `LazyVerticalGrid` → `.focusGroup()`
- FavouriteScreen `LazyVerticalGrid` → `.focusGroup()`

### What doesn't work (abandoned approaches)
- **Box wrapper + border**: Card's internal focus indicator overrides outer border
- **`combinedClickable` + `focusable()`**: `combinedClickable` steals focus, `onFocusChanged` never fires
- **`clickable(indication = null)` + `focusable()`**: Card never receives focus
- **ClassicCard (TV Material3)**: Not clickable on physical phone devices
- **Color inversion for dark theme**: Makes cards darker, not brighter

---

## Favourite Screen Long Press Menu

### Behavior
Long press on a favourite card shows a delete confirmation menu. Short press navigates to detail page.

| Input | Short Press | Long Press |
|-------|-------------|------------|
| Touch | Navigate to detail | Show delete menu (no navigation) |
| D-pad (Center button) | Navigate to detail | Show delete menu (no navigation) |

### Implementation
Long press is handled in `FavouriteScreen` (NOT in `MediaSmall`), using two independent mechanisms:

#### Touch long press
```
Modifier.pointerInput → awaitEachGesture → awaitFirstDown → track touchPressed state
LaunchedEffect(touchPressed) → delay(longPressTimeout) → show menu
```

- `pointerInput` detects finger down/up via `awaitEachGesture`
- Sets `touchPressed = true` on down, `false` on up
- `LaunchedEffect(touchPressed)` starts a timer when finger goes down
- If finger lifts before timeout → `touchPressed = false` → effect cancelled (short press)
- If timeout fires → `longPressConsumed = true` → show menu

#### D-pad long press
```
Modifier.onPreviewKeyEvent → Center KeyDown → Handler.postDelayed(longPressTimeout) → show menu
                         → Center KeyUp → Handler.removeCallbacksAndMessages → manual onClick
```

- `onPreviewKeyEvent` consumes Center key events (returns `true`), preventing Card's internal click handling
- KeyDown starts a `Handler.postDelayed` timer
- KeyUp cancels the timer. If timer hadn't fired (short press), manually calls `onNavigateToAnimeDetail()`
- `DisposableEffect(Unit)` cleans up Handler when composable leaves the tree

#### Shared flag
```
var longPressConsumed by remember { mutableStateOf(false) }
```

- Set to `true` when long press is detected (by either mechanism)
- Card's `onClick` checks: `if (!longPressConsumed) { navigate }`
- Reset to `false` after check (prevents stale state from blocking future clicks)

### Files
- `app/.../favourite/FavouriteScreen.kt` — all long press logic lives here
- `app/.../component/MediaSmall.kt` — no long press logic, pure Card + border + focus

### Why long press is NOT in MediaSmall
MediaSmall is shared across Home, Favourite, Detail, and Search screens. Only Favourite needs long press. Putting it in MediaSmall would:
- Add `onLongClick` parameter to all callers
- Risk Handler/LaunchedEffect leaks across screen navigations
- Couple a UI component with screen-specific behavior

### Why `onPreviewKeyEvent` consumes D-pad events
`Card(onClick)` internally handles Center key as a click. Without consuming, both Card's onClick AND the long press timer would fire. Consuming lets us control the flow: short press → manual onClick, long press → menu only.

### What doesn't work (abandoned approaches)
- **`detectTapGestures` on Box wrapper**: Consumes events, breaks Card's onClick
- **`combinedClickable` on Box wrapper**: Steals focus from inner Card, border disappears
- **`pointerInput` + `consume()` after long press**: Breaks `awaitEachGesture` loop, all subsequent gestures fail
- **LaunchedEffect for D-pad**: Timing inconsistency with key events vs coroutine scheduling
- **Handler without DisposableEffect**: Leaked callbacks fire after screen navigation
