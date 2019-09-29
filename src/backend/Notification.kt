package backend

class Notification(val type: Type, val severity: Severity, val sender: Entity) {
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
        return "{zoneID: ${sender.owningZone.id}, entityID: ${sender.id}, type: ${type.name}, severity: ${severity.name}}"
    }
}