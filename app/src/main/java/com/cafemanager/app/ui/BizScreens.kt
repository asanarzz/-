package com.cafemanager.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cafemanager.app.AppViewModel
import com.cafemanager.app.BizViewModel
import com.cafemanager.app.data.Debt
import com.cafemanager.app.data.Service
import com.cafemanager.app.util.fa
import com.cafemanager.app.util.formatDateTime

val serviceCategories = listOf(
    "ثبت‌نام", "پرینت", "کپی", "اسکن", "تایپ", "لمینت", "عکس",
    "خدمات اینترنتی", "خدمات دولتی", "خدمات خودرو", "خدمات بیمه", "سایر"
)
val serviceUnits = listOf("عدد", "صفحه", "ساعت", "مورد")
val expenseCategories = listOf("اجاره", "قبض", "حقوق", "کاغذ و جوهر", "تعمیرات", "تبلیغات", "سایر")
val incomeCategories = listOf("خدمات", "فروش", "سایر")

// ====================== بیشتر ======================
@Composable
fun MoreScreen(onOpen: (String) -> Unit, modifier: Modifier = Modifier) {
    val items = listOf(
        "services" to "خدمات و قیمت‌ها",
        "invoices" to "فاکتورها و رسیدها",
        "debts" to "مطالبات (بدهی مشتریان)",
        "cash" to "صندوق روزانه",
        "reports" to "گزارش‌ها",
        "settings" to "تنظیمات"
    )
    Column(modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { (k, l) ->
            Card(onClick = { onOpen(k) }, modifier = Modifier.fillMaxWidth()) {
                Text(l, modifier = Modifier.padding(20.dp), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

// ====================== خدمات ======================
@Composable
fun ServicesScreen(biz: BizViewModel, onBack: () -> Unit) {
    val list by biz.services.collectAsState()
    var editing by remember { mutableStateOf<Service?>(null) }
    var adding by remember { mutableStateOf(false) }

    SubScreen("خدمات", onBack, fab = {
        FloatingActionButton(onClick = { adding = true }) { Icon(Icons.Default.Add, "خدمت جدید") }
    }) { pad ->
        if (list.isEmpty()) {
            Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("هنوز خدمتی تعریف نشده است")
            }
        } else {
            LazyColumn(
                Modifier.padding(pad).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(list, key = { it.id }) { s ->
                    Card(onClick = { editing = s }, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(s.name, style = MaterialTheme.typography.titleMedium)
                            Text("${money(s.price)} / ${s.unit} — ${s.category}")
                        }
                    }
                }
            }
        }
    }
    if (adding) ServiceDialog(null, biz) { adding = false }
    editing?.let { ServiceDialog(it, biz) { editing = null } }
}

@Composable
private fun ServiceDialog(base: Service?, biz: BizViewModel, onClose: () -> Unit) {
    var name by remember { mutableStateOf(base?.name ?: "") }
    var price by remember { mutableStateOf(base?.price?.toString() ?: "") }
    var unit by remember { mutableStateOf(base?.unit ?: "عدد") }
    var cat by remember { mutableStateOf(base?.category ?: "سایر") }
    var desc by remember { mutableStateOf(base?.description ?: "") }
    var err by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(if (base == null) "خدمت جدید" else "ویرایش خدمت") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppField("نام خدمت", name, { name = it })
                MoneyField("قیمت (تومان)", price, { price = it })
                Text("واحد")
                ChipsRow(serviceUnits, unit) { unit = it }
                Text("دسته‌بندی")
                ChipsRow(serviceCategories, cat) { cat = it }
                AppField("توضیح", desc, { desc = it })
                err?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val p = price.toMoney()
                if (name.isBlank()) {
                    err = "نام خدمت را وارد کنید"
                } else if (p <= 0) {
                    err = "قیمت را وارد کنید"
                } else {
                    val s = (base ?: Service(name = "", price = 0)).copy(
                        name = name.trim(), price = p, unit = unit, category = cat, description = desc.trim()
                    )
                    biz.saveService(s, onClose)
                }
            }) { Text("ذخیره") }
        },
        dismissButton = {
            Row {
                if (base != null) TextButton(onClick = { biz.deleteService(base, onClose) }) { Text("حذف") }
                TextButton(onClick = onClose) { Text("انصراف") }
            }
        }
    )
}

