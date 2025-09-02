package com.example.campfireapp.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.campfireapp.util.DemoCredentials
import com.example.campfireapp.util.DummyDataSeeder
import com.example.campfireapp.presentation.viewmodel.UserViewModel
import kotlinx.coroutines.launch

/**
 * Demo screen for quick testing with dummy data
 * Shows demo login and seed data options
 */
@Composable
fun DemoScreen(
    onNavigateToLogin: () -> Unit,
    userViewModel: UserViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    var isSeeding by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🔥 Campfire Demo",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Quick testing options",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Demo Account",
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Email: ${DemoCredentials.DEMO_EMAIL}\nPassword: ${DemoCredentials.DEMO_PASSWORD}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Button(
                    onClick = {
                        userViewModel.loginUser(
                            DemoCredentials.DEMO_EMAIL,
                            DemoCredentials.DEMO_PASSWORD
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Login with Demo Account")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Test Data",
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Create sample groups and messages for testing self-destruct features",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Button(
                    onClick = {
                        scope.launch {
                            isSeeding = true
                            // This would require dependency injection in a real implementation
                            // For demo purposes, show the button but note it needs proper setup
                            kotlinx.coroutines.delay(2000)
                            isSeeding = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSeeding
                ) {
                    if (isSeeding) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Creating Sample Data...")
                    } else {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Seed Test Data")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        TextButton(onClick = onNavigateToLogin) {
            Text("Continue to Login")
        }
    }
}
