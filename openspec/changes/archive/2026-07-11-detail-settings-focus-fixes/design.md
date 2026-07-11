## Context

Animius currently has no shared focus utility layer. Every screen re-implements the same pattern: a `var isFocused by remember { mutableStateOf(false) }` combined with `Modifier.onFocusChanged { isFocused = it.isFocused }` and manual `primary`/`onPrimary` color switching. There are no D-pad interception tools, no focus boundary controls, and no explicit focus order configuration. The project targets `minSdk 26` and already depends on Compose Foundation (which includes `focusProperties`, `focusGroup`, `focusRestorer`, `FocusRequester`, `onKeyEvent`, `onPreviewKeyEvent`). No TV-specific library (`androidx.tv.material3`) is used, and by decision we are not introducing one.

Reference projects studied: `ComposeTv` (uses `handleDPadKeyEvents` modifier + TV M3 components) and `JetStreamCompose` (uses `handleDPadKeyEvents`, `focusOnInitialVisibility`, `FocusRequester.createRefs()` pattern, `bringIntoViewIfChildrenAreFocused`).

## Goals / Non-Goals

**Goals:**
- Create a minimal set of reusable focus modifiers in `app/.../util/focus/`
- Fix vertical focus flow on the anime detail page: episodes → episode controls → related
- Fix vertical focus flow on the appearance settings page: theme mode → (skip color swatches) → switches
- Replace repetitive per-element `onFocusChanged` boilerplate with a shared `onFocusHighlight` modifier on both screens

**Non-Goals:**
- Do NOT introduce `androidx.tv.material3` or any new external dependency
- Do NOT change the visual appearance of focus states (inverse-color pattern stays)
- Do NOT modify the player screen (separate change)
- Do NOT refactor other screens (Home, Search, Week, Favourite, Danmaku settings) — they can adopt the utilities later
- Do NOT implement a full `focusOrder` DSL — start with direction-key interception where needed

## Decisions

### Decision 1: Three utility modifiers, not one unified system

**Chosen:** Three independent modifier files under `app/.../util/focus/`:
- `FocusHighlight.kt` — replaces `onFocusChanged` + manual color state boilerplate
- `DpadKeyHandler.kt` — `handleDPadKeyEvents` for direction-key interception
- `FocusOrder.kt` — thin helpers for `focusProperties` configuration

**Rejected:** A full `FocusManager` Composable wrapper or a `Box`-level focus interceptor. Too heavy for the current needs. These three modifiers address the concrete problems and compose freely.

**Rationale:** Matches the pattern both ComposeTv and JetStreamCompose use — standalone modifier extensions, not a wrapping component. Easy to test, easy to adopt incrementally.

### Decision 2: `handleDPadKeyEvents` uses `onPreviewKeyEvent` for UP/DOWN/LEFT/RIGHT, not `onKeyEvent`

**Chosen:** `onPreviewKeyEvent` (fires before child composables process the event).

**Rejected:** `onKeyEvent` (fires after children).

**Rationale:** When we want to intercept a direction key at a boundary (e.g., skip over ColorBall on ↑↓), we need to consume the event *before* any child inside ColorBall has a chance to receive it. `onPreviewKeyEvent` gives us this priority. Events that should pass through (e.g., ← → within ColorBall) return `false` to let children handle them.

This matches the ComposeTv and JetStreamCompose implementations.

### Decision 3: Detail page — EpisodeListControl becomes a standalone Row

**Current layout:**
```
Box {
    AnimeEpisodes(LazyRow, full-width)
    EpisodeListControl(floated: Modifier.align(BottomEnd))
}
```

**New layout:**
```
Column {
    AnimeEpisodes(LazyRow, full-width)
    EpisodeListControl(Row, right-aligned, full-width)
}
```

The `Box` wrapping is replaced with `Column`. `EpisodeListControl` moves from `Modifier.align(Alignment.BottomEnd)` + `offset(y = ...)` to a simple `Row(horizontalArrangement = Arrangement.End)` inside the `Column`. This creates a proper vertical stacking so Compose's spatial algorithm naturally moves focus: episodes ↔ controls ↔ related.

The visual appearance is preserved because `EpisodeListControl` was already right-aligned; the only change is its Y position (slightly adjusted from the previous offset-based positioning).

### Decision 4: Settings page — ColorBall uses horizontal-only focus with vertical interception

**Approach:** Wrap `ColorBall`'s `LazyRow` in a `Box` with `handleDPadKeyEvents(onUp = { ... }, onDown = { ... })` that manually routes ↑↓ focus past the entire row.

