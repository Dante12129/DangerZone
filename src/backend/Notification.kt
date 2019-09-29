package backend

interface Notification {
    val cat: Category
    val sender: Entity

    enum class Category {
        Alert,
        Move,
        NewZone
    }

    fun toJSON(): String
}

class AlertNotification(override val cat: Notification.Category, val type: Type, val severity: Severity, override val sender: Entity) :
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

class ZoneNotification