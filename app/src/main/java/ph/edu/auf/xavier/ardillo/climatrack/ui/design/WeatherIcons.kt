package ph.edu.auf.xavier.ardillo.climatrack.ui.design

import androidx.annotation.DrawableRes
import ph.edu.auf.xavier.ardillo.climatrack.R
@DrawableRes
fun iconFor(code: Int?, dayPart: DayPart): Int {
    if (code == null) return R.drawable.ic_cloudy
    return when {
        code == 800 && dayPart == DayPart.EVENING -> R.drawable.ic_clear_night
        code == 800 -> R.drawable.ic_sunny
        code in 801..804 && dayPart == DayPart.EVENING -> R.drawable.ic_cloudy_night
        code in 801..804 -> R.drawable.ic_cloudy
        code in 200..599 -> R.drawable.ic_rain // thunder/drizzle/rain/snow -> rainy glyph
        else -> R.drawable.ic_cloudy
    }
}
