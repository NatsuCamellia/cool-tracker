package net.natsucamellia.cooltracker.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.School
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationItem(val filledIcon: ImageVector, val outlinedIcon: ImageVector, val title: String) {
    data object Courses : NavigationItem(Icons.Filled.School,
        Icons.Outlined.School, "Courses")
    data object Assignments : NavigationItem(Icons.AutoMirrored.Filled.Assignment,
        Icons.AutoMirrored.Outlined.Assignment, "Assignments")
    data object Account : NavigationItem(Icons.Filled.AccountCircle,
        Icons.Outlined.AccountCircle, "Account")
}