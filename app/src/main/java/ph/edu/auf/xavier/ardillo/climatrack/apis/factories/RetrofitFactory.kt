package ph.edu.auf.xavier.ardillo.climatrack.apis.factories

import ph.edu.auf.xavier.ardillo.climatrack.apis.interfaces.WeatherAPIService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitFactory {
    private const val DEFAULT_BASE_URL = "https://api.openweathermap.org/"
    fun createWeatherAPIService(baseUrl: String) : WeatherAPIService {
        return Retrofit.Builder()
            .baseUrl(DEFAULT_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherAPIService::class.java)
    }
}