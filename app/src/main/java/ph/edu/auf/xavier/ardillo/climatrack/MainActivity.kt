package ph.edu.auf.xavier.ardillo.climatrack

import android.R.attr.apiKey
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ph.edu.auf.xavier.ardillo.climatrack.ui.theme.ClimaTrackTheme
import ph.edu.auf.xavier.ardillo.climatrack.repositories.WeatherRepositories
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.util.Log
import ph.edu.auf.xavier.ardillo.climatrack.R

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClimaTrackTheme {
                val apiKey = getString(R.string.openweather_api_key)
                val repo = WeatherRepositories()
            }
        }
    }
}