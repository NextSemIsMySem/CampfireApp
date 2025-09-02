package com.example.campfireapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campfireapp.data.model.Group
import com.example.campfireapp.data.model.SelfDestructRule
import com.example.campfireapp.data.repository.GroupRepository
import com.example.campfireapp.data.repository.SelfDestructService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

/**
 * ViewModel for Group management
 * Handles group creation, updates, deletion, and membership
 */
@HiltViewModel
class GroupViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val selfDestructService: SelfDestructService
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupUiState())
    val uiState: StateFlow<GroupUiState> = _uiState.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val userGroups: StateFlow<List<Group>> = _currentUserId
        .flatMapLatest { userId ->
            if (userId != null) {
                groupRepository.getUserGroups(userId)
            } else {
                emptyFlow()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
    // START: MODIFIED SECTION
    private val _selectedGroupId = MutableStateFlow<String?>(null)
    private val _createdGroupId = MutableStateFlow<String?>(null)
    val createdGroupId: StateFlow<String?> = _createdGroupId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedGroupDetails: StateFlow<Group?> = _selectedGroupId
        .flatMapLatest { groupId ->
            if (groupId != null) {
                groupRepository.getGroupFlow(groupId)
            } else {
                emptyFlow()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun loadGroupById(groupId: String) {
        _selectedGroupId.value = groupId
    }
    // END: MODIFIED SECTION

    fun setCurrentUser(userId: String) {
        _currentUserId.value = userId
    }

    fun createGroup(
        name: String,
        description: String,
        createdBy: String,
        maxMessages: Int? = null,
        durationMinutes: Long? = null,
        inactivityTimeoutMinutes: Long? = null
    ) {
        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Group name is required")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        val selfDestructRule = SelfDestructRule(
            maxMessages = maxMessages,
            durationMinutes = durationMinutes,
            inactivityTimeoutMinutes = inactivityTimeoutMinutes
        )

        val group = Group(
            name = name,
            description = description,
            createdBy = createdBy,
            memberIds = listOf(createdBy),
            selfDestructRule = selfDestructRule
        )

        viewModelScope.launch {
            groupRepository.createGroup(group)
                .onSuccess { createdGroup ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Group created successfully"
                    )
                    _createdGroupId.value = createdGroup.id
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to create group"
                    )
                }
        }
    }
    
    fun onGroupCreationHandled() {
        _createdGroupId.value = null
    }

    fun updateGroup(group: Group) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            groupRepository.updateGroup(group)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Group updated successfully"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to update group"
                    )
                }
        }
    }

    fun deleteGroup(groupId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            groupRepository.deleteGroup(groupId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Group deleted successfully"
                    )
                    _selectedGroupId.value = null
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to delete group"
                    )
                }
        }
    }
    
    // ... rest of the functions (joinGroup, leaveGroup, etc.) remain the same
    fun joinGroup(groupId: String, userId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            groupRepository.joinGroup(groupId, userId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Joined group successfully"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to join group"
                    )
                }
        }
    }

    fun leaveGroup(groupId: String, userId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            groupRepository.leaveGroup(groupId, userId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Left group successfully"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to leave group"
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

    fun checkSelfDestruct() {
        viewModelScope.launch {
            selfDestructService.checkAllGroups()
        }
    }
}

/**
 * UI state for group-related screens
 */
data class GroupUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
