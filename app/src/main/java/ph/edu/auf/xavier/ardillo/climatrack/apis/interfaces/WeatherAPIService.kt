package ph.edu.auf.xavier.ardillo.climatrack.apis.interfaces

import ph.edu.auf.xavier.ardillo.climatrack.models.openweather.*
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherAPIService {

    // --- Current weather ---
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("appid") appid: String,
        @Query("units") units: String
    ): WeatherModel

    // --- One Call 3.0 (hourly) ---
    @GET("data/3.0/onecall")
    suspend fun getOneCallHourly(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("exclude") exclude: String = "current,minutely,daily,alerts",
        @Query("units") units: String = "metric",
        @Query("appid") appid: String
    ): OneCallResponse

    // --- 5-day / 3-hour (fallback) ---
    @GET("data/2.5/forecast")
    suspend fun getFiveDay3Hour(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("units") units: String = "metric",
        @Query("appid") appid: String
    ): ForecastResponse

    // --- PRO: Hourly forecast 4 days (1h resolution) ---
    @GET("data/2.5/forecast/hourly")
    suspend fun getHourly4Days(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("appid") appid: String,
        @Query("units") units: String = "metric",
        @Query("cnt") cnt: Int = 6
    ): ForecastResponse

    // --- Geocoding: Direct search for places (type-ahead) ---
    @GET("geo/1.0/direct")
    suspend fun geocodeDirect(
        @Query("q") query: String,
        @Query("limit") limit: Int = 5,
        @Query("appid") appid: String
    ): List<GeoPlace>
}
