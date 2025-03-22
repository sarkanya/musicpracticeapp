package com.adammusic.practicelog.ui.screens.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.adammusic.practicelog.data.model.Category
import com.adammusic.practicelog.data.model.Practice
import com.adammusic.practicelog.data.repository.PracticeRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.junit.Test
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class HomeViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: HomeViewModel
    private lateinit var repository: PracticeRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock(PracticeRepository::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test categories are loaded from repository`() = runTest {
        // Arrange
        val categories = listOf(
            Category(1, "Scales", 0xFF2196F3),
            Category(2, "Etudes", 0xFF4CAF50)
        )
        val categoriesFlow = MutableStateFlow(categories)
        `when`(repository.getAllCategories()).thenReturn(categoriesFlow)

        viewModel = HomeViewModel(repository)

        // Act
        val result = viewModel.categories.first()

        // Assert
        assertThat(result).isEqualTo(categories)
    }

    @Test
    fun `test practices are loaded from repository`() = runTest {
        // Arrange
        val category = Category(1, "Scales", 0xFF2196F3)
        val practices = listOf(
            Practice(1, "Scale Practice", "Description", category),
            Practice(2, "Another Practice", "Another Description", category)
        )
        val practicesFlow = MutableStateFlow(practices)
        `when`(repository.getAllPractices()).thenReturn(practicesFlow)

        viewModel = HomeViewModel(repository)

        // Act
        val result = viewModel.practices.first()

        // Assert
        assertThat(result).isEqualTo(practices)
    }

    @Test
    fun `test save practice calls repository`(): Unit = runTest {
        viewModel = HomeViewModel(repository)
        // Arrange
        val name = "New Practice"
        val description = "Description"
        val category = Category(1, "Scales", 0xFF2196F3)

        // Act
        viewModel.savePractice(name, description, category)

        // Assert
        verify(repository).addNewPractice(name, description, category.id)
    }
}