// ====================== فاکتورها ======================
@Composable
fun InvoicesScreen(vm: AppViewModel, biz: BizViewModel, onBack: () -> Unit) {
    val list by biz.invoices.collectAsState()
    val ctx = LocalContext.current
    SubScreen("فاکتورها و رسیدها", onBack) { pad ->
        if (list.isEmpty()) {
            Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("هنوز فاکتوری ثبت نشده است")
            }
        } else {
            LazyColumn(
                Modifier.padding(pad).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(list, key = { it.id }) { inv ->
                    Card(
                        onClick = { biz.receipt(inv.id, vm.profile) { shareText(ctx, it) } },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "فاکتور ${inv.id.toString().fa()} — ${inv.customerName.ifBlank { "مشتری متفرقه" }}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text("${money(inv.total)} • ${formatDateTime(inv.createdAt)}")
                            Text("برای اشتراک رسید لمس کنید", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

// ====================== دخل و خرج ======================
@Composable
fun LedgerScreen(biz: BizViewModel, modifier: Modifier = Modifier) {
    val list by biz.txns.collectAsState()
    val period by biz.period.collectAsState()
    var dialog by remember { mutableStateOf<Boolean?>(null) }
    val counted = list.filter { it.inProfit }
    val income = counted.filter { it.isIncome }.sumOf { it.amount }
    val expense = counted.filter { !it.isIncome }.sumOf { it.amount }

    Column(modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Spacer(Modifier.height(4.dp))
        PeriodChips(period) { biz.period.value = it }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { dialog = true }, modifier = Modifier.weight(1f)) { Text("ثبت درآمد") }
            Button(onClick = { dialog = false }, modifier = Modifier.weight(1f)) { Text("ثبت هزینه") }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                KV("درآمد", money(income))
                KV("هزینه", money(expense))
                KV("سود", money(income - expense))
            }
        }
        if (list.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("تراکنشی ثبت نشده است") }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                items(list, key = { it.id }) { t ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(t.title, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "${t.category} • ${t.method} • ${formatDateTime(t.createdAt)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Text((if (t.isIncome) "+" else "−") + money(t.amount))
                        }
                    }
                }
            }
        }
    }
    dialog?.let { TxnDialog(it, biz) { dialog = null } }
}

@Composable
private fun TxnDialog(isIncome: Boolean, biz: BizViewModel, onClose: () -> Unit) {
    var amount by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    val cats = if (isIncome) incomeCategories else expenseCategories
    var cat by remember { mutableStateOf(cats.last()) }
    var method by remember { mutableStateOf("نقدی") }
    var note by remember { mutableStateOf("") }
    var err by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(if (isIncome) "ثبت درآمد" else "ثبت هزینه") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MoneyField("مبلغ (تومان)", amount, { amount = it })
                AppField("عنوان", title, { title = it })
                Text("دسته‌بندی")
                ChipsRow(cats, cat) { cat = it }
                Text("روش پرداخت")
                ChipsRow(paymentMethods, method) { method = it }
                AppField("توضیح", note, { note = it })
                err?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val a = amount.toMoney()
                if (a <= 0) {
                    err = "مبلغ را وارد کنید"
                } else if (title.isBlank()) {
                    err = "عنوان را وارد کنید"
                } else {
                    biz.addTxn(isIncome, a, title.trim(), cat, method, note.trim(), onDone = onClose)
                }
            }) { Text("ذخیره") }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("انصراف") } }
    )
}

