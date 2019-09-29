package backend

class Entity(val id: Int, var owningZone: Zone) {
    val fullId: String
        get() = ("$owningZone:$id")

    fun notify(sender: Entity, message: String) {
        println("${sender.toJson()} says $message")
    }

    fun toJson(): String {
        return "{id: $id, zoneID: ${owningZone.id}}"
    }
}