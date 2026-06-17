package com.example.ruvo_app.core.utils

import java.text.NumberFormat
import java.util.Locale

object PriceFormatter {
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }

    fun format(price: Double): String {
        return currencyFormat.format(price)
    }

    fun formatRange(min: Double, max: Double): String {
        return "${format(min)} - ${format(max)}"
    }
}
