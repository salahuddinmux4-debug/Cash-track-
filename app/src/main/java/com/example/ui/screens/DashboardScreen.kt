package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.data.model.UserRole
import com.example.data.repository.SyncStatus
import com.example.ui.theme.CashInGreen
import com.example.ui.theme.CashInGreenContainer
import com.example.ui.theme.CashOutRed
import com.example.ui.theme.CashOutRedContainer
import com.example.ui.theme.OnCashInGreenContainer
import com.example.ui.theme.OnCashOutRedContainer
import com.example.ui.theme.OnlinePulseGreen
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.UtilityBlue
import com.example.ui.theme.UtilityBlueLight
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.CashSummary

@Composable
fun DashboardScreen(
    currentUser: UserRole,
    summary: CashSummary,
    recentTransactions: List<Transaction>,
    syncStatus: SyncStatus,
    isOnline: Boolean,
    onCashInClick: () -> Unit,
    onCashOutClick: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onViewAllClick: () -> Unit,
    onManualSync: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))

            // Header matching Clean Utility HTML: Title + Shared Wallet and User Badge with pulse dot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Cash Track",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        color = Slate800
                    )
                    Text(
                        text = "SHARED WALLET",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp,
                        color = UtilityBlue
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Active Profile Pill
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 1.dp,
                        modifier = Modifier.border(1.dp, Slate100, CircleShape)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(OnlinePulseGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentUser.displayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate600
                            )
                        }
                    }

                    CloudSyncStatusPill(syncStatus = syncStatus, isOnline = isOnline)

                    IconButton(
                        onClick = onManualSync,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("button_manual_sync")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync Now",
                            tint = Slate500,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Offline Banner if disconnected
        if (!isOnline) {
            item {
                Surface(
                    color = CashOutRedContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CashOutRed.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CloudOff, contentDescription = null, tint = CashOutRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Offline Mode: Changes will auto-sync when connection returns",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = OnCashOutRedContainer
                        )
                    }
                }
            }
        }

        // Prominent Shared Current Cash Balance Card (Clean Utility Minimal)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate100, RoundedCornerShape(24.dp))
                    .testTag("balance_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 22.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CURRENT BALANCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.5.sp,
                        color = Slate400
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = Transaction.formatCurrency(summary.currentBalance),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (summary.currentBalance >= 0) Slate900 else CashOutRed,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Integrated Today's In and Out row with divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "TODAY'S IN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Slate400
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "+ ${Transaction.formatCurrency(summary.todayCashIn)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = CashInGreen
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(32.dp)
                                .background(Slate100)
                        )

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "TODAY'S OUT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Slate400
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "- ${Transaction.formatCurrency(summary.todayCashOut)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = CashOutRed
                            )
                        }
                    }
                }
            }
        }

        // Two Action Buttons: Cash In & Cash Out
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cash In Button
                Button(
                    onClick = onCashInClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("cash_in_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CashInGreen),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "+",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CASH IN",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = Color.White
                        )
                    }
                }

                // Cash Out Button
                Button(
                    onClick = onCashOutClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("cash_out_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CashOutRed),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "-",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CASH OUT",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Recent Transactions Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT ACTIVITY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = Slate800
                )

                if (recentTransactions.isNotEmpty()) {
                    TextButton(
                        onClick = onViewAllClick,
                        modifier = Modifier.testTag("button_view_all_transactions")
                    ) {
                        Text(
                            text = "View All",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = UtilityBlue
                        )
                    }
                }
            }
        }

        // Recent Transactions List
        if (recentTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Slate100, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No activity yet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate800
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap Cash In or Cash Out to record a transaction.",
                            fontSize = 12.sp,
                            color = Slate500,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(recentTransactions, key = { it.id }) { transaction ->
                TransactionItemCard(
                    transaction = transaction,
                    onClick = { onTransactionClick(transaction) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun TransactionItemCard(
    transaction: Transaction,
    onClick: () -> Unit
) {
    val isCashIn = transaction.isCashIn
    val indicatorColor = if (isCashIn) CashInGreen else CashOutRed
    val containerBg = if (isCashIn) CashInGreenContainer else CashOutRedContainer

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Slate100, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("transaction_item_${transaction.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Badge (40.dp circle with soft container and arrow)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(containerBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isCashIn) "↓" else "↑",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = indicatorColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate800,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Added by ${transaction.creatorDisplayName} • ${transaction.formattedDate}",
                    fontSize = 11.sp,
                    color = Slate400,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!transaction.note.isNullOrBlank()) {
                    Text(
                        text = transaction.note,
                        fontSize = 11.sp,
                        color = Slate500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount
            Text(
                text = "${if (isCashIn) "+" else "-"} ${transaction.formattedAmount}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = indicatorColor
            )
        }
    }
}

@Composable
fun CloudSyncStatusPill(
    syncStatus: SyncStatus,
    isOnline: Boolean
) {
    val (bgColor, textColor, text) = when {
        !isOnline -> Triple(CashOutRedContainer, CashOutRed, "Offline")
        syncStatus is SyncStatus.Syncing -> Triple(UtilityBlueLight, UtilityBlue, "Syncing")
        syncStatus is SyncStatus.Synced -> Triple(Slate100, Slate600, "Cloud Synced")
        syncStatus is SyncStatus.Offline -> Triple(CashOutRedContainer, CashOutRed, "Offline (${syncStatus.pendingCount})")
        else -> Triple(Slate100, Slate600, "Ready")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.border(1.dp, Slate100, RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isOnline) OnlinePulseGreen else CashOutRed)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
