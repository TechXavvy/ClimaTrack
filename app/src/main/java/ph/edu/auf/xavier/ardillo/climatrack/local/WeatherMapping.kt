package ph.edu.auf.xavier.ardillo.climatrack.local

import ph.edu.auf.xavier.ardillo.climatrack.local.models.LocationEntity
import ph.edu.auf.xavier.ardillo.climatrack.local.models.WeatherSnapshot
import ph.edu.auf.xavier.ardillo.climatrack.models.openweather.WeatherModel

object WeatherMapping {

    fun toLocation(lat: Double, lon: Double, api: WeatherModel): LocationEntity =
        LocationEntity().apply {
            name = api.name
            country = api.sys.country
            this.lat = lat
            this.lon = lon
        }

    fun toSnapshot(locationId: String, api: WeatherModel): WeatherSnapshot =
        WeatherSnapshot().apply {
            this.locationId = locationId
            this.temp = api.main.temp
            this.feelsLike = api.main.feelsLike
            this.humidity = api.main.humidity
            this.pressure = api.main.pressure
            this.windSpeed = api.wind.speed
            this.conditionCode = api.weather.firstOrNull()?.id ?: 0
            this.updatedAt = api.dt
        }
}
