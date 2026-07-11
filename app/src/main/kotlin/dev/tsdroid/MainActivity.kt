package dev.tsdroid

import android.os.Bundle
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import dev.tsdroid.data.SettingsStore
import dev.tsdroid.viewmodel.ConnectionViewModel
import dev.tsdroid.ui.theme.TsDroidTheme
import dev.tsdroid.ui.screen.AppNavigation
import dev.tsdroid.ui.screen.SplashScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import android.content.Intent
import android.provider.Settings
import dev.tsdroid.service.TsConnectionService
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val connectionViewModel: ConnectionViewModel by viewModels()
    private val TAG = "MainActivity"

    // 标记本轮生命周期中是否已经弹过浮窗权限请求，避免 onStop 死循环
    private var overlayPermissionRequestedThisSession = false

    override fun attachBaseContext(newBase: Context) {
        val languageTag = resolveLanguageTag(newBase)
        val locale = Locale.forLanguageTag(languageTag)
        Locale.setDefault(locale)
        val config = newBase.resources.configuration
        config.setLocale(locale)
        config.setLocales(android.os.LocaleList(locale))
        val updatedContext = newBase.createConfigurationContext(config)
        super.attachBaseContext(updatedContext)
    }

    /** Load language setting synchronously with a short timeout.
     *  Must block here because attachBaseContext needs locale before super call. */
    private fun resolveLanguageTag(context: Context): String {
        return try {
            runBlocking {
                withTimeoutOrNull(100L) {
                    SettingsStore(context).language.first()
                } ?: "zh"
            }
        } catch (e: Exception) {
            "zh"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var showSplash by remember { mutableStateOf(true) }

            TsDroidTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (showSplash) {
                        SplashScreen(onReady = { showSplash = false })
                    } else {
                        AppNavigation()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // 每次回到前台时重置权限请求标记，允许再次申请
        overlayPermissionRequestedThisSession = false
    }

    override fun onStop() {
        super.onStop()
        onStopHandleFloatingWindow()
    }

    /** Handle floating window logic in onStop without blocking the main thread. */
    private fun onStopHandleFloatingWindow() {
        if (isChangingConfigurations) return
        // 如果本轮生命周期已经弹过权限请求，不再重复弹（防止死循环）
        if (overlayPermissionRequestedThisSession) return
        lifecycleScope.launch {
            // 用 DataStore 读取悬浮窗开关（与 SettingsStore 一致）
            val enableFloatingWindow = SettingsStore(this@MainActivity).enableFloatingWindow.first()
            if (!enableFloatingWindow) {
                Log.d(TAG, "Floating window is disabled in settings")
                return@launch
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this@MainActivity)) {
                Log.w(TAG, "Overlay permission not granted, prompting user")
                overlayPermissionRequestedThisSession = true
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:$packageName"))
                startActivity(intent)
            } else {
                connectionViewModel.showFloatingWindow()
            }
        }
    }
}
