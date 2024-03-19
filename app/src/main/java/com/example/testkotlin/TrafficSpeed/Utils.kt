package com.example.testkotlin.TrafficSpeed

import java.util.Locale

object Utils {
    private const val B: Long = 1
    private const val KB: Long = B * 1024
    private const val MB: Long = KB * 1024
    private const val GB: Long = MB * 1024

    fun parseSpeed(bytes: Double, inBits: Boolean): String {
        val value = if (inBits) bytes * 8 else bytes
        return when {
            value < KB -> String.format(Locale.getDefault(), "%.1f ${if (inBits) "b" else "B"}/s", value)
            value < MB -> String.format(Locale.getDefault(), "%.1f K${if (inBits) "b" else "B"}/s", value / KB)
            value < GB -> String.format(Locale.getDefault(), "%.1f M${if (inBits) "b" else "B"}/s", value / MB)
            else -> String.format(Locale.getDefault(), "%.2f G${if (inBits) "b" else "B"}/s", value / GB)
        }
    }
}