package backend

class ZoneManager {
    private val zones = mutableMapOf<Int, Zone>()
    private var highestZone = 0

    val size: Int
        get() = zones.size

    fun createZone(): Zone {
        val zone = Zone(highestZone)
        zones[highestZone] = zone

        highestZone++
        return zone
    }

    fun removeZone(zoneID: Int) {
        zones.remove(zoneID)
    }

    fun getZone(zoneID: Int): Zone? {
        return zones[zoneID]
    }
}