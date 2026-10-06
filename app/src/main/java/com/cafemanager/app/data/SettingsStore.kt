package com.cafemanager.app.data

import android.content.Context

data class Profile(val cafeName: String, val ownerName: String, val phone: String, val address: String)

/** تنظیمات سبک برنامه. رمز به‌صورت Hash+Salt ذخیره می‌شود، نه متن ساده. */
class SettingsStore(ctx: Context) {
    private val p = ctx.getSharedPreferences("cafe_settings", Context.MODE_PRIVATE)
    private fun s(k: String) = p.getString(k, "") ?: ""
    private fun put(k: String, v: String) { p.edit().putString(k, v).commit() }

    var setupDone: Boolean
        get() = p.getBoolean("setupDone", false)
        set(v) { p.edit().putBoolean("setupDone", v).commit() }
    var pinHash: String
        get() = s("pinHash")
        set(v) = put("pinHash", v)
    var pinSalt: String
        get() = s("pinSalt")
        set(v) = put("pinSalt", v)
    var themeMode: Int // 0 خودکار، 1 روشن، 2 تاریک
        get() = p.getInt("themeMode", 0)
        set(v) { p.edit().putInt("themeMode", v).commit() }
    var autoLockSec: Int
        get() = p.getInt("autoLockSec", 60)
        set(v) { p.edit().putInt("autoLockSec", v).commit() }

    val hasPin: Boolean get() = pinHash.isNotEmpty()

    fun profile() = Profile(s("cafeName"), s("ownerName"), s("phone"), s("address"))
    fun saveProfile(x: Profile) {
        p.edit().putString("cafeName", x.cafeName).putString("ownerName", x.ownerName)
            .putString("phone", x.phone).putString("address", x.address).commit()
    }
}
