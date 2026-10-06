package com.cafemanager.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cafemanager.app.AppViewModel
import com.cafemanager.app.BizViewModel
import com.cafemanager.app.CartLine
import com.cafemanager.app.data.Customer
import com.cafemanager.app.util.fa

@Composable
fun SaleScreen(vm: AppViewModel, biz: BizViewModel, onOpenServices: () -> Unit, modifier: Modifier = Modifier) {
    val services by biz.services.collectAsState()
    val customers by biz.allCustomers.collectAsState()
    val ctx = LocalContext.current
    var customer by remember { mutableStateOf<Customer?>(null) }
    val qty = remember { mutableStateMapOf<Long, Int>() }
    var search by remember { mutableStateOf("") }
    var picker by remember { mutableStateOf(false) }
    var checkout by remember { mutableStateOf(false) }
    var doneId by remember { mutableStateOf<Long?>(null) }

    val lines = services.filter { (qty[it.id] ?: 0) > 0 }.map { CartLine(it, qty[it.id] ?: 0) }
    val subtotal = lines.sumOf { it.service.price * it.qty }
    val s = search.trim()
    val shown = services.filter { s.isEmpty() || it.name.contains(s) || it.category.contains(s) }

    Column(modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = { picker = true }, modifier = Modifier.weight(1f)) {
                    Text(customer?.let { "${it.firstName} ${it.lastName}" } ?: "انتخاب مشتری (اختیاری)")
                }
                if (customer != null) TextButton(onClick = { customer = null }) { Text("حذف") }
            }
            OutlinedTextField(search, { search = it }, label = { Text("جستجوی خدمت") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
        }

        if (services.isEmpty()) {
            Column(
                Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("هنوز خدمتی تعریف نشده است.")
                Button(onClick = onOpenServices) { Text("تعریف خدمت") }
            }
        } else {
            LazyColumn(
                Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(shown, key = { it.id }) { sv ->
                    val q = qty[sv.id] ?: 0
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(sv.name, style = MaterialTheme.typography.titleMedium)
                                Text("${money(sv.price)} / ${sv.unit}", style = MaterialTheme.typography.bodySmall)
                            }
                            OutlinedButton(
                                onClick = { if (q > 0) qty[sv.id] = q - 1 },
                                contentPadding = PaddingValues(0.dp), modifier = Modifier.size(40.dp)
                            ) { Text("−") }
                            Text(q.fa(), modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)
                            Button(
                                onClick = { qty[sv.id] = q + 1 },
                                contentPadding = PaddingValues(0.dp), modifier = Modifier.size(40.dp)
                            ) { Text("+") }
                        }
                    }
                }
            }
        }

        Surface(tonalElevation = 3.dp) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("جمع")
                    Text(money(subtotal), style = MaterialTheme.typography.titleMedium)
                }
                Button(enabled = lines.isNotEmpty(), onClick = { checkout = true }) { Text("ادامه و ثبت") }
            }
        }
    }

    if (picker) CustomerPickerDialog(customers, onPick = { customer = it; picker = false }, onDismiss = { picker = false })

    if (checkout) CheckoutDialog(
        subtotal = subtotal, lines = lines, customer = customer, biz = biz,
        onClose = { checkout = false },
        onSaved = { id ->
            checkout = false
            qty.clear()
            customer = null
            doneId = id
        }
    )

    doneId?.let { id ->
        AlertDialog(
            onDismissRequest = { doneId = null },
            title = { Text("فاکتور ثبت شد") },
            text = { Text("می‌توانید رسید را برای مشتری بفرستید.") },
            confirmButton = {
                TextButton(onClick = {
                    biz.receipt(id, vm.profile) { shareText(ctx, it) }
                    doneId = null
                }) { Text("اشتراک رسید") }
            },
            dismissButton = { TextButton(onClick = { doneId = null }) { Text("بستن") } }
        )
    }
}

@Composable
private fun CheckoutDialog(
    subtotal: Long, lines: List<CartLine>, customer: Customer?, biz: BizViewModel,
    onClose: () -> Unit, onSaved: (Long) -> Unit
) {
    var discount by remember { mutableStateOf("") }
    var paidText by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("نقدی") }
    var note by remember { mutableStateOf("") }
    var err by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    val disc = discount.toMoney().coerceAtMost(subtotal)
    val total = subtotal - disc
    val paid = if (paidText.isBlank()) total else paidText.toMoney().coerceAtMost(total)

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("ثبت فاکتور") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(customer?.let { "مشتری: ${it.firstName} ${it.lastName}" } ?: "مشتری متفرقه")
                KV("جمع خدمات", money(subtotal))
                MoneyField("تخفیف (تومان)", discount, { discount = it })
                KV("مبلغ نهایی", money(total))
                MoneyField("مبلغ پرداخت‌شده (خالی = کل مبلغ)", paidText, { paidText = it })
                if (paid < total) Text("باقی‌مانده ${money(total - paid)} به‌عنوان بدهی ثبت می‌شود.")
                Text("روش پرداخت")
                ChipsRow(paymentMethods, method) { method = it }
                AppField("توضیح", note, { note = it })
                err?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(enabled = !saving, onClick = {
                if (paid < total && customer == null) {
                    err = "برای ثبت بدهی باید مشتری انتخاب شود"
                } else {
                    saving = true
                    biz.saveInvoice(customer, lines, disc, paid, method, note.trim(), onSaved)
                }
            }) { Text("ثبت") }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("انصراف") } }
    )
}
