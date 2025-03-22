package com.adammusic.practicelog.ui.screens.practice.editsession

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adammusic.practicelog.data.model.Practice
import com.adammusic.practicelog.data.model.PracticeSession
import com.adammusic.practicelog.data.repository.PracticeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

sealed class EditSessionUiState {
    data object Loading : EditSessionUiState()
    data object NotFound : EditSessionUiState()
    data class Success(val practice: Practice, val session: PracticeSession) : EditSessionUiState()
}

@HiltViewModel
class EditSessionViewModel @Inject constructor(
    private val repository: PracticeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val practiceId: Int = checkNotNull(savedStateHandle["practiceId"]).toString().toInt()
    private val sessionId: Int = checkNotNull(savedStateHandle["sessionId"]).toString().toInt()

    private val _uiState = MutableStateFlow<EditSessionUiState>(EditSessionUiState.Loading)
    val uiState: StateFlow<EditSessionUiState> = _uiState.asStateFlow()

    init {
        loadPracticeAndSession()
    }

    private fun loadPracticeAndSession() {
        viewModelScope.launch {
            repository.getPracticeById(practiceId).collectLatest { practice ->
                if (practice == null) {
                    _uiState.value = EditSessionUiState.NotFound
                    return@collectLatest
                }

                val session = practice.sessions.find { it.id == sessionId }
                if (session == null) {
                    _uiState.value = EditSessionUiState.NotFound
                    return@collectLatest
                }

                _uiState.value = EditSessionUiState.Success(practice, session)
            }
        }
    }

    fun updateSession(
        date: LocalDateTime,
        duration: Int,
        startingBpm: Int,
        achievedBpm: Int,
        notes: String?
    ) {
        viewModelScope.launch {
            repository.updateSession(
                id = sessionId,
                practiceId = practiceId,
                date = date,
                duration = duration,
                startingBpm = startingBpm,
                achievedBpm = achievedBpm,
                notes = notes
            )
        }
    }

    fun deleteSession() {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
        }
    }
} 