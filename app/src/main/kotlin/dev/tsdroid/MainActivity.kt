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

    /** Load language setting synchronously with a timeout to avoid ANR. */
    private fun resolveLanguageTag(context: Context): String {
        return try {
            runBlocking {
                withTimeoutOrNull(500L) {
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
    }

    override fun onStop() {
        super.onStop()
        onStopHandleFloatingWindow()
    }

    /** Handle floating window logic in onStop without blocking the main thread. */
    private fun onStopHandleFloatingWindow() {
        if (isChangingConfigurations) return
        val prefs = getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        val enableFloatingWindow = prefs.getBoolean("enable_floating_window", false)
        if (!enableFloatingWindow) {
            Log.d(TAG, "Floating window is disabled in settings")
            return
        }
        lifecycleScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this@MainActivity)) {
                Log.w(TAG, "Overlay permission not granted, prompting user")
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:$packageName"))
                startActivity(intent)
            } else {
                connectionViewModel.showFloatingWindow()
            }
        }
    }
}