// ====================== مطالبات ======================
@Composable
fun DebtsScreen(biz: BizViewModel, onBack: () -> Unit) {
    val debts by biz.debts.collectAsState()
    val customers by biz.allCustomers.collectAsState()
    var paying by remember { mutableStateOf<Debt?>(null) }
    var adding by remember { mutableStateOf(false) }

    SubScreen("مطالبات", onBack, fab = {
        FloatingActionButton(onClick = { adding = true }) { Icon(Icons.Default.Add, "ثبت بدهی") }
    }) { pad ->
        LazyColumn(
            Modifier.padding(pad).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                Text("جمع مطالبات: " + money(debts.sumOf { it.amount - it.paid }), style = MaterialTheme.typography.titleMedium)
            }
            if (debts.isEmpty()) item { Text("بدهی بازی وجود ندارد") }
            items(debts, key = { it.id }) { d ->
                Card(onClick = { paying = d }, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(d.customerName, style = MaterialTheme.typography.titleMedium)
                        Text("مانده: ${money(d.amount - d.paid)} از ${money(d.amount)}")
                        if (d.reason.isNotBlank()) Text(d.reason, style = MaterialTheme.typography.bodySmall)
                        Text(
                            "تاریخ: ${formatDateTime(d.createdAt)}" + (if (d.dueDate.isNotBlank()) " • مهلت: ${d.dueDate.fa()}" else ""),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
    paying?.let { PayDebtDialog(it, biz) { paying = null } }
    if (adding) AddDebtDialog(customers, biz) { adding = false }
}

@Composable
private fun PayDebtDialog(d: Debt, biz: BizViewModel, onClose: () -> Unit) {
    val remaining = d.amount - d.paid
    var amount by remember { mutableStateOf(remaining.toString()) }
    var method by remember { mutableStateOf("نقدی") }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("پرداخت بدهی — ${d.customerName}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                KV("مانده بدهی", money(remaining))
                MoneyField("مبلغ پرداخت (تومان)", amount, { amount = it })
                Text("روش پرداخت")
                ChipsRow(paymentMethods, method) { method = it }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val a = amount.toMoney()
                if (a > 0) biz.payDebt(d, a, method, onClose)
            }) { Text("ثبت پرداخت") }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("انصراف") } }
    )
}

@Composable
private fun AddDebtDialog(customers: List<com.cafemanager.app.data.Customer>, biz: BizViewModel, onClose: () -> Unit) {
    var customer by remember { mutableStateOf<com.cafemanager.app.data.Customer?>(null) }
    var picker by remember { mutableStateOf(false) }
    var amount by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var due by remember { mutableStateOf("") }
    var err by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("ثبت بدهی") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { picker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(customer?.let { "${it.firstName} ${it.lastName}" } ?: "انتخاب مشتری")
                }
                MoneyField("مبلغ بدهی (تومان)", amount, { amount = it })
                AppField("علت", reason, { reason = it })
                AppField("مهلت پرداخت (مثال: ۱۴۰۵/۰۷/۳۰)", due, { due = it })
                err?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val c = customer
                val a = amount.toMoney()
                if (c == null) {
                    err = "مشتری را انتخاب کنید"
                } else if (a <= 0) {
                    err = "مبلغ را وارد کنید"
                } else {
                    biz.addDebt(c, a, reason.trim(), due.trim(), onClose)
                }
            }) { Text("ذخیره") }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("انصراف") } }
    )
    if (picker) CustomerPickerDialog(customers, onPick = { customer = it; picker = false }, onDismiss = { picker = false })
}

