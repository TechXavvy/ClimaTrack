package ph.edu.auf.xavier.ardillo.climatrack.models.domain

data class WeatherInfoModel(
    val locationName: String,
    val temperature: Double,
    val condition: String,
    val conditionDescription: String,
    val humidity: Int,
    val windSpeed: Double,
    val iconUrl: String,
    val updatedAt: Long,
    val disasterAlert: String?
)
