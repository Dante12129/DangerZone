package backend

class Notification(val cat: Category, val type: Type, val severity: Severity, val sender: Entity) {
    enum class Category {
        Alert,
        Move,
        NewZone
    }
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

    fun toJSON(): String {
        return "{\"cat\": \"${cat.name}\", \"zoneID\": ${sender.owningZone.id}, \"entityID\": ${sender.id}, \"type\": \"${type.name}\", \"severity\": \"${severity.name}\"}"
    }
}