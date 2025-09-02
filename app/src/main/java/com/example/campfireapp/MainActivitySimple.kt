package com.example.campfireapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.campfireapp.presentation.navigation.CampfireNavGraph
import com.example.campfireapp.ui.theme.CampfireAppTheme

/**
 * Main activity for the Campfire app (Simplified version without Hilt)
 * Sets up navigation and theme
 * 
 * If you're having issues with Hilt setup, use this version instead
 */
class MainActivitySimple : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampfireAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    CampfireNavGraph(navController = navController)
                }
            }
        }
    }
}
