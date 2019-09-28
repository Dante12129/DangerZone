package backend

import io.javalin.Javalin

class Server {
    private val api = Javalin.create()

    fun run(args: Array<String>) {
        // Parse arguments
        var port = 8080
        if (args.isNotEmpty()) {
            port = args[0].toInt()
        }

        // Start server
        println("Starting server on port ${port}")
        api.start(port)

        api.get("/") { ctx -> ctx.result("You have ridden into the DangerZone.") }
    }
}