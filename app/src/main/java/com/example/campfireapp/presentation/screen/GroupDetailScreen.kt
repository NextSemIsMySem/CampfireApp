package com.example.campfireapp.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Message
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
 * Screen showing detailed information about a group
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToChat: (String) -> Unit,
    userViewModel: UserViewModel = hiltViewModel(),
    groupViewModel: GroupViewModel = hiltViewModel()
) {
    var group by remember { mutableStateOf<Group?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val currentUser by userViewModel.currentUser.collectAsState()
    val uiState by groupViewModel.uiState.collectAsState()
    
    // Load group details
    LaunchedEffect(groupId) {
        // This would ideally be a function in GroupViewModel to get single group
        // For now, we'll use the selected group from the flow
        groupViewModel.selectGroup(group ?: return@LaunchedEffect)
    }
    
    // Listen to selected group changes
    LaunchedEffect(Unit) {
        groupViewModel.selectedGroup.collect { selectedGroup ->
            if (selectedGroup?.id == groupId) {
                group = selectedGroup
            }
        }
    }
    
    // Navigate back after successful deletion
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage?.contains("deleted") == true) {
            onNavigateBack()
        }
    }
    
    group?.let { currentGroup ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentGroup.name) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        // Show edit/delete options only for group creator
                        if (currentGroup.createdBy == currentUser?.id) {
                            IconButton(onClick = { onNavigateToEdit(groupId) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Group")
                            }
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Group")
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { onNavigateToChat(groupId) }
                ) {
                    Icon(Icons.Default.Message, contentDescription = "Open Chat")
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Group name and description
                Text(
                    text = currentGroup.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                if (currentGroup.description.isNotBlank()) {
                    Text(
                        text = currentGroup.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Group stats
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Group Statistics",
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Members:")
                            Text("${currentGroup.memberIds.size}")
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Messages:")
                            Text("${currentGroup.messageCount}")
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Created:")
                            Text(formatDate(currentGroup.createdAt))
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Last Activity:")
                            Text(formatDate(currentGroup.lastActivity))
                        }
                    }
                }
                
                // Self-destruct rules
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🔥 Self-Destruct Rules",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        currentGroup.selfDestructRule.let { rule ->
                            rule.maxMessages?.let { max ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Max Messages:")
                                    Text("$max messages")
                                }
                            }
                            
                            rule.durationMinutes?.let { duration ->
                                val hours = duration / 60
                                val minutes = duration % 60
                                val timeText = when {
                                    hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                                    hours > 0 -> "${hours}h"
                                    else -> "${minutes}m"
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Duration:")
                                    Text(timeText)
                                }
                            }
                            
                            rule.inactivityTimeoutMinutes?.let { timeout ->
                                val hours = timeout / 60
                                val minutes = timeout % 60
                                val timeText = when {
                                    hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                                    hours > 0 -> "${hours}h"
                                    else -> "${minutes}m"
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Inactivity Timeout:")
                                    Text(timeText)
                                }
                            }
                            
                            if (rule.maxMessages == null && rule.durationMinutes == null && rule.inactivityTimeoutMinutes == null) {
                                Text(
                                    text = "No self-destruct rules set",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // Error/Success messages
                uiState.errorMessage?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                
                uiState.successMessage?.let { message ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
        
        // Delete confirmation dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Group") },
                text = { Text("Are you sure you want to delete this group? This action cannot be undone and all messages will be lost.") },
                confirmButton = {
                    Button(
                        onClick = {
                            groupViewModel.deleteGroup(groupId)
                            showDeleteDialog = false
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> "${diff / 86400_000}d ago"
    }
}
