package com.example.campfireapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campfireapp.data.model.Message
import com.example.campfireapp.data.repository.GroupRepository
import com.example.campfireapp.data.repository.MessageRepository
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
 * ViewModel for Message management
 * Handles sending, editing, deleting, and displaying messages
 */
@HiltViewModel
class MessageViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val groupRepository: GroupRepository,
    private val selfDestructService: SelfDestructService
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessageUiState())
    val uiState: StateFlow<MessageUiState> = _uiState.asStateFlow()

    private val _currentGroupId = MutableStateFlow<String?>(null)
    val currentGroupId: StateFlow<String?> = _currentGroupId.asStateFlow()

    // Fix: Use stateIn() for proper lifecycle-aware Flow collection
    @OptIn(ExperimentalCoroutinesApi::class)
    val messages: StateFlow<List<Message>> = _currentGroupId
        .flatMapLatest { groupId ->
            if (groupId != null) {
                messageRepository.getGroupMessages(groupId)
            } else {
                emptyFlow()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Fix: Replace the problematic loadMessages function
    fun setCurrentGroup(groupId: String) {
        _currentGroupId.value = groupId
    }

    fun sendMessage(
        groupId: String,
        senderId: String,
        senderName: String,
        content: String
    ) {
        if (content.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Message cannot be empty"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        val message = Message(
            groupId = groupId,
            senderId = senderId,
            senderName = senderName,
            content = content.trim(),
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            messageRepository.sendMessage(message)
                .onSuccess { sentMessage ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    
                    // Update group's message count and last activity
                    groupRepository.incrementMessageCount(groupId)
                    groupRepository.updateLastActivity(groupId)
                    
                    // Check if group should be destroyed after this message
                    selfDestructService.checkGroup(groupId)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to send message"
                    )
                }
        }
    }

    fun editMessage(messageId: String, newContent: String) {
        if (newContent.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Message cannot be empty"
            )
            return
        }

        val currentMessage = messages.value.find { it.id == messageId } ?: return
        val updatedMessage = currentMessage.copy(content = newContent.trim())

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            messageRepository.updateMessage(updatedMessage)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Message updated"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to update message"
                    )
                }
        }
    }

    fun deleteMessage(messageId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            messageRepository.deleteMessage(messageId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Message deleted"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to delete message"
                    )
                }
        }
    }

    fun canEditMessage(message: Message, currentUserId: String): Boolean {
        return message.senderId == currentUserId
    }

    fun canDeleteMessage(message: Message, currentUserId: String): Boolean {
        return message.senderId == currentUserId
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    fun formatMessageTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60_000 -> "Just now" // Less than 1 minute
            diff < 3600_000 -> "${diff / 60_000}m ago" // Less than 1 hour
            diff < 86400_000 -> "${diff / 3600_000}h ago" // Less than 1 day
            else -> "${diff / 86400_000}d ago" // More than 1 day
        }
    }
}

/**
 * UI state for message-related screens
 */
data class MessageUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
