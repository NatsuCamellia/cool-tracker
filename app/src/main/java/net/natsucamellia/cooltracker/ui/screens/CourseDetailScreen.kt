package net.natsucamellia.cooltracker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TwoRowsTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.natsucamellia.cooltracker.model.Course
import net.natsucamellia.cooltracker.model.englishName
import net.natsucamellia.cooltracker.ui.theme.ClipShapes
import net.natsucamellia.cooltracker.ui.widgets.AssignmentListItem

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CourseDetailScreen(
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
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            // Padding for the app bar
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .clip(ClipShapes.outerRoundedCornerShape)
            ) {
                course.assignments.forEach { assignment ->
                    AssignmentListItem(
                        assignment = assignment,
                        Modifier
                            .padding(vertical = 1.dp)
                            .clip(ClipShapes.innerRoundedCornerShape)
                    )
                }
            }
        }
    }
}