package ph.edu.auf.xavier.ardillo.climatrack.repositories

import android.util.Log
import ph.edu.auf.xavier.ardillo.climatrack.apis.factories.RetrofitFactory
import ph.edu.auf.xavier.ardillo.climatrack.models.openweather.*
import kotlin.math.abs

data class HourSlot(val epochSec: Long, val tempC: Double, val code: Int)
data class HourlyBundle(val offsetSec: Int, val hours: List<HourSlot>)

/** UI-friendly suggestion object */
data class LocationSuggestion(
    val name: String,
    val country: String,
    val state: String?,
    val lat: Double,
    val lon: Double
) {
    val display: String get() = listOfNotNull(name, state, country).joinToString(", ")
}

class WeatherRepositories {
    // api.openweathermap.org (free endpoints)
    private val weatherService = RetrofitFactory.createWeatherAPIService("")
    // pro.openweathermap.org (Developer/Student plan hourly 1h endpoint)
    private val proService     = RetrofitFactory.createWeatherAPIServicePro()

    // --- kept for compatibility with existing callers ---
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

    /** TODAY: next 6 hours starting at the next whole hour (local). */
    suspend fun getNext6Hourly(
        lat: Double,
        lon: Double,
        apiKey: String
    ): HourlyBundle {
        // 1) Prefer PRO Hourly 4 Days (1-hour resolution)
        runCatching {
            val res = proService.getHourly4Days(
                lat = lat.toString(),
                lon = lon.toString(),
                appid = apiKey,
                units = "metric",
                cnt = 6
            )
            val nowUtc = System.currentTimeMillis() / 1000L
            val hours = res.list
                .filter { it.dt > nowUtc }
                .take(6)
                .map { item ->
                    HourSlot(
                        epochSec = item.dt,
                        tempC = item.main.temp,
                        code = item.weather.firstOrNull()?.id ?: 0
                    )
                }
            if (hours.isNotEmpty()) {
                return HourlyBundle(offsetSec = res.city.timezone, hours = hours)
            }
        }.onFailure {
            Log.w("ClimaTrack", "PRO hourly failed (${it.javaClass.simpleName}): ${it.message}")
        }

        // 2) One Call 3.0 hourly
        runCatching {
            val one = weatherService.getOneCallHourly(
                lat = lat.toString(),
                lon = lon.toString(),
                appid = apiKey
            )
            return oneCallToNext6(one)
        }.onFailure {
            Log.w("ClimaTrack", "OneCall 3.0 failed (${it.javaClass.simpleName}): ${it.message}")
        }

        // 3) 5-day/3-hour → interpolate to 1-hour
        val fore = weatherService.getFiveDay3Hour(
            lat = lat.toString(),
            lon = lon.toString(),
            appid = apiKey
        )
        return forecastToNext6(fore)
    }

    /** TOMORROW: 00:00–05:00 local (6 items). */
    suspend fun getTomorrow6Hourly(
        lat: Double,
        lon: Double,
        apiKey: String
    ): HourlyBundle {
        // 1) PRO hourly
        runCatching {
            val res = proService.getHourly4Days(
                lat = lat.toString(),
                lon = lon.toString(),
                appid = apiKey,
                units = "metric",
                cnt = 96
            )
            val offset = res.city.timezone
            val nowUtc = System.currentTimeMillis() / 1000L
            val localNow = nowUtc + offset
            val localMidnightNext = ((localNow / 86400L) + 1L) * 86400L
            val targetsLocal = (0 until 6).map { localMidnightNext + it * 3600L }
            val targetsUtc = targetsLocal.map { it - offset }

            val hours = targetsUtc.map { t ->
                val nearest = res.list.minByOrNull { abs(it.dt - t) }!!
                HourSlot(
                    epochSec = t,
                    tempC = nearest.main.temp,
                    code = nearest.weather.firstOrNull()?.id ?: 0
                )
            }
            return HourlyBundle(offsetSec = offset, hours = hours)
        }.onFailure {
            Log.w("ClimaTrack", "PRO hourly (tomorrow) failed: ${it.message}")
        }

        // 2) One Call 3.0
        runCatching {
            val one = weatherService.getOneCallHourly(
                lat = lat.toString(),
                lon = lon.toString(),
                appid = apiKey
            )
            return oneCallToTomorrow6(one)
        }.onFailure {
            Log.w("ClimaTrack", "OneCall 3.0 (tomorrow) failed: ${it.message}")
        }

        // 3) 5-day/3-hour fallback
        val fore = weatherService.getFiveDay3Hour(
            lat = lat.toString(),
            lon = lon.toString(),
            appid = apiKey
        )
        return forecastToTomorrow6(fore)
    }

