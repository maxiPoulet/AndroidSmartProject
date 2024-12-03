package fr.isen.simona.example.androidsmartdevice

import android.content.Intent
import android.icu.text.CaseMap.Title
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.simona.example.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidSmartDeviceTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding -> MainPage(innerPadding, onButtonClick = {
                    val intent = Intent(this, ScanActivity::class.java)
                    startActivity(intent)

                    })
                }
            }
        }
    }
}

@Composable
fun MainPage(innerPadding: PaddingValues, onButtonClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentAlignment = Alignment.Center
    ) {
       Column(
           verticalArrangement = Arrangement.SpaceBetween,
           horizontalAlignment = Alignment.CenterHorizontally
       ){
           Text(
               text = "Welcome Traveler, to AndroidSmart Device",
               fontSize = 24.sp,
               textAlign = TextAlign.Center,
               modifier = Modifier.padding(innerPadding)

           )
           Text(text = "To launch the scan of other devices")

           Image(
               painter = painterResource(R.drawable.bluetooth),
               contentDescription = "logo"
           )
           Spacer(modifier = Modifier.height(130.dp))

           Button(onClick = onButtonClick) {
               Text("Valider")
           }
       }
    }
}

