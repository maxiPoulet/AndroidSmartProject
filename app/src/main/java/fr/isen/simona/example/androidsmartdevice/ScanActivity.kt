package fr.isen.simona.example.androidsmartdevice

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.content.Intent
import android.content.pm.PackageManager
import android.bluetooth.le.ScanResult
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import fr.isen.simona.example.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme

class ScanActivity : ComponentActivity() {

    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private val devices = mutableStateListOf<ScanResult>()
    private val scanPeriod: Long = 10000 // 10 seconds

    // Request permission for Bluetooth and location access (if needed)
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.BLUETOOTH_SCAN] == true &&
                permissions[Manifest.permission.BLUETOOTH_CONNECT] == true &&
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                startScan()
            } else {
                Toast.makeText(this, "Permissions denied, Bluetooth scanning won't work.", Toast.LENGTH_SHORT).show()
            }
        }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val isScanning = remember { mutableStateOf(false) }

            // Check and request permissions on start
            if (isAllPermissionsGranted()) {
                startScan()
            } else {
                requestPermissions()
            }

            // Composable UI for ScanActivity
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Header text
                    Text("Scanning for BLE Devices", style = MaterialTheme.typography.headlineMedium)

                    // Bouton image (Play/Pause)
                    androidx.compose.material3.IconButton(
                        onClick = {
                            if (isScanning.value) {
                                stopScan()
                            } else {
                                startScan()
                            }
                            isScanning.value = !isScanning.value
                        }
                    ) {
                        androidx.compose.material3.Icon(
                            painter = painterResource(
                                id = if (isScanning.value) {
                                    R.drawable.pause_circle
                                } else R.drawable.play_circle
                            ),
                            contentDescription = if (isScanning.value) "Pause Scan" else "Start Scan",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    // Button to start/stop scan
                    /*Button(
                        onClick = {
                            if (isScanning.value) {
                                stopScan()
                            } else {
                                startScan()
                            }
                            isScanning.value = !isScanning.value
                        }
                    ) {
                        Text(text = if (isScanning.value) "Stop Scan" else "Start Scan")
                    }
*/
                    Spacer(modifier = Modifier.height(16.dp))

                    // Display the scanned devices
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp)
                    ) {
                        items(devices) { scanResult ->
                            DeviceItem(scanResult = scanResult)
                        }
                    }
                }
            }
        }
    }

    // Check if the required permissions are granted
    private fun isAllPermissionsGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    // Request the necessary permissions
    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestPermissions() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    // Start Bluetooth scanning
    @SuppressLint("MissingPermission")
    private fun startScan() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        if (bluetoothLeScanner != null) {
            bluetoothLeScanner?.startScan(scanCallback)
            Log.d("ScanActivity", "Scanning started...")
            Handler(Looper.getMainLooper()).postDelayed({
                stopScan()
            }, scanPeriod)
        } else {
            Toast.makeText(this, "Bluetooth LE is not supported on this device.", Toast.LENGTH_SHORT).show()
        }
    }

    // Stop Bluetooth scanning
    private fun stopScan() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        )
            bluetoothLeScanner?.stopScan(scanCallback)
        Log.d("ScanActivity", "Scanning stopped.")
    }

    // Callback for Bluetooth LE scan results
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            // Add the device to the list if it's not already there
            if (devices.none { it.device.address == result.device.address }) {
                devices.add(result)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("ScanActivity", "Scan failed with error code: $errorCode")
        }
    }
}


@Composable
fun DeviceItem(scanResult: ScanResult) {
    val context = LocalContext.current

    // Vérification de la permission avant d'afficher les informations du périphérique
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(text = "Name: ${scanResult.device.name ?: "Unknown Device"}")
            Text(text = "Address: ${scanResult.device.address}")
            Text(text = "RSSI: ${scanResult.rssi}")

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                // Lancer DeviceActivity pour se connecter au périphérique
                val intent = Intent(context, DeviceActivity::class.java).apply {
                    putExtra("deviceName", scanResult.device.name ?: "Unknown Device")
                    putExtra("deviceAddress", scanResult.device.address)
                    putExtra("deviceRSSI", scanResult.rssi)
                    putExtra("device", scanResult.device)
                }
                context.startActivity(intent)
            }) {
                Text("Connect")
            }
        }
    } else {
        // Si la permission n'est pas accordée, afficher un message d'erreur ou rien
        Text(text = "Permission required to connect to the device.")
    }
}

