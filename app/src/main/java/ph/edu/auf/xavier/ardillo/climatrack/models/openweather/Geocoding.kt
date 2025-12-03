package ph.edu.auf.xavier.ardillo.climatrack.models.openweather

import com.google.gson.annotations.SerializedName

data class GeoPlace(
    @SerializedName("name") val name: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("country") val country: String,
    @SerializedName("state") val state: String?
)
