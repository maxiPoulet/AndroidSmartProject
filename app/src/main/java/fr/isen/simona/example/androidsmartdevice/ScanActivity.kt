package fr.isen.simona.example.androidsmartdevice

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.content.Intent
import android.content.pm.PackageManager
import android.bluetooth.le.ScanResult
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import fr.isen.simona.example.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme

class ScanActivity : ComponentActivity() {

    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private val devices = mutableStateListOf<ScanResult>()
    private val scanPeriod: Long = 10000 // Durée du scan
    private var isScanning = false // État du scan

    // Handler pour arrêter le scan après la période spécifiée
    private val handler = Handler(Looper.getMainLooper())

    // Permissions requises
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions[Manifest.permission.BLUETOOTH_SCAN] == true &&
                    permissions[Manifest.permission.BLUETOOTH_CONNECT] == true &&
                    permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true

            if (granted) {
                // Vérifie et active Bluetooth si nécessaire
                checkAndEnableBluetooth()
            } else {
                Toast.makeText(this, "Permissions nécessaires refusées.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScanScreen(
                devices = devices,
                isScanning = isScanning,
                onToggleScan = { toggleScan() }
            )
        }
    }

    private fun toggleScan() {
        if (isScanning) {
            stopScan()
        } else {
            if (isAllPermissionsGranted()) {
                checkAndEnableBluetooth()
            } else {
                requestPermissions()
            }
        }
    }

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

    private fun requestPermissions() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    @SuppressLint("MissingPermission")
    private fun checkAndEnableBluetooth() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enableBtIntent)
        } else {
            startScan()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startScan() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        if (bluetoothLeScanner != null) {
            devices.clear() // Réinitialise la liste des appareils
            bluetoothLeScanner?.startScan(scanCallback)
            isScanning = true

            // Arrête automatiquement le scan après la période définie
            handler.postDelayed({ stopScan() }, scanPeriod)
        } else {
            Toast.makeText(this, "Bluetooth LE non pris en charge.", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopScan() {
        bluetoothLeScanner?.stopScan(scanCallback)
        isScanning = false
    }

    // Callback pour gérer les résultats du scan
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (devices.none { it.device.address == result.device.address }) {
                devices.add(result)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("ScanActivity", "Échec du scan : $errorCode")
        }
    }
}

@Composable
fun DeviceItem(
    deviceName: String,
    deviceAddress: String,
    signalStrength: Int,
    signalColor: Color,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                val intent = Intent(context, DeviceActivity::class.java).apply {
                    putExtra("deviceName", deviceName)  // Pass the device name
                    putExtra("deviceAddress", deviceAddress)  // Pass the device address
                    putExtra("deviceRSSI", signalStrength)  // Pass the device RSSI
                }
                context.startActivity(intent)  // This should start DeviceActivity
            }
            .background(signalColor.copy(alpha = 0.2f))
            .padding(16.dp)
    ) {
        Text(
            text = "Nom : $deviceName",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(text = "Adresse : $deviceAddress", fontSize = 14.sp, color = Color.Gray)
        Text(text = "Signal : $signalStrength dBm", fontSize = 14.sp, color = Color.Gray)
    }
}

@SuppressLint("MissingPermission")
@Composable
fun ScanScreen(
    devices: List<ScanResult>,
    isScanning: Boolean,
    onToggleScan: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Titre
        Text(
            text = if (isScanning) "Scan BLE en cours ..." else "Lancer le Scan BLE",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        // Bouton Play/Pause
        androidx.compose.material3.IconButton(
            onClick = { onToggleScan() },
            modifier = Modifier.size(80.dp)
        ) {
            Icon(
                painter = painterResource(
                    id = if (isScanning) R.drawable.pause_circle else R.drawable.play_circle
                ),
                contentDescription = if (isScanning) "Stop scan" else "Launch scan", tint = Color(android.graphics.Color.parseColor("#FF0099CC")),
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Liste des appareils détectés
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(devices) { scanResult -> // Utilise scanResult ici, pas result
                val signalStrength = scanResult.rssi // Utilisation correcte de scanResult
                val deviceName = scanResult.device.name ?: "Appareil inconnu"
                val deviceAddress = scanResult.device.address
                val signalColor = when {
                    signalStrength > -50 -> Color.Green
                    signalStrength > -70 -> Color.Yellow
                    else -> Color.Red
                }

                // Récupération du contexte
                val context = LocalContext.current

                DeviceItem(
                    deviceName = deviceName,
                    deviceAddress = deviceAddress,
                    signalStrength = signalStrength,
                    signalColor = signalColor,
                    onClick = {
                        // Passer les données à DeviceActivity
                        val intent = Intent(context, DeviceActivity::class.java).apply {
                            putExtra("deviceName", deviceName)
                            putExtra("deviceAddress", deviceAddress)
                            putExtra("deviceRSSI", signalStrength)
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}