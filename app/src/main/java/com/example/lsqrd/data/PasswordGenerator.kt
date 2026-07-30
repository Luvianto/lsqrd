package com.example.lsqrd.data

object PasswordGenerator {
    private val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
    private val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private val DIGITS = "0123456789"
    private val SYMBOLS = "!@#\$%^&*()-_=+[]{}|;:,.<>?"

    fun generate(
        length: Int = 16,
        useUpperCase: Boolean = true,
        useDigits: Boolean = true,
        useSymbols: Boolean = true
    ): String {
        val charset = StringBuilder(LOWERCASE)
        if(useUpperCase) charset.append(UPPERCASE)
        if(useDigits) charset.append(DIGITS)
        if(useSymbols) charset.append(SYMBOLS)

        val required = buildList {
            add(LOWERCASE.random())
            if (useUpperCase) add(UPPERCASE.random())
            if (useDigits) add(DIGITS.random())
            if (useSymbols) add(SYMBOLS.random())
        }

        val rest = (1..(length - required.size)).map { charset.random() }
        return (required + rest).shuffled().joinToString("")
    }
}