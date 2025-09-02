package com.example.campfireapp.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.campfireapp.data.model.Group
import com.example.campfireapp.presentation.viewmodel.GroupViewModel
import com.example.campfireapp.presentation.viewmodel.UserViewModel

/**
 * Main screen showing list of user's groups
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupListScreen(
    onNavigateToCreateGroup: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToGroupDetail: (String) -> Unit,
    userViewModel: UserViewModel,
    groupViewModel: GroupViewModel
) {
    val currentUser by userViewModel.currentUser.collectAsState()
    val userGroups by groupViewModel.userGroups.collectAsState()
    
    // Initialize group data when user is available
    LaunchedEffect(currentUser) {
        val userId = currentUser?.id
        if (userId != null) {
            groupViewModel.setCurrentUser(userId)
        }
    }
    
    // Check for self-destruct periodically
    LaunchedEffect(Unit) {
        groupViewModel.checkSelfDestruct()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔥 Campfire Groups") },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateGroup
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Group")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (userGroups.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No groups yet",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Create your first self-destructing group chat!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                // Groups list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(userGroups) { group ->
                        GroupCard(
                            group = group,
                            onNavigateToChat = onNavigateToChat,
                            onNavigateToDetail = onNavigateToGroupDetail
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupCard(
    group: Group,
    onNavigateToChat: (String) -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    Card(
        onClick = { onNavigateToChat(group.id) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (group.description.isNotEmpty()) {
                        Text(
                            text = group.description,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                TextButton(
                    onClick = { onNavigateToDetail(group.id) }
                ) {
                    Text("Details")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${group.memberIds.size} members",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "${group.messageCount} messages",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Self-destruct rule indicator
            SelfDestructRuleIndicator(group = group)
        }
    }
}

@Composable
private fun SelfDestructRuleIndicator(group: Group) {
    val rule = group.selfDestructRule
    val indicators = mutableListOf<String>()

    if (rule.maxMessages != null) {
        indicators.add("Max: ${rule.maxMessages} msg")
    }

    if (rule.durationMinutes != null) {
        val duration = rule.durationMinutes
        val hours = duration / 60
        val minutes = duration % 60
        indicators.add(
            when {
                hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                hours > 0 -> "${hours}h"
                else -> "${minutes}m"
            }
        )
    }

    if (rule.inactivityTimeoutMinutes != null) {
        val timeout = rule.inactivityTimeoutMinutes
        val hours = timeout / 60
        val minutes = timeout % 60
        indicators.add(
            "Timeout: " + when {
                hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                hours > 0 -> "${hours}h"
                else -> "${minutes}m"
            }
        )
    }

    if (indicators.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "🔥 ${indicators.joinToString(" • ")}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
