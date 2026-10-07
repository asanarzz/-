package com.cafemanager.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cafemanager.app.AppViewModel
import com.cafemanager.app.R
import com.cafemanager.app.data.Customer
import com.cafemanager.app.util.fa
import com.cafemanager.app.util.normDigits

@Composable
fun CustomersScreen(vm: AppViewModel, onOpen: (Long) -> Unit, modifier: Modifier = Modifier) {
    val list by vm.customers.collectAsState()
    val q by vm.query.collectAsState()

    Column(modifier.padding(horizontal = 16.dp)) {
        OutlinedTextField(
            value = q, onValueChange = vm::setQuery,
            label = { Text("جستجو (نام، موبایل، کد ملی)") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )
        if (list.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (q.isBlank()) "هنوز مشتری ثبت نشده است" else "موردی پیدا نشد")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                items(list, key = { it.id }) { c ->
                    Card(onClick = { onOpen(c.id) }, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("${c.firstName} ${c.lastName}", style = MaterialTheme.typography.titleMedium)
                            if (c.mobile.isNotBlank()) Text(c.mobile.fa(), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

/** کادر متنی با آیکون کپی در گوشه */
@Composable
private fun CField(
    label: String, value: String, onChange: (String) -> Unit,
    kb: KeyboardType = KeyboardType.Text, lines: Int = 1
) {
    val clip = LocalClipboardManager.current
    val ctx = LocalContext.current
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = kb),
        singleLine = lines == 1, minLines = lines,
        trailingIcon = {
            IconButton(onClick = { copyText(ctx, clip, value) }) {
                Icon(painterResource(R.drawable.ic_copy), "کپی")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CustomerFormScreen(vm: AppViewModel, id: Long, onClose: () -> Unit) {
    val ctx = LocalContext.current
    val clip = LocalClipboardManager.current
    var base by remember { mutableStateOf<Customer?>(null) }
    var first by rememberSaveable { mutableStateOf("") }
    var last by rememberSaveable { mutableStateOf("") }
    var father by rememberSaveable { mutableStateOf("") }
    var nid by rememberSaveable { mutableStateOf("") }
    var mobile by rememberSaveable { mutableStateOf("") }
    var cityCode by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var birth by rememberSaveable { mutableStateOf("") }
    var postal by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }
    var picker by remember { mutableStateOf(false) }

    LaunchedEffect(id) {
        if (id != 0L) vm.getCustomer(id)?.let { c ->
            base = c; first = c.firstName; last = c.lastName; father = c.fatherName
            nid = c.nationalId; mobile = c.mobile; cityCode = c.cityCode; phone = c.phone
            birth = c.birthDate; postal = c.postalCode; address = c.address; notes = c.notes
        }
    }

    fun save() {
        if (id != 0L && base == null) return
        val n = nid.normDigits()
        val m = mobile.normDigits()
        error = when {
            first.isBlank() || last.isBlank() -> "نام و نام خانوادگی الزامی است"
            m.isNotEmpty() && !Regex("\\d{10,11}").matches(m) -> "شماره موبایل باید ۱۰ یا ۱۱ رقم باشد"
            n.isNotEmpty() && !Regex("\\d{10}").matches(n) -> "کد ملی باید ۱۰ رقم باشد"
            else -> null
        }
        if (error != null) return
        val c = (base ?: Customer(firstName = "", lastName = "")).copy(
            firstName = first.trim(), lastName = last.trim(), fatherName = father.trim(),
            nationalId = n, mobile = m, cityCode = cityCode.normDigits(), phone = phone.normDigits(),
            birthDate = birth, postalCode = postal.normDigits(), address = address.trim(), notes = notes.trim()
        )
        vm.saveCustomer(c) { onClose() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (id == 0L) "مشتری جدید" else "ویرایش مشتری") },
                navigationIcon = {
                    IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "بازگشت") }
                },
                actions = {
                    if (base != null) IconButton(onClick = { confirmDelete = true }) {
                        Icon(Icons.Default.Delete, "حذف")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier.padding(pad).verticalScroll(rememberScrollState()).imePadding().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CField("نام *", first, { first = it })
            CField("نام خانوادگی *", last, { last = it })
            CField("نام پدر", father, { father = it })
            CField("کد ملی", nid, { nid = it }, KeyboardType.Number)
            CField("شماره موبایل", mobile, { mobile = it }, KeyboardType.Phone)
            val m = mobile.normDigits()
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(enabled = m.isNotBlank(), onClick = { dialNumber(ctx, m) }) {
                    Icon(Icons.Default.Call, null)
                    Spacer(Modifier.width(4.dp))
                    Text("تماس")
                }
                OutlinedButton(enabled = m.isNotBlank(), onClick = { smsNumber(ctx, m) }) {
                    Icon(painterResource(R.drawable.ic_sms), null)
                    Spacer(Modifier.width(4.dp))
                    Text("پیامک")
                }
                OutlinedButton(enabled = m.isNotBlank(), onClick = { openEitaa(ctx, clip, m) }) {
                    Text("ایتا")
                }
            }
            CField("کد شهر", cityCode, { cityCode = it }, KeyboardType.Number)
            CField("شماره تلفن", phone, { phone = it }, KeyboardType.Phone)

            // تاریخ تولد: انتخاب از تقویم شمسی
            Box {
                OutlinedTextField(
                    value = birth.fa(), onValueChange = {}, readOnly = true,
                    label = { Text("تاریخ تولد (شمسی)") },
                    trailingIcon = { Icon(Icons.Default.DateRange, "تقویم") },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Box(Modifier.matchParentSize().clickable { picker = true })
            }

            CField("کد پستی", postal, { postal = it }, KeyboardType.Number)
            CField("آدرس", address, { address = it }, lines = 2)
            CField("توضیحات", notes, { notes = it }, lines = 3)
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = { save() }, modifier = Modifier.fillMaxWidth()) { Text("ذخیره") }
        }
    }

    if (picker) JalaliDatePickerDialog(
        initial = birth,
        onPick = { birth = it; picker = false },
        onClear = { birth = ""; picker = false },
        onDismiss = { picker = false }
    )

    if (confirmDelete) AlertDialog(
        onDismissRequest = { confirmDelete = false },
        title = { Text("حذف مشتری") },
        text = { Text("مشتری از فهرست حذف می‌شود. اطلاعات او در سطل زباله نگهداری می‌شود و پاک نمی‌شود.") },
        confirmButton = {
            TextButton(onClick = {
                confirmDelete = false
                base?.let { vm.trashCustomer(it) { onClose() } }
            }) { Text("حذف") }
        },
        dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("انصراف") } }
    )
}
