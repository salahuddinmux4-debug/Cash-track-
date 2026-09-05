package com.example.data.model

import com.squareup.moshi.JsonClass
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@JsonClass(generateAdapter = true)
data class Transaction(
    val id: String,
    val amount: Double,
    val type: String, // "cash_in" or "cash_out"
    val description: String,
    val note: String? = null,
    val createdBy: String, // "main" or "boss"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isCashIn: Boolean get() = type.equals("cash_in", ignoreCase = true)
    val isCashOut: Boolean get() = type.equals("cash_out", ignoreCase = true)

    val creatorDisplayName: String
        get() = when (createdBy.lowercase(Locale.ROOT)) {
            "main", "mujahid" -> "Mujahid"
            "boss" -> "Boss"
            else -> createdBy.replaceFirstChar { it.uppercase() }
        }

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
            return sdf.format(Date(createdAt))
        }

    val formattedAmount: String
        get() = formatCurrency(amount)

    companion object {
        const val TYPE_CASH_IN = "cash_in"
        const val TYPE_CASH_OUT = "cash_out"

        fun formatCurrency(amount: Double): String {
            val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
                maximumFractionDigits = 0
                minimumFractionDigits = 0
            }
            return "Rs. ${formatter.format(amount)}"
        }
    }
}

enum class UserRole(val id: String, val displayName: String, val subtitle: String) {
    MAIN("main", "Mujahid", "Main User"),
    BOSS("boss", "Boss", "Boss User");

    companion object {
        fun fromId(id: String?): UserRole {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: MAIN
        }
    }
}
