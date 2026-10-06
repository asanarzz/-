package com.cafemanager.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.cafemanager.app.AppViewModel
import com.cafemanager.app.util.normDigits

@Composable
fun LockScreen(vm: AppViewModel) {
    var pin by remember { mutableStateOf("") }
    fun submit() {
        vm.unlock(pin.normDigits())
        pin = ""
    }
    Column(
        Modifier.fillMaxSize().imePadding().padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
        Text(vm.profile.cafeName, style = MaterialTheme.typography.headlineSmall)
        Text("رمز ورود را وارد کنید")
        OutlinedTextField(
            value = pin, onValueChange = { pin = it.filter(Char::isDigit).take(8) },
            label = { Text("رمز") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        vm.lockMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = { submit() }, modifier = Modifier.fillMaxWidth()) { Text("ورود") }
    }
}
