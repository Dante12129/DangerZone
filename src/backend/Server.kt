package backend

import io.javalin.Javalin

class Server {
    private val api = Javalin.create()

    fun run(args: Array<String>) {
        // Start server
        println("Starting server...")
        api.start(8080)

        api.get("/") { ctx -> ctx.result("You have ridden into the DangerZone.") }
    }
}