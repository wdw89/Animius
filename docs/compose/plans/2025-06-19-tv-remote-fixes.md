# TV Remote Control Fixes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix D-pad behavior, improve focus styles, and align with JetStream/TvMaterialCatalog patterns.

**Architecture:** Modify video-player module's Slider and VideoPlayerControl for focus handling. Update app module's VideoPlayerScreen for D-pad logic. Update detail/search screens for focus visibility.

**Tech Stack:** Jetpack Compose, Material3

---

### Task 1: Fix D-pad left/right behavior in defaultRemoteControlHandler

**Covers:** [S1]

**Files:**
- Modify: `app/src/main/java/com/lanlinju/animius/presentation/screen/videoplayer/VideoPlayerScreen.kt:390-434`

- [ ] **Step 1: Update defaultRemoteControlHandler signature**

Add `onSliderFocusRequest` parameter:

```kotlin
private fun Modifier.defaultRemoteControlHandler(
    playerState: VideoPlayerState,
    onNextClick: () -> Unit = {},
    onSliderFocusRequest: () -> Unit = {},
) = onKeyEvent { keyEvent: KeyEvent ->
    if (keyEvent.type == KeyEventType.KeyUp)
        when (keyEvent.key) {
            Key.DirectionLeft -> {
                if (!playerState.isControlUiVisible.value) {
                    playerState.showControlUi()
                    onSliderFocusRequest()
                }
                true
            }

            Key.DirectionRight -> {
                if (!playerState.isControlUiVisible.value) {
                    playerState.showControlUi()
                    onSliderFocusRequest()
                }
                true
            }

            Key.DirectionUp -> {
                playerState.showControlUi()
                true
            }

            Key.DirectionDown -> {
                playerState.showControlUi()
                true
            }

            Key.DirectionCenter, Key.Spacebar -> {
                if (!playerState.isControlUiVisible.value) {
                    if (playerState.isPlaying.value) {
                        playerState.showControlUi()
                        playerState.control.pause()
                    } else {
                        playerState.control.play()
                    }
                }
                true
            }

            else -> false
        } else {
        false
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'; & 'E:\Github\Anime\Animius\gradlew.bat' :app:compileDebugKotlin`

Expected: BUILD SUCCESSFUL

---

### Task 2: Expose Slider FocusRequester

**Covers:** [S2]

**Files:**
- Modify: `video-player/src/main/java/com/lanlinju/videoplayer/component/Slider.kt:40-61`

