package com.adammusic.practicelog.ui.screens.practice.editpractice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adammusic.practicelog.data.model.Category
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
class EditPracticeViewModel @Inject constructor(
    private val repository: PracticeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val practiceId: Int = savedStateHandle.get<String>("practiceId")?.toIntOrNull() 
        ?: throw IllegalArgumentException("Practice ID is required")
    
    val practiceState: StateFlow<EditPracticeUiState> = repository.getPracticeById(practiceId)
        .map { practice -> 
            if (practice != null) {
                EditPracticeUiState.Success(practice)
            } else {
                EditPracticeUiState.NotFound
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EditPracticeUiState.Loading
        )
    
    val categories = repository.getAllCategories().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    fun updatePractice(
        name: String,
        description: String,
        category: Category,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.updatePractice(
                id = practiceId,
                name = name,
                description = description,
                categoryId = category.id
            )
            onComplete()
        }
    }
}

sealed class EditPracticeUiState {
    data object Loading : EditPracticeUiState()
    data class Success(val practice: Practice) : EditPracticeUiState()
    data object NotFound : EditPracticeUiState()
} 