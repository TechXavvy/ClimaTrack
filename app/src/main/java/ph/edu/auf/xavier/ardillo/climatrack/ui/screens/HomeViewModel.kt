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
    val conditionMain: String = "--",
    val weatherCode: Int? = null,

    // TODAY strip
    val hourTimes: List<String> = emptyList(),
    val hourTemps: List<String> = emptyList(),
    val hourIcons: List<Int> = emptyList(),

    // TOMORROW strip
    val tomorrowTimes: List<String> = emptyList(),
    val tomorrowTemps: List<String> = emptyList(),
    val tomorrowIcons: List<Int> = emptyList(),

    val status: String = "Idle",
    val error: String? = null
)

class HomeViewModel(
    private val repo: WeatherRepositories,
    private val apiKey: String
) : ViewModel() {

    private val _ui = MutableStateFlow(HomeUiState())
    val ui: StateFlow<HomeUiState> = _ui

    // Remember last coords so UI can request tomorrow on-demand
    private var lastLat: Double? = null
    private var lastLon: Double? = null

    /** Initial load by current coordinates. */
    fun loadByCoords(lat: Double, lon: Double) {
        lastLat = lat
        lastLon = lon

        _ui.value = _ui.value.copy(status = "Loading...", error = null)
        viewModelScope.launch {
            try {
                // ---- Current weather ----
                val res = repo.getAndCacheCurrent(lat, lon, apiKey)

                val windKmH = (res.wind.speed * 3.6)
                val feels = res.main.feelsLike
                val rainMm = 0.0

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

                // ---- TODAY hourly ----
                runCatching {
                    val hourly = repo.getNext6Hourly(lat, lon, apiKey)

                    val times = hourly.hours.map { slot -> formatHour(slot.epochSec, hourly.offsetSec) }
                    val temps = hourly.hours.map { slot -> "%.0f° C".format(slot.tempC) }
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
                }

                // (Optional) prefetch tomorrow quietly; comment out if you prefer on-click loading
                runCatching { loadTomorrowInternal() }

            } catch (e: Exception) {
                _ui.value = _ui.value.copy(status = "Error", error = e.message ?: "Unknown error")
            }
        }
    }

    /** UI calls this when the user taps the "Tomorrow" tab. */
    fun loadTomorrowIfNeeded() {
        viewModelScope.launch { loadTomorrowInternal() }
    }

    private suspend fun loadTomorrowInternal() {
        val lat = lastLat ?: return
        val lon = lastLon ?: return
        if (_ui.value.tomorrowTimes.isNotEmpty()) return // already loaded

        runCatching {
            val bundle = repo.getTomorrow6Hourly(lat, lon, apiKey)

            val times = bundle.hours.map { slot -> formatHour(slot.epochSec, bundle.offsetSec) }
            val temps = bundle.hours.map { slot -> "%.0f° C".format(slot.tempC) }
            val icons = bundle.hours.map { slot ->
                val hour = hourOf(slot.epochSec, bundle.offsetSec)
                val part = when (hour) {
                    in 5..11 -> DayPart.MORNING
                    in 12..17 -> DayPart.AFTERNOON
                    else -> DayPart.EVENING
                }
                iconFor(slot.code, part)
            }

            _ui.value = _ui.value.copy(
                tomorrowTimes = times,
                tomorrowTemps = temps,
                tomorrowIcons = icons
            )
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

/* ----- time helpers ----- */

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
