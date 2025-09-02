package com.example.campfireapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.campfireapp.presentation.screen.*
import com.example.campfireapp.presentation.viewmodel.UserViewModel
import com.example.campfireapp.presentation.viewmodel.GroupViewModel
import com.example.campfireapp.presentation.viewmodel.MessageViewModel

/**
 * Navigation graph for the Campfire app
 * Handles navigation between all screens with proper ViewModel sharing
 */
@Composable
fun CampfireNavGraph(
    navController: NavHostController
) {
    // Create shared ViewModels at the navigation level
    val userViewModel: UserViewModel = hiltViewModel()
    val groupViewModel: GroupViewModel = hiltViewModel()
    val messageViewModel: MessageViewModel = hiltViewModel()

    val currentUser by userViewModel.currentUser.collectAsState()
    val isAuthenticated = currentUser != null

    NavHost(
        navController = navController,
        startDestination = if (isAuthenticated) CampfireRoutes.GROUP_LIST else CampfireRoutes.LOGIN
    ) {
        // Authentication screens
        composable(CampfireRoutes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(CampfireRoutes.REGISTER)
                },
                onLoginSuccess = {
                    navController.navigate(CampfireRoutes.GROUP_LIST) {
                        popUpTo(CampfireRoutes.LOGIN) { inclusive = true }
                    }
                },
                userViewModel = userViewModel
            )
        }

        composable(CampfireRoutes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(CampfireRoutes.GROUP_LIST) {
                        popUpTo(CampfireRoutes.REGISTER) { inclusive = true }
                    }
                },
                userViewModel = userViewModel
            )
        }

        // Main app screens - pass shared ViewModels
        composable(CampfireRoutes.GROUP_LIST) {
            GroupListScreen(
                onNavigateToCreateGroup = {
                    navController.navigate(CampfireRoutes.CREATE_GROUP)
                },
                onNavigateToChat = { groupId ->
                    navController.navigate(CampfireRoutes.chat(groupId))
                },
                onNavigateToProfile = {
                    navController.navigate(CampfireRoutes.PROFILE)
                },
                onNavigateToGroupDetail = { groupId ->
                    navController.navigate(CampfireRoutes.groupDetail(groupId))
                },
                userViewModel = userViewModel,
                groupViewModel = groupViewModel
            )
        }

        composable(CampfireRoutes.CREATE_GROUP) {
            CreateGroupScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onGroupCreated = { groupId ->
                    navController.navigate(CampfireRoutes.chat(groupId)) {
                        popUpTo(CampfireRoutes.GROUP_LIST)
                    }
                },
                userViewModel = userViewModel,
                groupViewModel = groupViewModel
            )
        }

        composable(
            CampfireRoutes.GROUP_DETAIL,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            GroupDetailScreen(
                groupId = groupId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { editGroupId ->
                    navController.navigate(CampfireRoutes.editGroup(editGroupId))
                },
                onNavigateToChat = { chatGroupId ->
                    navController.navigate(CampfireRoutes.chat(chatGroupId))
                },
                userViewModel = userViewModel,
                groupViewModel = groupViewModel
            )
        }

        composable(
            CampfireRoutes.EDIT_GROUP,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            EditGroupScreen(
                groupId = groupId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                groupViewModel = groupViewModel
            )
        }

        composable(
            CampfireRoutes.CHAT,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            ChatScreen(
                groupId = groupId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToGroupDetail = { detailGroupId ->
                    navController.navigate(CampfireRoutes.groupDetail(detailGroupId))
                },
                userViewModel = userViewModel,
                messageViewModel = messageViewModel
            )
        }

        composable(CampfireRoutes.PROFILE) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate(CampfireRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                userViewModel = userViewModel
            )
        }
    }
}
