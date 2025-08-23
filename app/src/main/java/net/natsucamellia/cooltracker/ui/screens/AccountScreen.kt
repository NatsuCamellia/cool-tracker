package net.natsucamellia.cooltracker.ui.screens

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import net.natsucamellia.cooltracker.R
import net.natsucamellia.cooltracker.ui.widgets.SectionLabel

@Composable
fun AccountScreen(
    uiState: CoolViewModel.CoolUiState.Success,
    logout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val defaultAvatarUrl = "https://cool.ntu.edu.tw/images/messages/avatar-50.png"
    val profile = uiState.profile
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Account
        SectionLabel(
            text = stringResource(R.string.account),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(Modifier.height(8.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.clip(MaterialTheme.shapes.large)
        ) {
            // Name and avatar
            ListItem(
                headlineContent = { Text(stringResource(R.string.name)) },
                supportingContent = { Text(profile.name) },
                trailingContent = {
                    AsyncImage(
                        model = profile.avatarUrl ?: defaultAvatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .clip(CircleShape)
                            .border(
                                2.dp,
                                MaterialTheme.colorScheme.secondaryContainer,
                                CircleShape
                            )
                    )
                },
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraSmall)
                    .clickable(onClick = {})
            )
            // ID
            ListItem(
                headlineContent = { Text(stringResource(R.string.id)) },
                supportingContent = { Text("${profile.id}") },
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraSmall)
                    .clickable(onClick = {})
            )
            // Email
            ListItem(
                headlineContent = { Text(stringResource(R.string.email)) },
                supportingContent = { Text(profile.primaryEmail) },
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraSmall)
                    .clickable(onClick = {})
            )
            // Bio
            ListItem(
                headlineContent = { Text(stringResource(R.string.bio)) },
                supportingContent = { Text(profile.bio ?: "You don't have a bio yet.") },
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraSmall)
                    .clickable(onClick = {})
            )
            // Logout
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.logout),
                        color = MaterialTheme.colorScheme.error
                    )
                },
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraSmall)
                    .clickable(onClick = logout)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        // About the app
        SectionLabel(
            text = stringResource(R.string.about),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(Modifier.height(8.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.clip(MaterialTheme.shapes.large)
        ) {
            // Source Code
            val context = LocalContext.current
            ListItem(
                headlineContent = { Text(stringResource(R.string.source_code)) },
                supportingContent = { Text(stringResource(R.string.source_code_desc)) },
                trailingContent = {
                    // Hint the user to open an external link
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = stringResource(R.string.source_code_desc)
                    )
                },
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraSmall)
                    .clickable(onClick = { openGithub(context) })
            )
            // Version
            ListItem(
                headlineContent = { Text(stringResource(R.string.version)) },
                supportingContent = { Text(getAppVersion(context)) },
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraSmall)
                    .clickable(onClick = {})
            )
        }
    }
}

private fun openGithub(context: Context) {
    val url = "https://github.com/natsucamellia/cool-tracker"
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

private fun getAppVersion(context: Context): String {
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    return "${packageInfo.versionName} (${packageInfo.longVersionCode})"
}