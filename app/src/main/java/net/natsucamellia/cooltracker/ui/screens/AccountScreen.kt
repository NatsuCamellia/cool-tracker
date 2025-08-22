package net.natsucamellia.cooltracker.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import net.natsucamellia.cooltracker.R
import net.natsucamellia.cooltracker.ui.theme.ClipShapes
import net.natsucamellia.cooltracker.ui.widgets.SectionLabel

@Composable
fun AccountScreen(
    coolViewModel: CoolViewModel,
) {
    coolViewModel.accountUiState.collectAsState().value.let { accountUiState ->
        when (accountUiState) {
            is CoolViewModel.AccountUiState.Error -> ErrorScreen { coolViewModel.loadUserProfile() }
            is CoolViewModel.AccountUiState.Loading -> LoadingScreen()
            is CoolViewModel.AccountUiState.Success -> SuccessScreen(coolViewModel, accountUiState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuccessScreen(
    coolViewModel: CoolViewModel,
    accountUiState: CoolViewModel.AccountUiState.Success,
    modifier: Modifier = Modifier
) {
    val defaultAvatarUrl = "https://cool.ntu.edu.tw/images/messages/avatar-50.png"
    val profile = accountUiState.profile
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
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
            modifier = Modifier.clip(ClipShapes.outerRoundedCornerShape)
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
                modifier = modifier
                    .clip(ClipShapes.innerRoundedCornerShape)
                    .clickable(onClick = {})
            )
            // ID
            ListItem(
                headlineContent = { Text(stringResource(R.string.id)) },
                supportingContent = { Text("${profile.id}") },
                modifier = modifier
                    .clip(ClipShapes.innerRoundedCornerShape)
                    .clickable(onClick = {})
            )
            // Email
            ListItem(
                headlineContent = { Text(stringResource(R.string.email)) },
                supportingContent = { Text(profile.primaryEmail) },
                modifier = modifier
                    .clip(ClipShapes.innerRoundedCornerShape)
                    .clickable(onClick = {})
            )
            // Bio
            ListItem(
                headlineContent = { Text(stringResource(R.string.bio)) },
                supportingContent = { Text(profile.bio ?: "You don't have a bio yet.") },
                modifier = modifier
                    .clip(ClipShapes.innerRoundedCornerShape)
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
                modifier = modifier
                    .clip(ClipShapes.innerRoundedCornerShape)
                    .clickable(onClick = coolViewModel::logout)
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
            modifier = Modifier.clip(ClipShapes.outerRoundedCornerShape)
        ) {
            // Source Code
            val context = LocalContext.current
            ListItem(
                headlineContent = { Text(stringResource(R.string.source_code)) },
                supportingContent = { Text(stringResource(R.string.source_code_desc)) },
                modifier = modifier
                    .clip(ClipShapes.innerRoundedCornerShape)
                    .clickable(onClick = {
                        coolViewModel.openUrl(
                            context = context,
                            url = "https://github.com/natsucamellia/cool-tracker"
                        )
                    })
            )
            // Version
            ListItem(
                headlineContent = { Text(stringResource(R.string.version)) },
                supportingContent = { Text(coolViewModel.getAppVersion()) },
                modifier = modifier
                    .clip(ClipShapes.innerRoundedCornerShape)
                    .clickable(onClick = {})
            )
        }
    }
}