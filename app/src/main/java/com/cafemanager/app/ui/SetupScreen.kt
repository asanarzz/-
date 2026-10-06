package com.cafemanager.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.cafemanager.app.AppViewModel
import com.cafemanager.app.data.Profile
import com.cafemanager.app.util.fa
import com.cafemanager.app.util.normDigits

@Composable
fun SetupScreen(vm: AppViewModel) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var cafe by rememberSaveable { mutableStateOf("") }
    var owner by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var pin by rememberSaveable { mutableStateOf("") }
    var pin2 by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    fun next() {
        error = null
        when (step) {
            0 -> if (cafe.isBlank()) error = "نام کافی‌نت را وارد کنید" else step = 1
            1 -> step = 2
            else -> {
                val a = pin.normDigits()
                when {
                    !Regex("\\d{4,8}").matches(a) -> error = "رمز باید ۴ تا ۸ رقم باشد"
                    a != pin2.normDigits() -> error = "رمز و تکرار آن یکسان نیست"
                    else -> vm.completeSetup(Profile(cafe.trim(), owner.trim(), phone.trim(), address.trim()), a)
                }
            }
        }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Text("به «مدیریت کافی نت» خوش آمدید", style = MaterialTheme.typography.headlineSmall)
        Text("مرحله ${(step + 1).fa()} از ۳", style = MaterialTheme.typography.bodyMedium)
        LinearProgressIndicator(progress = { (step + 1) / 3f }, modifier = Modifier.fillMaxWidth())

        when (step) {
            0 -> {
                Text("اطلاعات کافی‌نت خود را وارد کنید")
                OutlinedTextField(cafe, { cafe = it }, label = { Text("نام کافی‌نت") },
                    singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(owner, { owner = it }, label = { Text("نام مدیر") },
                    singleLine = true, modifier = Modifier.fillMaxWidth())
            }
            1 -> {
                Text("اطلاعات تماس (اختیاری)")
                OutlinedTextField(phone, { phone = it }, label = { Text("شماره تماس") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(address, { address = it }, label = { Text("آدرس") },
                    minLines = 2, modifier = Modifier.fillMaxWidth())
            }
            else -> {
                Text("یک رمز عددی (PIN) برای قفل برنامه تعیین کنید")
                OutlinedTextField(pin, { pin = it.filter(Char::isDigit).take(8) }, label = { Text("رمز ۴ تا ۸ رقمی") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(pin2, { pin2 = it.filter(Char::isDigit).take(8) }, label = { Text("تکرار رمز") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        }

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (step > 0) OutlinedButton(onClick = { error = null; step -= 1 }) { Text("قبلی") }
            Button(onClick = { next() }, modifier = Modifier.weight(1f)) {
                Text(if (step == 2) "پایان و ورود" else "ادامه")
            }
        }
    }
}
