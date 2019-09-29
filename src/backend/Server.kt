package backend

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.apibuilder.CrudHandler
import io.javalin.http.Context

class Server {
    private val api = Javalin.create { config -> config.showJavalinBanner = false}
    private val zones = ZoneManager()

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
            get("/zones/total") { ctx -> ctx.result("{zones: ${zones.size}}") }
            crud("/zones/:zoneID", ZoneCrud())

            // Entities routes
            crud("/entities/:zoneID/:entityID", EntityCrud())

            // Test routes
            get("/test") { ctx -> ctx.result("${ctx.queryParam("p", "Nothing")}")}
        }

        // Test
        zones.createZone()
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

            var result = "{'zones': [ "
            for (i in 0 until zones.size) {
                result += zones.getZone(i)!!.toJson() + " "
            }
            result += "]}"

            ctx.result(result)
        }

        override fun getOne(ctx: Context, resourceId: String) {
            println("Processing request to get zone $resourceId")

            val zone = zones.getZone(resourceId.toInt())
            if (zone != null) {
                ctx.result(zone.toJson())
            } else {
                ctx.result("{error: 'No such zone exists'}")
                ctx.status(404)
            }
        }

        override fun update(ctx: Context, resourceId: String) {
            println("Processing request to update zone $resourceId")
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
                    result += zone.getEntity(i)!!.toJson() + " "
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

            val zone = zones.getZone(ctx.pathParam("zoneID").toInt())
            if (zone != null) {
                val entity = zone.getEntity(resourceId.toInt())
                if (entity != null) {
                    ctx.result(entity.toJson())
                } else {
                    ctx.result("{error: 'No such entity exists'}")
                    ctx.status(404)
                }
            } else {
                ctx.result("{error: 'No such zone exists'}")
                ctx.status(409)
            }
        }

        override fun update(ctx: Context, resourceId: String) {
            println("Processing request to update entity $resourceId in zone ${ctx.pathParam("zoneID")}")
        }

    }
}