package com.example.lsqrd

import com.example.lsqrd.data.PasswordGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordGeneratorTest {

    // ── Length ───────────────────────────────────────────

    @Test
    fun `default length is 16`() {
        val password = PasswordGenerator.generate()
        assertEquals(16, password.length)
    }

    @Test
    fun `allow custom lengths`() {
        val password = PasswordGenerator.generate(length = 24)
        assertEquals(24, password.length)
    }

    // ── Enabled char type ───────────────────────────────────────────

    @Test
    fun `always contains lowercase`() {
        val passwords = List(50) {
            PasswordGenerator.generate(useUpperCase = false, useDigits = false, useSymbols = false)
        }
        assertTrue(passwords.all { p -> p.any { it.isLowerCase() } })
    }

    @Test
    fun `contains uppercase when enabled`() {
        val passwords = List(50) {
            PasswordGenerator.generate(useUpperCase = true)
        }
        assertTrue(
            "At least one password should contain uppercase",
            passwords.any { p -> p.any { it.isUpperCase() } }
        )
    }

    @Test
    fun `contains digits when enabled`() {
        val passwords = List(50) {
            PasswordGenerator.generate(useDigits = true)
        }
        assertTrue(
            "At least one password should contain a digit",
            passwords.any { p -> p.any { it.isDigit() } }
        )
    }

    @Test
    fun `contains symbols when enabled`() {
        val symbols = "!@#\$%^&*()-_=+[]{}|;:,.<>?"
        val passwords = List(50) {
            PasswordGenerator.generate(useSymbols = true)
        }
        assertTrue(
            "At least one password should contain a symbol",
            passwords.any { p -> p.any { it in symbols } }
        )
    }

    // ── Disabled char type ───────────────────────────────────────────

    @Test
    fun `no uppercase when disabled`() {
        val passwords = List(20) {
            PasswordGenerator.generate(useUpperCase = false)
        }
        assertTrue(passwords.any { p -> p.none { it.isUpperCase() } })
    }

    @Test
    fun `no digits when disabled`() {
        val passwords = List(20) {
            PasswordGenerator.generate(useDigits = false)
        }
        assertTrue(passwords.any { p -> p.none { it.isDigit() } })
    }

    @Test
    fun `no symbols when disabled`() {
        val symbols = "!@#\$%^&*()-_=+[]{}|;:,.<>?"
        val passwords = List(20) {
            PasswordGenerator.generate(useSymbols = false)
        }
        assertTrue(
            "At least one password should contain a symbol",
            passwords.any { p -> p.none { it in symbols } }
        )
    }

    // ── Randomness ───────────────────────────────────────────

    @Test
    fun `consecutive passwords are not identical`() {
        val passwords = List(10) { PasswordGenerator.generate() }
        assertFalse(
            "All passwords were identical - generator is random",
            passwords.all { it == passwords[0] }
        )
    }
}