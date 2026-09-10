package com.example.lsqrd.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lsqrd.data.Vault
import com.example.lsqrd.ui.theme.LsqrdTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VaultRowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testVault = Vault(id = 1L, name = "Work")

    @Test
    fun vaultRow_showsVaultName() {
        composeTestRule.setContent {
            LsqrdTheme {
                VaultRow(vault = testVault, onClick = {}, onEdit = {}, onDelete = {})
            }
        }
        composeTestRule.onNodeWithText("Work").assertIsDisplayed()
    }

    @Test
    fun vaultRow_clickingRow_callsOnClick(){
        var clicked = false

        composeTestRule.setContent {
            LsqrdTheme {
                VaultRow(vault = testVault, onClick = {clicked = true}, onEdit = {}, onDelete = {})
            }
        }

        composeTestRule.onNodeWithText("Work").performClick()
        assertTrue("onClick should have been called", clicked)
    }

    @Test
    fun vaultRow_clickingDelete_callsOnEdit(){
        var edited = false

        composeTestRule.setContent {
            LsqrdTheme {
                VaultRow(vault = testVault, onClick = {}, onEdit = {edited = true}, onDelete = {})
            }
        }

        composeTestRule.onNodeWithContentDescription("Edit Vault").performClick()
        assertTrue("onEdit should have been called", edited)
    }

    @Test
    fun vaultRow_clickingDelete_callsOnDelete(){
        var deleted = false

        composeTestRule.setContent {
            LsqrdTheme {
                VaultRow(vault = testVault, onClick = {}, onEdit = {}, onDelete = {deleted = true})
            }
        }

        composeTestRule.onNodeWithContentDescription("Delete Vault").performClick()
        assertTrue("onDelete should have been called", deleted)
    }
}