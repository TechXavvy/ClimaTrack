package ph.edu.auf.xavier.ardillo.climatrack.ui.screens

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import ph.edu.auf.xavier.ardillo.climatrack.location.LocationUtils

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(apiKey: String) {
    val vm: HomeViewModel = viewModel(factory = HomeViewModel.provideFactory(apiKey))
    val state by vm.ui.collectAsState()
    val ctx = LocalContext.current

    // Request BOTH; proceed if either is granted (fine OR coarse)
    val perms: MultiplePermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    var requestedOnce by remember { mutableStateOf(false) }
    var fetchedOnce by remember { mutableStateOf(false) }

    // Ask on first compose
    LaunchedEffect(Unit) {
        if (!requestedOnce) {
            requestedOnce = true
            perms.launchMultiplePermissionRequest()
        }
    }

    // When any permission becomes granted, auto-fetch location -> weather once
    val anyGranted = perms.permissions.any { it.status.isGranted }
    LaunchedEffect(anyGranted) {
        if (anyGranted && !fetchedOnce) {
            fetchedOnce = true
            val coords = LocationUtils.getFreshLatLon(ctx) // <-- fresh-only
            if (coords != null) {
                vm.loadByCoords(coords.first, coords.second)
            } else {
                // Fallback if still no fix
                vm.loadByCoords(15.1485, 120.5895) // Angeles City fallback
            }
        }
    }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Current Weather", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        if (!anyGranted) {
            Text("Please allow location to show weather for your area.")
            Spacer(Modifier.height(12.dp))
            Button(onClick = { perms.launchMultiplePermissionRequest() }) {
                Text("Enable Location")
            }
        } else {
            Text("City: ${state.city}")
            Text("Temp: ${state.tempC} °C")
            Spacer(Modifier.height(8.dp))
            when (state.status) {
                "Loading..." -> CircularProgressIndicator()
                "Error" -> Text(
                    "Error: ${state.error ?: "Unknown"}",
                    color = MaterialTheme.colorScheme.error
                )
                else -> Text("Status: ${state.status}")
            }
        }
    }
}
