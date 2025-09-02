package com.example.campfireapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campfireapp.data.model.User
import com.example.campfireapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for User authentication and profile management
 * Handles login, registration, profile updates, and account deletion
 */
@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        // Observe current user changes
        viewModelScope.launch {
            userRepository.getCurrentUserFlow().collect { user ->
                _currentUser.value = user
            }
        }
    }

    fun registerUser(email: String, password: String, displayName: String) {
        if (!isValidInput(email, password, displayName)) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Please fill in all fields correctly"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            userRepository.registerUser(email, password, displayName)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true
                    )
                    _currentUser.value = user
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Registration failed"
                    )
                }
        }
    }

    fun loginUser(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Email and password are required"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            userRepository.loginUser(email, password)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true
                    )
                    _currentUser.value = user
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Login failed"
                    )
                }
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            userRepository.logoutUser()
                .onSuccess {
                    _uiState.value = UserUiState() // Reset to initial state
                    _currentUser.value = null
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Logout failed"
                    )
                }
        }
    }

    fun updateProfile(displayName: String, profileImageUrl: String = "") {
        val user = _currentUser.value ?: return
        
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        val updatedUser = user.copy(
            displayName = displayName,
            profileImageUrl = profileImageUrl
        )
        
        viewModelScope.launch {
            userRepository.updateUserProfile(updatedUser)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Profile updated successfully"
                    )
                    _currentUser.value = updatedUser
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Profile update failed"
                    )
                }
        }
    }

    fun deleteAccount() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            userRepository.deleteUserAccount()
                .onSuccess {
                    _uiState.value = UserUiState() // Reset to initial state
                    _currentUser.value = null
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Account deletion failed"
                    )
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    private fun isValidInput(email: String, password: String, displayName: String): Boolean {
        return email.isNotBlank() && 
               password.length >= 6 && 
               displayName.isNotBlank()
    }
}

/**
 * UI state for user-related screens
 */
data class UserUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
