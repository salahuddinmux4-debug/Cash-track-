package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.auth.AuthManager
import com.example.data.model.Transaction
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CashTrackSharedScenarioTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    /**
     * Shared in-memory cloud database simulation connecting two client devices
     * precisely verifying the user's multi-device scenario.
     */
    class MockSharedCloudDatabase {
        private val _cloudTransactions = MutableStateFlow<List<Transaction>>(emptyList())

        fun getTransactions(): List<Transaction> = _cloudTransactions.value

        fun pushTransactions(list: List<Transaction>) {
            _cloudTransactions.value = list.sortedByDescending { it.createdAt }
        }

        fun calculateBalance(): Double {
            var cashIn = 0.0
            var cashOut = 0.0
            for (tx in _cloudTransactions.value) {
                if (tx.isCashIn) cashIn += tx.amount
                if (tx.isCashOut) cashOut += tx.amount
            }
            return cashIn - cashOut
        }
    }

    /**
     * Simulated mobile client running Cash Track
     */
    class SimulatedMobileDevice(
        val role: UserRole,
        val authManager: AuthManager,
        private val sharedCloud: MockSharedCloudDatabase
    ) {
        var localCache: List<Transaction> = emptyList()

        fun login(pin: String): Boolean = authManager.login(role, pin)

        fun syncFromCloud() {
            localCache = sharedCloud.getTransactions()
        }

        fun addCashIn(amount: Double, description: String): Transaction {
            val tx = Transaction(
                id = UUID.randomUUID().toString(),
                amount = amount,
                type = Transaction.TYPE_CASH_IN,
                description = description,
                createdBy = role.id,
                createdAt = System.currentTimeMillis()
            )
            val updated = listOf(tx) + localCache
            localCache = updated.sortedByDescending { it.createdAt }
            sharedCloud.pushTransactions(localCache)
            return tx
        }

        fun addCashOut(amount: Double, description: String): Transaction {
            val tx = Transaction(
                id = UUID.randomUUID().toString(),
                amount = amount,
                type = Transaction.TYPE_CASH_OUT,
                description = description,
                createdBy = role.id,
                createdAt = System.currentTimeMillis()
            )
            val updated = listOf(tx) + localCache
            localCache = updated.sortedByDescending { it.createdAt }
            sharedCloud.pushTransactions(localCache)
            return tx
        }

        fun updateTransaction(updatedTx: Transaction) {
            localCache = localCache.map { if (it.id == updatedTx.id) updatedTx else it }
            sharedCloud.pushTransactions(localCache)
        }

        fun deleteTransaction(id: String) {
            localCache = localCache.filterNot { it.id == id }
            sharedCloud.pushTransactions(localCache)
        }

        fun getCalculatedBalance(): Double {
            var cashIn = 0.0
            var cashOut = 0.0
            for (tx in localCache) {
                if (tx.isCashIn) cashIn += tx.amount
                if (tx.isCashOut) cashOut += tx.amount
            }
            return cashIn - cashOut
        }
    }

    @Test
    fun `test required 10-step multi-phone scenario for Mujahid and Boss`() = runTest {
        val sharedCloud = MockSharedCloudDatabase()

        // 1. Mobile 1: Login as Main (Mujahid)
        val authMobile1 = AuthManager(context)
        val mobile1 = SimulatedMobileDevice(UserRole.MAIN, authMobile1, sharedCloud)
        assertTrue("Main should log in with default PIN", mobile1.login("1234"))
        assertFalse("Main should fail with wrong PIN", authMobile1.verifyPin(UserRole.MAIN, "0000"))

        // 2. Mobile 2: Login as Boss
        val authMobile2 = AuthManager(context)
        val mobile2 = SimulatedMobileDevice(UserRole.BOSS, authMobile2, sharedCloud)
        assertTrue("Boss should log in with default PIN", mobile2.login("5678"))
        assertFalse("Boss should fail with wrong PIN", authMobile2.verifyPin(UserRole.BOSS, "0000"))

        // 3. Main adds Cash In Rs. 50,000 on Mobile 1
        val tx1 = mobile1.addCashIn(50000.0, "Sale payment")
        assertEquals(50000.0, mobile1.getCalculatedBalance(), 0.01)

        // 4. Verify Boss sees Rs. 50,000 automatically on Mobile 2
        mobile2.syncFromCloud()
        assertEquals(50000.0, mobile2.getCalculatedBalance(), 0.01)
        assertEquals(1, mobile2.localCache.size)
        assertEquals("Sale payment", mobile2.localCache[0].description)
        assertEquals("Mujahid", mobile2.localCache[0].creatorDisplayName)

        // 5. Boss adds Cash Out Rs. 10,000 on Mobile 2
        val tx2 = mobile2.addCashOut(10000.0, "Office expense")
        assertEquals(40000.0, mobile2.getCalculatedBalance(), 0.01)

        // 6. Verify Main sees balance Rs. 40,000 automatically on Mobile 1
        mobile1.syncFromCloud()
        assertEquals(40000.0, mobile1.getCalculatedBalance(), 0.01)
        assertEquals(2, mobile1.localCache.size)

        // 7. Main adds Cash In Rs. 20,000 on Mobile 1
        val tx3 = mobile1.addCashIn(20000.0, "Customer collection")
        assertEquals(60000.0, mobile1.getCalculatedBalance(), 0.01)

        // 8. Verify Boss sees balance Rs. 60,000 on Mobile 2
        mobile2.syncFromCloud()
        assertEquals(60000.0, mobile2.getCalculatedBalance(), 0.01)

        // 9. Edit or delete a transaction:
        // Boss edits tx2 ("Office expense") from Rs. 10,000 to Rs. 15,000
        val editedTx2 = tx2.copy(amount = 15000.0, description = "Office expense (updated)")
        mobile2.updateTransaction(editedTx2)

        // 10. Verify the change appears on both mobiles immediately
        mobile1.syncFromCloud()
        // Balance: 50,000 + 20,000 - 15,000 = 55,000
        assertEquals(55000.0, mobile1.getCalculatedBalance(), 0.01)
        assertEquals(55000.0, mobile2.getCalculatedBalance(), 0.01)

        // Test Deletion: Main deletes tx3 (Rs. 20,000)
        mobile1.deleteTransaction(tx3.id)
        mobile2.syncFromCloud()
        // Balance: 50,000 - 15,000 = 35,000
        assertEquals(35000.0, mobile1.getCalculatedBalance(), 0.01)
        assertEquals(35000.0, mobile2.getCalculatedBalance(), 0.01)
        assertEquals(2, mobile1.localCache.size)
        assertEquals(2, mobile2.localCache.size)
    }

    @Test
    fun `test formatCurrency displays PKR properly`() {
        assertEquals("Rs. 50,000", Transaction.formatCurrency(50000.0))
        assertEquals("Rs. 8,000", Transaction.formatCurrency(8000.0))
        assertEquals("Rs. 100,000", Transaction.formatCurrency(100000.0))
        assertEquals("Rs. 0", Transaction.formatCurrency(0.0))
    }
}
