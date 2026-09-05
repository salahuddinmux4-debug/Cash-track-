package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Transaction
import com.example.data.model.UserRole
import com.example.ui.theme.CashInGreen
import com.example.ui.theme.CashInGreenContainer
import com.example.ui.theme.CashOutRed
import com.example.ui.theme.CashOutRedContainer
import com.example.ui.theme.OnCashInGreenContainer
import com.example.ui.theme.OnCashOutRedContainer
import com.example.ui.theme.Slate50
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
import com.example.ui.theme.UtilityBlue
import com.example.ui.theme.UtilityBlueLight
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTransactionDialog(
    isCashIn: Boolean,
    currentBalance: Double,
    currentUser: UserRole,
    onDismiss: () -> Unit,
    onSave: (amount: Double, description: String, dateMillis: Long, note: String?) -> Unit
) {
    var amountInput by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }
    val dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showNegativeWarningDialog by remember { mutableStateOf(false) }

    val primaryColor = if (isCashIn) CashInGreen else CashOutRed
    val headerTitle = if (isCashIn) "Add Cash In" else "Add Cash Out"
    val formattedDate = remember(dateMillis) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(Date(dateMillis))
    }

    val quickSuggestions = if (isCashIn) {
        listOf("Sale payment", "Client advance", "Customer collection", "Cash recovery", "Bank deposit cash", "Personal cash")
    } else {
        listOf("Office expense", "Shop rent", "Supplies", "Fuel / Transport", "Tea & Refreshments", "Utility bill", "Staff salary", "Vendor payment")
    }

    fun submitTransaction() {
        val amount = amountInput.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            errorMessage = "Please enter a valid positive amount"
            return
        }
        if (descriptionInput.isBlank()) {
            errorMessage = "Please enter a description"
            return
        }

        // Validate Cash Out against current balance
        if (!isCashIn && amount > currentBalance) {
            showNegativeWarningDialog = true
            return
        }

        onSave(amount, descriptionInput, dateMillis, noteInput)
        onDismiss()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .border(1.dp, Slate100, RoundedCornerShape(24.dp))
                .testTag(if (isCashIn) "dialog_add_cash_in" else "dialog_add_cash_out"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Header with pill badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isCashIn) CashInGreenContainer else CashOutRedContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isCashIn) "↓" else "↑",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = headerTitle,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp,
                                color = Slate800
                            )
                            Text(
                                text = "Recorded by ${currentUser.displayName}",
                                fontSize = 12.sp,
                                color = Slate500
                            )
                        }
                    }

                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Slate500)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Amount Input
                Text(
                    text = "AMOUNT (PKR) *",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Slate400
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' }) {
                            amountInput = input
                            errorMessage = null
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_amount"),
                    leadingIcon = {
                        Text(
                            text = "Rs.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = primaryColor,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    },
                    placeholder = { Text("0", fontSize = 17.sp, color = Slate400) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Slate200,
                        focusedTextColor = Slate900,
                        unfocusedTextColor = Slate900
                    )
                )

                // Live formatted currency preview
                val previewAmount = amountInput.toDoubleOrNull()
                if (previewAmount != null && previewAmount > 0) {
                    Text(
                        text = "Formatted: ${Transaction.formatCurrency(previewAmount)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description Input
                Text(
                    text = "DESCRIPTION *",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Slate400
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = descriptionInput,
                    onValueChange = {
                        descriptionInput = it
                        errorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_description"),
                    placeholder = { Text("e.g. Sale payment, Office expense", color = Slate400) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = {
                        Icon(Icons.Default.Description, contentDescription = null, tint = Slate400)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Slate200,
                        focusedTextColor = Slate900,
                        unfocusedTextColor = Slate900
                    )
                )

                // Quick suggestions chips
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Quick suggestions:",
                    fontSize = 11.sp,
                    color = Slate500
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickSuggestions.forEach { suggestion ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Slate200, RoundedCornerShape(8.dp))
                                .clickable { descriptionInput = suggestion },
                            color = Slate50,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = suggestion,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                color = Slate700
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date/Time Display
                Text(
                    text = "DATE & TIME",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Slate400
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Slate100, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    color = Slate50
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Slate400, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = formattedDate, fontSize = 13.sp, color = Slate700)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Note Input (Optional)
                Text(
                    text = "NOTE (OPTIONAL)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Slate400
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_note"),
                    placeholder = { Text("Add any extra details or reference...", color = Slate400) },
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Slate200,
                        focusedTextColor = Slate900,
                        unfocusedTextColor = Slate900
                    )
                )

                // Error message
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage!!,
                        color = CashOutRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Submit Button
                Button(
                    onClick = { submitTransaction() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_transaction_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                ) {
                    Text(
                        text = if (isCashIn) "↓  Save Cash In" else "↑  Save Cash Out",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }

    // High Balance Warning Alert for Cash Out exceeding balance
    if (showNegativeWarningDialog) {
        val amount = amountInput.toDoubleOrNull() ?: 0.0
        val resultingBalance = currentBalance - amount

        AlertDialog(
            onDismissRequest = { showNegativeWarningDialog = false },
            icon = {
                Icon(Icons.Default.Warning, contentDescription = null, tint = CashOutRed, modifier = Modifier.size(36.dp))
            },
            title = {
                Text(
                    text = "Warning: Negative Balance",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Slate800
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Cash Out amount exceeds the current shared cash balance!",
                        color = Slate700,
                        fontWeight = FontWeight.Medium
                    )
                    Surface(
                        color = WarningAmberBg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Current Balance: ${Transaction.formatCurrency(currentBalance)}", fontSize = 13.sp, color = Slate800)
                            Text("Requested Cash Out: ${Transaction.formatCurrency(amount)}", fontSize = 13.sp, color = CashOutRed, fontWeight = FontWeight.Bold)
                            Text("Resulting Balance: ${Transaction.formatCurrency(resultingBalance)}", fontSize = 13.sp, color = CashOutRed, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = "Do you still want to proceed with this Cash Out?",
                        fontSize = 13.sp,
                        color = Slate500
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNegativeWarningDialog = false
                        onSave(amount, descriptionInput, dateMillis, noteInput)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CashOutRed),
                    modifier = Modifier.testTag("confirm_negative_cash_out_button")
                ) {
                    Text("Confirm Cash Out")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showNegativeWarningDialog = false },
                    modifier = Modifier.testTag("cancel_negative_cash_out_button")
                ) {
                    Text("Cancel", color = Slate600)
                }
            }
        )
    }
}

@Composable
fun EditTransactionDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit,
    onDelete: (String) -> Unit
) {
    var amountInput by remember { mutableStateOf(transaction.amount.toLong().toString()) }
    var descriptionInput by remember { mutableStateOf(transaction.description) }
    var noteInput by remember { mutableStateOf(transaction.note ?: "") }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isCashIn = transaction.isCashIn
    val primaryColor = if (isCashIn) CashInGreen else CashOutRed

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .border(1.dp, Slate100, RoundedCornerShape(24.dp))
                .testTag("dialog_edit_transaction"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isCashIn) CashInGreenContainer else CashOutRedContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Edit ${if (isCashIn) "Cash In" else "Cash Out"}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp,
                                color = Slate800
                            )
                            Text(
                                text = "Added by ${transaction.creatorDisplayName} • ${transaction.formattedDate}",
                                fontSize = 12.sp,
                                color = Slate500
                            )
                        }
                    }

                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.testTag("button_trigger_delete")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CashOutRed)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Amount
                Text(
                    text = "AMOUNT (PKR) *",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Slate400
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' }) {
                            amountInput = input
                            errorMessage = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("edit_input_amount"),
                    leadingIcon = {
                        Text(
                            text = "Rs.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = primaryColor,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Slate200,
                        focusedTextColor = Slate900,
                        unfocusedTextColor = Slate900
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = "DESCRIPTION *",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Slate400
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = descriptionInput,
                    onValueChange = {
                        descriptionInput = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth().testTag("edit_input_description"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Slate200,
                        focusedTextColor = Slate900,
                        unfocusedTextColor = Slate900
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Note
                Text(
                    text = "NOTE (OPTIONAL)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Slate400
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    modifier = Modifier.fillMaxWidth().testTag("edit_input_note"),
                    shape = RoundedCornerShape(14.dp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Slate200,
                        focusedTextColor = Slate900,
                        unfocusedTextColor = Slate900
                    )
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = errorMessage!!, color = CashOutRed, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate700)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val newAmount = amountInput.toDoubleOrNull()
                            if (newAmount == null || newAmount <= 0) {
                                errorMessage = "Please enter a valid positive amount"
                                return@Button
                            }
                            if (descriptionInput.isBlank()) {
                                errorMessage = "Please enter a description"
                                return@Button
                            }

                            onSave(
                                transaction.copy(
                                    amount = newAmount,
                                    description = descriptionInput.trim(),
                                    note = noteInput.trim().ifEmpty { null },
                                    updatedAt = System.currentTimeMillis()
                                )
                            )
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f).height(50.dp).testTag("save_edit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        DeleteConfirmationDialog(
            transaction = transaction,
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDelete(transaction.id)
                onDismiss()
            }
        )
    }
}

@Composable
fun DeleteConfirmationDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Delete, contentDescription = null, tint = CashOutRed, modifier = Modifier.size(36.dp))
        },
        title = {
            Text("Delete Transaction", fontWeight = FontWeight.Bold, color = Slate800)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Are you sure you want to permanently delete this transaction?", color = Slate600)
                Surface(
                    color = Slate50,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Slate100, RoundedCornerShape(10.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "${if (transaction.isCashIn) "Cash In" else "Cash Out"}: ${transaction.formattedAmount}",
                            fontWeight = FontWeight.Bold,
                            color = if (transaction.isCashIn) CashInGreen else CashOutRed
                        )
                        Text(text = transaction.description, fontSize = 13.sp, color = Slate800)
                        Text(text = "Created by ${transaction.creatorDisplayName} on ${transaction.formattedDate}", fontSize = 12.sp, color = Slate500)
                    }
                }
                Text(
                    text = "The shared balance will recalculate automatically across both mobiles.",
                    fontSize = 12.sp,
                    color = Slate500
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = CashOutRed),
                modifier = Modifier.testTag("confirm_delete_button")
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_delete_button")
            ) {
                Text("Cancel", color = Slate600)
            }
        }
    )
}
