## Why

Player control buttons, icons, and text currently render without any background — they appear as pure white or light gray directly on top of video content. When the underlying video frame is bright or white, all controls become nearly invisible. The current uniform `Color.Black.copy(0.2f)` overlay is too faint to provide adequate contrast.

## What Changes

- Replace the uniform semi-transparent black background (`Color.Black.copy(0.2f)`) on the control overlay with **vertical gradient overlays** — a dark-to-transparent gradient at the top and a transparent-to-dark gradient at the bottom
- Increase effective contrast in the header (title, back button) and playback control areas while keeping the center of the video fully visible
- Reference implementation: [open-ani/animeko](https://github.com/open-ani/animeko) `VideoScaffold.kt`
- No API changes; existing `background` parameter on `VideoPlayerControl` remains functional for callers that override it

## Capabilities

### New Capabilities
- `player-gradient-overlay`: Vertical gradient backgrounds for the video player control overlay's header and footer regions, providing adequate contrast for white icons/text against any video content

### Modified Capabilities
<!-- None — existing specs (player-focus-navigation, tv-focus-inverse-color, tv-focus-traversal) are unaffected -->

## Impact

- **Module**: `video-player` (`VideoPlayerControl.kt`) — single file change
- **Breaking**: None — the `background` composable parameter remains available; only its default rendering changes
- **TV/D-pad**: Focus highlight (primary color on focused buttons) continues to work; focus navigation specs unaffected
