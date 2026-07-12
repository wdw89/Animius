## Context

The `video-player` module's `VideoPlayerControl` composable renders a control overlay on top of video content. Currently it uses a uniform `Color.Black.copy(0.2f)` background across the entire overlay. All buttons use `Color.Transparent` as their container color in the normal (unfocused) state, with `Color.White` or `Color.LightGray` for icons and text. This creates a visibility problem against bright/white video content.

The reference implementation [open-ani/animeko](https://github.com/open-ani/animeko) uses `Brush.verticalGradient` in `VideoScaffold.kt` to create dark-to-transparent gradients at the top and bottom edges of the player, providing strong contrast where controls are while keeping the center of the video unobstructed.

The change is scoped to a single file: `VideoPlayerControl.kt` in the `video-player` module.

## Goals / Non-Goals

**Goals:**
- Replace the uniform overlay background with vertical gradient overlays at the top and bottom
- Ensure white/LightGray icons and text remain clearly visible against any video content (including pure white frames)
- Preserve the existing `background` parameter API on `VideoPlayerControl` for callers that override it
- Keep the center of the video unobstructed and clear

**Non-Goals:**
- Adding shadows (`Modifier.shadow()`) to individual icons or text — gradients provide sufficient contrast
- Adding per-button background circles (`Color.Black.copy(0.25f)` on `AdaptiveIconButton`) — not needed with gradient approach
- Modifying the `app` module's `VideoPlayerScreen.kt` — all changes stay in the library module
- Changing TV/D-pad focus behavior (focus navigation, inverse colors, focus traversal specs are unaffected)

## Decisions

### Decision 1: Use two `Box` overlays with `Brush.verticalGradient` instead of modifying the uniform `background` parameter

**Rationale**: Animeko's approach uses separate gradient `Box` elements that sit *under* the control UI but *over* the video. This is cleaner than trying to express a gradient through the existing `background: Color` parameter. The existing parameter continues to work for callers that pass it, but the default rendering no longer relies on it.

**Alternatives considered**:
- *Increase uniform background alpha from 0.2f to 0.5f*: Simpler, but makes the entire overlay darker and looks amateurish.
- *Add `Modifier.shadow()` to individual buttons/text*: Shadow on circular IconButtons renders as a square shadow (Android Compose limitation). Text shadows help but don't fully solve the problem.
- *Add semi-transparent circular backgrounds to each `AdaptiveIconButton`*: Works but looks "heavy" — many dark circles scattered across the overlay.

### Decision 2: Use `matchParentSize()` for gradient boxes + `Column` for controls

**Rationale**: Two `Box` composables sized to `matchParentSize()` sit behind the control `Column`. The top box covers the full width/height but only the top ~35% has visible gradient. The bottom box similarly covers only the bottom ~35%. The `Column` with `Spacer(Modifier.weight(1f))` keeps controls at their edge positions without overlap.

```kotlin
Box(modifier.fillMaxSize()) {
    // Gradient layers (behind controls)
    Box(Modifier.matchParentSize().background(topGradient))
    Box(Modifier.matchParentSize().background(bottomGradient))
    // Control layers (on top)
    Column {
        ControlHeader(...)
        Spacer(Modifier.weight(1f))
        BottomControlBar(...)
    }
}
```

### Decision 3: Gradient parameters

Following animeko's lead:
- **Top gradient**: `0f → Color.Black.copy(0.55f)`, `0.35f → Color.Transparent`
- **Bottom gradient**: `0f → Color.Transparent`, `0.65f → Color.Transparent`, `1f → Color.Black.copy(0.55f)`

The 0.55f alpha is stronger than the current 0.2f but only applies at the edges. The middle 30% of the screen (where controls don't live) stays fully transparent.

### Decision 4: Keep `AdaptiveIconButton` unchanged

With gradient overlays providing sufficient contrast, `AdaptiveIconButton`'s `Color.Transparent` default container color is fine. TV focus highlighting (`containerColor = primary` when focused) still works on top of the gradient.

## Risks / Trade-offs

- **Gradient colors tuned for dark mode only**: The player currently always uses a dark overlay. If light mode player support is added later, gradients would need inversion. Mitigation: not in scope; the entire player UI assumes dark backgrounds.
- **Gradient rendering on low-end devices**: `Brush.verticalGradient` on a full-screen `Box` may cause overdraw. Mitigation: gradients are simple 2-3 stop linear gradients; Compose handles these efficiently.
- **Edge case — very short videos in portrait mode**: In portrait (non-fullscreen), the gradient may cover proportionally more of the video since the video area is shorter. Mitigation: this is acceptable — portrait mode already has limited video real estate, and controls still need contrast.
