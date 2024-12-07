package com.appdev.medicare.utils

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

fun hashString(string: String): String {
    try {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(string.toByteArray(Charsets.UTF_8))
        val hexString = StringBuilder()
        for (b in hash) {
            val hex = Integer.toHexString(0xff and b.toInt())
            if (hex.length == 1) hexString.append('0')
            hexString.append(hex)
        }
        return hexString.toString()
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    }
    return ""
}