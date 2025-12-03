package ph.edu.auf.xavier.ardillo.climatrack.apis.interfaces

import ph.edu.auf.xavier.ardillo.climatrack.models.openweather.ForecastResponse
import ph.edu.auf.xavier.ardillo.climatrack.models.openweather.OneCallResponse
import ph.edu.auf.xavier.ardillo.climatrack.models.openweather.WeatherModel
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherAPIService {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("appid") appid: String,
        @Query("units") units: String
    ): WeatherModel

    @GET("data/3.0/onecall")
    suspend fun getOneCallHourly(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("exclude") exclude: String = "current,minutely,daily,alerts",
        @Query("units") units: String = "metric",
        @Query("appid") appid: String
    ): OneCallResponse

    @GET("data/2.5/forecast")
    suspend fun getFiveDay3Hour(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("units") units: String = "metric",
        @Query("appid") appid: String
    ): ForecastResponse

    @GET("data/2.5/forecast/hourly")
    suspend fun getHourly4Days(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("appid") appid: String,
        @Query("units") units: String = "metric",
        @Query("cnt") cnt: Int = 6 // we only need the next 6 hours
    ): ForecastResponse

}