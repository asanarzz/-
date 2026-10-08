package com.cafemanager.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cafemanager.app.AppViewModel
import com.cafemanager.app.BizViewModel

@Composable
fun CafeApp(vm: AppViewModel) {
    val dark = when (vm.themeMode) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }
    CafeTheme(dark) {
        val d = LocalDensity.current
        CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Rtl,
            LocalDensity provides Density(d.density, d.fontScale * vm.fontScale)
        ) {
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
    val biz: BizViewModel = viewModel()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    // null = بسته، 0 = مشتری جدید، غیر از آن = شناسه مشتری برای ویرایش
    var editing by rememberSaveable { mutableStateOf<Long?>(null) }
    var sub by rememberSaveable { mutableStateOf<String?>(null) }

    vm.error?.let { msg ->
        AlertDialog(
            onDismissRequest = { vm.error = null },
            text = { Text(msg) },
            confirmButton = { TextButton(onClick = { vm.error = null }) { Text("باشه") } }
        )
    }
    biz.error?.let { msg ->
        AlertDialog(
            onDismissRequest = { biz.error = null },
            text = { Text(msg) },
            confirmButton = { TextButton(onClick = { biz.error = null }) { Text("باشه") } }
        )
    }

    val e = editing
    if (e != null) {
        BackHandler { editing = null }
        CustomerFormScreen(vm, e, onClose = { editing = null })
        return
    }

    val s = sub
    if (s != null) {
        val back = { sub = null }
        when (s) {
            "services" -> ServicesScreen(biz, back)
            "invoices" -> InvoicesScreen(vm, biz, back)
            "debts" -> DebtsScreen(biz, back)
            "cash" -> CashScreen(biz, back)
            "reports" -> ReportsScreen(biz, back)
            else -> SubScreen("تنظیمات", back) { pad -> SettingsScreen(vm, biz, Modifier.padding(pad)) }
        }
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = tab == 0, onClick = { tab = 0 },
                    icon = { Icon(Icons.Default.Home, null) }, label = { Text("داشبورد", maxLines = 1, style = MaterialTheme.typography.labelSmall) })
                NavigationBarItem(selected = tab == 1, onClick = { tab = 1 },
                    icon = { Icon(Icons.Default.Person, null) }, label = { Text("مشتریان", maxLines = 1, style = MaterialTheme.typography.labelSmall) })
                NavigationBarItem(selected = tab == 2, onClick = { tab = 2 },
                    icon = { Icon(Icons.Default.ShoppingCart, null) }, label = { Text("فروش", maxLines = 1, style = MaterialTheme.typography.labelSmall) })
                NavigationBarItem(selected = tab == 3, onClick = { tab = 3 },
                    icon = { Icon(Icons.Default.AccountBox, null) }, label = { Text("مالی", maxLines = 1, style = MaterialTheme.typography.labelSmall) })
                NavigationBarItem(selected = tab == 4, onClick = { tab = 4 },
                    icon = { Icon(Icons.Default.Menu, null) }, label = { Text("بیشتر", maxLines = 1, style = MaterialTheme.typography.labelSmall) })
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
            0 -> DashboardScreen(vm, biz, onNewCustomer = { editing = 0L }, onNewSale = { tab = 2 }, onLedger = { tab = 3 }, modifier = m)
            1 -> CustomersScreen(vm, onOpen = { editing = it }, modifier = m)
            2 -> SaleScreen(vm, biz, onOpenServices = { sub = "services" }, modifier = m)
            3 -> LedgerScreen(biz, modifier = m)
            else -> MoreScreen(onOpen = { sub = it }, modifier = m)
        }
    }
}
