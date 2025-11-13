package ph.edu.auf.xavier.ardillo.climatrack.repositories

import ph.edu.auf.xavier.ardillo.climatrack.apis.factories.RetrofitFactory
import ph.edu.auf.xavier.ardillo.climatrack.models.openweather.WeatherModel

class WeatherRepositories {
    private val weatherService = RetrofitFactory.createWeatherAPIService("")

    suspend fun getCurrentWeather(lat: String, lon: String): WeatherModel {
        return weatherService.getCurrentWeather(lat, lon, "", "metric")
    }

    suspend fun getCurrentWeather(
        lat: String,
        lon: String,
        apiKey: String
    ): WeatherModel {
        return weatherService.getCurrentWeather(
            lat = lat,
            lon = lon,
            appid = apiKey,
            units = "metric"
        )
    }

    suspend fun getAndCacheCurrent(
        lat: Double,
        lon: Double,
        apiKey: String
    ): WeatherModel {
        val api = weatherService.getCurrentWeather(
            lat = lat.toString(),
            lon = lon.toString(),
            appid = apiKey,
            units = "metric"
        )

        val loc = ph.edu.auf.xavier.ardillo.climatrack.local.WeatherMapping.toLocation(lat, lon, api)
        ph.edu.auf.xavier.ardillo.climatrack.local.WeatherDao.upsertLocation(loc)

        val snap = ph.edu.auf.xavier.ardillo.climatrack.local.WeatherMapping.toSnapshot(loc.id, api)
        ph.edu.auf.xavier.ardillo.climatrack.local.WeatherDao.saveSnapshot(snap)

        return api
    }


}