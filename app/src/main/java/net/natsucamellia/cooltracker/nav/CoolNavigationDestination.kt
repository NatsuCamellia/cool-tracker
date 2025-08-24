package net.natsucamellia.cooltracker.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.School
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import net.natsucamellia.cooltracker.R

sealed interface Route {
    @Serializable
    object Courses : Route

    @Serializable
    object Assignments : Route

    @Serializable
    object Account : Route
}

sealed class CoolNavigationDestination(
    /** The icon to show when the item is selected */
    val selectedIcon: ImageVector,
    /** The icon to show when the item is not selected */
    val unselectedIcon: ImageVector, val titleResId: Int, val route: Route
) {
    data object Courses : CoolNavigationDestination(
        selectedIcon = Icons.Filled.School,
        unselectedIcon = Icons.Outlined.School,
        titleResId = R.string.courses,
        route = Route.Courses
    )

    data object Home : CoolNavigationDestination(
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        titleResId = R.string.home,
        route = Route.Assignments
    )

    data object Account : CoolNavigationDestination(
        selectedIcon = Icons.Filled.AccountCircle,
        unselectedIcon = Icons.Outlined.AccountCircle,
        titleResId = R.string.account,
        route = Route.Account
    )
}

val TOP_LEVEL_DESTINATIONS = listOf(
    CoolNavigationDestination.Courses,
    CoolNavigationDestination.Home,
    CoolNavigationDestination.Account
)