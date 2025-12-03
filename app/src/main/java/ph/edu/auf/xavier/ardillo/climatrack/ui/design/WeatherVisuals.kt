package ph.edu.auf.xavier.ardillo.climatrack.ui.design

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.LocalTime

/** Coarse categories from OpenWeather codes */
enum class WeatherCategory { SUNNY, CLOUDY, RAINY }
enum class DayPart { MORNING, AFTERNOON, EVENING }

data class WeatherTheme(
    val gradient: Brush,
    val topTextColor: Color,
    val bigTempColor: Color,
    val conditionIcon: ImageVector
)

/** Decide MORNING/AFTERNOON/EVENING by local time */
fun currentDayPart(now: LocalTime = LocalTime.now()): DayPart = when (now.hour) {
    in 5..11 -> DayPart.MORNING
    in 12..17 -> DayPart.AFTERNOON
    else -> DayPart.EVENING
}

/** Map OpenWeather condition code → our coarse category */
fun toCategory(weatherCode: Int?): WeatherCategory = when (weatherCode) {
    800 -> WeatherCategory.SUNNY // Clear
    in 801..804 -> WeatherCategory.CLOUDY // Clouds
    in 200..599 -> WeatherCategory.RAINY // Thunderstorm/Drizzle/Rain/Snow (treat as wet)
    else -> WeatherCategory.CLOUDY
}

/** Choose a nice gradient + icon per (category, daypart) */
fun themeFor(category: WeatherCategory, dayPart: DayPart): WeatherTheme {
    // colors (feel free to tweak to match your mock more closely)
    val skyBlue = Color(0xFF66C8FF)
    val skyLight = Color(0xFFBDE8FF)
    val dusk1 = Color(0xFF4158D0)
    val dusk2 = Color(0xFFC850C0)
    val night1 = Color(0xFF0F2027)
    val night2 = Color(0xFF203A43)
    val storm1 = Color(0xFF232526)
    val storm2 = Color(0xFF414345)
    val cloud1 = Color(0xFFD7E1EC)
    val cloud2 = Color(0xFFA6B4C9)
    val sun1 = Color(0xFFFFE29F)
    val sun2 = Color(0xFFFFA99F)

    return when (category) {
        WeatherCategory.SUNNY -> when (dayPart) {
            DayPart.MORNING -> WeatherTheme(
                gradient = Brush.verticalGradient(listOf(skyLight, skyBlue)),
                topTextColor = Color.White,
                bigTempColor = Color.White,
                conditionIcon = Icons.Default.WbSunny
            )
            DayPart.AFTERNOON -> WeatherTheme(
                gradient = Brush.verticalGradient(listOf(sun1, sun2)),
                topTextColor = Color.White,
                bigTempColor = Color.White,
                conditionIcon = Icons.Default.WbSunny
            )
            DayPart.EVENING -> WeatherTheme(
                gradient = Brush.verticalGradient(listOf(dusk1, dusk2)),
                topTextColor = Color.White,
                bigTempColor = Color.White,
                conditionIcon = Icons.Default.LightMode
            )
        }
        WeatherCategory.CLOUDY -> when (dayPart) {
            DayPart.MORNING -> WeatherTheme(
                gradient = Brush.verticalGradient(listOf(cloud1, cloud2)),
                topTextColor = Color(0xFF0E1726),
                bigTempColor = Color(0xFF0E1726),
                conditionIcon = Icons.Default.Cloud
            )
            DayPart.AFTERNOON -> WeatherTheme(
                gradient = Brush.verticalGradient(listOf(skyLight, cloud2)),
                topTextColor = Color.White,
                bigTempColor = Color.White,
                conditionIcon = Icons.Default.Cloud
            )
            DayPart.EVENING -> WeatherTheme(
                gradient = Brush.verticalGradient(listOf(night1, night2)),
                topTextColor = Color.White,
                bigTempColor = Color.White,
                conditionIcon = Icons.Default.Cloud
            )
        }
        WeatherCategory.RAINY -> when (dayPart) {
            DayPart.MORNING -> WeatherTheme(
                gradient = Brush.verticalGradient(listOf(storm2, storm1)),
                topTextColor = Color.White,
                bigTempColor = Color.White,
                conditionIcon = Icons.Default.Thunderstorm
            )
            DayPart.AFTERNOON -> WeatherTheme(
                gradient = Brush.verticalGradient(listOf(storm1, night2)),
                topTextColor = Color.White,
                bigTempColor = Color.White,
                conditionIcon = Icons.Default.Thunderstorm
            )
            DayPart.EVENING -> WeatherTheme(
                gradient = Brush.verticalGradient(listOf(night1, Color.Black)),
                topTextColor = Color.White,
                bigTempColor = Color.White,
                conditionIcon = Icons.Default.Thunderstorm
            )
        }
    }
}
