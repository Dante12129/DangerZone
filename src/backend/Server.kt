package backend

import io.javalin.Javalin

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

        // Test
        println(zones.createZone())
        println(zones.createZone())
        println(zones.size())
        zones.removeZone(0)
        println(zones.size())
        println(zones.createZone())
        println(zones.size())
    }

    private fun addZone() {
        println("Adding a zone")
    }

    private fun removeZone() {
        println("Removing a zone")
    }
}