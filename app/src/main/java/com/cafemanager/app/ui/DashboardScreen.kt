package com.cafemanager.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cafemanager.app.AppViewModel
import com.cafemanager.app.BizViewModel
import com.cafemanager.app.util.fa
import com.cafemanager.app.util.formatDateTime

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall)
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    vm: AppViewModel, biz: BizViewModel,
    onNewCustomer: () -> Unit, onNewSale: () -> Unit, onLedger: () -> Unit,
    modifier: Modifier = Modifier
) {
    val count by vm.customerCount.collectAsState()
    val recent by vm.recent.collectAsState()
    val today by biz.todayTxns.collectAsState()
    val debts by biz.debts.collectAsState()
    val counted = today.filter { it.inProfit }
    val income = counted.filter { it.isIncome }.sumOf { it.amount }
    val expense = counted.filter { !it.isIncome }.sumOf { it.amount }
    val debtTotal = debts.sumOf { it.amount - it.paid }

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(vm.profile.cafeName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        item { Text("آمار امروز", style = MaterialTheme.typography.titleMedium) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("درآمد", money(income), Modifier.weight(1f))
                StatCard("هزینه", money(expense), Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("سود", money(income - expense), Modifier.weight(1f))
                StatCard("مطالبات باز", money(debtTotal), Modifier.weight(1f))
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text("تعداد مشتریان: " + count.fa())
                }
            }
        }
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onNewCustomer) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(4.dp))
                    Text("مشتری", maxLines = 1)
                }
                Button(onClick = onNewSale) { Text("ثبت خدمت", maxLines = 1) }
                Button(onClick = onLedger) { Text("دخل و خرج", maxLines = 1) }
            }
        }
        item { Text("فعالیت‌های اخیر", style = MaterialTheme.typography.titleMedium) }
        if (recent.isEmpty()) item { Text("فعالیتی ثبت نشده است") }
        items(recent, key = { it.id }) { e ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(e.message)
                    Text(formatDateTime(e.createdAt), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
