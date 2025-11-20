package ph.edu.auf.xavier.ardillo.climatrack.local.models

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID

class ChecklistItem : RealmObject {
    @PrimaryKey
    var id: String = UUID.randomUUID().toString()
    var label: String = ""     // e.g., "Flashlight"
    var isDone: Boolean = false
}
