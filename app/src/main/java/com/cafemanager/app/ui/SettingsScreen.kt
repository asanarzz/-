package com.cafemanager.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.cafemanager.app.AppViewModel
import com.cafemanager.app.BizViewModel
import com.cafemanager.app.data.Profile
import com.cafemanager.app.util.fa
import com.cafemanager.app.util.normDigits

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
    var resetCount by remember { mutableStateOf<Int?>(null) }
    var askPin by remember { mutableStateOf(false) }
    var pinText by remember { mutableStateOf("") }
    val periodLabels = listOf("امروز", "این هفته", "این ماه")
    val ctx = LocalContext.current
    val version = remember {
        try { ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "" } catch (e: Exception) { "" }
    }

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
                FilterChip(selected = vm.fontScale == f, onClick = { vm.changeFontScale(f) }, label = { Text(l) })
            }
        }

        HorizontalDivider()
        Text("قفل ورود به برنامه", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(if (vm.lockEnabled) "فعال" else "غیرفعال")
                Text(
                    if (vm.lockEnabled) "برای ورود به برنامه رمز (PIN) لازم است."
                    else "برنامه بدون رمز باز می‌شود.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(
                checked = vm.lockEnabled,
                onCheckedChange = { on ->
                    if (on) {
                        vm.setLockEnabled(true)
                    } else {
                        pinText = ""
                        vm.clearLockMessage()
                        askPin = true
                    }
                }
            )
        }
        Text("قفل خودکار بعد از")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(30 to "۳۰ ثانیه", 60 to "۱ دقیقه", 300 to "۵ دقیقه", 900 to "۱۵ دقیقه").forEach { (s, l) ->
                FilterChip(
                    selected = vm.autoLockSec == s, enabled = vm.lockEnabled,
                    onClick = { vm.setAutoLock(s) }, label = { Text(l) }
                )
            }
        }
        OutlinedButton(enabled = vm.lockEnabled, onClick = { vm.lock() }) {
            Icon(Icons.Default.Lock, null)
            Spacer(Modifier.width(8.dp))
            Text("قفل کردن برنامه")
        }

        HorizontalDivider()
        Text("ریست اطلاعات مالی", style = MaterialTheme.typography.titleMedium)
        Text("درآمد، هزینه، فاکتورها، بدهی‌های ثبت‌شده و صندوق بازه انتخاب‌شده پاک می‌شود. مشتری‌ها پاک نمی‌شوند.")
        ChipsRow(periodLabels, periodLabels[resetPeriod]) { resetPeriod = periodLabels.indexOf(it); resetCount = null }
        Button(
            onClick = { confirmReset = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) { Text("ریست اطلاعات مالی " + periodLabels[resetPeriod]) }
        resetCount?.let {
            Text(if (it == 0) "در این بازه چیزی برای پاک‌کردن نبود." else "ریست انجام شد؛ ${it.fa()} مورد پاک شد.")
        }

        HorizontalDivider()
        Text("CAFEMANAGER $version", style = MaterialTheme.typography.bodySmall)
    }

    if (confirmReset) AlertDialog(
        onDismissRequest = { confirmReset = false },
        title = { Text("ریست اطلاعات مالی") },
        text = {
            Text(
                "همه درآمدها و هزینه‌های ${periodLabels[resetPeriod]} پاک می‌شود و قابل برگشت نیست. " +
                    "شامل درآمد، هزینه، فاکتورها، بدهی‌های ثبت‌شده در همین بازه و صندوق است. مشتری‌ها پاک نمی‌شوند."
            )
        },
        confirmButton = {
            TextButton(onClick = {
                confirmReset = false
                biz.resetFinance(resetPeriod) { resetCount = it }
            }) { Text("ریست کن") }
        },
        dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("انصراف") } }
    )

    if (askPin) AlertDialog(
        onDismissRequest = { askPin = false },
        title = { Text("غیرفعال‌کردن قفل") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("برای تأیید، رمز (PIN) فعلی را وارد کنید.")
                OutlinedTextField(
                    value = pinText, onValueChange = { pinText = it.filter(Char::isDigit).take(8) },
                    label = { Text("رمز") }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )
                vm.lockMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (vm.setLockEnabled(false, pinText.normDigits())) askPin = false else pinText = ""
            }) { Text("غیرفعال کن") }
        },
        dismissButton = { TextButton(onClick = { askPin = false }) { Text("انصراف") } }
    )
}
