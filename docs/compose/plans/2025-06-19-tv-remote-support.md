# TV Remote Control Support Implementation Plan

> [!NOTE]
> This document may not reflect the current implementation.
> See the final report for up-to-date state:
> [Final Report](../reports/tv-remote-support.md)

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add D-pad remote control support to the video player and improve focus highlight visibility across the app for Android TV.

**Architecture:** Add focusable modifiers + focus border visual feedback to all interactive elements in `VideoPlayerControl`. Enhance `defaultRemoteControlHandler` in `VideoPlayScreen` for full D-pad navigation. Extract a reusable `tvFocusBorder` modifier. Adjust existing focus border styles in other screens.

**Tech Stack:** Jetpack Compose, Media3 ExoPlayer, standard Material3 (no TV Material3 dependency)

---

### Task 1: Create reusable TV focus border modifier

**Covers:** [S2]

**Files:**
- Modify: `video-player/src/main/java/com/lanlinju/videoplayer/VideoPlayerControl.kt`

- [ ] **Step 1: Add focus border modifier at the top of VideoPlayerControl.kt**

Add these imports after the existing imports (around line 39):

```kotlin
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.IntSize
```

Then add this modifier function after the `safeValue()` function (around line 291):

```kotlin
private val FocusBorderShape = RoundedCornerShape(8.dp)

@Composable
private fun Modifier.tvFocusBorder(
    interactionSource: MutableInteractionSource,
    borderWidth: Dp = 2.dp,
): Modifier {
    val isFocused by interactionSource.collectIsFocusedAsState()
    return this
        .border(
            width = if (isFocused) borderWidth else 0.dp,
            color = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
            shape = FocusBorderShape
        )
}
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew :video-player:compileDebugKotlin`

Expected: BUILD SUCCESSFUL

---

### Task 2: Add focus support to AdaptiveIconButton

**Covers:** [S2]

**Files:**
- Modify: `video-player/src/main/java/com/lanlinju/videoplayer/VideoPlayerControl.kt:346-370`

- [ ] **Step 1: Update AdaptiveIconButton to support focus**

Replace the `AdaptiveIconButton` composable (lines 346-370) with:

```kotlin
@Composable
private fun AdaptiveIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    enabledIndication: Boolean = true,
    content: @Composable () -> Unit
) {
    val indication = LocalIndication.current

    Box(
        modifier = modifier
            .clip(CircleShape)
            .tvFocusBorder(interactionSource)
            .clickable(
                onClick = onClick,
                enabled = enabled,
                interactionSource = interactionSource,
                indication = if (enabledIndication) indication else null
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
```

Key change: Added `.tvFocusBorder(interactionSource)` before `.clickable()`.

- [ ] **Step 2: Verify compilation**

Run: `./gradlew :video-player:compileDebugKotlin`

Expected: BUILD SUCCESSFUL

---

### Task 3: Add focus support to AdaptiveTextButton

**Covers:** [S2]

**Files:**
- Modify: `video-player/src/main/java/com/lanlinju/videoplayer/VideoPlayerControl.kt:326-344`

- [ ] **Step 1: Update AdaptiveTextButton to use interactionSource for focus**

Replace the `AdaptiveTextButton` composable (lines 326-344) with:

```kotlin
@Composable
fun AdaptiveTextButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    color: Color = LocalContentColor.current,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    val interactionSource = remember { MutableInteractionSource() }
    AdaptiveIconButton(
        modifier = modifier.size(MediumIconButtonSize),
        onClick = onClick,
        interactionSource = interactionSource
    ) {
        Text(
            text = text,
            color = color,
            style = style,
        )
    }
}
```

Key change: Creates and passes its own `interactionSource` so the focus border works.

- [ ] **Step 2: Verify compilation**

Run: `./gradlew :video-player:compileDebugKotlin`

Expected: BUILD SUCCESSFUL

---

### Task 4: Add focus support to Slider

**Covers:** [S2]

**Files:**
- Modify: `video-player/src/main/java/com/lanlinju/videoplayer/component/Slider.kt`

