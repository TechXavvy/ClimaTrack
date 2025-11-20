package ph.edu.auf.xavier.ardillo.climatrack.local

import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import ph.edu.auf.xavier.ardillo.climatrack.local.models.ChecklistItem

object ChecklistDao {
    suspend fun add(label: String) {
        val item = ChecklistItem().apply { this.label = label }
        LocalRealm.instance.write { copyToRealm(item, updatePolicy = UpdatePolicy.ALL) }
    }
    suspend fun toggle(id: String, done: Boolean) {
        LocalRealm.instance.write {
            val obj = query<ChecklistItem>("id == $0", id).first().find()
            if (obj != null) obj.isDone = done
        }
    }
    fun all(): List<ChecklistItem> =
        LocalRealm.instance.query<ChecklistItem>().find().sortedBy { it.label }
}
