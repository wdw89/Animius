---
feature: tv-remote-support
status: delivered
specs: []
plans:
  - docs/compose/plans/2025-06-19-tv-remote-support.md
branch: main
commits: (uncommitted)
---

# TV Remote Control Support — Final Report

## What Was Built

Added D-pad remote control support to the video player and improved focus highlight visibility across the app for Android TV. The video player now supports full D-pad navigation: left/right to seek, up to show controls, down for next episode, center/enter to play/pause. All player control buttons (play/pause, next, danmaku, speed, resize, episode selector) display a 2dp primary-color border when focused. The progress slider supports D-pad left/right seeking with visual feedback. On Android TV, focus is automatically applied to the first control button when the control overlay appears. Other screens (anime detail episode buttons, search result items) also received improved focus border visibility.

## Architecture

### Files Modified

| File | Changes |
|------|---------|
| `video-player/.../VideoPlayerControl.kt` | Added `tvFocusBorder` modifier, wired into `AdaptiveIconButton` and `AdaptiveTextButton`, added `modifier` parameter to `VideoPlayerControl` |
| `video-player/.../component/Slider.kt` | Added focusable, focus border, D-pad key handling (left/right seek, center confirm) |
| `app/.../videoplayer/VideoPlayerScreen.kt` | Enhanced `defaultRemoteControlHandler` (KeyUp instead of KeyDown), added auto-focus on TV via `FocusRequester` + `LaunchedEffect` |
| `app/.../detail/AnimeDetailScreen.kt` | Added focus border to episode buttons |
| `app/.../search/SearchScreen.kt` | Added focus border to search result items |

### Key Components

- **`tvFocusBorder` modifier** — Reusable Compose modifier that shows a 2dp primary-color `RoundedCornerShape(8.dp)` border when an element has D-pad focus. Uses `MutableInteractionSource.collectIsFocusedAsState()` for idiomatic focus tracking.
- **`defaultRemoteControlHandler`** — `Modifier.onKeyEvent` extension mapping D-pad keys to player actions. Consumes events on `KeyUp` (not `KeyDown`) to prevent repeat-firing.
- **Auto-focus on TV** — `LaunchedEffect` watches `isControlUiVisible` and requests focus on the first control button via `FocusRequester` when controls appear on Android TV.

### Design Decisions

- **Plain Material3 + manual focus** (not TV Material3) — Minimizes migration risk, phone and TV share the same UI code with `isAndroidTV()` checks for focus behavior only.
- **KeyUp consumption** — Prevents D-pad repeat-firing that caused unintended rapid seek/play-pause toggling.
- **2dp primary border** — Visible but unobtrusive; uses `MaterialTheme.colorScheme.primary` for consistency with the app theme.

## Verification

- `./gradlew assembleDebug` — BUILD SUCCESSFUL (128 tasks)
- `./gradlew :video-player:compileDebugKotlin` — BUILD SUCCESSFUL
- `./gradlew :app:compileDebugKotlin` — BUILD SUCCESSFUL (only pre-existing deprecation warnings)
- All imports verified correct across all modified files
- No new warnings introduced

## Journey Log

- [lesson] `collectIsFocusedAsState()` requires `import androidx.compose.runtime.getValue` for `by` delegate — missing import caused initial compilation failure
- [lesson] Gradle 8.14 distribution download can timeout on slow networks — use Android Studio bundled JDK at `C:\Program Files\Android\Android Studio\jbr` as fallback
