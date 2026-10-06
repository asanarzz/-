package com.cafemanager.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cafemanager.app.AppViewModel
import com.cafemanager.app.data.Profile

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(vm: AppViewModel, modifier: Modifier = Modifier) {
    var cafe by remember { mutableStateOf(vm.profile.cafeName) }
    var owner by remember { mutableStateOf(vm.profile.ownerName) }
    var phone by remember { mutableStateOf(vm.profile.phone) }
    var address by remember { mutableStateOf(vm.profile.address) }
    var saved by remember { mutableStateOf(false) }

    Column(
        modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("اطلاعات کافی‌نت", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(cafe, { cafe = it; saved = false }, label = { Text("نام کافی‌نت") },
            singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(owner, { owner = it; saved = false }, label = { Text("نام مدیر") },
            singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(phone, { phone = it; saved = false }, label = { Text("شماره تماس") },
            singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(address, { address = it; saved = false }, label = { Text("آدرس") },
            minLines = 2, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            if (cafe.isNotBlank()) {
                vm.saveProfile(Profile(cafe.trim(), owner.trim(), phone.trim(), address.trim()))
                saved = true
            }
        }) { Text("ذخیره") }
        if (saved) Text("ذخیره شد")

        HorizontalDivider()
        Text("ظاهر", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(0 to "خودکار", 1 to "روشن", 2 to "تاریک").forEach { (m, l) ->
                FilterChip(selected = vm.themeMode == m, onClick = { vm.setTheme(m) }, label = { Text(l) })
            }
        }

        HorizontalDivider()
        Text("قفل خودکار بعد از", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(30 to "۳۰ ثانیه", 60 to "۱ دقیقه", 300 to "۵ دقیقه", 900 to "۱۵ دقیقه").forEach { (s, l) ->
                FilterChip(selected = vm.autoLockSec == s, onClick = { vm.setAutoLock(s) }, label = { Text(l) })
            }
        }
        OutlinedButton(onClick = { vm.lock() }) {
            Icon(Icons.Default.Lock, null)
            Spacer(Modifier.width(8.dp))
            Text("قفل کردن برنامه")
        }
    }
}
