package ph.edu.auf.xavier.ardillo.climatrack.local

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import ph.edu.auf.xavier.ardillo.climatrack.local.models.LocationEntity
import ph.edu.auf.xavier.ardillo.climatrack.local.models.WeatherSnapshot

object LocalRealm {
    private val config by lazy {
        RealmConfiguration.create(
            schema = setOf(
                LocationEntity::class,
                WeatherSnapshot::class,
                ph.edu.auf.xavier.ardillo.climatrack.local.models.ChecklistItem::class
            )
        )
    }
    val instance: Realm by lazy { Realm.open(config) }
}
