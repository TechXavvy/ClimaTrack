package ph.edu.auf.xavier.ardillo.climatrack.ui.screens

import android.Manifest
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ph.edu.auf.xavier.ardillo.climatrack.location.LocationUtils
import ph.edu.auf.xavier.ardillo.climatrack.ui.design.*
import java.time.LocalTime

private enum class DayTab { TODAY, TOMORROW }

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(apiKey: String) {
    val vm: HomeViewModel = viewModel(factory = HomeViewModel.provideFactory(apiKey))
    val ui by vm.ui.collectAsState()
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // Location permissions
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

    // Visual theme binding
    val category = toCategory(ui.weatherCode)
    val dayPart = currentDayPart(LocalTime.now())
    val art = artFor(category, dayPart)

    // Tabs (Today/Tomorrow)
    var selectedTab by remember { mutableStateOf(DayTab.TODAY) }

    // Location picker bottom sheet state
    var showPicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(Modifier.fillMaxSize()) {
        // BACKGROUND IMAGE
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

            // Top row (location + actions)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Layers,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    // Place icon → opens picker
                    Icon(
                        Icons.Default.Place,
                        contentDescription = "Choose location",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .noRippleClickable {
                                vm.refreshRecents()
                                showPicker = true
                            }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Text("Today", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                "• • •",
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

            // Weather Now card
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

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Metric(Icons.Default.Thermostat, "Feels Like", ui.feelsLikeC + "° C")
                        Metric(Icons.Default.Air, "Wind", ui.windKmh + " km/h")
                        Metric(Icons.Default.WaterDrop, "Humidity", ui.humidityPct + "%")
                    }

                    Spacer(Modifier.height(20.dp))

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

            // Today/Tomorrow row (clickable)
            TodayTomorrowRow(
                selected = selectedTab,
                onSelect = { tab ->
                    selectedTab = tab
                    if (tab == DayTab.TOMORROW) vm.loadTomorrowIfNeeded()
                }
            )

            Spacer(Modifier.height(16.dp))

            // Crossfade between Today and Tomorrow strips
            Crossfade(targetState = selectedTab, label = "hourly_tabs") { tab ->
                val hTimes = if (tab == DayTab.TODAY) ui.hourTimes else ui.tomorrowTimes
                val hTemps = if (tab == DayTab.TODAY) ui.hourTemps else ui.tomorrowTemps
                val hIcons = if (tab == DayTab.TODAY) ui.hourIcons else ui.tomorrowIcons

                HourlyStrip(
                    hours = hTimes.ifEmpty { listOf("—","—","—","—","—","—") },
                    temps = hTemps.ifEmpty { listOf("—","—","—","—","—","—") },
                    icons = hIcons.ifEmpty { List(6) { ph.edu.auf.xavier.ardillo.climatrack.R.drawable.ic_cloudy } },
                    selectedIndex = 1
                )
            }

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

    // Location Picker Bottom Sheet
    if (showPicker) {
        ModalBottomSheet(
            onDismissRequest = { showPicker = false },
            sheetState = sheetState
        ) {
            LocationPickerSheet(
                ui = ui,
                onQuery = { q -> vm.searchLocations(q) },
                onClearQuery = { vm.clearSuggestions() },
                onSelect = { lat, lon ->
                    scope.launch {
                        // animate close first (suspend)
                        sheetState.hide()
                        showPicker = false
                        // then load the new location
                        vm.loadByCoords(lat, lon)
                    }
                }
            )
        }
    }
}

/* ---------- Picker content ---------- */

@Composable
private fun LocationPickerSheet(
    ui: HomeUiState,
    onQuery: (String) -> Unit,
    onClearQuery: () -> Unit,
    onSelect: (Double, Double) -> Unit
) {
    var query by remember { mutableStateOf("") }

    // Debounce typing for suggestions
    LaunchedEffect(query) {
        if (query.length >= 2) {
            delay(300)
            onQuery(query)
        } else {
            onClearQuery()
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            "Choose location",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search city, province, or country") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 320.dp)
        ) {
            if (ui.suggestions.isNotEmpty()) {
                item {
                    Text(
                        "Suggestions",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                items(ui.suggestions) { s ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .noRippleClickable { onSelect(s.lat, s.lon) }
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(s.display, fontWeight = FontWeight.Bold)
                            Text(
                                "Lat ${"%.2f".format(s.lat)}, Lon ${"%.2f".format(s.lon)}",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            if (ui.recents.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Recent",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                items(ui.recents) { r ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .noRippleClickable { onSelect(r.lat, r.lon) }
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.History, contentDescription = null)
                        Spacer(Modifier.width(10.dp))
                        Text(r.display)
                    }
                }
            }

            if (ui.suggestions.isEmpty() && ui.recents.isEmpty()) {
                item {
                    Text(
                        "No places yet. Try searching above.",
                        modifier = Modifier.padding(8.dp),
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

/* ---------- Reused widgets ---------- */

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
        Icon(icon, contentDescription = label, tint = Color.Black, modifier = Modifier.size(32.dp))
        Spacer(Modifier.height(8.dp))
        Text(label, color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(value, color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
private fun TodayTomorrowRow(
    selected: DayTab,
    onSelect: (DayTab) -> Unit
) {
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
                color = Color.White.copy(alpha = if (selected == DayTab.TODAY) 1f else 0.6f),
                modifier = Modifier
                    .padding(end = 16.dp)
                    .noRippleClickable { onSelect(DayTab.TODAY) }
            )
            Text(
                "Tomorrow",
                color = Color.White.copy(alpha = if (selected == DayTab.TOMORROW) 1f else 0.6f),
                fontSize = 22.sp,
                modifier = Modifier.noRippleClickable { onSelect(DayTab.TOMORROW) }
            )
        }
    }
}

@Composable
private fun HourlyStrip(
    hours: List<String>,
    temps: List<String>,
    icons: List<Int>,
    selectedIndex: Int
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(hours.indices.toList()) { idx ->
            val selected = idx == selectedIndex
            HourChip(
                time = hours[idx],
                temp = temps[idx],
                selected = selected,
                iconRes = icons[idx]
            )
        }
    }
}

@Composable
private fun HourChip(time: String, temp: String, selected: Boolean, iconRes: Int) {
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
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(32.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(temp, color = txt, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(4.dp))
        Text(time, color = txt.copy(alpha = 0.7f), fontSize = 12.sp)
    }
}

/* no-ripple helper */
private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    this.then(
        Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) { onClick() }
    )
}
