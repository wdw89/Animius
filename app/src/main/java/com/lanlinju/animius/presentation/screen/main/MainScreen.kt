package com.lanlinju.animius.presentation.screen.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.lanlinju.animius.presentation.component.AdaptiveNavigationBar
import com.lanlinju.animius.presentation.component.NavigationBarPath
import com.lanlinju.animius.presentation.screen.favourite.FavouriteScreen
import com.lanlinju.animius.presentation.screen.home.HomeScreen
import com.lanlinju.animius.presentation.screen.week.VersionUpdateDialog
import com.lanlinju.animius.presentation.screen.week.WeekScreen
import com.lanlinju.animius.util.SourceMode
import com.lanlinju.animius.util.focus.rememberIsFocused
import com.lanlinju.animius.util.isWideScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    onNavigateToAnimeDetail: (detailUrl: String, mode: SourceMode) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToDownload: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToDanmakuSettings: () -> Unit,
) {
    val viewModel = hiltViewModel<MainViewModel>()
    val showVersionUpdateDialog by viewModel.showVersionUpdateDialog.collectAsState()
    var currentDestination by rememberSaveable { mutableStateOf(NavigationBarPath.Home.route) }
    val pagerState = rememberPagerState(initialPage = 1) { NavigationBarPath.entries.size }
    val scope = rememberCoroutineScope()
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler { showExitDialog = true }

    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val activity = context as? android.app.Activity

    // 在屏幕方向变化时更新 isWideScreen
    val isWideScreen = remember(configuration.orientation) { isWideScreen(context) }

    val navigationBar: @Composable () -> Unit = {
        AdaptiveNavigationBar(
            destinations = NavigationBarPath.entries,
            currentDestination = currentDestination,
            onNavigateToDestination = {
                currentDestination = NavigationBarPath.entries[it].route
                scope.launch { pagerState.scrollToPage(it) }
            },
            isWideScreen = isWideScreen,
        )
    }

    val pagerContent: @Composable (Modifier) -> Unit = { modifier ->
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = modifier
        ) { page ->
            when (page) {
                0 -> WeekScreen(
                    onNavigateToAnimeDetail = onNavigateToAnimeDetail,
                    onNavigateToSearch = onNavigateToSearch,
                    onNavigateToHistory = onNavigateToHistory,
                    onNavigateToDownload = onNavigateToDownload,
                    onNavigateToAppearance = onNavigateToAppearance,
                    onNavigateToDanmakuSettings = onNavigateToDanmakuSettings
                )

                1 -> HomeScreen(onNavigateToAnimeDetail)
                2 -> FavouriteScreen(onNavigateToAnimeDetail)
            }
        }
    }

    if (isWideScreen) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .displayCutoutPadding()
        ) {
            navigationBar()
            pagerContent(
                Modifier
                    .fillMaxHeight()
                    .weight(1f)
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            pagerContent(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            navigationBar()
        }
    }

    if (showVersionUpdateDialog) {
        VersionUpdateDialog(
            updateVersionName = viewModel.updateVersionName,
            updateDescription = viewModel.updateDescription,
            onDismissUpdateDialog = viewModel::dismissUpdateDialog,
            onDownloadUpdate = {
                viewModel.dismissUpdateDialog()
                viewModel.downloadUpdate(context)
            }
        )
    }

    if (showExitDialog) {
        val (confirmFocused, confirmModifier) = rememberIsFocused()
        val (cancelFocused, cancelModifier) = rememberIsFocused()
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("确认退出？") },
            confirmButton = {
                TextButton(
                    onClick = { activity?.finish() },
                    modifier = confirmModifier,
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (confirmFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (confirmFocused) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.primary
                    )
                ) { Text("确定") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExitDialog = false },
                    modifier = cancelModifier,
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (cancelFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (cancelFocused) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.primary
                    )
                ) { Text("取消") }
            }
        )
    }
}
