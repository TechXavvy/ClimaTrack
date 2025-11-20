package ph.edu.auf.xavier.ardillo.climatrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ph.edu.auf.xavier.ardillo.climatrack.repositories.WeatherRepositories

@Composable
fun HomeScreen(apiKey: String) {
    val repo = remember { WeatherRepositories() }
    var city by remember { mutableStateOf("--") }
    var temp by remember { mutableStateOf("--") }
    var status by remember { mutableStateOf("Idle") }
    val scope = rememberCoroutineScope()

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Current Weather", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Text("City: $city")
        Text("Temp: $temp °C")
        Spacer(Modifier.height(12.dp))
        Text("Status: $status")
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            scope.launch {
                try {
                    status = "Loading..."
                    // Angeles City sample coordinates
                    val result = repo.getAndCacheCurrent(15.1485, 120.5895, apiKey)
                    city = result.name
                    temp = result.main.temp.toString()
                    status = "Done"
                } catch (e: Exception) {
                    status = "Error: ${e.message}"
                }
            }
        }) {
            Text("Fetch Angeles City")
        }
    }
}
