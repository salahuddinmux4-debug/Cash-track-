package com.example.data.auth

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("cash_track_auth", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<UserRole?>(loadCurrentUser())
    val currentUser: StateFlow<UserRole?> = _currentUser.asStateFlow()

    private fun loadCurrentUser(): UserRole? {
        val savedId = prefs.getString(KEY_CURRENT_USER_ID, null) ?: return null
        return UserRole.entries.firstOrNull { it.id == savedId }
    }

    fun getPinForUser(role: UserRole): String {
        val defaultPin = if (role == UserRole.MAIN) DEFAULT_PIN_MAIN else DEFAULT_PIN_BOSS
        return prefs.getString(KEY_PIN_PREFIX + role.id, defaultPin) ?: defaultPin
    }

    fun verifyPin(role: UserRole, enteredPin: String): Boolean {
        val correctPin = getPinForUser(role)
        return enteredPin.trim() == correctPin.trim()
    }

    fun login(role: UserRole, pin: String): Boolean {
        if (verifyPin(role, pin)) {
            prefs.edit().putString(KEY_CURRENT_USER_ID, role.id).apply()
            _currentUser.value = role
            return true
        }
        return false
    }

    fun logout() {
        prefs.edit().remove(KEY_CURRENT_USER_ID).apply()
        _currentUser.value = null
    }

    fun changePin(role: UserRole, oldPin: String, newPin: String): Boolean {
        if (!verifyPin(role, oldPin)) return false
        if (newPin.length < 4) return false
        prefs.edit().putString(KEY_PIN_PREFIX + role.id, newPin.trim()).apply()
        return true
    }

    fun isLoggedIn(): Boolean = _currentUser.value != null

    companion object {
        private const val KEY_CURRENT_USER_ID = "current_user_id"
        private const val KEY_PIN_PREFIX = "user_pin_"
        const val DEFAULT_PIN_MAIN = "1234"
        const val DEFAULT_PIN_BOSS = "5678"
    }
}
