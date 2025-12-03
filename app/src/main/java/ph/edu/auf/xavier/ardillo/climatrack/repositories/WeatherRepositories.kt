package ph.edu.auf.xavier.ardillo.climatrack.repositories

import android.util.Log
import ph.edu.auf.xavier.ardillo.climatrack.apis.factories.RetrofitFactory
import ph.edu.auf.xavier.ardillo.climatrack.models.openweather.OneCallResponse
import ph.edu.auf.xavier.ardillo.climatrack.models.openweather.ForecastResponse
import ph.edu.auf.xavier.ardillo.climatrack.models.openweather.WeatherModel
import kotlin.math.abs

data class HourSlot(val epochSec: Long, val tempC: Double, val code: Int)
data class HourlyBundle(val offsetSec: Int, val hours: List<HourSlot>)

class WeatherRepositories {
    private val weatherService = RetrofitFactory.createWeatherAPIService("")

    suspend fun getCurrentWeather(lat: String, lon: String): WeatherModel {
        // your older overload — keep as-is even if unused
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

    /** Public: get next 6 hours starting at the next whole hour (local to location). */
    suspend fun getNext6Hourly(
        lat: Double,
        lon: Double,
        apiKey: String
    ): HourlyBundle {
        // Try One Call 3.0 first
        runCatching {
            val one = weatherService.getOneCallHourly(
                lat = lat.toString(),
                lon = lon.toString(),
                appid = apiKey
            )
            return oneCallTo6Hours(one)
        }.onFailure {
            Log.w("ClimaTrack", "OneCall 3.0 failed (${it.javaClass.simpleName}): ${it.message}")
        }

        // Fallback: 5-day/3-hour forecast (free). Interpolate temps to 1h slots.
        val fore = weatherService.getFiveDay3Hour(
            lat = lat.toString(),
            lon = lon.toString(),
            appid = apiKey
        )
        return forecastTo6Hours(fore)
    }

    /* ---------- helpers ---------- */

    private fun oneCallTo6Hours(res: OneCallResponse): HourlyBundle {
        val nowUtc = System.currentTimeMillis() / 1000L
        val next = res.hourly
            .filter { it.dt > nowUtc }
            .take(6)
            .map {
                HourSlot(
                    epochSec = it.dt,
                    tempC = it.temp,
                    code = it.weather.firstOrNull()?.id ?: 0
                )
            }
        return HourlyBundle(offsetSec = res.timezoneOffset, hours = next)
    }

    private fun forecastTo6Hours(res: ForecastResponse): HourlyBundle {
        val now = System.currentTimeMillis() / 1000L
        val nextHour = ((now / 3600L) + 1L) * 3600L
        val targets = (0 until 6).map { nextHour + it * 3600L }

        val sorted = res.list.sortedBy { it.dt }
        val hours = targets.map { t ->
            // find neighbors
            val prev = sorted.lastOrNull { it.dt <= t } ?: sorted.first()
            val next = sorted.firstOrNull { it.dt >= t } ?: sorted.last()

            val temp = if (prev.dt == next.dt) {
                prev.main.temp
            } else {
                val f = (t - prev.dt).toDouble() / (next.dt - prev.dt).toDouble()
                prev.main.temp + f * (next.main.temp - prev.main.temp)
            }

            val pick = if (abs(t - prev.dt) <= abs(next.dt - t)) prev else next
            val code = pick.weather.firstOrNull()?.id ?: 0

            HourSlot(epochSec = t, tempC = temp, code = code)
        }

        return HourlyBundle(offsetSec = res.city.timezone, hours = hours)
    }
}
