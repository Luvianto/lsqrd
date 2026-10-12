package com.example.lsqrd

import com.example.lsqrd.data.PasswordGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordGeneratorTest {

    // ── Length ───────────────────────────────────────────

    @Test
    fun default_length_is_16() {
        val password = PasswordGenerator.generate()
        assertEquals(16, password.length)
    }

    @Test
    fun allow_custom_lengths() {
        val password = PasswordGenerator.generate(length = 24)
        assertEquals(24, password.length)
    }

    // ── Enabled char type ───────────────────────────────────────────

    @Test
    fun always_contains_lowercase() {
        val passwords = List(50) {
            PasswordGenerator.generate(useUpperCase = false, useDigits = false, useSymbols = false)
        }
        assertTrue(passwords.all { p -> p.any { it.isLowerCase() } })
    }

    @Test
    fun contains_uppercase_when_enabled() {
        val passwords = List(50) {
            PasswordGenerator.generate(useUpperCase = true)
        }
        assertTrue(
            "At least one password should contain uppercase",
            passwords.any { p -> p.any { it.isUpperCase() } }
        )
    }

    @Test
    fun contains_digits_when_enabled() {
        val passwords = List(50) {
            PasswordGenerator.generate(useDigits = true)
        }
        assertTrue(
            "At least one password should contain a digit",
            passwords.any { p -> p.any { it.isDigit() } }
        )
    }

    @Test
    fun contains_symbols_when_enabled() {
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
    fun no_uppercase_when_disabled() {
        val passwords = List(20) {
            PasswordGenerator.generate(useUpperCase = false)
        }
        assertTrue(passwords.any { p -> p.none { it.isUpperCase() } })
    }

    @Test
    fun no_digits_when_disabled() {
        val passwords = List(20) {
            PasswordGenerator.generate(useDigits = false)
        }
        assertTrue(passwords.any { p -> p.none { it.isDigit() } })
    }

    @Test
    fun no_symbols_when_disabled() {
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
    fun consecutive_passwords_are_not_identical() {
        val passwords = List(10) { PasswordGenerator.generate() }
        assertFalse(
            "All passwords were identical - generator is random",
            passwords.all { it == passwords[0] }
        )
    }
}