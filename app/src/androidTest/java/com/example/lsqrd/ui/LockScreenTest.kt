package com.example.lsqrd.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lsqrd.ui.theme.LsqrdTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LockScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun lockScreen_showsAppName() {
        composeTestRule.setContent {
            LsqrdTheme { LockScreen(onUnlockRequest = {}) }
        }
        composeTestRule.onNodeWithText("lsqrd").assertIsDisplayed()
    }

    @Test
    fun lockScreen_showsLockedMessage(){
        composeTestRule.setContent {
            LsqrdTheme {LockScreen(onUnlockRequest = {}) }
        }
        composeTestRule.onNodeWithText("Your vault is locked").assertIsDisplayed()
    }

    @Test
    fun lockScreen_showsUnlockButton(){
        composeTestRule.setContent {
            LsqrdTheme{ LockScreen(onUnlockRequest = {}) }
        }
        composeTestRule.onNodeWithText("Unlock").assertIsDisplayed()
    }

    @Test
    fun clickingUnlock_callsCallback(){
        var unlockCalled = false

        composeTestRule.setContent {
            LsqrdTheme {LockScreen(onUnlockRequest = {unlockCalled = true}) }
        }

        composeTestRule.onNodeWithText("Unlock").performClick()
        assertTrue("onUnlockRequest should have been called", unlockCalled)
    }
}
