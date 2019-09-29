package backend

class Zone(val id: Int, var nickname: String? = null) {
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
        var entitiesAsJson = "[ "
        entities.forEach { _, v ->
            entitiesAsJson += v.toJson() + ", "
        }
        if (entities.isNotEmpty()) entitiesAsJson = entitiesAsJson.dropLast(2)
        entitiesAsJson += " ]"
        return if (nickname == null) "{\"zoneID\": $id, \"entities\": $entitiesAsJson}" else "{\"zoneID\": $id, \"nickname\": \"$nickname\", \"entities\": $entitiesAsJson}"
    }
}