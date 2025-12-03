package ph.edu.auf.xavier.ardillo.climatrack.models.openweather

import com.google.gson.annotations.SerializedName

// Unified models used by BOTH:
// - /data/2.5/forecast (3-hour steps)
// - pro /data/2.5/forecast/hourly (1-hour steps)
data class ForecastResponse(
    @SerializedName("city") val city: ForecastCity,
    @SerializedName("list") val list: List<ForecastItem>
)

data class ForecastCity(
    // seconds offset from UTC for the requested location
    @SerializedName("timezone") val timezone: Int
)

data class ForecastItem(
    @SerializedName("dt") val dt: Long,                 // epoch seconds (UTC)
    @SerializedName("main") val main: ForecastMain,
    @SerializedName("weather") val weather: List<Weather>
)

data class ForecastMain(
    @SerializedName("temp") val temp: Double            // Celsius when units=metric
)
