package com.educode.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import com.educode.app.core.connectivity.ConnectivityObserver
import com.educode.app.core.connectivity.NetworkConnectivityObserver
import com.educode.app.components.OfflineScreen
import com.educode.app.features.barmujai.BarmujAssistant
import com.educode.app.navigation.EduCodeNavGraph
import com.educode.app.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private lateinit var connectivityObserver: ConnectivityObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectivityObserver = NetworkConnectivityObserver(applicationContext)
        enableEdgeToEdge()
        setContent {
            val status by connectivityObserver.observe().collectAsState(initial = ConnectivityObserver.Status.Available)
            var showOfflineOverlay by remember { mutableStateOf(false) }
            
            LaunchedEffect(status) {
                if (status == ConnectivityObserver.Status.Lost || status == ConnectivityObserver.Status.Unavailable) {
                    showOfflineOverlay = true
                }
            }

            MyApplicationTheme {
                BarmujAssistant {
                    Box {
                        EduCodeNavGraph()
                        
                        if (showOfflineOverlay) {
                            OfflineScreen(onContinueOffline = { showOfflineOverlay = false })
                        }
                    }
                }
            }
        }
    }
}
