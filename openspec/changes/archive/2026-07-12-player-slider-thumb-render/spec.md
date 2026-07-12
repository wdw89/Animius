## Requirements

### Requirement: Slider thumb renders without visual artifacts

The slider thumb SHALL render as a clean, solid circle without any visible edge artifacts, regardless of the background behind it.

#### Scenario: Thumb against gradient overlay background

- **WHEN** the player control overlay is visible with its gradient background
- **AND** the slider thumb is displayed
- **THEN** the thumb SHALL NOT show any visible sub-pixel white/grey ring around its edge
- **AND** the thumb SHALL appear as a clean solid circle with smooth edges

#### Scenario: Thumb against transparent background

- **WHEN** the slider is displayed without any background overlay (e.g., compact seek mode)
- **THEN** the thumb SHALL NOT show any visible ring artifact
- **AND** the thumb SHALL appear as a clean solid circle

#### Scenario: Thumb rendering is independent of focus state

- **WHEN** the slider thumb is NOT focused and NOT in seeking mode
- **THEN** the thumb SHALL NOT have any visible border or edge artifact
- **AND** the thumb SHALL be a solid circle rendered at its base size (15dp)

#### Scenario: Thumb border only appears when active

- **WHEN** the slider is focused OR in seeking mode (isActive = true)
- **THEN** the thumb SHALL display a 2dp white circular border
- **AND** when isActive becomes false, the border modifier SHALL be removed entirely from the chain (not set to 0dp)
- **AND** the border SHALL disappear completely with no residual edge artifact

## Implementation

### Root cause

Two issues combined to produce the visible ring:

1. **Chip anti-aliasing**: `.clip(CircleShape)` followed by `.background(color)` renders the thumb as a color-filled rectangle, then clips it to a circle using an alpha mask. At the circle's edge, GPU anti-aliasing produces semi-transparent pixels. When the background behind the thumb is itself semi-transparent (gradient overlay), these edge pixels blend to produce a visible lighter ring.

2. **zero-width border side effect**: `.border(0.dp, Color.White, CircleShape)` still inserts a `BorderStroke` into the modifier chain, which Skia processes even at zero width — producing a ~1px edge artifact on some rendering paths.

### Fix

Two changes to the thumb modifier chain:

```kotlin
// Before
.clip(CircleShape)
.size(thumbSize)
.border(if (isActive) 3.dp else 0.dp, Color.White, CircleShape)
.background(color)

// After
.size(thumbSize)
.then(if (isActive) Modifier.border(2.dp, Color.White, CircleShape) else Modifier)
.background(color, CircleShape)
```

1. **Remove `.clip(CircleShape)`**: use `.background(color, CircleShape)` instead — Skia rasterizes the background directly as a circle, eliminating the clip alpha-mask pass and its anti-aliasing artifact.
2. **Conditional border via `.then()`**: skip the border modifier entirely when not active, rather than passing `0.dp`. Also reduced active border from 3dp to 2dp.
