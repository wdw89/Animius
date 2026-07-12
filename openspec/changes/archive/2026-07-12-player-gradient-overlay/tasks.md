## 1. Core gradient overlay implementation

- [x] 1.1 Add `Brush` import to `VideoPlayerControl.kt` (`androidx.compose.ui.graphics.Brush`)
- [x] 1.2 Remove the uniform `Modifier.background(background)` from the root `Box` in `VideoPlayerControl`
- [x] 1.3 Add a top gradient `Box` with `Modifier.matchParentSize().background(Brush.verticalGradient(0f to Color.Black.copy(0.55f), 0.35f to Color.Transparent))` as the first child of the root `Box`
- [x] 1.4 Add a bottom gradient `Box` with `Modifier.matchParentSize().background(Brush.verticalGradient(0f to Color.Transparent, 0.65f to Color.Transparent, 1f to Color.Black.copy(0.55f)))` as the second child of the root `Box`
- [x] 1.5 Replace `Arrangement.SpaceBetween` in the control `Column` with `Spacer(Modifier.weight(1f))` between `ControlHeader` and `BottomControlBar` (ensures controls stay at edges while gradient boxes span full height)

## 2. Backward compatibility

- [x] 2.1 Wrap the gradient overlay logic in a conditional: when caller passes a non-default `background` value, fall back to the old uniform `Modifier.background(background)` behavior; when using the default, render the new gradient overlays

## 3. Verification

- [x] 3.1 Build debug APK and install on device (`./gradlew :app:installDebug`)
- [ ] 3.2 Play a video with bright/white scenes and verify all controls (back button, title, subtitle, play/pause, next, danmaku, timestamp, text buttons) remain clearly visible
- [ ] 3.3 Play a video with dark scenes and verify controls are visible and the gradient edges do not look out of place
- [ ] 3.4 Verify center of video remains unobstructed (no uniform dimming)
- [ ] 3.5 Verify TV D-pad focus navigation still works correctly (focus highlights visible on top of gradient)
