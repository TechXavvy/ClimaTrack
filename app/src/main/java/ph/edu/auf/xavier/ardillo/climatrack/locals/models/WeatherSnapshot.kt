package ph.edu.auf.xavier.ardillo.climatrack.local.models

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID

class WeatherSnapshot : RealmObject {
    @PrimaryKey
    var id: String = UUID.randomUUID().toString()
    var locationId: String = ""
    var temp: Double = 0.0
    var feelsLike: Double = 0.0
    var humidity: Int = 0
    var pressure: Int = 0
    var windSpeed: Double = 0.0
    var conditionCode: Int = 0
    var updatedAt: Long = 0L
}
