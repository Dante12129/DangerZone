package frontend

import io.javalin.Javalin
import io.javalin.http.staticfiles.Location

fun main(args: Array<String>) {
    val app = Javalin.create { config ->
        config.showJavalinBanner = false
        config.addStaticFiles("src/resources", Location.EXTERNAL)
    }

    // Parse arguments
    var port = 8081
    if (args.isNotEmpty()) {
        port = args[0].toInt()
    }

    // Start server
    println("Starting server on port $port")
    app.start(port)
    println("Server running...")
}