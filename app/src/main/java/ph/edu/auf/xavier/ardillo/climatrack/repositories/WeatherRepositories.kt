package ph.edu.auf.xavier.ardillo.climatrack.repositories

import ph.edu.auf.xavier.ardillo.climatrack.apis.factories.RetrofitFactory
import ph.edu.auf.xavier.ardillo.climatrack.models.WeatherModel

class WeatherRepositories {
    private val weatherService = RetrofitFactory.createWeatherAPIService("")

    suspend fun getCurrentWeather(lat: String, lon: String): WeatherModel {
        return weatherService.getCurrentWeather(lat, lon, "", "metric")
    }
}