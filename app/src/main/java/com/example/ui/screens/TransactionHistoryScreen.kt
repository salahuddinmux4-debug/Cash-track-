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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.theme.CashInGreen
import com.example.ui.theme.CashInGreenContainer
import com.example.ui.theme.CashOutRed
import com.example.ui.theme.CashOutRedContainer
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

enum class TransactionFilter {
    ALL,
    CASH_IN,
    CASH_OUT,
    BY_MUJAHID,
    BY_BOSS
}

@Composable
fun TransactionHistoryScreen(
    transactions: List<Transaction>,
    onTransactionClick: (Transaction) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(TransactionFilter.ALL) }

    val filteredTransactions = remember(transactions, searchQuery, selectedFilter) {
        transactions.filter { tx ->
            val matchesFilter = when (selectedFilter) {
                TransactionFilter.ALL -> true
                TransactionFilter.CASH_IN -> tx.isCashIn
                TransactionFilter.CASH_OUT -> tx.isCashOut
                TransactionFilter.BY_MUJAHID -> tx.createdBy.equals("main", true) || tx.createdBy.equals("mujahid", true)
                TransactionFilter.BY_BOSS -> tx.createdBy.equals("boss", true)
            }

            val matchesSearch = if (searchQuery.isBlank()) true else {
                tx.description.contains(searchQuery, ignoreCase = true) ||
                        (tx.note?.contains(searchQuery, ignoreCase = true) == true) ||
                        tx.formattedAmount.contains(searchQuery, ignoreCase = true) ||
                        tx.creatorDisplayName.contains(searchQuery, ignoreCase = true)
            }

            matchesFilter && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("transactions_screen")
    ) {
        // Top Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Transaction History",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                color = Slate800
            )
            Text(
                text = "ALL SHARED ACTIVITY",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp,
                color = UtilityBlue
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_transactions_input"),
                placeholder = { Text("Search by description, amount, note...", color = Slate400, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Slate400)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Slate400)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = UtilityBlue,
                    unfocusedBorderColor = Slate200,
                    focusedTextColor = Slate900,
                    unfocusedTextColor = Slate900
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == TransactionFilter.ALL,
                        onClick = { selectedFilter = TransactionFilter.ALL },
                        label = { Text("All (${transactions.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = UtilityBlue,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Slate600
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedFilter == TransactionFilter.ALL,
                            borderColor = if (selectedFilter == TransactionFilter.ALL) UtilityBlue else Slate200
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == TransactionFilter.CASH_IN,
                        onClick = { selectedFilter = TransactionFilter.CASH_IN },
                        label = { Text("Cash In", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CashInGreen,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Slate600
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedFilter == TransactionFilter.CASH_IN,
                            borderColor = if (selectedFilter == TransactionFilter.CASH_IN) CashInGreen else Slate200
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == TransactionFilter.CASH_OUT,
                        onClick = { selectedFilter = TransactionFilter.CASH_OUT },
                        label = { Text("Cash Out", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CashOutRed,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Slate600
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedFilter == TransactionFilter.CASH_OUT,
                            borderColor = if (selectedFilter == TransactionFilter.CASH_OUT) CashOutRed else Slate200
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == TransactionFilter.BY_MUJAHID,
                        onClick = { selectedFilter = TransactionFilter.BY_MUJAHID },
                        label = { Text("By Mujahid", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = UtilityBlue,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Slate600
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedFilter == TransactionFilter.BY_MUJAHID,
                            borderColor = if (selectedFilter == TransactionFilter.BY_MUJAHID) UtilityBlue else Slate200
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == TransactionFilter.BY_BOSS,
                        onClick = { selectedFilter = TransactionFilter.BY_BOSS },
                        label = { Text("By Boss", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = UtilityBlue,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Slate600
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedFilter == TransactionFilter.BY_BOSS,
                            borderColor = if (selectedFilter == TransactionFilter.BY_BOSS) UtilityBlue else Slate200
                        )
                    )
                }
            }
        }

        // List of transactions
        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No matching transactions found" else "No transactions in this filter",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate800
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try clearing filters or search query",
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .testTag("transactions_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredTransactions, key = { it.id }) { transaction ->
                    DetailedTransactionCard(
                        transaction = transaction,
                        onClick = { onTransactionClick(transaction) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun DetailedTransactionCard(
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
            .testTag("history_item_${transaction.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Type pill + Added by + Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(containerBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isCashIn) "↓" else "↑",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = indicatorColor
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isCashIn) "Cash In" else "Cash Out",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = indicatorColor
                    )
                }

                Text(
                    text = "${if (isCashIn) "+" else "-"} ${transaction.formattedAmount}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = indicatorColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Description
            Text(
                text = transaction.description,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Slate800
            )

            // Optional Note
            if (!transaction.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = transaction.note,
                    fontSize = 12.sp,
                    color = Slate500
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer: Creator and Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Slate100,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Added by ${transaction.creatorDisplayName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate600,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = transaction.formattedDate,
                    fontSize = 11.sp,
                    color = Slate400
                )
            }
        }
    }
}