    /* ---------- NEW: search + recents ---------- */

    suspend fun searchLocations(query: String, apiKey: String, limit: Int = 5): List<LocationSuggestion> {
        if (query.length < 2) return emptyList()
        val results = weatherService.geocodeDirect(query, limit, apiKey)
        return results.map {
            LocationSuggestion(
                name = it.name,
                country = it.country,
                state = it.state,
                lat = it.lat,
                lon = it.lon
            )
        }
    }

    fun recentLocations(limit: Int = 10): List<LocationSuggestion> {
        val all = ph.edu.auf.xavier.ardillo.climatrack.local.WeatherDao.allLocations()
        return all
            .distinctBy { it.name.trim().lowercase() + "|" + it.country }
            .sortedBy { it.name }
            .take(limit)
            .map {
                LocationSuggestion(
                    name = it.name,
                    country = it.country,
                    state = null,
                    lat = it.lat,
                    lon = it.lon
                )
            }
    }

    /* ---------------- Internal mappers ---------------- */

    private fun oneCallToNext6(res: OneCallResponse): HourlyBundle {
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

    private fun forecastToNext6(res: ForecastResponse): HourlyBundle {
        val now = System.currentTimeMillis() / 1000L
        val nextHour = ((now / 3600L) + 1L) * 3600L
        val targets = (0 until 6).map { nextHour + it * 3600L }
        return buildFrom3hTargets(res, targets)
    }

    private fun oneCallToTomorrow6(res: OneCallResponse): HourlyBundle {
        val offset = res.timezoneOffset
        val nowUtc = System.currentTimeMillis() / 1000L
        val localNow = nowUtc + offset
        val localMidnightNext = ((localNow / 86400L) + 1L) * 86400L
        val targetsLocal = (0 until 6).map { localMidnightNext + it * 3600L }
        val targetsUtc = targetsLocal.map { it - offset }

        val hours = targetsUtc.map { t ->
            val nearest = res.hourly.minByOrNull { abs(it.dt - t) }!!
            HourSlot(
                epochSec = t,
                tempC = nearest.temp,
                code = nearest.weather.firstOrNull()?.id ?: 0
            )
        }
        return HourlyBundle(offsetSec = offset, hours = hours)
    }

    private fun forecastToTomorrow6(res: ForecastResponse): HourlyBundle {
        val offset = res.city.timezone
        val nowUtc = System.currentTimeMillis() / 1000L
        val localNow = nowUtc + offset
        val localMidnightNext = ((localNow / 86400L) + 1L) * 86400L
        val targetsLocal = (0 until 6).map { localMidnightNext + it * 3600L }
        val targetsUtc = targetsLocal.map { it - offset }

        return buildFrom3hTargets(res, targetsUtc)
    }

    private fun buildFrom3hTargets(res: ForecastResponse, targetsUtc: List<Long>): HourlyBundle {
        val sorted = res.list.sortedBy { it.dt }
        val hours = targetsUtc.map { t ->
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
        val offset = res.city.timezone
        return HourlyBundle(offsetSec = offset, hours = hours)
    }
}
