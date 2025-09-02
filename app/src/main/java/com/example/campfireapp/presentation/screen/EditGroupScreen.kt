package com.example.campfireapp.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.campfireapp.data.model.Group
import com.example.campfireapp.data.model.SelfDestructRule
import com.example.campfireapp.presentation.viewmodel.GroupViewModel

/**
 * Screen for editing group settings and self-destruct rules
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGroupScreen(
    groupId: String,
    onNavigateBack: () -> Unit,
    groupViewModel: GroupViewModel = hiltViewModel()
) {
    var group by remember { mutableStateOf<Group?>(null) }
    var groupName by remember { mutableStateOf("") }
    var groupDescription by remember { mutableStateOf("") }
    var maxMessages by remember { mutableStateOf("") }
    var durationHours by remember { mutableStateOf("") }
    var inactivityHours by remember { mutableStateOf("") }
    
    val uiState by groupViewModel.uiState.collectAsState()
    
    // Load group details and populate fields
    LaunchedEffect(Unit) {
        groupViewModel.selectedGroup.collect { selectedGroup ->
            if (selectedGroup?.id == groupId) {
                group = selectedGroup
                groupName = selectedGroup.name
                groupDescription = selectedGroup.description
                selectedGroup.selfDestructRule.let { rule ->
                    maxMessages = rule.maxMessages?.toString() ?: ""
                    durationHours = rule.durationMinutes?.let { (it / 60).toString() } ?: ""
                    inactivityHours = rule.inactivityTimeoutMinutes?.let { (it / 60).toString() } ?: ""
                }
            }
        }
    }
    
    // Navigate back on successful update
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage?.contains("updated") == true) {
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Group") },
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
                text = "Update conditions for automatic group deletion (leave empty for no limit)",
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
                value = inactivityHours,
                onValueChange = { if (it.all { char -> char.isDigit() }) inactivityHours = it },
                label = { Text("Inactivity Timeout (hours)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                placeholder = { Text("e.g., 2") }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Update button
            Button(
                onClick = {
                    group?.let { currentGroup ->
                        val selfDestructRule = SelfDestructRule(
                            maxMessages = maxMessages.toIntOrNull(),
                            durationMinutes = durationHours.toLongOrNull()?.times(60),
                            inactivityTimeoutMinutes = inactivityHours.toLongOrNull()?.times(60)
                        )
                        
                        val updatedGroup = currentGroup.copy(
                            name = groupName,
                            description = groupDescription,
                            selfDestructRule = selfDestructRule
                        )
                        
                        groupViewModel.updateGroup(updatedGroup)
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
                    Text("Update Group")
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
