package com.adammusic.practicelog.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adammusic.practicelog.data.repository.PracticeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: PracticeRepository
) : ViewModel() {
    val categories = repository.getAllCategories().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    private val _deleteCategoryResult = MutableStateFlow<DeleteCategoryResult?>(null)
    val deleteCategoryResult: StateFlow<DeleteCategoryResult?> = _deleteCategoryResult.asStateFlow()
    
    fun clearDeleteCategoryResult() {
        _deleteCategoryResult.value = null
    }

    fun addCategory(name: String, color: Long) {
        viewModelScope.launch {
            repository.addNewCategory(name, color)
        }
    }
    
    fun deleteCategory(categoryId: Int) {
        viewModelScope.launch {
            val practiceCount = repository.getCategoryPracticeCount(categoryId)
            
            if (practiceCount > 0) {
                _deleteCategoryResult.value = DeleteCategoryResult.HasPractices(practiceCount)
            } else {
                repository.deleteCategory(categoryId)
                _deleteCategoryResult.value = DeleteCategoryResult.Success
            }
        }
    }
}

sealed class DeleteCategoryResult {
    data object Success : DeleteCategoryResult()
    data class HasPractices(val practiceCount: Int) : DeleteCategoryResult()
}