package com.cafemanager.app.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import java.text.Collator
import java.util.Locale

/** دکمهٔ «مرتب‌سازی» با منوی کشویی؛ گزینهٔ انتخاب‌شده با ✓ مشخص می‌شود. */
@Composable
fun SortMenu(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = { open = true }) { Text("مرتب‌سازی: " + options[selected], maxLines = 1) }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEachIndexed { i, o ->
                DropdownMenuItem(
                    text = { Text((if (i == selected) "✓ " else "") + o) },
                    onClick = { open = false; onSelect(i) }
                )
            }
        }
    }
}

/** مقایسهٔ حروف الفبای فارسی (الف تا ی). */
val faCollator: Collator = Collator.getInstance(Locale("fa")).apply { strength = Collator.PRIMARY }
