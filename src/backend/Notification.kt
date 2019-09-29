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

class AlertNotification(override val cat: Notification.Category, val type: Type, val severity: Severity, val sender: Entity) :
    Notification {

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

class ZoneNotification(override val cat: Notification.Category, val id: Int) : Notification {
    override fun toJSON(): String {
        return "{\"cat\": \"${cat.name}\", \"zoneID\": $id}"
    }
}