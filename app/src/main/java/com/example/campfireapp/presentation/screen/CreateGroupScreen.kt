package com.example.campfireapp.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.campfireapp.presentation.viewmodel.GroupViewModel
import com.example.campfireapp.presentation.viewmodel.UserViewModel

/**
 * Screen for creating a new group with self-destruct rules
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    onNavigateBack: () -> Unit,
    onGroupCreated: (String) -> Unit,
    userViewModel: UserViewModel,
    groupViewModel: GroupViewModel
) {
    var groupName by remember { mutableStateOf("") }
    var groupDescription by remember { mutableStateOf("") }
    var maxMessages by remember { mutableStateOf("") }
    var durationHours by remember { mutableStateOf("") }
    var inactivityMinutes by remember { mutableStateOf("") }

    val currentUser by userViewModel.currentUser.collectAsState()
    val uiState by groupViewModel.uiState.collectAsState()
    
    // Navigate to chat when group is created
    LaunchedEffect(groupViewModel.selectedGroup.collectAsState().value) {
        groupViewModel.selectedGroup.value?.let { group ->
            onGroupCreated(group.id)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Group") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Group Info Section
            Text(
                text = "Group Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Group Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = groupDescription,
                onValueChange = { groupDescription = it },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Self-Destruct Rules Section
            Text(
                text = "🔥 Self-Destruct Rules",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Set conditions for automatic group deletion (leave empty for no limit)",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            OutlinedTextField(
                value = maxMessages,
                onValueChange = { if (it.all { char -> char.isDigit() }) maxMessages = it },
                label = { Text("Maximum Messages") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                placeholder = { Text("e.g., 100") }
            )
            
            OutlinedTextField(
                value = durationHours,
                onValueChange = { if (it.all { char -> char.isDigit() }) durationHours = it },
                label = { Text("Group Duration (hours)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                placeholder = { Text("e.g., 24") }
            )
            
            OutlinedTextField(
                value = inactivityMinutes,
                onValueChange = { if (it.all { char -> char.isDigit() }) inactivityMinutes = it },
                label = { Text("Inactivity Timeout (minutes)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                placeholder = { Text("e.g., 120") }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Create button
            Button(
                onClick = {
                    currentUser?.let { user ->
                        groupViewModel.createGroup(
                            name = groupName,
                            description = groupDescription,
                            createdBy = user.id,
                            maxMessages = maxMessages.toIntOrNull(),
                            durationMinutes = durationHours.toLongOrNull()?.times(60),
                            inactivityTimeoutMinutes = inactivityMinutes.toLongOrNull()
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && groupName.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create Group")
                }
            }
            
            // Error message
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
        }
    }
}
