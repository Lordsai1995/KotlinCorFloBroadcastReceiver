package com.example.kotlincorflobroadcastreceiver

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// ------------------ Flow for Internet Status ------------------
fun Context.internetStatusFlow(): Flow<Boolean> = callbackFlow {
    val connectivityManager =
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            trySend(true)
        }

        override fun onLost(network: Network) {
            trySend(false)
        }
    }

    val request = NetworkRequest.Builder().build()
    connectivityManager.registerNetworkCallback(request, networkCallback)

    awaitClose {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}

// ------------------ Main Activity ------------------
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val context = this
                val isConnected by context.internetStatusFlow()
                    .collectAsState(initial = false)

                CounterScreen(isConnected)
            }
        }
    }
}

// ------------------ Counter Composable ------------------
@Composable
fun CounterScreen(isConnected: Boolean) {
    var counter by remember { mutableStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }

    // Launch the counter coroutine
    LaunchedEffect(isConnected) {
        if (isConnected) {
            isRunning = true
            while (isRunning && isConnected) {
                delay(1000)
                counter++
            }
        } else {
            isRunning = false
        }
    }

    // UI
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isConnected) "Internet Connected ✅" else "No Internet ❌",
                color = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Counter: $counter",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
