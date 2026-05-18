package com.tuapp.pollenwidget

import android.graphics.Color

data class PollenData(
    val grass: Double,
    val birch: Double,
    val alder: Double,
    val timestamp: String
) {
    fun maxLevel(): PollenLevel {
        val max = maxOf(grass, birch, alder)
        return when {
            max < 10  -> PollenLevel.LOW
            max < 50  -> PollenLevel.MODERATE
            max < 200 -> PollenLevel.HIGH
            else      -> PollenLevel.VERY_HIGH
        }
    }
}

enum class PollenLevel(val label: String, val bgColor: Int) {
    LOW      ("🟢 Polen BAJO",     Color.parseColor("#2E7D32")),
    MODERATE ("🟡 Polen MODERADO", Color.parseColor("#F9A825")),
    HIGH     ("🟠 Polen ALTO",     Color.parseColor("#E65100")),
    VERY_HIGH("🔴 Polen MUY ALTO", Color.parseColor("#B71C1C"))
}
