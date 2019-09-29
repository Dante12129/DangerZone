package backend

interface Notification {
    val cat: Category

    enum class Category {
        Alert,
        Move,
        NewZone
    }

    fun toJSON(): String
}

class AlertNotification(val type: Type, val severity: Severity, val sender: Entity) :
    Notification {

    override val cat = Notification.Category.Alert

    enum class Type {
        Fall,
        HazardousExposure,
        Electrocution,
        Squeeze,
        Fire
    }

    enum class Severity {
        Self,
        Zone,
        All
    }

    override fun toJSON(): String {
        return "{\"cat\": \"${cat.name}\", \"zoneID\": ${sender.owningZone.id}, \"entityID\": ${sender.id}, \"type\": \"${type.name}\", \"severity\": \"${severity.name}\"}"
    }
}

class ZoneNotification(val id: Int) : Notification {
    override val cat = Notification.Category.NewZone

    override fun toJSON(): String {
        return "{\"cat\": \"${cat.name}\", \"zoneID\": $id}"
    }
}

class MoveNotification(val newZone: Int, val newEntity: Int) : Notification {
    override val cat = Notification.Category.Move

    override fun toJSON(): String {
        return "{\"cat\": \"${cat.name}\", \"newZone\": $newZone, \"newEntity\": $newEntity}"
    }
}