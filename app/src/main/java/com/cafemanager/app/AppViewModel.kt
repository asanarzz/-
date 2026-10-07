package com.cafemanager.app

import android.app.Application
import android.os.SystemClock
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.cafemanager.app.data.*
import com.cafemanager.app.security.Crypto
import com.cafemanager.app.security.PinManager
import com.cafemanager.app.util.normDigits
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.security.MessageDigest

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    private val store = SettingsStore(app)

    var setupDone by mutableStateOf(store.setupDone); private set
    var locked by mutableStateOf(store.setupDone && store.hasPin); private set
    var themeMode by mutableIntStateOf(store.themeMode); private set
    var autoLockSec by mutableIntStateOf(store.autoLockSec); private set
    var fontScale by mutableFloatStateOf(store.fontScale); private set
    var profile by mutableStateOf(store.profile()); private set
    var lockMessage by mutableStateOf<String?>(null); private set
    var error by mutableStateOf<String?>(null)

    private var failed = 0
    private var lockedUntil = 0L
    private var backgroundAt = -1L

    // ---------- راه‌اندازی و قفل ----------
    fun completeSetup(p: Profile, pin: String) {
        val salt = PinManager.newSalt()
        store.pinSalt = PinManager.encode(salt)
        store.pinHash = PinManager.encode(PinManager.hash(pin, salt))
        store.saveProfile(p)
        store.setupDone = true
        profile = p
        setupDone = true
        locked = false
        log("setup", "راه‌اندازی برنامه انجام شد")
    }

    fun unlock(pin: String): Boolean {
        val now = SystemClock.elapsedRealtime()
        if (now < lockedUntil) {
            lockMessage = "لطفاً چند ثانیه صبر کنید"
            return false
        }
        val ok = MessageDigest.isEqual(
            PinManager.hash(pin, PinManager.decode(store.pinSalt)),
            PinManager.decode(store.pinHash)
        )
        if (ok) {
            failed = 0
            lockMessage = null
            locked = false
            log("login", "ورود به برنامه")
            return true
        }
        failed++
        if (failed >= 5) {
            failed = 0
            lockedUntil = now + 30_000
            lockMessage = "چند بار رمز اشتباه وارد شد؛ ۳۰ ثانیه صبر کنید"
        } else {
            lockMessage = "رمز اشتباه است"
        }
        return false
    }

    fun lock() {
        if (store.hasPin) {
            locked = true
            log("logout", "قفل شدن برنامه")
        }
    }

    fun onBackground() { backgroundAt = SystemClock.elapsedRealtime() }

    fun onForeground() {
        val t = backgroundAt
        backgroundAt = -1
        if (t >= 0 && setupDone && !locked && autoLockSec > 0 &&
            SystemClock.elapsedRealtime() - t > autoLockSec * 1000L
        ) locked = true
    }

    // ---------- تنظیمات ----------
    fun setTheme(m: Int) { store.themeMode = m; themeMode = m }
    fun setAutoLock(sec: Int) { store.autoLockSec = sec; autoLockSec = sec }
    fun changeFontScale(f: Float) { store.fontScale = f; fontScale = f }
    fun saveProfile(p: Profile) {
        store.saveProfile(p)
        profile = p
        log("settings", "اطلاعات کافی‌نت تغییر کرد")
    }

    // ---------- مشتریان ----------
    val query = MutableStateFlow("")
    fun setQuery(q: String) { query.value = q.normDigits() }

    @OptIn(ExperimentalCoroutinesApi::class)
    val customers: StateFlow<List<Customer>> = query
        .flatMapLatest { db.customers().search(it.trim()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val customerCount: StateFlow<Int> = db.customers().count()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val recent: StateFlow<List<ActivityEntry>> = db.activity().recent(20)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    suspend fun getCustomer(id: Long): Customer? = db.customers().get(id)

    suspend fun getPasswords(id: Long): List<PasswordDraft> =
        db.passwords().forCustomer(id).map { PasswordDraft(it.system, it.username, Crypto.decrypt(it.password)) }

    fun saveCustomer(c: Customer, passwords: List<PasswordDraft>, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                db.withTransaction {
                    val name = "${c.firstName} ${c.lastName}"
                    val cid: Long
                    if (c.id == 0L) {
                        cid = db.customers().insert(c)
                        db.activity().insert(ActivityEntry(type = "customer_add", message = "مشتری جدید اضافه شد: $name"))
                    } else {
                        db.customers().update(c)
                        cid = c.id
                        db.activity().insert(ActivityEntry(type = "customer_edit", message = "اطلاعات مشتری ویرایش شد: $name"))
                    }
                    db.passwords().deleteForCustomer(cid)
                    db.passwords().insertAll(
                        passwords.filter { it.system.isNotBlank() || it.password.isNotBlank() }.map {
                            CustomerPassword(
                                customerId = cid, system = it.system.trim(),
                                username = it.username.trim(), password = Crypto.encrypt(it.password)
                            )
                        }
                    )
                }
                onDone()
            } catch (e: Exception) {
                error = "ذخیره‌سازی انجام نشد. دوباره تلاش کنید."
            }
        }
    }

    fun trashCustomer(c: Customer, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                db.withTransaction {
                    db.customers().trash(c.id, System.currentTimeMillis())
                    db.activity().insert(ActivityEntry(type = "customer_delete", message = "مشتری حذف شد: ${c.firstName} ${c.lastName}"))
                }
                onDone()
            } catch (e: Exception) {
                error = "حذف انجام نشد. دوباره تلاش کنید."
            }
        }
    }

    private fun log(type: String, msg: String) {
        viewModelScope.launch { db.activity().insert(ActivityEntry(type = type, message = msg)) }
    }
}

/** رمز یک سامانه (در حافظه به‌صورت متن ساده، در دیتابیس رمزنگاری‌شده) */
data class PasswordDraft(val system: String, val username: String, val password: String)
