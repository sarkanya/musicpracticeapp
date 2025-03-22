package com.adammusic.practicelog.ui.screens.practice.active

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adammusic.practicelog.data.model.Practice
import com.adammusic.practicelog.data.repository.PracticeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivePracticeViewModel @Inject constructor(
    private val repository: PracticeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val practiceId: Int = savedStateHandle.get<String>("practiceId")?.toIntOrNull() 
        ?: throw IllegalArgumentException("Practice ID szükséges")


    val practiceState: StateFlow<ActivePracticeUiState> = repository.getPracticeById(practiceId)
        .map { practice ->
            if (practice != null) {
                ActivePracticeUiState.Success(practice)
            } else {
                ActivePracticeUiState.NotFound
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ActivePracticeUiState.Loading
        )
    
    fun getLastAchievedBpm(practice: Practice): Int {
        return practice.sessions.maxByOrNull { it.date }
            ?.achievedBpm
            ?: 100
    }
    
    fun savePracticeSession(
        duration: Int,
        startingBpm: Int,
        achievedBpm: Int,
        notes: String? = null,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.addPracticeSession(
                practiceId = practiceId,
                duration = duration,
                startingBpm = startingBpm,
                achievedBpm = achievedBpm,
                notes = notes
            )
            onComplete()
        }
    }
}

sealed class ActivePracticeUiState {
    data object Loading : ActivePracticeUiState()
    data class Success(val practice: Practice) : ActivePracticeUiState()
    data object NotFound : ActivePracticeUiState()
}