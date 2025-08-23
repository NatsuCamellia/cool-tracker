package net.natsucamellia.cooltracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TwoRowsTopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.window.core.layout.WindowSizeClass
import net.natsucamellia.cooltracker.R
import net.natsucamellia.cooltracker.model.Course
import net.natsucamellia.cooltracker.model.chineseName
import net.natsucamellia.cooltracker.model.englishName
import net.natsucamellia.cooltracker.ui.widgets.AssignmentListItem
import net.natsucamellia.cooltracker.ui.widgets.SectionLabel

@Composable
fun CourseScreen(
    uiState: CoolViewModel.CoolUiState.Success,
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
) {
    val splitScreen =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    if (splitScreen) {
        var selectedCourse by remember { mutableStateOf<Course?>(null) }
        Row(modifier = modifier.padding(horizontal = 16.dp)) {
            // Left for course list
            CourseListScreen(
                uiState = uiState,
                onCourseClick = { selectedCourse = it },
                modifier = Modifier.weight(4f)
            )
            // and right for course detail
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(5f)
            ) {
                val course = selectedCourse
                if (course != null) {
                    CourseDetailScreen(
                        course = course,
                        modifier = Modifier
                            .padding(start = 16.dp)
                    )
                }
            }
        }
    } else {
        // The screen is not wide enough, show stacked screens
        val navController = rememberNavController()
        NavHost(navController, startDestination = "/", modifier = modifier) {
            // Show the course list
            composable("/") {
                CourseListScreen(
                    uiState = uiState,
                    onCourseClick = { navController.navigate("/${it.id}") },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            // Show the assignment list of a course
            composable(
                route = "/{courseId}",
                arguments = listOf(navArgument("courseId") { type = NavType.IntType })
            ) {
                val courseId = it.arguments?.getInt("courseId")
                val course = uiState.courses.find { course -> course.id == courseId }
                // Ideally this should never be null
                if (course != null) {
                    CourseDetailScreen(
                        course = course,
                        navigateUp = { navController.navigateUp() },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CourseListScreen(
    uiState: CoolViewModel.CoolUiState.Success,
    modifier: Modifier = Modifier,
    onCourseClick: (Course) -> Unit = {}
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        // Currently, this app only shows ongoing courses.
        SectionLabel(
            text = stringResource(R.string.ongoing),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(Modifier.height(8.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
        ) {
            uiState.courses.forEach {
                CourseListItem(
                    course = it,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.extraSmall)
                        .clickable(
                            onClick = { onCourseClick(it) }
                        )
                )
            }
        }
    }
}

@Composable
private fun CourseListItem(
    course: Course,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = {
            Text(
                text = course.chineseName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Text(
                text = course.englishName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CourseDetailScreen(
    course: Course,
    modifier: Modifier = Modifier,
    navigateUp: (() -> Unit)? = null
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        topBar = {
            TwoRowsTopAppBar(
                title = {
                    Text(
                        course.courseCode,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                subtitle = {
                    Text(
                        course.englishName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    if (navigateUp != null) {
                        IconButton(
                            onClick = navigateUp
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                scrollBehavior = scrollBehavior
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = contentColorFor(MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        Column(
            modifier = modifier.verticalScroll(rememberScrollState())
        ) {
            // Padding for the app bar
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .clip(MaterialTheme.shapes.large)
            ) {
                course.assignments.forEach { assignment ->
                    AssignmentListItem(
                        assignment = assignment,
                        Modifier
                            .padding(vertical = 1.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                    )
                }
            }
        }
    }
}