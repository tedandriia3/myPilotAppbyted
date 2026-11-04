package com.pilotprep.app.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import com.pilotprep.app.ui.screens.HomeScreen
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun homeScreen_displaysQuickLinks() {
        composeRule.setContent {
            val navController = rememberNavController()
            HomeScreen(navController = navController)
        }

        composeRule.onNodeWithText("Cours").assertIsDisplayed()
        composeRule.onNodeWithText("Quiz").assertIsDisplayed()
        composeRule.onNodeWithText("Audio").assertIsDisplayed()
    }
}

