package com.example.lsqrd.data

object TotpGenerator {
    private const val BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    /**
     * Generate a 6-digit TOTP code (RFC 6238, HMAC-SHA1, 30-second window).
     * @param secret Base32-encoded shared secret (spaces and lowercase accepted)
     * @param timeMillis Current time in milliseconds (defaults to now)
     */
    fun generate(secret: String, timeMillis: Long= System.currentTimeMillis()): String{
        return ""
    }

    fun base32Decode(input: String): ByteArray{
        var bits = 0
        var bitsCount = 0
        val output = mutableListOf<Byte>()
        for (char in input){
            if (char == '=') break
            val value = BASE32.indexOf(char)
            require(value >= 0){"Invalid Base32 character: $char"}
            bits = (bits shl 5) or value
            bitsCount += 5
            if(bitsCount >= 8){
                bitsCount -= 8
                output.add((bits shr bitsCount).toByte())
                bits = bits and ((1 shl bitsCount) - 1)
            }
        }
        return output.toByteArray()
    }
}