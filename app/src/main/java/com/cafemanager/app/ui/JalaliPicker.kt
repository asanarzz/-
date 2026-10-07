package com.cafemanager.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cafemanager.app.util.Jalali
import com.cafemanager.app.util.fa
import java.util.Locale

@Composable
private fun RowScope.DayCell(n: Int, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.weight(1f).aspectRatio(1f).padding(2.dp).clip(CircleShape)
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            n.fa(), style = MaterialTheme.typography.bodyMedium,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}

/** تقویم شمسی. تاریخ را به‌صورت «۱۳۷۰/۰۵/۱۲» (ارقام انگلیسی) برمی‌گرداند. */
@Composable
fun JalaliDatePickerDialog(
    initial: String,
    onPick: (String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    val thisYear = remember { Jalali.date(System.currentTimeMillis()).substringBefore("/").toInt() }
    val init = remember(initial) {
        initial.split("/").mapNotNull { it.toIntOrNull() }.takeIf { it.size == 3 } ?: listOf(1370, 1, 1)
    }
    var year by remember { mutableIntStateOf(init[0].coerceIn(1300, thisYear)) }
    var month by remember { mutableIntStateOf(init[1].coerceIn(1, 12)) }
    var day by remember { mutableIntStateOf(init[2].coerceIn(1, 31)) }
    var yearMode by remember { mutableStateOf(false) }
    val len = Jalali.monthLength(year, month)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("انتخاب تاریخ") },
        text = {
            Column {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        if (month == 1) { month = 12; year -= 1 } else month -= 1
                    }) { Icon(Icons.Default.KeyboardArrowRight, "ماه قبل") }
                    TextButton(onClick = { yearMode = !yearMode }) {
                        Text("${Jalali.monthNames[month - 1]} $year".fa())
                    }
                    IconButton(onClick = {
                        if (month == 12) { month = 1; year += 1 } else month += 1
                    }) { Icon(Icons.Default.KeyboardArrowLeft, "ماه بعد") }
                }

                if (yearMode) {
                    val years = remember { (thisYear downTo 1300).toList() }
                    val state = rememberLazyListState(
                        initialFirstVisibleItemIndex = (years.indexOf(year) - 2).coerceAtLeast(0)
                    )
                    LazyColumn(Modifier.height(260.dp), state = state) {
                        items(years) { y ->
                            TextButton(
                                onClick = { year = y; yearMode = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    y.fa(), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
                                    color = if (y == year) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                } else {
                    Row(Modifier.fillMaxWidth()) {
                        listOf("ش", "ی", "د", "س", "چ", "پ", "ج").forEach { w ->
                            Text(w, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    val offset = Jalali.firstWeekdayOffset(year, month)
                    repeat(6) { r ->
                        if (r * 7 - offset + 1 <= len) {
                            Row(Modifier.fillMaxWidth()) {
                                repeat(7) { c ->
                                    val n = r * 7 + c - offset + 1
                                    if (n in 1..len) {
                                        DayCell(n, selected = n == day.coerceAtMost(len), onClick = { day = n })
                                    } else {
                                        Spacer(Modifier.weight(1f).aspectRatio(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onPick("%04d/%02d/%02d".format(Locale.US, year, month, day.coerceAtMost(len)))
            }) { Text("تأیید") }
        },
        dismissButton = {
            Row {
                if (initial.isNotBlank()) TextButton(onClick = onClear) { Text("حذف تاریخ") }
                TextButton(onClick = onDismiss) { Text("انصراف") }
            }
        }
    )
}
