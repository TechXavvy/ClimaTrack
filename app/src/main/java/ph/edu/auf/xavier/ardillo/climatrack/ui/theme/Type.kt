package ph.edu.auf.xavier.ardillo.climatrack.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ph.edu.auf.xavier.ardillo.climatrack.R

val GalanoAlt = FontFamily(
    Font(R.font.galano_classic_demo_alt_bold, FontWeight.Bold)
)

val AppTypography = Typography(
    displayLarge = TextStyle(fontFamily = GalanoAlt, fontWeight = FontWeight.Bold, fontSize = 48.sp),
    displayMedium = TextStyle(fontFamily = GalanoAlt, fontWeight = FontWeight.Bold, fontSize = 40.sp),
    displaySmall = TextStyle(fontFamily = GalanoAlt, fontWeight = FontWeight.Bold, fontSize = 32.sp),
    headlineLarge = TextStyle(fontFamily = GalanoAlt, fontWeight = FontWeight.Bold, fontSize = 28.sp),
    headlineMedium = TextStyle(fontFamily = GalanoAlt, fontWeight = FontWeight.Bold, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = GalanoAlt, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = GalanoAlt, fontWeight = FontWeight.Bold, fontSize = 18.sp),
    bodyLarge = TextStyle(fontFamily = GalanoAlt, fontWeight = FontWeight.Bold, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = GalanoAlt, fontWeight = FontWeight.Bold, fontSize = 14.sp),
)
