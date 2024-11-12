package fr.isen.simona.example.androidsmartdevice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import fr.isen.simona.example.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme

class ScanActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidSmartDeviceTheme {
                ScanScreen()
            }
        }
    }
}

@Composable
fun ScanScreen() {
    Text(text = "Lancer le scan BLE")
}

@Preview(showBackground = true)
@Composable
fun ScanScreenPreview() {
    AndroidSmartDeviceTheme {
        ScanScreen()
    }
}