```
Box(
    modifier = Modifier.handleDPadKeyEvents(
        onUp = { themeModeFocusRequester.requestFocus() },
        onDown = { dynamicColorSwitchFocusRequester.requestFocus() }
    )
) {
    ColorBall LazyRow(...)
}
```

Individual color circles inside ColorBall still use standard ← → navigation within the row. The outer Box intercepts ↑↓ before they reach any child.

**Alternative considered:** `focusProperties { up = Cancel; down = Cancel }` on each circle. Rejected because it blocks vertical movement entirely without providing a target — the focus system would do nothing on ↑↓, which is worse UX than the current random-jump behavior.

### Decision 5: Focus highlight stays inline, not extracted from individual screens yet

The `FocusHighlight` modifier will be used on the two target screens (detail, settings). Other screens (Home, Search, Week, Favourite) keep their existing manual `onFocusChanged` patterns. They can be migrated later as a follow-up refactor. This keeps the change focused and minimizes regression risk.

### Decision 6: Package structure

```
app/src/main/java/com/lanlinju/animius/util/focus/
├── FocusHighlight.kt      # rememberIsFocused()
├── DpadKeyHandler.kt      # Modifier.handleDPadKeyEvents()
```

Two files at package `com.lanlinju.animius.util.focus`. No new module — stays in `:app`.

### Decision 7 (v4 — 彻底重构): 放弃 lastPlayedFocusRef，改用 moveFocus

**问题:** 在 LazyRow item 内部用 `LaunchedEffect` 管理 `lastPlayedFocusRef` 产生了一系列
时序问题：异步窗口期崩溃、倒序后 ref 失效、正序后 `initialFocusDone` 重置导致抢焦点。
经过 v1-v3 迭代修复均未彻底解决。

**新方案:** 完全放弃 `lastPlayedFocusRef` 机制。

1. `AnimeEpisodes` 不再接收 `lastPlayedFocusRef` 参数
2. `EpisodeListControl.onUpFocusRequest` 改为 `focusManager.moveFocus(FocusDirection.Up)`
   — 让 Compose 空间算法自然向上导航到 LazyRow 中空间最近的 episode
3. `FavouriteIcon.onDown` 同样改为 `focusManager.moveFocus(FocusDirection.Down)`
4. 初始 focus：`AnimeEpisodes` 函数级别用 `LaunchedEffect(Unit)` 滚动到 `lastPosition`
   并 `requestFocus` 第一个可见 item（通过 `scrollState.firstVisibleItemIndex` 获取）
5. 倒序后焦点留在按钮上（不抢），按↑时 `moveFocus(Up)` 自然导航

**优势:**
- 无 FocusRequester 生命周期问题（不存储跨 composable 引用）
- 无异步时序窗口
- 倒序/正序切换不抢焦点
- `moveFocus` 是 Compose 原生 API，稳定可靠

**代价:**
- 按↑从控件行到集数时，聚焦到空间最近的 episode（不一定是最后播放的）
- 但 LazyRow 已滚动到 `lastPosition` 附近，所以空间最近的通常就是最后播放的

### Decision 8: Reverse list button shows toggle state

**Chosen:** Change button text based on current `reverseList` state.
When `reverseList` is `false`, show the string resource `reverse_list` ("列表倒序").
When `reverseList` is `true`, show "列表正序". No new string resource needed —
hardcoded Chinese literal since other UI text in this row ("线路", "更多") is also hardcoded.

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| `handleDPadKeyEvents` with `onPreviewKeyEvent` may conflict with existing `onKeyEvent`/`onPreviewKeyEvent` modifiers on the same composable | Read current code for each usage site; only install the modifier where no existing key handler is present |
| `FocusRequester.requestFocus()` calls from `handleDPadKeyEvents` may fail if the target composable is not yet composed (scrolled off-screen, conditional) | Verify target composables are always in the composition tree when the direction-key callback fires. Detail page elements (episodes, controls, related) are always visible together |
| Animation-driven `ColorBall` uses `animateDpAsState` — focus border animation may conflict with focus highlight | `onFocusChanged` already exists on `ColorBall` items; replacing with `onFocusHighlight` preserves the same triggers |
| Changing EpisodeListControl from Box-floating to Column-row shifts the vertical layout slightly | Trivial Y-offset difference; visually negligible since EpisodeListControl was tiny and already near the LazyRow bottom |
