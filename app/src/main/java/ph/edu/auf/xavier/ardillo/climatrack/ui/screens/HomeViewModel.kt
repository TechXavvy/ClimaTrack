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
    val tempC: String = "--",
    val status: String = "Idle",
    val error: String? = null
)

class HomeViewModel(
    private val repo: WeatherRepositories,
    private val apiKey: String
) : ViewModel() {

    private val _ui = MutableStateFlow(HomeUiState())
    val ui: StateFlow<HomeUiState> = _ui

    /** Call this from the UI (button, onStart, etc.) */
    fun load(lat: Double, lon: Double) {
        _ui.value = _ui.value.copy(status = "Loading...", error = null)
        viewModelScope.launch {
            try {
                // Uses your existing repository’s fetch+cache method
                val result = repo.getAndCacheCurrent(lat, lon, apiKey)
                _ui.value = HomeUiState(
                    city = result.name,
                    tempC = result.main.temp.toString(),
                    status = "Done",
                    error = null
                )
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(
                    status = "Error",
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    /** Simple factory so we can pass apiKey without DI right now */
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
