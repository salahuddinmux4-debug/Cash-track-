package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.data.model.UserRole
import com.example.ui.dialogs.AddTransactionDialog
import com.example.ui.dialogs.EditTransactionDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransactionHistoryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.UtilityBlue
import com.example.ui.theme.UtilityBlueLight
import com.example.ui.viewmodel.CashTrackViewModel

enum class AppTab(val title: String, val testTag: String) {
    HOME("Home", "tab_home"),
    TRANSACTIONS("Transactions", "tab_transactions"),
    SETTINGS("Settings", "tab_settings")
}

class MainActivity : ComponentActivity() {

    private val viewModel: CashTrackViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                CashTrackApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CashTrackApp(viewModel: CashTrackViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val recentTransactions by viewModel.recentTransactions.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var currentTab by remember { mutableStateOf(AppTab.HOME) }

    // Dialog States
    var showCashInDialog by remember { mutableStateOf(false) }
    var showCashOutDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }

    // Show toast / snackbar for messages
    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearUserMessage()
        }
    }

    if (currentUser == null) {
        LoginScreen(
            onLoginSuccess = { role ->
                // logged in
            },
            onLoginAttempt = { role, pin ->
                viewModel.login(role, pin)
            }
        )
    } else {
        val user = currentUser!!

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .testTag("bottom_navigation_bar")
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    val navItemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = UtilityBlue,
                        selectedTextColor = UtilityBlue,
                        indicatorColor = UtilityBlueLight,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400
                    )

                    NavigationBarItem(
                        selected = currentTab == AppTab.HOME,
                        onClick = { currentTab = AppTab.HOME },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AppTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = {
                            Text(
                                text = "HOME",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        },
                        modifier = Modifier.testTag(AppTab.HOME.testTag),
                        colors = navItemColors
                    )

                    NavigationBarItem(
                        selected = currentTab == AppTab.TRANSACTIONS,
                        onClick = { currentTab = AppTab.TRANSACTIONS },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AppTab.TRANSACTIONS) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                                contentDescription = "Transactions"
                            )
                        },
                        label = {
                            Text(
                                text = "HISTORY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        },
                        modifier = Modifier.testTag(AppTab.TRANSACTIONS.testTag),
                        colors = navItemColors
                    )

                    NavigationBarItem(
                        selected = currentTab == AppTab.SETTINGS,
                        onClick = { currentTab = AppTab.SETTINGS },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AppTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = "Settings"
                            )
                        },
                        label = {
                            Text(
                                text = "SETTINGS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        },
                        modifier = Modifier.testTag(AppTab.SETTINGS.testTag),
                        colors = navItemColors
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    AppTab.HOME -> {
                        DashboardScreen(
                            currentUser = user,
                            summary = summary,
                            recentTransactions = recentTransactions,
                            syncStatus = syncStatus,
                            isOnline = isOnline,
                            onCashInClick = { showCashInDialog = true },
                            onCashOutClick = { showCashOutDialog = true },
                            onTransactionClick = { tx -> editingTransaction = tx },
                            onViewAllClick = { currentTab = AppTab.TRANSACTIONS },
                            onManualSync = { viewModel.syncNow() }
                        )
                    }

                    AppTab.TRANSACTIONS -> {
                        TransactionHistoryScreen(
                            transactions = transactions,
                            onTransactionClick = { tx -> editingTransaction = tx }
                        )
                    }

                    AppTab.SETTINGS -> {
                        SettingsScreen(
                            currentUser = user,
                            syncStatus = syncStatus,
                            isOnline = isOnline,
                            sharedDbId = viewModel.repository.getSharedDatabaseId(),
                            onLogout = { viewModel.logout() },
                            onManualSync = { viewModel.syncNow() },
                            onChangePin = { oldPin, newPin ->
                                viewModel.changePin(user, oldPin, newPin)
                            }
                        )
                    }
                }
            }
        }

        // Add Cash In Dialog
        if (showCashInDialog) {
            AddTransactionDialog(
                isCashIn = true,
                currentBalance = summary.currentBalance,
                currentUser = user,
                onDismiss = { showCashInDialog = false },
                onSave = { amount, description, dateMillis, note ->
                    viewModel.addCashIn(amount, description, dateMillis, note)
                }
            )
        }

        // Add Cash Out Dialog
        if (showCashOutDialog) {
            AddTransactionDialog(
                isCashIn = false,
                currentBalance = summary.currentBalance,
                currentUser = user,
                onDismiss = { showCashOutDialog = false },
                onSave = { amount, description, dateMillis, note ->
                    viewModel.addCashOut(amount, description, dateMillis, note)
                }
            )
        }

        // Edit/Delete Transaction Dialog
        editingTransaction?.let { tx ->
            EditTransactionDialog(
                transaction = tx,
                onDismiss = { editingTransaction = null },
                onSave = { updated ->
                    viewModel.updateTransaction(updated)
                },
                onDelete = { txId ->
                    viewModel.deleteTransaction(txId)
                }
            )
        }
    }
}
