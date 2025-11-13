package ph.edu.auf.xavier.ardillo.climatrack.models

import com.google.gson.annotations.SerializedName

data class Clouds(
    @SerializedName("all")
    val all: Int
)
