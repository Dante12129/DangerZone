package backend

class Entity(val id: Int, var owningZone: Zone, var nickname: String? = null) {
    val fullId: String
        get() = ("$owningZone:$id")

    fun notify(sender: Entity, message: String) {
        println("${sender.toJson()} says $message to ${toJson()}")
    }

    fun toJson(): String {
        return if (nickname == null) "{id: $id, zoneID: ${owningZone.id}}" else "{id: $id, zoneID: ${owningZone.id}, nickname: $nickname}"
    }
}