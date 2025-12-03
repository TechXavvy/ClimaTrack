package ph.edu.auf.xavier.ardillo.climatrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ph.edu.auf.xavier.ardillo.climatrack.ui.screens.AppNavigation
import ph.edu.auf.xavier.ardillo.climatrack.ui.theme.ClimaTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ClimaTrackTheme {
                val apiKey = getString(R.string.openweather_api_key)
                AppNavigation(apiKey = apiKey)
            }
        }
    }
}
