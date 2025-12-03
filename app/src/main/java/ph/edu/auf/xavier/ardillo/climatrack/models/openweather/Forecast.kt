package ph.edu.auf.xavier.ardillo.climatrack.models.openweather

import com.google.gson.annotations.SerializedName

data class ForecastResponse(
    @SerializedName("city") val city: ForecastCity,
    @SerializedName("list") val list: List<ForecastItem>
)

data class ForecastCity(
    @SerializedName("timezone") val timezone: Int // seconds offset from UTC
)

data class ForecastItem(
    @SerializedName("dt") val dt: Long,                 // epoch seconds (UTC)
    @SerializedName("main") val main: ForecastMain,
    @SerializedName("weather") val weather: List<Weather>
)

data class ForecastMain(
    @SerializedName("temp") val temp: Double            // Celsius when units=metric
)
