package com.example.lsqrd.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lsqrd.data.Credential
import com.example.lsqrd.ui.theme.LsqrdTheme
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CredentialRowTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val testCredential = Credential(id = 1L, vaultId = 1L, name = "Github")

    @Test
    fun credential_row_shows_credential_name() {
        composeTestRule.setContent {
            LsqrdTheme {
                CredentialRow(
                    credential = testCredential,
                    onClick = {},
                    onEdit = {},
                    onMove = {},
                    onDelete = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Github").assertIsDisplayed()
    }

    @Test
    fun credential_row_clicking_row_calls_on_click() {
        var clicked = false
        composeTestRule.setContent {
            LsqrdTheme {
                CredentialRow(
                    credential = testCredential,
                    onClick = { clicked = true },
                    onEdit = {},
                    onMove = {},
                    onDelete = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Github").performClick()
        assertTrue("onClick should have been called", clicked)
    }

    @Test
    fun credential_row_clicking_edit_calls_on_edit() {
        var edited = false
        composeTestRule.setContent {
            LsqrdTheme {
                CredentialRow(
                    credential = testCredential,
                    onClick = { },
                    onEdit = { edited = true },
                    onMove = {},
                    onDelete = {}
                )
            }
        }
        composeTestRule.onNodeWithContentDescription("Edit credential").performClick()
        assertTrue("onEdit should have been called", edited)
    }

    @Test
    fun credential_row_more_vert_then_delete_calls_on_delete() {
        var deleted = false
        composeTestRule.setContent {
            LsqrdTheme {
                CredentialRow(
                    credential = testCredential,
                    onClick = { },
                    onEdit = { },
                    onMove = {},
                    onDelete = { deleted = true }
                )
            }
        }
        composeTestRule.onNodeWithContentDescription("More options").performClick()
        composeTestRule.onNodeWithText("Delete").performClick()
        assertTrue("onDelete should have been called", deleted)
    }

    @Test
    fun credential_row_more_vert_then_move_calls_on_move() {
        var moved = false
        composeTestRule.setContent {
            LsqrdTheme {
                CredentialRow(
                    credential = testCredential,
                    onClick = { },
                    onEdit = { },
                    onMove = { moved = true },
                    onDelete = {}
                )
            }
        }
        composeTestRule.onNodeWithContentDescription("More options").performClick()
        composeTestRule.onNodeWithText("Move to vault").performClick()
        assertTrue("onMove should have been called", moved)
    }
}