- [ ] **Step 1: Add imports to Slider.kt**

Add after existing imports (around line 26):

```kotlin
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.IntSize
```

- [ ] **Step 2: Update Slider composable to support D-pad seeking**

Replace the `Slider` composable (lines 28-124) with:

```kotlin
@Composable
fun Slider(
    value: Float,
    secondValue: Float,
    modifier: Modifier = Modifier,
    onClick: (Float) -> Unit,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit = {},
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = Color.LightGray.copy(alpha = 0.38f),
    secondTrackColor: Color = Color.LightGray.copy(alpha = 0.78f),
    isSeeking: Boolean = false
) {
    val isAnimHeight = remember(isSeeking) { mutableStateOf(isSeeking) }
    val animHeight = animateDpAsState(
        targetValue = if (isAnimHeight.value) 4.dp else 2.dp,
        animationSpec = tween()
    )
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val focusRequester = remember { FocusRequester() }
    val seekStep = 0.02f // 2% per D-pad press

    Box(
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged { if (it.isFocused) isAnimHeight.value = true }
            .focusable(interactionSource = interactionSource)
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.DirectionLeft -> {
                            onValueChange((value - seekStep).coerceIn(0f, 1f))
                            true
                        }
                        Key.DirectionRight -> {
                            onValueChange((value + seekStep).coerceIn(0f, 1f))
                            true
                        }
                        Key.DirectionCenter, Key.Spacebar -> {
                            onClick(value)
                            onValueChangeFinished()
                            true
                        }
                        else -> false
                    }
                } else false
            }
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) color else Color.Transparent,
                shape = RoundedCornerShape(2.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(onTap = { offset ->
                    isAnimHeight.value = true
                    onClick(offset.x / size.width)
                }, onPress = {
                    tryAwaitRelease()
                    delay(150)
                    isAnimHeight.value = false
                })
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, _ ->
                        onValueChange((change.position.x / size.width).coerceIn(0f, 1f))
                        change.consume()
                    },
                    onDragEnd = onValueChangeFinished
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {

        // track
        Box(
            modifier = Modifier
                .clip(
                    FractionClip(fraction = value, start = false)
                )
                .fillMaxWidth()
                .height(animHeight.value)
                .background(
                    color = trackColor,
                    shape = RoundedCornerShape(2.dp)
                )
        )

        // 视频缓冲进度
        Box(
            modifier = Modifier
                .fillMaxWidth(secondValue)
                .height(animHeight.value)
                .background(
                    color = secondTrackColor,
                    shape = RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp)
                )
        )

        // 视频播放进度
        Box(
            modifier = Modifier
                .clip(
                    FractionClip(fraction = value, start = true)
                )
                .fillMaxWidth()
                .height(animHeight.value)
                .background(
                    color = color,
                    shape = RoundedCornerShape(2.dp)
                )
        )

        // thumb
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .align(
                    BiasAlignment(
                        horizontalBias = (value * 2) - 1f, // -1 start | 0 center | 1 end
                        verticalBias = 0f
                    )
                )
                .size(15.dp)
                .background(color)
        )
    }
}
```

Key changes:
- Added `focusRequester`, `interactionSource`, `isFocused` state
- Added `.focusable()`, `.onFocusChanged()`, `.onKeyEvent()` for D-pad left/right seek
- Added focus border on the slider track

- [ ] **Step 3: Verify compilation**

Run: `./gradlew :video-player:compileDebugKotlin`

Expected: BUILD SUCCESSFUL

---

### Task 5: Add auto-focus on TV when controls appear

**Covers:** [S2, S3]

**Files:**
- Modify: `app/src/main/java/com/lanlinju/animius/presentation/screen/videoplayer/VideoPlayerScreen.kt:214-234`

- [ ] **Step 1: Add FocusRequester import and auto-focus logic**

In `VideoPlayScreen`, add a `FocusRequester` for the first control button and request focus on TV when controls appear.

Replace the `VideoPlayerControl` call block (lines 214-234) with:

