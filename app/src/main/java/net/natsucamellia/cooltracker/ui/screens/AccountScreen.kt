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
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import net.natsucamellia.cooltracker.R
import net.natsucamellia.cooltracker.ui.widgets.SectionLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    uiState: CoolViewModel.CoolUiState.Success,
    logout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val defaultAvatarUrl = "https://cool.ntu.edu.tw/images/messages/avatar-50.png"
    var showLogoutDialog by remember { mutableStateOf(false) }
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
            SettingListItem(
                title = stringResource(R.string.name),
                description = profile.name,
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
            )
            // ID
            SettingListItem(
                title = stringResource(R.string.id),
                description = "${profile.id}"
            )
            // Email
            SettingListItem(
                title = stringResource(R.string.email),
                description = profile.primaryEmail
            )
            // Bio
            SettingListItem(
                title = stringResource(R.string.bio),
                description = profile.bio ?: "You don't have a bio yet."
            )
            // Logout
            SettingListItem(
                title = stringResource(R.string.logout),
                onClick = { showLogoutDialog = true }
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
            SettingListItem(
                title = stringResource(R.string.source_code),
                description = stringResource(R.string.source_code_desc),
                trailingContent = {
                    // Hint the user to open an external link
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = stringResource(R.string.source_code_desc)
                    )
                },
                onClick = { openGithub(context) }
            )
            // Version
            SettingListItem(
                title = stringResource(R.string.version),
                description = getAppVersion(context)
            )
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            confirmButton = {
                Button(
                    onClick = logout,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    )
                ) {
                    Text(stringResource(R.string.logout))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = stringResource(R.string.source_code_desc)
                )
            },
            title = {
                Text(stringResource(R.string.logout))
            },
            text = {
                Text(stringResource(R.string.logout_text))
            }
        )
    }
}

@Composable
private fun SettingListItem(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit) = {}
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = description?.let { { Text(description) } },
        trailingContent = trailingContent,
        modifier = modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .clickable(onClick = onClick)
    )
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