package com.cafemanager.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** تبدیل ارقام انگلیسی به فارسی برای نمایش */
fun String.fa(): String = map { if (it in '0'..'9') '۰' + (it - '0') else it }.joinToString("")
fun Int.fa(): String = toString().fa()

/** تبدیل ارقام فارسی/عربی به انگلیسی برای ذخیره و جستجو */
fun String.normDigits(): String = map {
    when (it) {
        in '۰'..'۹' -> '0' + (it - '۰')
        in '٠'..'٩' -> '0' + (it - '٠')
        else -> it
    }
}.joinToString("")

fun validNationalId(s: String): Boolean {
    if (!Regex("\\d{10}").matches(s) || s.all { it == s[0] }) return false
    val sum = (0..8).sumOf { (s[it] - '0') * (10 - it) }
    val r = sum % 11
    val c = s[9] - '0'
    return if (r < 2) c == r else c == 11 - r
}

object Jalali {
    fun toJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val gdm = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gd + gdm[gm - 1]
        var jy = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm: Int
        val jd: Int
        if (days < 186) {
            jm = 1 + days / 31
            jd = 1 + days % 31
        } else {
            jm = 7 + (days - 186) / 30
            jd = 1 + (days - 186) % 30
        }
        return Triple(jy, jm, jd)
    }

    fun toGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        val y = jy + 1595
        var days = -355668 + (365 * y) + ((y / 33) * 8) + (((y % 33) + 3) / 4) + jd +
            (if (jm < 7) (jm - 1) * 31 else ((jm - 7) * 30) + 186)
        var gy = 400 * (days / 146097)
        days %= 146097
        if (days > 36524) {
            days--
            gy += 100 * (days / 36524)
            days %= 36524
            if (days >= 365) days++
        }
        gy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            gy += (days - 1) / 365
            days = (days - 1) % 365
        }
        var gd = days + 1
        val leap = (gy % 4 == 0 && gy % 100 != 0) || gy % 400 == 0
        val sal = intArrayOf(0, 31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gm = 0
        while (gm < 13 && gd > sal[gm]) {
            gd -= sal[gm]
            gm++
        }
        return Triple(gy, gm, gd)
    }

    fun isLeap(y: Int): Boolean = (y % 33) in intArrayOf(1, 5, 9, 13, 17, 22, 26, 30)

    fun monthLength(y: Int, m: Int): Int = when {
        m <= 6 -> 31
        m <= 11 -> 30
        else -> if (isLeap(y)) 30 else 29
    }

    /** فاصله اولین روز ماه از شنبه (۰ = شنبه) */
    fun firstWeekdayOffset(jy: Int, jm: Int): Int {
        val (gy, gm, gd) = toGregorian(jy, jm, 1)
        val c = Calendar.getInstance()
        c.set(gy, gm - 1, gd)
        return (c.get(Calendar.DAY_OF_WEEK) - Calendar.SATURDAY + 7) % 7
    }

    val monthNames = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    fun date(ms: Long): String {
        val c = java.util.GregorianCalendar().apply { timeInMillis = ms }
        val (y, m, d) = toJalali(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
        return "%04d/%02d/%02d".format(Locale.US, y, m, d)
    }
}

fun formatDateTime(ms: Long): String {
    val t = SimpleDateFormat("HH:mm", Locale.US).format(Date(ms))
    return "${Jalali.date(ms)}  $t".fa()
}
