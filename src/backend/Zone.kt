package backend

class Zone(val id: Int) {
    fun toJson(): String {
        return "{zoneID: $id}"
    }
}