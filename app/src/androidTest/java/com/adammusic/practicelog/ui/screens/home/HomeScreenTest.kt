package com.adammusic.practicelog.ui.screens.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adammusic.practicelog.data.model.Category
import com.adammusic.practicelog.data.model.Practice
import com.adammusic.practicelog.ui.theme.MusicPracticeAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class HomeScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun categoriesDisplayedCorrectly() {
        // Arrange
        val categories = listOf(
            Category(1, "Skálák", 0xFF2196F3),
            Category(2, "Etűdök", 0xFF4CAF50)
        )

        // Act
        composeTestRule.setContent {
            MusicPracticeAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CategorySelector(
                        categories = categories,
                        selectedCategoryId = null,
                        onCategorySelected = {}
                    )
                }
            }
        }

        // Assert
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Összes").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Skálák").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Etűdök").assertExists().assertIsDisplayed()
    }

    @Test
    fun practiceCardsDisplayCorrectInfo() {
        // Arrange
        val category = Category(1, "Skálák", 0xFF2196F3)
        val practice = Practice(
            id = 1,
            name = "Skála gyakorlat",
            description = "Napi skála gyakorlat",
            category = category,
            sessions = emptyList()
        )

        // Act
        composeTestRule.setContent {
            MusicPracticeAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PracticeCard(practice = practice, onClick = {})
                }
            }
        }

        // Assert
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Skála gyakorlat").assertIsDisplayed()
        composeTestRule.onNodeWithText("Napi skála gyakorlat").assertIsDisplayed()
        composeTestRule.onNodeWithText("Skálák").assertIsDisplayed()
    }
}