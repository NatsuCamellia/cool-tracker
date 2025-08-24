package net.natsucamellia.cooltracker.nav

import androidx.compose.animation.core.Animatable
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.window.core.layout.WindowSizeClass
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CoolNavigationWrapper(
    currentDestination: NavDestination?,
    navigateToTopLevelDestination: (CoolNavigationDestination) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val layoutType = when {
        adaptiveInfo.windowPosture.isTabletop -> NavigationSuiteType.NavigationBar
        adaptiveInfo.windowSizeClass.isCompact() -> NavigationSuiteType.NavigationBar
        else -> NavigationSuiteType.NavigationRail
    }
    val scaleSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val scaleAnimation = remember { Animatable(0f) }
    val alphaSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val alphaAnimation = remember { Animatable(0f) }
    LaunchedEffect(true) {
        delay(200)
        launch {
            scaleAnimation.animateTo(
                animationSpec = scaleSpec,
                targetValue = 1f
            )
        }
        launch {
            alphaAnimation.animateTo(
                animationSpec = alphaSpec,
                targetValue = 1f
            )
        }
    }
    NavigationSuiteScaffold(
        layoutType = layoutType,
        navigationSuiteItems = {
            TOP_LEVEL_DESTINATIONS.forEach { coolDestination ->
                item(
                    selected = currentDestination.hasRoute(coolDestination),
                    icon = {
                        Icon(
                            if (currentDestination.hasRoute(coolDestination)) coolDestination.selectedIcon else coolDestination.unselectedIcon,
                            contentDescription = stringResource(coolDestination.titleResId),
                            modifier = Modifier.scale(scaleAnimation.value)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(coolDestination.titleResId),
                            modifier = Modifier.alpha(alphaAnimation.value)
                        )
                    },
                    onClick = { navigateToTopLevelDestination(coolDestination) }
                )
            }
        },
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationRailContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            navigationRailContentColor = contentColorFor(MaterialTheme.colorScheme.surfaceContainer),
        ),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = contentColorFor(MaterialTheme.colorScheme.surfaceContainer),
        modifier = modifier
    ) {
        content()
    }
}

private fun WindowSizeClass.isCompact() =
    !isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

private fun NavDestination?.hasRoute(destination: CoolNavigationDestination): Boolean =
    this?.hasRoute(destination.route::class) ?: false