package com.example.ui.utils

import java.text.NumberFormat
import java.util.Locale

object Formatters {
    fun formatIndianCurrency(amount: Double): String {
        return try {
            val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            formatter.maximumFractionDigits = 0
            formatter.format(amount)
        } catch (e: Exception) {
            "₹%,.0f".format(Locale.US, amount)
        }
    }

    fun formatRelativeTime(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val minutes = diff / 60000
        return when {
            minutes < 1 -> "Just now"
            minutes == 1L -> "1 minute ago"
            minutes < 60 -> "$minutes minutes ago"
            else -> {
                val hours = minutes / 60
                if (hours == 1L) "1 hour ago" else "$hours hours ago"
            }
        }
    }
}
