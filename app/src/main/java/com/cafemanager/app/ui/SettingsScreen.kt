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
import com.cafemanager.app.BizViewModel
import com.cafemanager.app.data.Profile

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(vm: AppViewModel, biz: BizViewModel, modifier: Modifier = Modifier) {
    var cafe by remember { mutableStateOf(vm.profile.cafeName) }
    var owner by remember { mutableStateOf(vm.profile.ownerName) }
    var phone by remember { mutableStateOf(vm.profile.phone) }
    var address by remember { mutableStateOf(vm.profile.address) }
    var saved by remember { mutableStateOf(false) }
    var resetPeriod by remember { mutableIntStateOf(0) }
    var confirmReset by remember { mutableStateOf(false) }
    var resetDone by remember { mutableStateOf(false) }
    val periodLabels = listOf("امروز", "این هفته", "این ماه")

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
        Text("اندازه متن")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1f to "عادی", 1.25f to "بزرگ", 1.5f to "درشت", 1.75f to "خیلی درشت").forEach { (f, l) ->
                FilterChip(selected = vm.fontScale == f, onClick = { vm.setFontScale(f) }, label = { Text(l) })
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

        HorizontalDivider()
        Text("ریست اطلاعات مالی", style = MaterialTheme.typography.titleMedium)
        Text("درآمدها و هزینه‌های بازه انتخاب‌شده در بخش مالی پاک می‌شود.")
        ChipsRow(periodLabels, periodLabels[resetPeriod]) { resetPeriod = periodLabels.indexOf(it); resetDone = false }
        Button(
            onClick = { confirmReset = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) { Text("ریست اطلاعات مالی " + periodLabels[resetPeriod]) }
        if (resetDone) Text("ریست انجام شد")
    }

    if (confirmReset) AlertDialog(
        onDismissRequest = { confirmReset = false },
        title = { Text("ریست اطلاعات مالی") },
        text = {
            Text(
                "همه درآمدها و هزینه‌های ${periodLabels[resetPeriod]} پاک می‌شود و قابل برگشت نیست. " +
                    "فاکتورها، مشتریان و بدهی‌ها پاک نمی‌شوند."
            )
        },
        confirmButton = {
            TextButton(onClick = {
                confirmReset = false
                biz.resetFinance(resetPeriod) { resetDone = true }
            }) { Text("ریست کن") }
        },
        dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("انصراف") } }
    )
}
