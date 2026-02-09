package com.titushm.udplug

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.titushm.udplug.ui.theme.UDPlugTheme

class MainActivity : ComponentActivity() {
    companion object {
        const val EXTRA_IP_ADDRESS = "com.titushm.udplug.EXTRA_IP_ADDRESS"
        const val EXTRA_PORT = "com.titushm.udplug.EXTRA_PORT"
        const val EXTRA_PAYLOAD = "com.titushm.udplug.EXTRA_PAYLOAD"
        const val EXTRA_IS_HEX_PAYLOAD = "com.titushm.udplug.EXTRA_IS_HEX_PAYLOAD"
        const val EXTRA_USE_HEX_RESPONSE = "com.titushm.udplug.EXTRA_USE_HEX_RESPONSE"
        const val EXTRA_WAIT_FOR_RESPONSE = "com.titushm.udplug.EXTRA_WAIT_FOR_RESPONSE"
        const val EXTRA_TIMEOUT = "com.titushm.udplug.EXTRA_TIMEOUT"
        const val EXTRA_MAX_BUFFER_SIZE = "com.titushm.udplug.EXTRA_MAX_BUFFER_SIZE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UDPlugTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppContent(
                        modifier = Modifier.padding(innerPadding),
                        initialIpAddress = intent.getStringExtra(EXTRA_IP_ADDRESS) ?: "",
                        initialPort = intent.getStringExtra(EXTRA_PORT) ?: "",
                        initialPayload = intent.getStringExtra(EXTRA_PAYLOAD) ?: "",
                        initialIsHexPayload = intent.getBooleanExtra(EXTRA_IS_HEX_PAYLOAD, false),
                        initialUseHexResponse = intent.getBooleanExtra(EXTRA_USE_HEX_RESPONSE, false),
                        initialWaitForResponse = intent.getBooleanExtra(EXTRA_WAIT_FOR_RESPONSE, false),
                        initialTimeout = intent.getStringExtra(EXTRA_TIMEOUT) ?: "1000",
                        initialMaxBufferSize = intent.getStringExtra(EXTRA_MAX_BUFFER_SIZE) ?: "1024"
                    )
                }
            }
        }
    }
}

fun onSaveClick(
    activity: Activity,
    ipAddress: String,
    port: String,
    isHexPayload: Boolean,
    useHexResponse: Boolean,
    waitForResponse: Boolean,
    payload: String,
    timeout: String,
    maxBufferSize: String,
) {
    val resultIntent = Intent().apply {
        putExtra(MainActivity.EXTRA_IP_ADDRESS, ipAddress)
        putExtra(MainActivity.EXTRA_PORT, port)
        putExtra(MainActivity.EXTRA_PAYLOAD, payload)
        putExtra(MainActivity.EXTRA_IS_HEX_PAYLOAD, isHexPayload)
        putExtra(MainActivity.EXTRA_USE_HEX_RESPONSE, useHexResponse)
        putExtra(MainActivity.EXTRA_WAIT_FOR_RESPONSE, waitForResponse)
        putExtra(MainActivity.EXTRA_TIMEOUT, timeout)
        putExtra(MainActivity.EXTRA_MAX_BUFFER_SIZE, maxBufferSize)
    }
    activity.setResult(Activity.RESULT_OK, resultIntent)
    activity.finish()
}

@Composable
fun AppContent(
    modifier: Modifier = Modifier,
    initialIpAddress: String,
    initialPort: String,
    initialPayload: String,
    initialIsHexPayload: Boolean,
    initialUseHexResponse: Boolean,
    initialWaitForResponse: Boolean,
    initialTimeout: String,
    initialMaxBufferSize: String,
) {
    val activity = LocalActivity.current

    var ipAddress by remember { mutableStateOf(initialIpAddress) }
    var port by remember { mutableStateOf(initialPort) }
    var isHexPayload by remember { mutableStateOf(initialIsHexPayload) }
    var useHexResponse by remember { mutableStateOf(initialUseHexResponse) }
    var waitForResponse by remember { mutableStateOf(initialWaitForResponse) }
    var payload by remember { mutableStateOf(initialPayload) }
    var timeout by remember { mutableStateOf(initialTimeout) }
    var maxBufferSize by remember { mutableStateOf(initialMaxBufferSize) }


    Column(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = ipAddress,
                onValueChange = { ipAddress = it },
                label = { Text("IP Address") },
                placeholder = { Text("192.168.1.0") },
                modifier = Modifier.weight(0.7f),
                singleLine = true
            )

            OutlinedTextField(
                value = port,
                onValueChange = { port = it },
                label = { Text("Port") },
                placeholder = { Text("8080") },
                modifier = Modifier.weight(0.3f),
                singleLine = true
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Checkbox(
                checked = isHexPayload,
                onCheckedChange = { isHexPayload = it }
            )
            Text("Use hex payload?")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Checkbox(
                checked = useHexResponse,
                onCheckedChange = { useHexResponse = it }
            )
            Text("Use hex response?")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Checkbox(
                checked = waitForResponse,
                onCheckedChange = { waitForResponse = it }
            )
            Text("Wait for response?")
        }


        OutlinedTextField(
            value = payload,
            onValueChange = { payload = it },
            label = { Text("Payload") },
            placeholder = { Text("{\"key\": \"value\"}") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 15,
            minLines = 10
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = timeout,
                onValueChange = { timeout = it },
                label = { Text("Timeout (ms)") },
                placeholder = { Text("1000") },
                modifier = Modifier.weight(0.3f),
                singleLine = true
            )

            OutlinedTextField(
                value = maxBufferSize,
                onValueChange = { maxBufferSize = it },
                label = { Text("Max buffer size") },
                placeholder = { Text("1024") },
                modifier = Modifier.weight(0.3f),
                singleLine = true
            )
        }

        Button(
            onClick = {
                if (activity == null) return@Button
                onSaveClick(
                    activity,
                    ipAddress,
                    port,
                    isHexPayload,
                    useHexResponse,
                    waitForResponse,
                    payload,
                    timeout,
                    maxBufferSize
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}