```kotlin
                    val controlFocusRequester = remember { FocusRequester() }
                    val isAndroidTV = remember { isAndroidTV(activity) }

                    VideoPlayerControl(
                        state = playerState,
                        title = "${video.title}-${video.episodeName}",
                        danmakuEnabled = danmakuEnabled,
                        onBackClick = { handleBackPress(playerState, onBackClick, view, activity) },
                        onNextClick = {
                            playerState.control.pause()
                            playerState.setLoading(true)
                            viewModel.playNextEpisode(playerState.player.currentPosition)
                        },
                        optionsContent = {
                            OptionsContent(
                                video = video,
                                isAutoContinuePlayEnabled = isAutoContinuePlayEnabled,
                                onAutoContinuePlayClick = { isAutoContinuePlayEnabled = it },
                                onForwardClick = { playerState.control.skip(85000) }
                            )
                        },
                        onDanmakuClick = { viewModel.setEnabledDanmaku(it) },
                        modifier = if (isAndroidTV) Modifier.focusRequester(controlFocusRequester) else Modifier
                    )

                    // Auto-focus first control on TV when controls appear
                    LaunchedEffect(playerState.isControlUiVisible.value, isAndroidTV) {
                        if (isAndroidTV && playerState.isControlUiVisible.value) {
                            controlFocusRequester.requestFocus()
                        }
                    }
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew :app:compileDebugKotlin`

Expected: BUILD SUCCESSFUL

---

### Task 6: Add modifier parameter to VideoPlayerControl

**Covers:** [S2]

**Files:**
- Modify: `video-player/src/main/java/com/lanlinju/videoplayer/VideoPlayerControl.kt:49-101`

- [ ] **Step 1: Add modifier parameter to VideoPlayerControl**

Update the `VideoPlayerControl` function signature to accept a `modifier` parameter:

```kotlin
@Composable
fun VideoPlayerControl(
    state: VideoPlayerState,
    title: String,
    subtitle: String? = null,
    background: Color = Color.Black.copy(0.2f),
    contentColor: Color = Color.LightGray,
    progressLineColor: Color = MaterialTheme.colorScheme.inversePrimary,
    danmakuEnabled: Boolean,
    onBackClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
    onDanmakuClick: (Boolean) -> Unit = {},
    optionsContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
)
```

Then update the internal Box to use this modifier:

```kotlin
    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(background)
                .padding(
                    start = horizontalPadding(),
                    end = horizontalPadding(),
                    top = 18.dp
                )
        ) {
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew :video-player:compileDebugKotlin`

Expected: BUILD SUCCESSFUL

---

### Task 7: Enhance defaultRemoteControlHandler

**Covers:** [S3]

**Files:**
- Modify: `app/src/main/java/com/lanlinju/animius/presentation/screen/videoplayer/VideoPlayerScreen.kt:379-422`

- [ ] **Step 1: Improve D-pad handler to consume on KeyUp instead of KeyDown**

Replace the `defaultRemoteControlHandler` (lines 379-422) with:

```kotlin
private fun Modifier.defaultRemoteControlHandler(
    playerState: VideoPlayerState,
    onNextClick: () -> Unit = {},
) = onKeyEvent { keyEvent: KeyEvent ->
    if (keyEvent.type == KeyEventType.KeyUp)
        when (keyEvent.key) {
            Key.DirectionLeft -> {
                playerState.showControlUi()
                playerState.control.rewind()
                true
            }

            Key.DirectionRight -> {
                playerState.showControlUi()
                playerState.control.forward()
                true
            }

            Key.DirectionUp -> {
                playerState.showControlUi()
                true
            }

            Key.DirectionDown -> {
                playerState.showControlUi()
                onNextClick()
                true
            }

            Key.DirectionCenter, Key.Spacebar -> {
                if (playerState.isPlaying.value) {
                    playerState.showControlUi()
                    playerState.control.pause()
                } else {
                    playerState.control.play()
                }
                true
            }

            else -> false
        } else {
        false
    }
}
```

