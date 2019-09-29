package backend

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.apibuilder.CrudHandler
import io.javalin.http.BadRequestResponse
import io.javalin.http.ConflictResponse
import io.javalin.http.Context
import io.javalin.http.NotFoundResponse
import io.javalin.websocket.WsContext
import org.json.*
import java.util.concurrent.ConcurrentHashMap

class Server {
    private val api = Javalin.create { config ->
        config.showJavalinBanner = false
        config.enableCorsForAllOrigins()
    }
    private val zones = ZoneManager()
    private val connections = ConcurrentHashMap<Entity, WsContext>()

    fun run(args: Array<String>) {
        // Parse arguments
        var port = 8080
        if (args.isNotEmpty()) {
            port = args[0].toInt()
        }

        // Start server
        println("Starting server on port $port")
        api.start(port)
        println("Server running...")

        // Base routes
        api.get("/") { ctx -> ctx.result("You have ridden into the DangerZone.") }

        // Main routes
        api.routes {
            // Zones routes
            get("/zones/total") { ctx -> ctx.result("{\"zoneCount\": ${zones.size}}") }
            crud("/zones/:zoneID", ZoneCrud())

            // Entities routes
            crud("/entities/:zoneID/:entityID", EntityCrud())

            // Websocket communication
            ws("/sockets/:zoneID/:entityID") { socket ->
                socket.onConnect { ctx ->
                    val entity = getEntity(ctx.pathParam("zoneID").toInt(), ctx.pathParam("entityID").toInt())
                    connections[entity] = ctx

                    println("Incoming websocket connection from ${entity.toJson()}")
                }
                socket.onClose { ctx ->
                    val entity = getEntity(ctx.pathParam("zoneID").toInt(), ctx.pathParam("entityID").toInt())
                    connections.remove(entity)

                    println("Closed websocket connection from ${entity.toJson()}")
                }
                socket.onMessage { ctx ->
                    sendNotification(parseAlert(ctx.message()))
                }
            }

            // Test routes
            get("/test") { ctx -> ctx.result("${ctx.queryParam("p", "Nothing")}")}
        }

        // Test
        val zone1 = zones.createZone()
        zone1.nickname = "Default"
    }

    fun getZone(zoneID: Int): Zone {
        val zone = zones.getZone(zoneID)
        if (zone != null) {
            return zone
        } else {
            throw NotFoundResponse("No such zone exists")
        }
    }

    fun updateZone(zoneID: Int, data: String) {
        val json = JSONObject(data)
        val zone = getZone(zoneID)

        when (json.getString("type")) {
            "name" -> {
                val name = json.getString("name")
                zone.nickname = name
            }
            else -> throw BadRequestResponse("Unsupported zone update type")
        }
    }

    fun updateEntity(zoneID: Int, entityID: Int, data: String) {
        val json = JSONObject(data)
        val entity = getEntity(zoneID, entityID)

        when (json.getString("type")) {
            "name" -> {
                val name = json.getString("name")
                entity.nickname = name
            }
            else -> throw BadRequestResponse("Unsupported zone update type")
        }
    }

    fun getEntity(zoneID: Int, entityID: Int): Entity {
        val zone = try {
            getZone(zoneID)
        } catch (e: NotFoundResponse) {
            throw ConflictResponse("No such zone exists")
        }

        val entity = zone.getEntity(entityID)
        if (entity != null) {
            return entity
        } else {
            throw NotFoundResponse("No such entity exists")
        }
    }

    private fun parseAlert(data: String): Notification {
        val json = JSONObject(data)
        val zoneID = json.getInt("zoneID")
        val entityID = json.getInt("entityID")
        val type = json.getString("type")
        val severity = json.getString("severity")

        return AlertNotification(Notification.Category.Alert, AlertNotification.Type.valueOf(type), AlertNotification.Severity.valueOf(severity), getEntity(zoneID, entityID))
    }

