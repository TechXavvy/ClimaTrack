package ph.edu.auf.xavier.ardillo.climatrack.local

import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import ph.edu.auf.xavier.ardillo.climatrack.local.models.LocationEntity
import ph.edu.auf.xavier.ardillo.climatrack.local.models.WeatherSnapshot

object WeatherDao {

    suspend fun upsertLocation(loc: LocationEntity) {
        LocalRealm.instance.write {
            copyToRealm(loc, updatePolicy = UpdatePolicy.ALL)
        }
    }

    suspend fun saveSnapshot(s: WeatherSnapshot) {
        LocalRealm.instance.write {
            copyToRealm(s, updatePolicy = UpdatePolicy.ALL)
        }
    }

    fun latestSnapshot(locationId: String): WeatherSnapshot? {
        val r = LocalRealm.instance
        return r.query<WeatherSnapshot>("locationId == $0", locationId)
            .find()
            .maxByOrNull { it.updatedAt }
    }
}
