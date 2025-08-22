package com.mindblowers.leasehub.utils

import at.favre.lib.crypto.bcrypt.BCrypt
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityUtils @Inject constructor() {

    /**
     * Hashes a password using BCrypt algorithm
     * @param password The plain text password to hash
     * @return The hashed password string
     */
    fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    /**
     * Verifies if a plain text password matches a hashed password
     * @param password The plain text password to verify
     * @param hash The hashed password to compare against
     * @return True if the password matches the hash, false otherwise
     */
    fun verifyPassword(password: String, hash: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), hash).verified
    }

    /**
     * Validates password strength
     * @param password The password to validate
     * @return True if the password meets minimum security requirements
     */
    fun isPasswordStrong(password: String): Boolean {
        if (password.length < 8) return false
        if (!password.any { it.isDigit() }) return false
        if (!password.any { it.isUpperCase() }) return false
        if (!password.any { it.isLowerCase() }) return false
        if (!password.any { !it.isLetterOrDigit() }) return false
        return true
    }

    /**
     * Generates a secure random password
     * @param length The length of the password (default: 12)
     * @return A randomly generated secure password
     */
    fun generateSecurePassword(length: Int = 12): String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9') + "!@#$%^&*()_-+=<>?".toList()
        return (1..length)
            .map { charPool.random() }
            .joinToString("")
    }
}