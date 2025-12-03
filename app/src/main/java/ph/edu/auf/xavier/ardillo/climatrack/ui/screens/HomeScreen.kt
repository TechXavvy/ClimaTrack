package ph.edu.auf.xavier.ardillo.climatrack.ui.screens

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import ph.edu.auf.xavier.ardillo.climatrack.location.LocationUtils
import ph.edu.auf.xavier.ardillo.climatrack.ui.design.DayPart
import ph.edu.auf.xavier.ardillo.climatrack.ui.theme.ClimaTrackTheme
import ph.edu.auf.xavier.ardillo.climatrack.ui.design.currentDayPart
import ph.edu.auf.xavier.ardillo.climatrack.ui.design.themeFor
import ph.edu.auf.xavier.ardillo.climatrack.ui.design.toCategory
import java.time.LocalTime

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(apiKey: String) {
    val vm: HomeViewModel = viewModel(factory = HomeViewModel.provideFactory(apiKey))
    val ui by vm.ui.collectAsState()
    val ctx = LocalContext.current

    val perms: MultiplePermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    var requestedOnce by remember { mutableStateOf(false) }
    var fetchedOnce by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!requestedOnce) {
            requestedOnce = true
            perms.launchMultiplePermissionRequest()
        }
    }

    val anyGranted = perms.permissions.any { it.status.isGranted }
    LaunchedEffect(anyGranted) {
        if (anyGranted && !fetchedOnce) {
            fetchedOnce = true
            val fresh = LocationUtils.getFreshLatLon(ctx)
            if (fresh != null) vm.loadByCoords(fresh.first, fresh.second)
        }
    }

    // -------- visuals (background) ----------
    val category = toCategory(ui.weatherCode)
    val dayPart = currentDayPart(LocalTime.now())
    val theme = themeFor(category, dayPart)

    // background canvas
    Box(
        Modifier
            .fillMaxSize()
            .background(theme.gradient)
            .padding(16.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            // Top location line
            Text(
                text = "${ui.city}, ${ui.country}",
                color = theme.topTextColor,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(Modifier.height(12.dp))

            // Big temp + condition center
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${ui.tempC}°",
                    color = theme.bigTempColor,
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = ui.conditionMain.uppercase(),
                    color = theme.bigTempColor,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(Modifier.height(16.dp))

            // Card: Weather Now (6 tiles)
            WeatherNowCard(
                feelsLike = ui.feelsLikeC + "° C",
                wind = ui.windKmh + " km/h",
                humidity = ui.humidityPct + "%",
                pressure = ui.pressureMbar + " mbar",
                rainfall = ui.rainfallMm + "mm",
                uv = "—", // needs OneCall API; placeholder for now
            )

            Spacer(Modifier.height(16.dp))

            // Hourly row ("Today / Tomorrow" header)
            TodayTomorrowRow()
            Spacer(Modifier.height(8.dp))
            HourlyStrip(
                hours = listOf("09:00","10:00","11:00","12:00","13:00"),
                temps = List(5) { "${ui.tempC}° C" },
                selectedIndex = 1
            )
        }

        // Bottom tiny status / spinner
        if (ui.status == "Loading...") {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
                color = Color.White
            )
        }
    }
}

/* --------- components --------- */

@Composable
private fun WeatherNowCard(
    feelsLike: String,
    wind: String,
    humidity: String,
    pressure: String,
    rainfall: String,
    uv: String
) {
    Surface(
        tonalElevation = 6.dp,
        shape = MaterialTheme.shapes.large,
        color = Color.White.copy(alpha = 0.92f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                text = "Weather Now",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
            )
            Spacer(Modifier.height(16.dp))

            // two rows of three metrics
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Metric(icon = Icons.Default.Thermostat, label = "Feels Like", value = feelsLike)
                Metric(icon = Icons.Default.Air, label = "Wind", value = wind)
                Metric(icon = Icons.Default.WaterDrop, label = "Humidity", value = humidity)
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Metric(icon = Icons.Default.Speed, label = "Pressure", value = pressure)
                Metric(icon = Icons.Default.Thunderstorm, label = "Rainfall", value = rainfall)
                Metric(icon = Icons.Default.LightMode, label = "UV", value = uv)
            }
        }
    }
}

@Composable
private fun Metric(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Icon(icon, contentDescription = label, tint = Color.Black.copy(alpha = 0.85f))
        Spacer(Modifier.height(4.dp))
        Text(label, color = Color.Black.copy(alpha = 0.6f), fontSize = 12.sp)
        Text(value, color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TodayTomorrowRow() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Row {
            Text("Today", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            Spacer(Modifier.width(16.dp))
            Text("Tomorrow", color = Color.Black.copy(alpha = 0.5f), fontSize = 20.sp)
        }
        Text("24 hours >", color = Color.Black.copy(alpha = 0.5f))
    }
}

@Composable
private fun HourlyStrip(hours: List<String>, temps: List<String>, selectedIndex: Int) {
    LazyRow {
        items(hours.indices.toList()) { idx ->
            val selected = idx == selectedIndex
            HourChip(time = hours[idx], temp = temps[idx], selected = selected)
            Spacer(Modifier.width(12.dp))
        }
    }
}

@Composable
private fun HourChip(time: String, temp: String, selected: Boolean) {
    val bg = if (selected) Color(0xFF49B4FF) else Color.White.copy(alpha = 0.9f)
    val txt = if (selected) Color.White else Color.Black
    Column(
        modifier = Modifier
            .width(100.dp)
            .clip(MaterialTheme.shapes.large)
            .background(bg)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(time, color = txt.copy(alpha = 0.8f))
        Spacer(Modifier.height(6.dp))
        Text(temp, color = txt, fontWeight = FontWeight.Bold)
    }
}
