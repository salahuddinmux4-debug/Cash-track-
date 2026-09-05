package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthManager
import com.example.data.model.Transaction
import com.example.data.model.UserRole
import com.example.data.repository.CashTrackRepository
import com.example.data.repository.SyncStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

data class CashSummary(
    val currentBalance: Double = 0.0,
    val todayCashIn: Double = 0.0,
    val todayCashOut: Double = 0.0,
    val totalCashIn: Double = 0.0,
    val totalCashOut: Double = 0.0
)

class CashTrackViewModel(
    application: Application,
    val authManager: AuthManager = AuthManager(application),
    val repository: CashTrackRepository = CashTrackRepository(application)
) : AndroidViewModel(application) {

    val currentUser: StateFlow<UserRole?> = authManager.currentUser
    val transactions: StateFlow<List<Transaction>> = repository.transactions
    val syncStatus: StateFlow<SyncStatus> = repository.syncStatus
    val isOnline: StateFlow<Boolean> = repository.isOnline

    // Derived summary calculations
    val summary: StateFlow<CashSummary> = transactions.map { list ->
        var totalIn = 0.0
        var totalOut = 0.0
        var todayIn = 0.0
        var todayOut = 0.0

        for (tx in list) {
            if (tx.isCashIn) {
                totalIn += tx.amount
                if (isToday(tx.createdAt)) {
                    todayIn += tx.amount
                }
            } else if (tx.isCashOut) {
                totalOut += tx.amount
                if (isToday(tx.createdAt)) {
                    todayOut += tx.amount
                }
            }
        }

        CashSummary(
            currentBalance = totalIn - totalOut,
            todayCashIn = todayIn,
            todayCashOut = todayOut,
            totalCashIn = totalIn,
            totalCashOut = totalOut
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CashSummary()
    )

    val recentTransactions: StateFlow<List<Transaction>> = transactions.map { list ->
        list.take(5)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun login(role: UserRole, pin: String): Boolean {
        val success = authManager.login(role, pin)
        if (!success) {
            _userMessage.value = "Incorrect PIN for ${role.displayName}"
        }
        return success
    }

    fun logout() {
        authManager.logout()
    }

    fun addCashIn(
        amount: Double,
        description: String,
        dateMillis: Long = System.currentTimeMillis(),
        note: String? = null
    ) {
        val creator = currentUser.value ?: UserRole.MAIN
        val transaction = Transaction(
            id = UUID.randomUUID().toString(),
            amount = amount,
            type = Transaction.TYPE_CASH_IN,
            description = description.trim(),
            note = note?.trim()?.ifEmpty { null },
            createdBy = creator.id,
            createdAt = dateMillis,
            updatedAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            repository.addTransaction(transaction)
        }
    }

    fun addCashOut(
        amount: Double,
        description: String,
        dateMillis: Long = System.currentTimeMillis(),
        note: String? = null
    ) {
        val creator = currentUser.value ?: UserRole.MAIN
        val transaction = Transaction(
            id = UUID.randomUUID().toString(),
            amount = amount,
            type = Transaction.TYPE_CASH_OUT,
            description = description.trim(),
            note = note?.trim()?.ifEmpty { null },
            createdBy = creator.id,
            createdAt = dateMillis,
            updatedAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            repository.addTransaction(transaction)
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            repository.deleteTransaction(transactionId)
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            repository.syncNow()
        }
    }

    fun changePin(role: UserRole, oldPin: String, newPin: String): Boolean {
        val success = authManager.changePin(role, oldPin, newPin)
        if (success) {
            _userMessage.value = "PIN updated successfully"
        } else {
            _userMessage.value = "Failed to update PIN. Check your old PIN (min 4 digits)."
        }
        return success
    }

    private fun isToday(timestamp: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp }
        val cal2 = Calendar.getInstance()
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
