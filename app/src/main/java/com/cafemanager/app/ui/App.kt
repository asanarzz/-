package com.cafemanager.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.cafemanager.app.AppViewModel

@Composable
fun CafeApp(vm: AppViewModel) {
    val dark = when (vm.themeMode) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }
    CafeTheme(dark) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                when {
                    !vm.setupDone -> SetupScreen(vm)
                    vm.locked -> LockScreen(vm)
                    else -> MainScaffold(vm)
                }
            }
        }
    }
}

@Composable
fun MainScaffold(vm: AppViewModel) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    // null = بسته، 0 = مشتری جدید، غیر از آن = شناسه مشتری برای ویرایش
    var editing by rememberSaveable { mutableStateOf<Long?>(null) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(vm.error) {
        vm.error?.let { snackbar.showSnackbar(it); vm.error = null }
    }

    val e = editing
    if (e != null) {
        BackHandler { editing = null }
        CustomerFormScreen(vm, e, onClose = { editing = null })
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = tab == 0, onClick = { tab = 0 },
                    icon = { Icon(Icons.Default.Home, null) }, label = { Text("داشبورد") })
                NavigationBarItem(selected = tab == 1, onClick = { tab = 1 },
                    icon = { Icon(Icons.Default.Person, null) }, label = { Text("مشتریان") })
                NavigationBarItem(selected = tab == 2, onClick = { tab = 2 },
                    icon = { Icon(Icons.Default.Settings, null) }, label = { Text("تنظیمات") })
            }
        },
        floatingActionButton = {
            if (tab == 1) FloatingActionButton(onClick = { editing = 0L }) {
                Icon(Icons.Default.Add, "مشتری جدید")
            }
        }
    ) { pad ->
        val m = Modifier.padding(pad)
        when (tab) {
            0 -> DashboardScreen(vm, onNewCustomer = { editing = 0L }, modifier = m)
            1 -> CustomersScreen(vm, onOpen = { editing = it }, modifier = m)
            else -> SettingsScreen(vm, modifier = m)
        }
    }
}
