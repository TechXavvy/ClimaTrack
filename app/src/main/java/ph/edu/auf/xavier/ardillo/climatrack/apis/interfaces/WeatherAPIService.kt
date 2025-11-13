package ph.edu.auf.xavier.ardillo.climatrack.apis.interfaces

import ph.edu.auf.xavier.ardillo.climatrack.models.WeatherModel
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherAPIService {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("appid") appid: String,
        @Query("units") units: String
    ): WeatherModel
}