package backend

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.apibuilder.CrudHandler
import io.javalin.http.Context

class Server {
    private val api = Javalin.create()
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

        // Base routes
        api.get("/") { ctx -> ctx.result("You have ridden into the DangerZone.") }

        // Node routes
        api.routes {
            get("/zones/total") { ctx -> ctx.result("{zones: ${zones.size}}") }
            crud("/zones/:zoneID", ZoneCrud())
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
}