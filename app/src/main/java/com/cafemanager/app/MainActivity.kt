package com.cafemanager.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.cafemanager.app.ui.CafeApp

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // مخفی‌کردن محتوا در Recent Apps (و جلوگیری از اسکرین‌شات)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        setContent { CafeApp(vm) }
    }

    override fun onStop() { super.onStop(); vm.onBackground() }
    override fun onStart() { super.onStart(); vm.onForeground() }
}