- [ ] **Step 1: Add focusRequester parameter to Slider**

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
    isSeeking: Boolean = false,
    focusRequester: FocusRequester = remember { FocusRequester() },
) {
```

- [ ] **Step 2: Verify compilation**

Run: `$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'; & 'E:\Github\Anime\Animius\gradlew.bat' :video-player:compileDebugKotlin`

Expected: BUILD SUCCESSFUL

---

### Task 3: Pass Slider FocusRequester through VideoPlayerControl

**Covers:** [S2]

**Files:**
- Modify: `video-player/src/main/java/com/lanlinju/videoplayer/VideoPlayerControl.kt:57-70`
- Modify: `video-player/src/main/java/com/lanlinju/videoplayer/VideoPlayerControl.kt:159-166`
- Modify: `video-player/src/main/java/com/lanlinju/videoplayer/VideoPlayerControl.kt:188-199`

- [ ] **Step 1: Add sliderFocusRequester to VideoPlayerControl**

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
    sliderFocusRequester: FocusRequester = remember { FocusRequester() },
) {
```

Add import: `import androidx.compose.ui.focus.FocusRequester`

- [ ] **Step 2: Add sliderFocusRequester to BottomControlBar**

```kotlin
@Composable
private fun BottomControlBar(
    modifier: Modifier,
    progressLineColor: Color,
    state: VideoPlayerState,
    enabledDanmaku: Boolean,
    onNextClick: () -> Unit,
    onDanmakuClick: (Boolean) -> Unit,
    sliderFocusRequester: FocusRequester,
) {
```

- [ ] **Step 3: Pass sliderFocusRequester to Slider**

```kotlin
Slider(
    value = state.videoProgress.value.safeValue(),
    secondValue = state.videoBufferedProgress.value.safeValue(),
    onClick = { state.onClickSlider(it) },
    onValueChange = { state.onSeeking(it) },
    onValueChangeFinished = { state.onSeeked() },
    modifier = Modifier
        .fillMaxWidth()
        .height(30.dp),
    isSeeking = state.isSeeking.value,
    color = progressLineColor,
    focusRequester = sliderFocusRequester,
)
```

- [ ] **Step 4: Pass sliderFocusRequester in VideoPlayerControl body**

```kotlin
BottomControlBar(
    modifier = Modifier.fillMaxWidth(),
    progressLineColor = progressLineColor,
    state = state,
    enabledDanmaku = danmakuEnabled,
    onNextClick = onNextClick,
    onDanmakuClick = onDanmakuClick,
    sliderFocusRequester = sliderFocusRequester
)
```

- [ ] **Step 5: Verify compilation**

Run: `$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'; & 'E:\Github\Anime\Animius\gradlew.bat' :video-player:compileDebugKotlin`

Expected: BUILD SUCCESSFUL

---

### Task 4: Wire sliderFocusRequester in VideoPlayScreen

**Covers:** [S1, S2]

**Files:**
- Modify: `app/src/main/java/com/lanlinju/animius/presentation/screen/videoplayer/VideoPlayerScreen.kt:200-245`

- [ ] **Step 1: Create sliderFocusRequester and pass to both components**

```kotlin
// Video player composable
val sliderFocusRequester = remember { FocusRequester() }

VideoPlayer(
    url = video.url,
    videoPosition = video.lastPlayPosition,
    playerState = playerState,
    headers = video.headers,
    onBackPress = { handleBackPress(playerState, onBackClick, view, activity) },
    modifier = Modifier
        .focusable()
        .defaultRemoteControlHandler(
            playerState = playerState,
            onNextClick = { viewModel.playNextEpisode(playerState.player.currentPosition) },
            onSliderFocusRequest = { sliderFocusRequester.requestFocus() }
        )
) {
    // ... existing code ...
    VideoPlayerControl(
        // ... existing params ...
        modifier = if (isAndroidTV) Modifier.focusRequester(controlFocusRequester) else Modifier,
        sliderFocusRequester = sliderFocusRequester
    )
```

- [ ] **Step 2: Verify compilation**

Run: `$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'; & 'E:\Github\Anime\Animius\gradlew.bat' :app:compileDebugKotlin`

Expected: BUILD SUCCESSFUL

---

### Task 5: Update player control button focus style (scale + background)

**Covers:** [S5]

**Files:**
- Modify: `video-player/src/main/java/com/lanlinju/videoplayer/VideoPlayerControl.kt:301-315`

- [ ] **Step 1: Replace tvFocusBorder with scale + background style**

Remove the `tvFocusBorder` modifier. Update `AdaptiveIconButton` to use scale + background:

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
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1f,
        animationSpec = tween()
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(
                color = if (isFocused) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                else Color.Transparent
            )
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

Add imports:
```kotlin
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
```

- [ ] **Step 2: Remove tvFocusBorder modifier and FocusBorderShape**

Delete the `tvFocusBorder` function and `FocusBorderShape` constant.

- [ ] **Step 3: Verify compilation**

Run: `$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'; & 'E:\Github\Anime\Animius\gradlew.bat' :video-player:compileDebugKotlin`

Expected: BUILD SUCCESSFUL

---

### Task 6: Update Slider thumb focus style

**Covers:** [S2]

**Files:**
- Modify: `video-player/src/main/java/com/lanlinju/videoplayer/component/Slider.kt:53-92`

- [ ] **Step 1: Remove border from Slider, animate thumb size on focus**

Replace the border modifier with thumb size animation:

```kotlin
val thumbSize by animateDpAsState(
    targetValue = if (isFocused) 10.dp else 6.dp,
    animationSpec = tween()
)
val thumbColor by animateColorAsState(
    targetValue = if (isFocused) MaterialTheme.colorScheme.primary else color,
    animationSpec = tween()
)
```

Remove the `.border(...)` modifier. Update the thumb Box:

```kotlin
// thumb
Box(
    modifier = Modifier
        .clip(CircleShape)
        .align(
            BiasAlignment(
                horizontalBias = (value * 2) - 1f,
                verticalBias = 0f
            )
        )
        .size(thumbSize)
        .background(thumbColor)
)
```

Add import: `import androidx.compose.animation.animateColorAsState`

- [ ] **Step 2: Verify compilation**

Run: `$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'; & 'E:\Github\Anime\Animius\gradlew.bat' :video-player:compileDebugKotlin`

Expected: BUILD SUCCESSFUL

---

### Task 7: Remove focusable from detail episode buttons

**Covers:** [S3]

**Files:**
- Modify: `app/src/main/java/com/lanlinju/animius/presentation/screen/detail/AnimeDetailScreen.kt:624-656`

- [ ] **Step 1: Remove focusable() and focus border from episode buttons**

Remove the `focusable()`, `onFocusChanged`, and `border` modifiers from the episode button. The button should only use `clickable`:

```kotlin
FilledTonalButton(
    onClick = { onEpisodeClick(index, episode) },
    colors = ButtonDefaults.filledTonalButtonColors(containerColor = color.copy(0.5f)),
    modifier = if (isAndroidTV) {
        Modifier.focusRequester(focusRequester)
    } else Modifier
) {
    // ... text content ...
}
```

Remove: `isFocused` state, `onFocusChanged`, `border`, `interactionSource`, `hoverable`, `indication`, `focusable`.

- [ ] **Step 2: Verify compilation**

Run: `$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'; & 'E:\Github\Anime\Animius\gradlew.bat' :app:compileDebugKotlin`

Expected: BUILD SUCCESSFUL

---

### Task 8: Update home/detail focus style (border + alpha)

**Covers:** [S4]

**Files:**
- Modify: `app/src/main/java/com/lanlinju/animius/presentation/screen/search/SearchScreen.kt:211-219`

- [ ] **Step 1: Update SearchScreen focus border + alpha**

```kotlin
.onFocusChanged(onFocusChanged = { isFocused = it.isFocused })
.focusRequester(mediaFocusRequester)
.focusable()
.border(
    width = if (isFocused && isAndroidTV) 3.dp else 0.dp,
    color = if (isFocused && isAndroidTV) MaterialTheme.colorScheme.onSurface else Color.Transparent,
    shape = RoundedCornerShape(4.dp)
)
.graphicsLayer {
    alpha = if (isFocused && isAndroidTV) 1f else 0.7f
}
```

Add import: `import androidx.compose.ui.graphics.graphicsLayer`

- [ ] **Step 2: Verify compilation**

Run: `$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'; & 'E:\Github\Anime\Animius\gradlew.bat' :app:compileDebugKotlin`

Expected: BUILD SUCCESSFUL

---

### Task 9: Full build and commit

**Covers:** All

- [ ] **Step 1: Run full build**

Run: `$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'; & 'E:\Github\Anime\Animius\gradlew.bat' assembleDebug`

Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Commit**

```bash
git add video-player/src/main/java/com/lanlinju/videoplayer/VideoPlayerControl.kt
git add video-player/src/main/java/com/lanlinju/videoplayer/component/Slider.kt
git add app/src/main/java/com/lanlinju/animius/presentation/screen/videoplayer/VideoPlayerScreen.kt
git add app/src/main/java/com/lanlinju/animius/presentation/screen/detail/AnimeDetailScreen.kt
git add app/src/main/java/com/lanlinju/animius/presentation/screen/search/SearchScreen.kt
git commit -m "fix: improve TV remote control behavior and focus styles

- Fix D-pad left/right: only seek when slider focused, focus slider when UI hidden
- Player buttons: scale 1.05x + onSurface 20% alpha background on focus
- Slider thumb: animate size (6dp→10dp) and color on focus, no border
- Detail page episodes: OK directly plays, no focus border
- Search results: border + alpha brightness on focus"
```
