package backend

class Zone(val id: Int, var nickname: String?) {
    private val entities = mutableMapOf<Int, Entity>()
    private var highestEntity = 0;

    val size: Int
        get() = entities.size

    fun createEntity(): Entity {
        val entity = Entity(highestEntity, this)
        entities[highestEntity] = entity

        highestEntity++
        return entity
    }

    fun removeEntity(entityID: Int) {
        entities.remove(entityID)
    }

    fun getEntity(entityID: Int): Entity? {
        return entities[entityID]
    }

    fun toJson(): String {
        var entitiesAsJson = ""
        entitiesAsJson = "[ "
        entities.forEach { _, v ->
            entitiesAsJson += v.toJson() + " "
        }
        entitiesAsJson += "]"
        return if (nickname == null) "{zoneID: $id, entities: $entitiesAsJson}" else "{zoneID: $id, entities: $entitiesAsJson}, nickname: $nickname"
    }
}