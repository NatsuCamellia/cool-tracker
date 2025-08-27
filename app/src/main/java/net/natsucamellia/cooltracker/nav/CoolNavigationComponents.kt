package net.natsucamellia.cooltracker.nav

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailDefaults
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.window.core.layout.WindowSizeClass

@Composable
fun CoolNavigationSuiteScaffold(
    currentDestination: NavDestination?,
    navigateToTopLevelDestination: (CoolNavigationDestination) -> Unit,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable () -> Unit,
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val useNavigationBar = windowSizeClass.isCompact()

    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = {
            if (useNavigationBar) {
                CoolNavigationBar(
                    currentDestination = currentDestination,
                    navigateToTopLevelDestination = navigateToTopLevelDestination
                )
            }
        },
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
        contentColor = contentColor,
        contentWindowInsets = contentWindowInsets,
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            // We put navigation rail and the row in the scaffold because we want the scaffold to
            // consume the status bar insets for us, which is more convenient.
            if (!useNavigationBar) {
                CoolNavigationRail(
                    currentDestination = currentDestination,
                    navigateToTopLevelDestination = navigateToTopLevelDestination,
                    windowInsets = NavigationRailDefaults.windowInsets
                )
            }
            content()
        }
    }
}

@Composable
fun CoolNavigationBar(
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier,
    navigateToTopLevelDestination: (CoolNavigationDestination) -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: Dp = NavigationBarDefaults.Elevation,
    windowInsets: WindowInsets = NavigationBarDefaults.windowInsets
) {
    NavigationBar(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        windowInsets = windowInsets,
    ) {
        TOP_LEVEL_DESTINATIONS.forEach { coolDestination ->
            val selected = currentDestination.hasRoute(coolDestination)
            NavigationBarItem(
                selected = selected,
                onClick = { navigateToTopLevelDestination(coolDestination) },
                icon = {
                    Icon(
                        if (selected) coolDestination.selectedIcon else coolDestination.unselectedIcon,
                        contentDescription = stringResource(coolDestination.titleResId)
                    )
                },
                label = {
                    Text(
                        text = stringResource(coolDestination.titleResId)
                    )
                }
            )
        }
    }
}

@Composable
fun CoolNavigationRail(
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier,
    navigateToTopLevelDestination: (CoolNavigationDestination) -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = contentColorFor(containerColor),
    header: @Composable (ColumnScope.() -> Unit)? = null,
    windowInsets: WindowInsets = NavigationRailDefaults.windowInsets,
) {
    NavigationRail(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        header = header,
        windowInsets = windowInsets,
    ) {
        TOP_LEVEL_DESTINATIONS.forEach { coolDestination ->
            val selected = currentDestination.hasRoute(coolDestination)
            NavigationRailItem(
                selected = selected,
                onClick = { navigateToTopLevelDestination(coolDestination) },
                icon = {
                    Icon(
                        if (selected) coolDestination.selectedIcon else coolDestination.unselectedIcon,
                        contentDescription = stringResource(coolDestination.titleResId)
                    )
                },
                label = {
                    Text(
                        text = stringResource(coolDestination.titleResId)
                    )
                }
            )
        }
    }
}

private fun WindowSizeClass.isCompact() =
    !isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

private fun NavDestination?.hasRoute(destination: CoolNavigationDestination): Boolean =
    this?.hasRoute(destination.route::class) ?: false