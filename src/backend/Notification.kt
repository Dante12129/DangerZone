package backend

class Notification(val type: Type, val severity: Severity, sender: Entity) {
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
}