package ph.edu.auf.xavier.ardillo.climatrack.ui.screens

import android.Manifest
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
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
import ph.edu.auf.xavier.ardillo.climatrack.ui.design.*
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
            LocationUtils.getFreshLatLon(ctx)?.let { (lat, lon) ->
                vm.loadByCoords(lat, lon)
            }
        }
    }

    // Map weather/time -> artwork + gradient
    val category = toCategory(ui.weatherCode)
    val dayPart = currentDayPart(LocalTime.now())
    val art = artFor(category, dayPart)

    Box(Modifier.fillMaxSize()) {
        // BACKGROUND IMAGE - Full screen
        val bgPainter: Painter = painterResource(id = art.backgroundRes)
        Image(
            painter = bgPainter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // CONTENT
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // Top location with icons
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${ui.city}, ${ui.country}",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row {
                    Icon(
                        Icons.Default.Layers,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Icon(
                        Icons.Default.Place,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // "Today" label with dots
            Text(
                text = "Today",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "• • •",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                letterSpacing = 4.sp
            )

            Spacer(Modifier.height(24.dp))

            // Big gradient temperature
            Text(
                text = "${ui.tempC}°",
                style = TextStyle(
                    brush = art.tempGradient,
                    fontSize = 120.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-4).sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = ui.conditionMain.uppercase(),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(32.dp))

            // "Weather Now" white card with rounded corners
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text(
                        text = "Weather Now",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(20.dp))

                    // First row of metrics
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Metric(Icons.Default.Thermostat, "Feels Like", ui.feelsLikeC + "° C")
                        Metric(Icons.Default.Air, "Wind", ui.windKmh + " km/h")
                        Metric(Icons.Default.WaterDrop, "Humidity", ui.humidityPct + "%")
                    }

                    Spacer(Modifier.height(20.dp))

                    // Second row of metrics
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Metric(Icons.Default.Speed, "Pressure", ui.pressureMbar + "mbar")
                        Metric(Icons.Default.Thunderstorm, "Rainfall", ui.rainfallMm + "mm")
                        Metric(Icons.Default.LightMode, "UV", "10")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Today/Tomorrow row
            TodayTomorrowRow()

            Spacer(Modifier.height(16.dp))

            // Hourly forecast strip
            HourlyStrip(
                hours = listOf("09:00", "10:00", "11:00", "12:00", "13:00"),
                temps = List(5) { "${ui.tempC}° C" },
                selectedIndex = 1
            )

            Spacer(Modifier.height(16.dp))
        }

        if (ui.status == "Loading...") {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                color = Color.White
            )
        }
    }
}

@Composable
private fun Metric(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = Color.Black,
            modifier = Modifier.size(32.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            label,
            color = Color.Gray,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TodayTomorrowRow() {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row {
            Text(
                "Today",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.White
            )
            Spacer(Modifier.width(16.dp))
            Text(
                "Tomorrow",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 22.sp
            )
        }
        Text(
            "24 hours >",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp
        )
    }
}

@Composable
private fun HourlyStrip(
    hours: List<String>,
    temps: List<String>,
    selectedIndex: Int
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(hours.indices.toList()) { idx ->
            val selected = idx == selectedIndex
            HourChip(
                time = hours[idx],
                temp = temps[idx],
                selected = selected
            )
        }
    }
}

@Composable
private fun HourChip(time: String, temp: String, selected: Boolean) {
    val bg = if (selected) Color(0xFF49B4FF) else Color.White.copy(alpha = 0.95f)
    val txt = if (selected) Color.White else Color.Black

    Column(
        modifier = Modifier
            .width(110.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(vertical = 16.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Weather icon placeholder (you can add actual weather icons)
        Icon(
            Icons.Default.WbSunny,
            contentDescription = null,
            tint = txt.copy(alpha = 0.9f),
            modifier = Modifier.size(32.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            temp,
            color = txt,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            time,
            color = txt.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
    }
}