    private fun sendNotification(notification: Notification) {
        if (notification as? AlertNotification != null) {
            when (notification.severity) {
                AlertNotification.Severity.All -> {
                    for (i in 0 until zones.size) {
                        for (j in 0 until getZone(i).size) {
                            connections[getEntity(i, j)]?.send(notification.toJSON())
                        }
                    }
                }
                AlertNotification.Severity.Self -> {
                    connections[notification.sender]?.send(notification.toJSON())
                }
                AlertNotification.Severity.Zone -> {
                    val zone = getZone(notification.sender.owningZone.id)
                    for (i in 0 until zone.size) {
                        connections[getEntity(zone.id, i)]?.send(notification.toJSON())
                    }
                }
            }
        } else {
            for (i in 0 until zones.size) {
                for (j in 0 until getZone(i).size) {
                    connections[getEntity(i, j)]?.send(notification.toJSON())
                }
            }
        }
    }

    inner class ZoneCrud: CrudHandler {
        override fun create(ctx: Context) {
            println("Processing request to create a zone")

            val zone = this@Server.zones.createZone()
            ctx.status(201)
            ctx.result(zone.toJson())
        }

        override fun delete(ctx: Context, resourceId: String) {
            println("Processing request to delete zone $resourceId")

            zones.removeZone(resourceId.toInt())
        }

        override fun getAll(ctx: Context) {
            println("Processing request to get all zones")

            var result = "{\"zones\": [ "
            for (i in 0 until zones.size) {
                result += getZone(i).toJson() + " "
            }
            result += "]}"

            ctx.result(result)
        }

        override fun getOne(ctx: Context, resourceId: String) {
            println("Processing request to get zone $resourceId")

            ctx.result(getZone(resourceId.toInt()).toJson())
        }

        override fun update(ctx: Context, resourceId: String) {
            println("Processing request to update zone $resourceId")

            updateZone(resourceId.toInt(), ctx.body())
            ctx.result("{success: Zone updated")
        }

    }

    inner class EntityCrud : CrudHandler {
        override fun create(ctx: Context) {
            println("Processing request to create an entity in zone ${ctx.pathParam("zoneID")}")

            val zone = zones.getZone(ctx.pathParam("zoneID").toInt())
            if (zone != null) {
                val entity = zone.createEntity()
                ctx.result(entity.toJson())
                ctx.status(201)
            } else {
                ctx.result("{error: 'No such zone exists'}")
                ctx.status(409)
            }
        }

        override fun delete(ctx: Context, resourceId: String) {
            println("Processing request to delete entity $resourceId in zone ${ctx.pathParam("zoneID")}")

            val zone = zones.getZone(ctx.pathParam("zoneID").toInt())
            if (zone != null) {
                zone.removeEntity(resourceId.toInt())
            } else {
                ctx.result("{error: 'No such zone exists'}")
                ctx.status(409)
            }
        }

        override fun getAll(ctx: Context) {
            println("Processing request to get all entities in zone ${ctx.pathParam("zoneID")}")

            val zone = zones.getZone(ctx.pathParam("zoneID").toInt())
            if (zone != null) {
                var result = "{entities: [ "
                for (i in 0 until zone.size) {
                    result += getEntity(ctx.pathParam("zoneID").toInt(), i).toJson() + " "
                }
                result += "]}"
                ctx.result(result)
            } else {
                ctx.result("{error: 'No such zone exists'}")
                ctx.status(409)
            }
        }

        override fun getOne(ctx: Context, resourceId: String) {
            println("Processing request to get entity $resourceId in zone ${ctx.pathParam("zoneID")}")

            ctx.result(getEntity(ctx.pathParam("zoneID").toInt(), resourceId.toInt()).toJson())
        }

        override fun update(ctx: Context, resourceId: String) {
            println("Processing request to update entity $resourceId in zone ${ctx.pathParam("zoneID")}")

            updateEntity(ctx.pathParam("zoneID").toInt(), resourceId.toInt(), ctx.body())
            ctx.result("{success: Entity updated")
        }

    }
}