package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.data.repository.SyncStatus
import com.example.ui.theme.CashInGreen
import com.example.ui.theme.CashInGreenContainer
import com.example.ui.theme.CashOutRed
import com.example.ui.theme.CashOutRedContainer
import com.example.ui.theme.OnCashInGreenContainer
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    currentUser: UserRole,
    syncStatus: SyncStatus,
    isOnline: Boolean,
    sharedDbId: String,
    onLogout: () -> Unit,
    onManualSync: () -> Unit,
    onChangePin: (oldPin: String, newPin: String) -> Boolean
) {
    var showChangePinDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Settings",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                color = Slate800
            )
            Text(
                text = "PREFERENCES & CLOUD SYNC",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp,
                color = UtilityBlue
            )
        }

        // Logged-in User Profile Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Slate100, RoundedCornerShape(20.dp))
                .testTag("current_user_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "ACTIVE PROFILE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = Slate400
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape),
                        color = UtilityBlue
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = currentUser.displayName.take(1),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser.displayName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate800
                        )
                        Text(
                            text = currentUser.subtitle,
                            fontSize = 13.sp,
                            color = Slate500
                        )
                    }

                    Surface(
                        color = CashInGreenContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Logged In",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnCashInGreenContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showChangePinDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("button_change_pin"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate700)
                    ) {
                        Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Change PIN", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { showLogoutConfirmDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("button_logout"),
                        colors = ButtonDefaults.buttonColors(containerColor = CashOutRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Logout", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Shared Real-Time Cloud Database Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Slate100, RoundedCornerShape(20.dp))
                .testTag("cloud_database_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SHARED CLOUD DATABASE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Slate400
                    )

                    Surface(
                        color = if (isOnline) CashInGreenContainer else CashOutRedContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = if (isOnline) CashInGreen else CashOutRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isOnline) "Connected" else "Offline",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOnline) OnCashInGreenContainer else CashOutRed
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Shared Database Room ID:",
                    fontSize = 12.sp,
                    color = Slate500
                )

                Surface(
                    color = Slate50,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.dp, Slate100, RoundedCornerShape(10.dp))
                ) {
                    Text(
                        text = sharedDbId,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate700,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val statusText = when (syncStatus) {
                    is SyncStatus.Synced -> "Last synced: ${SimpleDateFormat("hh:mm:ss a", Locale.ENGLISH).format(Date(syncStatus.lastSyncTime))}"
                    is SyncStatus.Syncing -> "Synchronizing with cloud..."
                    is SyncStatus.Offline -> "Offline (${syncStatus.pendingCount} pending updates)"
                    is SyncStatus.Error -> "Sync status: ${syncStatus.message}"
                    else -> "Cloud ready"
                }

                Text(
                    text = statusText,
                    fontSize = 12.sp,
                    color = Slate500
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onManualSync,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("button_settings_sync"),
                    colors = ButtonDefaults.buttonColors(containerColor = UtilityBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sync Cloud Database Now", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Two-User Architecture Details Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Slate100, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = UtilityBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "How Cash Track Works",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate800
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "• Both Mujahid and Boss connect to the exact same cloud database.\n" +
                            "• A single shared balance is calculated: (Total Cash In - Total Cash Out).\n" +
                            "• When Main adds or edits a transaction, Boss sees it automatically.\n" +
                            "• When Boss adds or edits a transaction, Main sees it automatically.\n" +
                            "• If offline, transactions are saved locally and synced once internet returns.",
                    fontSize = 12.sp,
                    color = Slate600,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Change PIN Dialog
    if (showChangePinDialog) {
        var oldPin by remember { mutableStateOf("") }
        var newPin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = { Text("Change ${currentUser.displayName}'s PIN", fontWeight = FontWeight.Bold, color = Slate800) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = oldPin,
                        onValueChange = { oldPin = it },
                        label = { Text("Current PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_old_pin"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = UtilityBlue,
                            unfocusedBorderColor = Slate200
                        )
                    )

                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { newPin = it },
                        label = { Text("New PIN (min 4 digits)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_new_pin"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = UtilityBlue,
                            unfocusedBorderColor = Slate200
                        )
                    )

                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { confirmPin = it },
                        label = { Text("Confirm New PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_confirm_pin"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = UtilityBlue,
                            unfocusedBorderColor = Slate200
                        )
                    )

                    if (pinError != null) {
                        Text(text = pinError!!, color = CashOutRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPin != confirmPin) {
                            pinError = "New PINs do not match"
                            return@Button
                        }
                        if (newPin.length < 4) {
                            pinError = "PIN must be at least 4 digits"
                            return@Button
                        }
                        val success = onChangePin(oldPin, newPin)
                        if (success) {
                            showChangePinDialog = false
                        } else {
                            pinError = "Current PIN is incorrect"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UtilityBlue),
                    modifier = Modifier.testTag("button_save_pin")
                ) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) {
                    Text("Cancel", color = Slate600)
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = { Text("Log Out", fontWeight = FontWeight.Bold, color = Slate800) },
            text = { Text("Are you sure you want to log out of ${currentUser.displayName}'s profile?", color = Slate600) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CashOutRed),
                    modifier = Modifier.testTag("confirm_logout_button")
                ) {
                    Text("Log Out")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Cancel", color = Slate600)
                }
            }
        )
    }
}
