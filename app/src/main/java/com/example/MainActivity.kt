package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.data.IsayaRepository
import com.example.ui.client.ClientAppRoot
import com.example.ui.partner.PartnerAppRoot
import com.example.ui.theme.*

enum class AppArchitectureMode(val title: String, val badge: String, val iconName: String) {
    CLIENT_APP("APK 1: App Clientes", "CLIENTE", "Menú • Pago Móvil • Rastreo"),
    PARTNER_APP("APK 2: App Partner", "COCINA", "Auth Firebase • Comandas • Push")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IsayaSushiTheme {
                IsayaSystemSwitcherApp()
            }
        }
    }
}

@Composable
fun IsayaSystemSwitcherApp() {
    val context = LocalContext.current
    val repository = remember { IsayaRepository.getInstance(context) }
    var selectedAppMode by remember { mutableStateOf(AppArchitectureMode.PARTNER_APP) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DeepBlack
    ) {
        when (selectedAppMode) {
            AppArchitectureMode.CLIENT_APP -> {
                ClientAppRoot(
                    repository = repository,
                    onSwitchToPartner = { selectedAppMode = AppArchitectureMode.PARTNER_APP }
                )
            }

            AppArchitectureMode.PARTNER_APP -> {
                PartnerAppRoot(
                    repository = repository,
                    onSwitchToClient = { selectedAppMode = AppArchitectureMode.CLIENT_APP }
                )
            }
        }
    }
}

// Keep Greeting for backward compatibility with Robolectric tests
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
