package com.adammusic.practicelog.ui.screens.practice.detail

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
class PracticeDetailViewModel @Inject constructor(
    private val repository: PracticeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val practiceId: Int = savedStateHandle.get<String>("practiceId")?.toIntOrNull() 
        ?: throw IllegalArgumentException("Practice ID szükséges")
    
    val practiceState: StateFlow<PracticeDetailUiState> = repository.getPracticeById(practiceId)
        .map { practice -> 
            if (practice != null) {
                PracticeDetailUiState.Success(practice)
            } else {
                PracticeDetailUiState.NotFound
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PracticeDetailUiState.Loading
        )
        
    fun deletePractice() {
        viewModelScope.launch {
            repository.deletePractice(practiceId)
        }
    }
}

sealed class PracticeDetailUiState {
    data object Loading : PracticeDetailUiState()
    data class Success(val practice: Practice) : PracticeDetailUiState()
    data object NotFound : PracticeDetailUiState()
} 