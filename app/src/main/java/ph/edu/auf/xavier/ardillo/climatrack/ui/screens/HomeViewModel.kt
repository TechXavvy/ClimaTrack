package ph.edu.auf.xavier.ardillo.climatrack.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ph.edu.auf.xavier.ardillo.climatrack.repositories.WeatherRepositories
import ph.edu.auf.xavier.ardillo.climatrack.ui.design.DayPart
import ph.edu.auf.xavier.ardillo.climatrack.ui.design.iconFor
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class HomeUiState(
    val city: String = "--",
    val country: String = "--",
    val tempC: String = "--",
    val feelsLikeC: String = "--",
    val windKmh: String = "--",
    val humidityPct: String = "--",
    val pressureMbar: String = "--",
    val rainfallMm: String = "0",
    val conditionMain: String = "--",    // e.g., SUNNY / RAINY / CLOUDS text
    val weatherCode: Int? = null,        // to map visuals

    // Hourly strip (next 6 hours from the next whole hour)
    val hourTimes: List<String> = emptyList(),   // ["21:00", "22:00", ...]
    val hourTemps: List<String> = emptyList(),   // ["30° C", ...]
    val hourIcons: List<Int> = emptyList(),      // drawable res ids

    val status: String = "Idle",
    val error: String? = null
)

class HomeViewModel(
    private val repo: WeatherRepositories,
    private val apiKey: String
) : ViewModel() {

    private val _ui = MutableStateFlow(HomeUiState())
    val ui: StateFlow<HomeUiState> = _ui

    /** Call this once you have coordinates. */
    fun loadByCoords(lat: Double, lon: Double) {
        _ui.value = _ui.value.copy(status = "Loading...", error = null)
        viewModelScope.launch {
            try {
                // ---- Current weather ----
                val res = repo.getAndCacheCurrent(lat, lon, apiKey)

                val windKmH = (res.wind.speed * 3.6) // m/s -> km/h
                val feels = res.main.feelsLike
                val rainMm = 0.0 // WeatherModel has no 'rain' field in your current schema

                _ui.value = HomeUiState(
                    city = res.name,
                    country = res.sys.country,
                    tempC = "%.0f".format(res.main.temp),
                    feelsLikeC = "%.0f".format(feels),
                    windKmh = "%.0f".format(windKmH),
                    humidityPct = "${res.main.humidity}",
                    pressureMbar = "${res.main.pressure}",
                    rainfallMm = "%.0f".format(rainMm),
                    conditionMain = res.weather.firstOrNull()?.main ?: "--",
                    weatherCode = res.weather.firstOrNull()?.id,
                    status = "Done",
                    error = null
                )

                // ---- Hourly (next 6 from next whole hour) ----
                try {
                    val hourly = repo.getNext6Hourly(lat, lon, apiKey)

                    // Times formatted for the location's timezone
                    val times = hourly.hours.map { slot ->
                        formatHour(slot.epochSec, hourly.offsetSec)
                    }

                    // Temps formatted as "30° C"
                    val temps = hourly.hours.map { slot ->
                        "%.0f° C".format(slot.tempC)
                    }

                    // Icon per slot using its local hour -> DayPart
                    val icons = hourly.hours.map { slot ->
                        val hour = hourOf(slot.epochSec, hourly.offsetSec)
                        val part = when (hour) {
                            in 5..11 -> DayPart.MORNING
                            in 12..17 -> DayPart.AFTERNOON
                            else -> DayPart.EVENING
                        }
                        iconFor(slot.code, part)
                    }

                    _ui.value = _ui.value.copy(
                        hourTimes = times,
                        hourTemps = temps,
                        hourIcons = icons
                    )
                } catch (_: Exception) {
                    // ignore hourly failure; keep current weather
                }

            } catch (e: Exception) {
                _ui.value = _ui.value.copy(status = "Error", error = e.message ?: "Unknown error")
            }
        }
    }

    companion object {
        fun provideFactory(apiKey: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(
                        repo = WeatherRepositories(),
                        apiKey = apiKey
                    ) as T
                }
            }
    }
}

private val hhFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:00")

private fun formatHour(epochSec: Long, offsetSec: Int): String =
    Instant.ofEpochSecond(epochSec)
        .atOffset(ZoneOffset.ofTotalSeconds(offsetSec))
        .toLocalTime()
        .withMinute(0)
        .format(hhFormatter)

private fun hourOf(epochSec: Long, offsetSec: Int): Int =
    Instant.ofEpochSecond(epochSec)
        .atOffset(ZoneOffset.ofTotalSeconds(offsetSec))
        .hour
