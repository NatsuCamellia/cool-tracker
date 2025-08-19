package net.natsucamellia.cooltracker.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun AccountScreen(
    coolViewModel: CoolViewModel,
) {
    coolViewModel.accountUiState.collectAsState().value.let { accountUiState ->
        when (accountUiState) {
            is CoolViewModel.AccountUiState.Error -> ErrorScreen { coolViewModel.loadUserProfile() }
            is CoolViewModel.AccountUiState.Loading -> LoadingScreen { coolViewModel.loadUserProfile() }
            is CoolViewModel.AccountUiState.Success -> SuccessScreen(coolViewModel, accountUiState)
        }
    }
}

@Composable
fun SuccessScreen(
    coolViewModel: CoolViewModel,
    accountUiState: CoolViewModel.AccountUiState.Success,
    modifier: Modifier = Modifier
) {
    val defaultAvatarUrl = "https://cool.ntu.edu.tw/images/messages/avatar-50.png"
    val profile = accountUiState.profile
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = coolViewModel::logout) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
            }
        }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            AsyncImage(
                model = profile.avatarUrl ?: defaultAvatarUrl,
                contentDescription = "Avatar",
                modifier = Modifier
                    .clip(CircleShape)
                    .size(128.dp)
                    .border(2.dp, MaterialTheme.colorScheme.secondaryContainer, CircleShape)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "#${profile.id}")
            Text(text = profile.name, style = MaterialTheme.typography.titleLarge)
            if (profile.bio != null) {
                Text(text = profile.bio, fontStyle = FontStyle.Italic)
            }
        }
    }
}