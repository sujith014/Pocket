package com.sujith.pocket.ui.extensions


import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormats {
    const val DEFAULT = "dd MMM yyyy, hh:mm a"
    const val DATE_ONLY = "dd MMM yyyy"
    const val TIME_ONLY = "hh:mm a"
    const val FULL = "EEE, dd MMM yyyy - hh:mm a"
    const val ISO = "yyyy-MM-dd'T'HH:mm:ss'Z'"
}

/**
 * Convert Long (timestamp millis) → formatted date
 */
fun Long.toFormattedDate(
    pattern: String = DateFormats.DEFAULT,
    locale: Locale = Locale.getDefault()
): String {
    return try {
        val sdf = SimpleDateFormat(pattern, locale)
        sdf.format(Date(this))
    } catch (e: Exception) {
        ""
    }
}

/**
 * Convert String (millis) → formatted date
 */
fun String.toFormattedDate(
    pattern: String = DateFormats.DEFAULT,
    locale: Locale = Locale.getDefault()
): String {
    return this.toLongOrNull()?.toFormattedDate(pattern, locale) ?: ""
}