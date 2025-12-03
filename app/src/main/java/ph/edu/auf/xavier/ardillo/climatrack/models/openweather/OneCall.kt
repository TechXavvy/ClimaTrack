package ph.edu.auf.xavier.ardillo.climatrack.models.openweather

import com.google.gson.annotations.SerializedName

data class OneCallResponse(
    @SerializedName("timezone_offset") val timezoneOffset: Int,
    @SerializedName("hourly") val hourly: List<HourlyEntry>
)

data class HourlyEntry(
    @SerializedName("dt") val dt: Long,         // epoch seconds (UTC)
    @SerializedName("temp") val temp: Double,   // Celsius if units=metric
    @SerializedName("weather") val weather: List<Weather>
)
