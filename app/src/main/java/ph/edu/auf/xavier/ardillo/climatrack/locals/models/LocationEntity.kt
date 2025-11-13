package ph.edu.auf.xavier.ardillo.climatrack.local.models

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID

class LocationEntity : RealmObject {
    @PrimaryKey
    var id: String = UUID.randomUUID().toString()
    var name: String = ""
    var country: String = ""
    var lat: Double = 0.0
    var lon: Double = 0.0
    var isFavorite: Boolean = false
}
