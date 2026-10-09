package com.cafemanager.app

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.cafemanager.app.data.*
import com.cafemanager.app.ui.money
import com.cafemanager.app.util.Jalali
import com.cafemanager.app.util.fa
import com.cafemanager.app.util.formatDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class CartLine(val service: Service, val qty: Int)

/** بازه زمانی: 0 امروز، 1 این هفته (از شنبه)، 2 این ماه شمسی */
fun rangeFor(p: Int): Pair<Long, Long> {
    val c = java.util.GregorianCalendar()
    c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
    val todayStart = c.timeInMillis
    val day = 24L * 60 * 60 * 1000
    val start = when (p) {
        0 -> todayStart
        1 -> todayStart - ((c.get(Calendar.DAY_OF_WEEK) - Calendar.SATURDAY + 7) % 7) * day
        else -> {
            val jd = Jalali.toJalali(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)).third
            todayStart - (jd - 1) * day
        }
    }
    return start to (todayStart + day - 1)
}

class BizViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)

    private fun <T> Flow<T>.hot(init: T): StateFlow<T> =
        stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), init)

    var error by mutableStateOf<String?>(null)

    private fun guard(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                error = "عملیات انجام نشد. دوباره تلاش کنید."
            }
        }
    }

    val services = db.services().all().hot(emptyList())
    val invoices = db.invoices().recent().hot(emptyList())
    val debts = db.debts().open().hot(emptyList())
    val allCustomers = db.customers().search("").hot(emptyList())

    val period = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val txns = period.flatMapLatest {
        val (a, b) = rangeFor(it)
        db.txns().between(a, b)
    }.hot(emptyList())

    val todayTxns = run {
        val (a, b) = rangeFor(0)
        db.txns().between(a, b)
    }.hot(emptyList())

    val todayKey: String = Jalali.date(System.currentTimeMillis())
    val cashDay = db.cash().day(todayKey).hot(null)

    // ---------- خدمات ----------
    fun saveService(s: Service, onDone: () -> Unit) = guard {
        if (s.id == 0L) db.services().insert(s) else db.services().update(s)
        onDone()
    }

    fun deleteService(s: Service, onDone: () -> Unit) = guard {
        db.services().trash(s.id, System.currentTimeMillis())
        onDone()
    }

    // ---------- دخل و خرج ----------
    fun addTxn(
        isIncome: Boolean, amount: Long, title: String, category: String,
        method: String, note: String, inProfit: Boolean = true, onDone: () -> Unit = {}
    ) = guard {
        db.withTransaction {
            db.txns().insert(
                Txn(isIncome = isIncome, amount = amount, title = title, category = category,
                    method = method, note = note, inProfit = inProfit)
            )
            db.activity().insert(
                ActivityEntry(
                    type = if (isIncome) "income" else "expense",
                    message = (if (isIncome) "درآمد ثبت شد: " else "هزینه ثبت شد: ") + title
                )
            )
        }
        onDone()
    }

    // ---------- فاکتور ----------
    fun saveInvoice(
        customer: Customer?, customerName: String, lines: List<CartLine>, discount: Long, paid: Long,
        method: String, note: String, onDone: (Long) -> Unit
    ) = guard {
        val subtotal = lines.sumOf { it.service.price * it.qty }
        val total = (subtotal - discount).coerceAtLeast(0)
        val paidAmt = paid.coerceIn(0, total)
        val name = customerName.trim()
        var newId = 0L
        db.withTransaction {
            newId = db.invoices().insert(
                Invoice(customerId = customer?.id, customerName = name, subtotal = subtotal,
                    discount = discount, total = total, paid = paidAmt, method = method, note = note)
            )
            db.invoices().insertItems(lines.map {
                InvoiceItem(invoiceId = newId, serviceName = it.service.name,
                    unitPrice = it.service.price, qty = it.qty, lineTotal = it.service.price * it.qty)
            })
            if (paidAmt > 0) {
                db.txns().insert(
                    Txn(isIncome = true, amount = paidAmt, title = "خدمات: " + lines.joinToString("، ") { it.service.name },
                        category = "خدمات", method = method, customerId = customer?.id)
                )
            }
            if (paidAmt < total) {
                db.debts().insert(
                    Debt(customerId = customer?.id, customerName = name.ifBlank { "مشتری متفرقه" },
                        amount = total - paidAmt, reason = "فاکتور شماره " + newId.toString().fa())
                )
            }
            db.activity().insert(ActivityEntry(type = "invoice", message = "فاکتور ثبت شد" + (if (name.isNotBlank()) ": $name" else "")))
        }
        onDone(newId)
    }

    fun receipt(id: Long, p: Profile, onText: (String) -> Unit) = guard {
        val inv = db.invoices().get(id) ?: return@guard
        val items = db.invoices().items(id)
        val sb = StringBuilder()
        sb.appendLine(p.cafeName)
        if (p.phone.isNotBlank()) sb.appendLine("تلفن: " + p.phone)
        if (p.address.isNotBlank()) sb.appendLine(p.address)
        sb.appendLine("────────────")
        sb.appendLine("رسید شماره " + id.toString().fa())
        sb.appendLine("تاریخ: " + formatDateTime(inv.createdAt))
        sb.appendLine("مشتری: " + inv.customerName.ifBlank { "متفرقه" })
        sb.appendLine("────────────")
        sb.appendLine("خدمات انجام‌شده:")
        items.forEach {
            sb.appendLine("${it.serviceName} × ${it.qty.toString().fa()} = ${money(it.lineTotal)}")
        }
        sb.appendLine("────────────")
        sb.appendLine("جمع: " + money(inv.subtotal))
        if (inv.discount > 0) sb.appendLine("تخفیف: " + money(inv.discount))
        sb.appendLine("مبلغ نهایی: " + money(inv.total))
        sb.appendLine("پرداخت‌شده: " + money(inv.paid) + " (" + inv.method + ")")
        if (inv.paid < inv.total) sb.appendLine("باقی‌مانده: " + money(inv.total - inv.paid))
        if (inv.note.isNotBlank()) sb.appendLine("توضیح: " + inv.note)
        onText(sb.toString())
    }

    // ---------- ریست اطلاعات مالی ----------
    /** همه اطلاعات مالی بازه (درآمد/هزینه، فاکتور، بدهی‌های ثبت‌شده در آن بازه و صندوق) پاک می‌شود. مشتری‌ها پاک نمی‌شوند. */
    fun resetFinance(p: Int, onDone: (Int) -> Unit) = guard {
        val (a, _) = rangeFor(p)
        val b = Long.MAX_VALUE
        val label = when (p) { 0 -> "امروز"; 1 -> "این هفته"; else -> "این ماه" }
        var total = 0
        db.withTransaction {
            total += db.txns().deleteBetween(a, b)
            db.invoices().deleteItemsBetween(a, b)
            total += db.invoices().deleteBetween(a, b)
            total += db.debts().deleteBetween(a, b)
            total += db.cash().deleteBetween(Jalali.date(a), "9999/99/99")
            db.activity().insert(ActivityEntry(type = "reset", message = "اطلاعات مالی $label ریست شد"))
        }
        onDone(total)
    }

    // ---------- بدهی ----------
    fun addDebt(c: Customer, amount: Long, reason: String, due: String, onDone: () -> Unit) = guard {
        val name = "${c.firstName} ${c.lastName}"
        db.withTransaction {
            db.debts().insert(Debt(customerId = c.id, customerName = name, amount = amount, reason = reason, dueDate = due))
            db.activity().insert(ActivityEntry(type = "debt", message = "بدهی ثبت شد: $name"))
        }
        onDone()
    }

    fun payDebt(d: Debt, amount: Long, method: String, onDone: () -> Unit) = guard {
        val remaining = d.amount - d.paid
        if (remaining < 1) return@guard
        val pay = amount.coerceIn(1L, remaining)
        db.withTransaction {
            db.debts().addPayment(d.id, pay)
            db.txns().insert(
                Txn(isIncome = true, amount = pay, title = "دریافت بدهی: " + d.customerName,
                    category = "دریافت بدهی", method = method, customerId = d.customerId)
            )
            db.activity().insert(ActivityEntry(type = "debt_pay", message = "پرداخت بدهی ثبت شد: " + d.customerName))
        }
        onDone()
    }

    // ---------- صندوق ----------
    fun openDay(opening: Long) = guard {
        db.cash().upsert(CashDay(todayKey, opening))
        db.activity().insert(ActivityEntry(type = "cash", message = "صندوق امروز باز شد"))
    }

    fun closeDay(day: CashDay, expected: Long, actual: Long) = guard {
        db.cash().upsert(day.copy(closedAt = System.currentTimeMillis(), actual = actual, expectedAtClose = expected))
        db.activity().insert(ActivityEntry(type = "cash", message = "صندوق امروز بسته شد"))
    }
}
