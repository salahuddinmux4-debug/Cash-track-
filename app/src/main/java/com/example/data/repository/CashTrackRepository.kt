package com.example.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.data.model.Transaction
import com.example.data.remote.CloudObjectRequest
import com.example.data.remote.CloudSyncApi
import com.example.data.remote.CloudTransactionsData
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

sealed class SyncStatus {
    object Idle : SyncStatus()
    object Syncing : SyncStatus()
    data class Synced(val lastSyncTime: Long) : SyncStatus()
    data class Offline(val pendingCount: Int = 0) : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}

class CashTrackRepository(
    private val context: Context,
    private val api: CloudSyncApi = CloudSyncApi.create(),
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val prefs = context.getSharedPreferences("cash_track_repo", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val listType = Types.newParameterizedType(List::class.java, Transaction::class.java)
    private val transactionListAdapter = moshi.adapter<List<Transaction>>(listType)

    private val mutex = Mutex()
    private var pollingJob: Job? = null

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _isOnline = MutableStateFlow(isNetworkAvailable())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    init {
        // Load initial cached transactions
        _transactions.value = loadLocalTransactions()
        registerNetworkCallback()
        startRealtimePolling()
    }

    fun getSharedDatabaseId(): String {
        return prefs.getString(KEY_SHARED_DB_ID, CloudSyncApi.DEFAULT_SHARED_DATABASE_ID)
            ?: CloudSyncApi.DEFAULT_SHARED_DATABASE_ID
    }

    fun setSharedDatabaseId(id: String) {
        prefs.edit().putString(KEY_SHARED_DB_ID, id.trim()).apply()
        externalScope.launch {
            syncNow()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun registerNetworkCallback() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager?.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isOnline.value = true
                externalScope.launch {
                    syncNow()
                }
            }

            override fun onLost(network: Network) {
                _isOnline.value = false
                val pending = loadPendingCount()
                _syncStatus.value = SyncStatus.Offline(pending)
            }
        })
    }

    fun startRealtimePolling() {
        pollingJob?.cancel()
        pollingJob = externalScope.launch {
            while (isActive) {
                if (_isOnline.value) {
                    pullFromCloud()
                } else {
                    val pending = loadPendingCount()
                    _syncStatus.value = SyncStatus.Offline(pending)
                }
                delay(2500) // Poll every 2.5s for real-time multiplayer updates
            }
        }
    }

    suspend fun syncNow(): Result<Unit> = withContext(Dispatchers.IO) {
        if (!_isOnline.value) {
            val pending = loadPendingCount()
            _syncStatus.value = SyncStatus.Offline(pending)
            return@withContext Result.failure(Exception("Offline"))
        }

        _syncStatus.value = SyncStatus.Syncing
        val pushResult = pushPendingToCloud()
        val pullResult = pullFromCloud()

        if (pullResult.isSuccess) {
            _syncStatus.value = SyncStatus.Synced(System.currentTimeMillis())
            Result.success(Unit)
        } else {
            val errorMsg = pullResult.exceptionOrNull()?.localizedMessage ?: "Sync failed"
            _syncStatus.value = SyncStatus.Error(errorMsg)
            Result.failure(Exception(errorMsg))
        }
    }

    private suspend fun pullFromCloud(): Result<List<Transaction>> = withContext(Dispatchers.IO) {
        try {
            val dbId = getSharedDatabaseId()
            val response = api.getSharedData(dbId)

            if (response.isSuccessful) {
                val cloudData = response.body()?.data?.transactions ?: emptyList()
                mutex.withLock {
                    // Sort descending by createdAt (newest first)
                    val sorted = cloudData.sortedByDescending { it.createdAt }
                    _transactions.value = sorted
                    saveLocalTransactions(sorted)
                }
                _syncStatus.value = SyncStatus.Synced(System.currentTimeMillis())
                Result.success(_transactions.value)
            } else if (response.code() == 404) {
                // Shared object does not exist yet on cloud, create it
                createCloudStore()
            } else {
                Result.failure(Exception("Cloud HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun createCloudStore(): Result<List<Transaction>> = withContext(Dispatchers.IO) {
        try {
            val currentList = _transactions.value
            val request = CloudObjectRequest(
                data = CloudTransactionsData(
                    transactions = currentList,
                    lastUpdated = System.currentTimeMillis()
                )
            )
            val response = api.createSharedData(request)
            if (response.isSuccessful && response.body()?.id != null) {
                val newId = response.body()!!.id!!
                setSharedDatabaseId(newId)
                Result.success(currentList)
            } else {
                Result.failure(Exception("Failed to create cloud database"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addTransaction(transaction: Transaction): Result<Unit> = withContext(Dispatchers.IO) {
        mutex.withLock {
            val updated = listOf(transaction) + _transactions.value.filterNot { it.id == transaction.id }
            _transactions.value = updated.sortedByDescending { it.createdAt }
            saveLocalTransactions(_transactions.value)
        }

        if (_isOnline.value) {
            pushToCloud()
        } else {
            incrementPendingCount()
            _syncStatus.value = SyncStatus.Offline(loadPendingCount())
            Result.success(Unit)
        }
    }

    suspend fun updateTransaction(transaction: Transaction): Result<Unit> = withContext(Dispatchers.IO) {
        mutex.withLock {
            val updated = _transactions.value.map {
                if (it.id == transaction.id) transaction.copy(updatedAt = System.currentTimeMillis()) else it
            }
            _transactions.value = updated.sortedByDescending { it.createdAt }
            saveLocalTransactions(_transactions.value)
        }

        if (_isOnline.value) {
            pushToCloud()
        } else {
            incrementPendingCount()
            _syncStatus.value = SyncStatus.Offline(loadPendingCount())
            Result.success(Unit)
        }
    }

    suspend fun deleteTransaction(transactionId: String): Result<Unit> = withContext(Dispatchers.IO) {
        mutex.withLock {
            val updated = _transactions.value.filterNot { it.id == transactionId }
            _transactions.value = updated
            saveLocalTransactions(_transactions.value)
        }

        if (_isOnline.value) {
            pushToCloud()
        } else {
            incrementPendingCount()
            _syncStatus.value = SyncStatus.Offline(loadPendingCount())
            Result.success(Unit)
        }
    }

    private suspend fun pushToCloud(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            _syncStatus.value = SyncStatus.Syncing
            val dbId = getSharedDatabaseId()
            val request = CloudObjectRequest(
                data = CloudTransactionsData(
                    transactions = _transactions.value,
                    lastUpdated = System.currentTimeMillis()
                )
            )
            val response = api.updateSharedData(dbId, request)
            if (response.isSuccessful) {
                clearPendingCount()
                _syncStatus.value = SyncStatus.Synced(System.currentTimeMillis())
                Result.success(Unit)
            } else {
                incrementPendingCount()
                _syncStatus.value = SyncStatus.Error("Failed to sync to cloud: ${response.code()}")
                Result.failure(Exception("Cloud update failed"))
            }
        } catch (e: Exception) {
            incrementPendingCount()
            _syncStatus.value = SyncStatus.Error(e.localizedMessage ?: "Sync error")
            Result.failure(e)
        }
    }

    private suspend fun pushPendingToCloud(): Result<Unit> = withContext(Dispatchers.IO) {
        if (loadPendingCount() > 0) {
            pushToCloud()
        } else {
            Result.success(Unit)
        }
    }

    private fun loadLocalTransactions(): List<Transaction> {
        val json = prefs.getString(KEY_LOCAL_TRANSACTIONS, null) ?: return emptyList()
        return try {
            transactionListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveLocalTransactions(list: List<Transaction>) {
        try {
            val json = transactionListAdapter.toJson(list)
            prefs.edit().putString(KEY_LOCAL_TRANSACTIONS, json).apply()
        } catch (_: Exception) {}
    }

    private fun loadPendingCount(): Int {
        return prefs.getInt(KEY_PENDING_CHANGES, 0)
    }

    private fun incrementPendingCount() {
        val current = loadPendingCount()
        prefs.edit().putInt(KEY_PENDING_CHANGES, current + 1).apply()
    }

    private fun clearPendingCount() {
        prefs.edit().putInt(KEY_PENDING_CHANGES, 0).apply()
    }

    companion object {
        private const val KEY_LOCAL_TRANSACTIONS = "local_transactions_cache"
        private const val KEY_SHARED_DB_ID = "shared_cloud_db_id"
        private const val KEY_PENDING_CHANGES = "pending_offline_changes_count"
    }
}