// ====================== صندوق ======================
@Composable
fun CashScreen(biz: BizViewModel, onBack: () -> Unit) {
    val day by biz.cashDay.collectAsState()
    val today by biz.todayTxns.collectAsState()
    val cashIn = today.filter { it.isIncome && it.method == "نقدی" }.sumOf { it.amount }
    val cashOut = today.filter { !it.isIncome && it.method == "نقدی" }.sumOf { it.amount }
    var opening by remember { mutableStateOf("") }
    var actual by remember { mutableStateOf("") }
    var move by remember { mutableStateOf<Boolean?>(null) }
    val d = day

    SubScreen("صندوق روزانه", onBack) { pad ->
        Column(
            Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("تاریخ: " + biz.todayKey.fa(), style = MaterialTheme.typography.titleMedium)
            if (d == null) {
                Text("صندوق امروز هنوز باز نشده است.")
                MoneyField("موجودی اولیه (تومان)", opening, { opening = it })
                Button(onClick = { biz.openDay(opening.toMoney()) }, modifier = Modifier.fillMaxWidth()) { Text("شروع روز") }
            } else {
                val expected = d.opening + cashIn - cashOut
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        KV("موجودی اولیه", money(d.opening))
                        KV("دریافت نقدی امروز", money(cashIn))
                        KV("پرداخت نقدی امروز", money(cashOut))
                        KV("موجودی مورد انتظار", money(expected))
                    }
                }
                if (d.closedAt == null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { move = true }, modifier = Modifier.weight(1f)) { Text("واریز به صندوق") }
                        OutlinedButton(onClick = { move = false }, modifier = Modifier.weight(1f)) { Text("برداشت از صندوق") }
                    }
                    MoneyField("موجودی واقعی صندوق (تومان)", actual, { actual = it })
                    if (actual.isNotBlank()) {
                        Text("اختلاف: " + money(actual.toMoney() - expected))
                    }
                    Button(
                        enabled = actual.isNotBlank(),
                        onClick = { biz.closeDay(d, expected, actual.toMoney()) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("بستن صندوق امروز") }
                } else {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("صندوق امروز بسته شده است.", style = MaterialTheme.typography.titleSmall)
                            KV("موجودی واقعی", money(d.actual ?: 0))
                            KV("مورد انتظار هنگام بستن", money(d.expectedAtClose ?: 0))
                            KV("اختلاف", money((d.actual ?: 0) - (d.expectedAtClose ?: 0)))
                        }
                    }
                }
            }
        }
    }
    move?.let { MoveDialog(it, biz) { move = null } }
}

@Composable
private fun MoveDialog(isDeposit: Boolean, biz: BizViewModel, onClose: () -> Unit) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(if (isDeposit) "واریز به صندوق" else "برداشت از صندوق") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MoneyField("مبلغ (تومان)", amount, { amount = it })
                AppField("توضیح", note, { note = it })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val a = amount.toMoney()
                if (a > 0) {
                    biz.addTxn(
                        isIncome = isDeposit, amount = a,
                        title = if (isDeposit) "واریز به صندوق" else "برداشت از صندوق",
                        category = if (isDeposit) "واریز به صندوق" else "برداشت از صندوق",
                        method = "نقدی", note = note.trim(), inProfit = false, onDone = onClose
                    )
                }
            }) { Text("ثبت") }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("انصراف") } }
    )
}

// ====================== گزارش‌ها ======================
@Composable
fun ReportsScreen(biz: BizViewModel, onBack: () -> Unit) {
    val list by biz.txns.collectAsState()
    val period by biz.period.collectAsState()
    val debts by biz.debts.collectAsState()
    val counted = list.filter { it.inProfit }
    val inc = counted.filter { it.isIncome }
    val exp = counted.filter { !it.isIncome }
    val income = inc.sumOf { it.amount }
    val expense = exp.sumOf { it.amount }

    SubScreen("گزارش‌ها", onBack) { pad ->
        Column(
            Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PeriodChips(period) { biz.period.value = it }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    KV("درآمد", money(income))
                    KV("هزینه", money(expense))
                    KV("سود", money(income - expense))
                    KV("جمع مطالبات باز", money(debts.sumOf { it.amount - it.paid }))
                }
            }
            Text("درآمد بر اساس روش پرداخت", style = MaterialTheme.typography.titleMedium)
            if (inc.isEmpty()) Text("—")
            inc.groupBy { it.method }.forEach { (m, l) -> KV(m, money(l.sumOf { it.amount })) }
            Text("هزینه بر اساس دسته", style = MaterialTheme.typography.titleMedium)
            if (exp.isEmpty()) Text("—")
            exp.groupBy { it.category.ifBlank { "سایر" } }.forEach { (c, l) -> KV(c, money(l.sumOf { it.amount })) }
        }
    }
}
