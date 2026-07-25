package com.lanlinju.animius.presentation.screen.favourite

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.hilt.navigation.compose.hiltViewModel
import com.lanlinju.animius.R
import com.lanlinju.animius.presentation.component.LoadingIndicator
import com.lanlinju.animius.presentation.component.MediaSmall
import com.lanlinju.animius.presentation.component.SourceBadge
import com.lanlinju.animius.presentation.component.StateHandler
import com.lanlinju.animius.util.SourceMode
import com.lanlinju.animius.util.focus.rememberIsFocused
import com.lanlinju.animius.util.isWideScreen
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouriteScreen(
    onNavigateToAnimeDetail: (detailUrl: String, mode: SourceMode) -> Unit,
) {
    val favouriteViewModel: FavouriteViewModel = hiltViewModel()
    val availableDataList = favouriteViewModel.favouriteList.collectAsState()

    StateHandler(state = availableDataList.value, onLoading = {
        LoadingIndicator()
    }, onFailure = {}) { resource ->
        var isDeleteMode by remember { mutableStateOf(false) }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.my_favourite),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    },
                    actions = {
                        val (deleteFocused, deleteModifier) = rememberIsFocused()
                        TextButton(
                            onClick = { isDeleteMode = !isDeleteMode },
                            modifier = deleteModifier,
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = if (deleteFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (deleteFocused) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(
                                text = if (isDeleteMode) stringResource(id = R.string.cancel)
                                else stringResource(id = R.string.delete)
                            )
                        }
                    }
                )
            },
            contentWindowInsets = WindowInsets.systemBars.exclude(WindowInsets.navigationBars)
        ) { paddingValues ->
            if (isDeleteMode) {
                BackHandler { isDeleteMode = false }
            }

            val context = LocalContext.current
            val gridFocusRequester = remember { FocusRequester() }
            var initialFocusDone by remember { mutableStateOf(false) }

            // After deletion, return focus to the grid instead of NavigationBar
            LaunchedEffect(resource.data?.size) {
                if (resource.data != null && initialFocusDone) {
                    runCatching { gridFocusRequester.requestFocus() }
                }
                if (resource.data != null) initialFocusDone = true
            }

            LazyVerticalGrid(
                modifier = Modifier
                    .padding(paddingValues)
                    .focusGroup()
                    .focusRequester(gridFocusRequester),
                columns = if (isWideScreen(context)) GridCells.Adaptive(dimensionResource(R.dimen.min_media_card_width)) else GridCells.Fixed(
                    3
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                resource.data?.let { favouriteList ->
                    items(favouriteList) { anime ->

                        var expanded by remember { mutableStateOf(false) }
                        var longPressConsumed by remember { mutableStateOf(false) }
                        var touchPressed by remember { mutableStateOf(false) }
                        val haptic = LocalHapticFeedback.current
                        val longPressTimeout = LocalViewConfiguration.current.longPressTimeoutMillis

                        LaunchedEffect(touchPressed) {
                            if (touchPressed) {
                                delay(longPressTimeout)
                                longPressConsumed = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                expanded = true
                                touchPressed = false
                            }
                        }

                        Box {
                            SourceBadge(
                                text = anime.sourceMode.name,
                                isAlignmentStart = false,
                                style = MaterialTheme.typography.bodySmall
                            ) {
                                MediaSmall(
                                    image = anime.imgUrl,
                                    label = anime.title,
                                    onClick = {
                                        if (isDeleteMode) {
                                            favouriteViewModel.removeFavourite(anime.detailUrl)
                                        } else if (!longPressConsumed) {
                                            onNavigateToAnimeDetail(anime.detailUrl, anime.sourceMode)
                                        }
                                        longPressConsumed = false
                                    },
                                    modifier = Modifier
                                        .onPreviewKeyEvent { event ->
                                            when {
                                                event.key == Key.Menu && event.type == KeyEventType.KeyDown -> {
                                                    if (expanded) expanded = false else expanded = true
                                                    true
                                                }
                                                else -> false
                                            }
                                        }
                                        .pointerInput(Unit) {
                                            awaitEachGesture {
                                                awaitFirstDown(requireUnconsumed = false)
                                                touchPressed = true
                                                while (touchPressed) {
                                                    val event = awaitPointerEvent()
                                                    if (event.changes.all { !it.pressed }) {
                                                        touchPressed = false
                                                    }
                                                }
                                            }
                                        }
                                )
                            }

                            // Delete mode: trash icon overlay centered on image
                            AnimatedVisibility(
                                visible = isDeleteMode,
                                enter = fadeIn(),
                                exit = fadeOut(),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(0.7f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Delete,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                offset = DpOffset(x = 40.dp, y = 0.dp),
                            ) {

                                val (focused, focusModifier) = rememberIsFocused()
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(id = R.string.delete),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (focused) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        expanded = false
                                        favouriteViewModel.removeFavourite(anime.detailUrl)
                                    },
                                    modifier = focusModifier.then(
                                        if (focused) Modifier.background(MaterialTheme.colorScheme.primary)
                                        else Modifier
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
