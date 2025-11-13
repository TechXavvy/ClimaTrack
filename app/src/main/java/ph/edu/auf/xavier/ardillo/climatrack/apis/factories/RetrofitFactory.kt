package ph.edu.auf.xavier.ardillo.climatrack.apis.factories

import ph.edu.auf.xavier.ardillo.climatrack.apis.interfaces.WeatherAPIService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitFactory {
    fun createWeatherAPIService(baseUrl: String) : WeatherAPIService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherAPIService::class.java)
    }
}