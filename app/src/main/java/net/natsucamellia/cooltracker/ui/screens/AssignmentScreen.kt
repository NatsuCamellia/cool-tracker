package net.natsucamellia.cooltracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun AssignmentScreen() {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Ongoing",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold
        )
        CourseCard(modifier = Modifier.padding(vertical = 16.dp))
        CourseCard(modifier = Modifier.padding(vertical = 16.dp))
        CourseCard(modifier = Modifier.padding(vertical = 16.dp))
        CourseCard(modifier = Modifier.padding(vertical = 16.dp))

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Closed",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold
        )
        CourseCard(modifier = Modifier.padding(vertical = 16.dp))
        CourseCard(modifier = Modifier.padding(vertical = 16.dp))
    }
}

@Composable
fun CourseCard(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            "系統軟體設計與實作特論\nAdvanced Topics in Software Systems Design and Implementation",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        AssignmentCard(Modifier.padding(vertical = 8.dp))
        AssignmentCard(Modifier.padding(vertical = 8.dp))
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AssignmentCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "HW4",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Default.TaskAlt,
                    contentDescription = "Status: Completed", // Add content description
                    modifier = Modifier
                        .padding(start = 8.dp),
                    tint = MaterialTheme.colorScheme.primary // Use theme color
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.HourglassTop, // Or your preferred hourglass icon
                    contentDescription = "Time remaining",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.secondary // Use theme color
                )
                Text(
                    text = "3d 6h",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearWavyProgressIndicator(
            progress = { 0.6f },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            Text(
                "Sep 30 01:04",
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "Oct 14 23:59",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AssignmentScreenPreview() {
    AssignmentScreen()
}