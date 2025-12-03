package ph.edu.auf.xavier.ardillo.climatrack.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ph.edu.auf.xavier.ardillo.climatrack.repositories.WeatherRepositories

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
                val res = repo.getAndCacheCurrent(lat, lon, apiKey)

                // OpenWeather wind.speed is m/s by default in metric; convert to km/h
                val windKmH = (res.wind.speed * 3.6)
                val feels = res.main.feelsLike
                val rainMm = 0.0 // your current WeatherModel has no 'rain' field; keep 0 for now

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
