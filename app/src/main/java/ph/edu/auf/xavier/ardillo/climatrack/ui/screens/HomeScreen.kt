package ph.edu.auf.xavier.ardillo.climatrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(apiKey: String) {
    // Get a VM instance tied to this screen, with our factory to pass the key.
    val vm: HomeViewModel = viewModel(factory = HomeViewModel.provideFactory(apiKey))
    val state by vm.ui.collectAsState()

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Current Weather", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text("City: ${state.city}")
        Text("Temp: ${state.tempC} °C")
        Spacer(Modifier.height(8.dp))
        Text("Status: ${state.status}")
        if (state.error != null) {
            Spacer(Modifier.height(4.dp))
            Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            // Example coords: Angeles City
            vm.load(15.1485, 120.5895)
        }) {
            Text("Fetch Angeles City")
        }
    }
}
