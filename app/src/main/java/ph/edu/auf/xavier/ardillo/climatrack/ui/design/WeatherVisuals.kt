package ph.edu.auf.xavier.ardillo.climatrack.ui.design

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import java.time.LocalTime
import ph.edu.auf.xavier.ardillo.climatrack.R

enum class WeatherCategory { SUNNY, CLOUDY, RAINY }
enum class DayPart { MORNING, AFTERNOON, EVENING }

data class WeatherArtTheme(
    @DrawableRes val backgroundRes: Int,
    val onBackgroundText: Color,
    val tempGradient: Brush
)

fun currentDayPart(now: LocalTime = LocalTime.now()): DayPart = when (now.hour) {
    in 5..11 -> DayPart.MORNING
    in 12..17 -> DayPart.AFTERNOON
    else -> DayPart.EVENING
}

fun toCategory(openWeatherCode: Int?): WeatherCategory = when (openWeatherCode) {
    800 -> WeatherCategory.SUNNY
    in 801..804 -> WeatherCategory.CLOUDY
    in 200..599 -> WeatherCategory.RAINY
    else -> WeatherCategory.CLOUDY
}

/** Build the theme (background image + gradient color mix) for the screen */
fun artFor(category: WeatherCategory, dayPart: DayPart): WeatherArtTheme {
    // Choose which drawable to use based on weather and time
    val bgRes = when (category) {
        WeatherCategory.RAINY -> R.drawable.bg_rainy // Use for both day and evening
        WeatherCategory.SUNNY -> {
            if (dayPart == DayPart.EVENING) R.drawable.bg_evening
            else R.drawable.bg_sunny_day
        }
        WeatherCategory.CLOUDY -> {
            if (dayPart == DayPart.EVENING) R.drawable.bg_evening
            else R.drawable.bg_cloudy_day
        }
    }

    // Define the base color from the background + white mix for gradient
    val (topColor, bottomColor) = when (category) {
        WeatherCategory.SUNNY -> {
            if (dayPart == DayPart.EVENING) {
                // Evening sunny: warm orange/yellow tones
                Pair(Color(0xFFFFD68F), Color.White)
            } else {
                // Day sunny: bright sky blue
                Pair(Color(0xFF87CEEB), Color.White)
            }
        }
        WeatherCategory.CLOUDY -> {
            if (dayPart == DayPart.EVENING) {
                // Evening cloudy: purple/blue tones
                Pair(Color(0xFFB8B8D4), Color.White)
            } else {
                // Day cloudy: lighter gray-blue
                Pair(Color(0xFFD0D8E0), Color.White)
            }
        }
        WeatherCategory.RAINY -> {
            // Rainy: darker blue-gray tones
            Pair(Color(0xFF6B8DA8), Color.White)
        }
    }

    // Create vertical gradient for temperature text
    val tempGradient = Brush.verticalGradient(
        colors = listOf(topColor, bottomColor)
    )

    return WeatherArtTheme(
        backgroundRes = bgRes,
        onBackgroundText = Color.White,
        tempGradient = tempGradient
    )
}