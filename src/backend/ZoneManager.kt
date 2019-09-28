package backend

class ZoneManager {
    private val zones = mutableMapOf<Int, Zone>()
    private var highestZone = 0

    fun createZone(): Int {
        zones[highestZone] = Zone()

        return highestZone++
    }

    fun removeZone(zoneID: Int) {
        zones.remove(zoneID)
    }

    fun size(): Int {
        return zones.size
    }
}