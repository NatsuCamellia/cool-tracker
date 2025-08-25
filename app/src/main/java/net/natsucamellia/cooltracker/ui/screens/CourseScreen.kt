package net.natsucamellia.cooltracker.ui.screens

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
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
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import net.natsucamellia.cooltracker.R
import net.natsucamellia.cooltracker.model.Course
import net.natsucamellia.cooltracker.model.CourseWithAssignments
import net.natsucamellia.cooltracker.model.chineseName
import net.natsucamellia.cooltracker.model.englishName
import net.natsucamellia.cooltracker.ui.widgets.AssignmentListItem
import net.natsucamellia.cooltracker.ui.widgets.SectionLabel

@Composable
fun TwoPaneCourseView(
    coursesWithAssignments: List<CourseWithAssignments>,
    onCourseClick: (Course) -> Unit,
    modifier: Modifier = Modifier,
    courseId: Int? = null
) {
    val selectedCourse = coursesWithAssignments.find { it.course.id == courseId }

    Row(modifier = modifier) {
        // Left for course detail
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(5f)
        ) {
            val course = selectedCourse
            if (course != null) {
                CourseDetailScreen(
                    courseWithAssignments = course,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                )
            }
        }
        // and right for course list
        Surface(
            color = MaterialTheme.colorScheme.surfaceBright,
            modifier = Modifier
                .weight(4f)
                .clip(MaterialTheme.shapes.extraLarge)
        ) {
            CourseListScreen(
                coursesWithAssignments = coursesWithAssignments,
                onCourseClick = onCourseClick,
                modifier = Modifier
                    .padding(16.dp)
                    .clip(MaterialTheme.shapes.large)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CourseListScreen(
    coursesWithAssignments: List<CourseWithAssignments>,
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
            coursesWithAssignments.forEach {
                CourseListItem(
                    course = it.course,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.extraSmall)
                        .clickable(
                            onClick = { onCourseClick(it.course) }
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
fun CourseDetailScreen(
    courseWithAssignments: CourseWithAssignments,
    modifier: Modifier = Modifier,
    navigateUp: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val course = courseWithAssignments.course
    val assignments = courseWithAssignments.assignments
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        course.chineseName,
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
                actions = {
                    IconButton(
                        onClick = { openCourseInBrowser(context = context, courseId = course.id) }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                            contentDescription = "Open in browser"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = contentColorFor(MaterialTheme.colorScheme.surfaceContainer)
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
                assignments.forEach { assignment ->
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

private fun openCourseInBrowser(context: Context, courseId: Int) {
    val url = "https://cool.ntu.edu.tw/courses/$courseId"
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}