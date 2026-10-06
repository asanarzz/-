package com.cafemanager.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cafemanager.app.AppViewModel
import com.cafemanager.app.util.fa
import com.cafemanager.app.util.formatDateTime

@Composable
fun DashboardScreen(vm: AppViewModel, onNewCustomer: () -> Unit, modifier: Modifier = Modifier) {
    val count by vm.customerCount.collectAsState()
    val recent by vm.recent.collectAsState()

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(vm.profile.cafeName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("تعداد مشتریان")
                        Text(count.fa(), style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
        }
        item {
            Button(onClick = onNewCustomer, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("مشتری جدید")
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