Key changes:
- Changed from `KeyDown` to `KeyUp` to prevent repeat-firing (matching JetStreamCompose pattern)
- Changed `DirectionUp` from `showEpisodeUi()` to `showControlUi()` (episode UI is accessed via the episode button in the control bar)
- Changed `DirectionDown` to only show control UI + play next (removed redundant `showControlUi()`)

- [ ] **Step 2: Verify compilation**

Run: `./gradlew :app:compileDebugKotlin`

Expected: BUILD SUCCESSFUL

---

### Task 8: Improve existing focus border styles in other screens

**Covers:** [S4]

**Files:**
- Modify: `app/src/main/java/com/lanlinju/animius/presentation/screen/detail/AnimeDetailScreen.kt:626-638`
- Modify: `app/src/main/java/com/lanlinju/animius/presentation/screen/search/SearchScreen.kt:210-227`

- [ ] **Step 1: Add focus border to AnimeDetailScreen episode buttons**

In `AnimeDetailScreen.kt`, update the `FilledTonalButton` modifier block (lines 629-638) to add a focus border:

```kotlin
                modifier = Modifier.run {
                    if (isAndroidTV) {
                        clip(CircleShape)
                            .indication(interactionSource, LocalIndication.current)
                            .hoverable(interactionSource)
                            .focusRequester(focusRequester)
                            .focusable(interactionSource = interactionSource)
                            .border(
                                width = if (isFocused) 2.dp else 0.dp,
                                color = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape
                            )
                    } else this
                }
```

Also add the `isFocused` state and imports:

```kotlin
import androidx.compose.foundation.border
import androidx.compose.ui.focus.onFocusChanged
```

And in the `itemsIndexed` block, add:

```kotlin
var isFocused by remember { mutableStateOf(false) }
```

And update the modifier chain:

```kotlin
.onFocusChanged { isFocused = it.isFocused }
```

- [ ] **Step 2: Add focus border to SearchScreen search result items**

In `SearchScreen.kt`, update the `MediaSmall` modifier block to add a focus border when focused on TV.

Add imports:

```kotlin
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
```

Update the modifier chain (around line 211-222) to add:

```kotlin
.border(
    width = if (isFocused && isAndroidTV) 2.dp else 0.dp,
    color = if (isFocused && isAndroidTV) MaterialTheme.colorScheme.primary else Color.Transparent,
    shape = RoundedCornerShape(8.dp)
)
```

- [ ] **Step 3: Verify compilation**

Run: `./gradlew :app:compileDebugKotlin`

Expected: BUILD SUCCESSFUL

---

### Task 9: Run full build and verify

**Covers:** [S1, S2, S3, S4]

**Files:** None (verification only)

- [ ] **Step 1: Run full debug build**

Run: `./gradlew assembleDebug`

Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Run unit tests**

Run: `./gradlew :video-player:testDebugUnitTest`

Expected: Tests pass (or no tests to run)

- [ ] **Step 3: Run app unit tests**

Run: `./gradlew :app:testDebugUnitTest`

Expected: Tests pass

---

### Task 10: Commit changes

**Covers:** All

- [ ] **Step 1: Stage and commit**

```bash
git add video-player/src/main/java/com/lanlinju/videoplayer/VideoPlayerControl.kt
git add video-player/src/main/java/com/lanlinju/videoplayer/component/Slider.kt
git add app/src/main/java/com/lanlinju/animius/presentation/screen/videoplayer/VideoPlayerScreen.kt
git add app/src/main/java/com/lanlinju/animius/presentation/screen/detail/AnimeDetailScreen.kt
git add app/src/main/java/com/lanlinju/animius/presentation/screen/search/SearchScreen.kt
git commit -m "feat: add D-pad remote control support to video player

- Add focusable() + focus border to all player control buttons
- Add D-pad seeking to progress slider
- Enhance defaultRemoteControlHandler with KeyUp consumption
- Add auto-focus on TV when controls appear
- Improve focus border visibility in detail and search screens"
```
