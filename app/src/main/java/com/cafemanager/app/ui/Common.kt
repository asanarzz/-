package com.cafemanager.app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cafemanager.app.data.Customer
import com.cafemanager.app.util.fa
import com.cafemanager.app.util.normDigits
import java.util.Locale

val paymentMethods = listOf("نقدی", "کارت به کارت", "کارتخوان", "سایر")

fun money(v: Long): String = "%,d".format(Locale.US, v).replace(',', '٬').fa() + " تومان"

fun String.toMoney(): Long = normDigits().filter { it.isDigit() }.take(12).toLongOrNull() ?: 0L

fun shareText(ctx: Context, text: String) {
    val i = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    ctx.startActivity(Intent.createChooser(i, "اشتراک‌گذاری"))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubScreen(
    title: String,
    onBack: () -> Unit,
    fab: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    BackHandler(onBack = onBack)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "بازگشت") }
                }
            )
        },
        floatingActionButton = fab,
        content = content
    )
}

@Composable
fun AppField(
    label: String, value: String, onChange: (String) -> Unit,
    kb: KeyboardType = KeyboardType.Text, lines: Int = 1
) {
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = kb),
        singleLine = lines == 1, minLines = lines,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun MoneyField(label: String, value: String, onChange: (String) -> Unit) {
    AppField(label, value, { onChange(it.normDigits().filter { c -> c.isDigit() }.take(12)) }, KeyboardType.Number)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipsRow(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { o ->
            FilterChip(selected = o == selected, onClick = { onSelect(o) }, label = { Text(o) })
        }
    }
}

@Composable
fun PeriodChips(period: Int, onSelect: (Int) -> Unit) {
    val labels = listOf("امروز", "این هفته", "این ماه")
    ChipsRow(labels, labels[period]) { onSelect(labels.indexOf(it)) }
}

@Composable
fun KV(k: String, v: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(k)
        Text(v, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
fun CustomerPickerDialog(customers: List<Customer>, onPick: (Customer) -> Unit, onDismiss: () -> Unit) {
    var q by remember { mutableStateOf("") }
    val n = q.normDigits().trim()
    val shown = customers.filter { c ->
        n.isEmpty() || "${c.firstName} ${c.lastName}".contains(n) || c.mobile.contains(n) || c.nationalId.contains(n)
    }.take(50)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("انتخاب مشتری") },
        text = {
            Column {
                OutlinedTextField(q, { q = it }, label = { Text("جستجو") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                if (shown.isEmpty()) Text("مشتری‌ای پیدا نشد")
                LazyColumn(Modifier.heightIn(max = 320.dp)) {
                    items(shown, key = { it.id }) { c ->
                        TextButton(onClick = { onPick(c) }, modifier = Modifier.fillMaxWidth()) {
                            Text("${c.firstName} ${c.lastName}", modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("بستن") } }
    )
}

fun copyText(ctx: Context, clip: ClipboardManager, text: String) {
    if (text.isBlank()) {
        Toast.makeText(ctx, "کادر خالی است", Toast.LENGTH_SHORT).show()
        return
    }
    clip.setText(AnnotatedString(text))
    Toast.makeText(ctx, "کپی شد", Toast.LENGTH_SHORT).show()
}

private fun safeStart(ctx: Context, i: Intent): Boolean = try {
    ctx.startActivity(i)
    true
} catch (e: Exception) {
    false
}

fun dialNumber(ctx: Context, n: String) {
    if (!safeStart(ctx, Intent(Intent.ACTION_DIAL, Uri.parse("tel:$n")))) {
        Toast.makeText(ctx, "برنامه تماس پیدا نشد", Toast.LENGTH_SHORT).show()
    }
}

fun smsNumber(ctx: Context, n: String) {
    if (!safeStart(ctx, Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$n")))) {
        Toast.makeText(ctx, "برنامه پیامک پیدا نشد", Toast.LENGTH_SHORT).show()
    }
}

/**
 * ایتا از روی شماره موبایل لینک مستقیم ندارد؛ فقط با آیدی (نام کاربری) می‌شود وارد پیوی شد.
 * با آیدی: پیوی همان فرد باز می‌شود. بدون آیدی: ایتا باز می‌شود و شماره کپی می‌شود.
 */
fun openEitaa(ctx: Context, clip: ClipboardManager, number: String, eitaaId: String) {
    val id = eitaaId.trim().removePrefix("https://").removePrefix("eitaa.com/").removePrefix("@").trim()
    if (id.isNotEmpty()) {
        val uri = Uri.parse("https://eitaa.com/$id")
        if (safeStart(ctx, Intent(Intent.ACTION_VIEW, uri).setPackage("ir.eitaa.messenger"))) return
        if (safeStart(ctx, Intent(Intent.ACTION_VIEW, uri))) return
        Toast.makeText(ctx, "ایتا یا مرورگر پیدا نشد", Toast.LENGTH_LONG).show()
        return
    }
    clip.setText(AnnotatedString(number))
    val launch = ctx.packageManager.getLaunchIntentForPackage("ir.eitaa.messenger")
    if (launch != null && safeStart(ctx, launch)) {
        Toast.makeText(ctx, "آیدی ایتا ثبت نشده؛ شماره کپی شد. آیدی را ثبت کنید تا پیوی مستقیم باز شود", Toast.LENGTH_LONG).show()
    } else {
        Toast.makeText(ctx, "ایتا روی گوشی نصب نیست", Toast.LENGTH_LONG).show()
    }
}
