package ar.com.smartnet

import ar.com.smartnet.entity.Todo
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.ext.web.Router

fun main(args: Array<String>) {
    println("Hello, World")

    val vertx = Vertx.vertx()
    val httpServer = vertx.createHttpServer()

    val router = Router.router(vertx)

    router.get("/")
        .handler { routingContext ->
            val response = routingContext.response()
            response.putHeader("content-type", "text/plain")
                .setChunked(true)
                .write("hi\n")
                .end("Ended")
        }

    router.get("/json/:name")
        .handler { routingContext ->
            val request = routingContext.request()
            val name = request.getParam("name")
            val response = routingContext.response()
            response.putHeader("content-type", "application/json")
                .setChunked(true)
                .write(Json.encodePrettily(ResponseObj(name)))
                .end()
        }

    router.get("/todo")
        .handler { routingContext ->
            val response = routingContext.response()
            val todo = Todo(1,"titulo", true, 1, "1/titulo")
            response.putHeader("content-type", "application/json")
                .setChunked(true)
                .write(Json.encodePrettily(todo))
                .end()
        }
    httpServer
        .requestHandler(router::accept)
        .listen(8080)

}


data class ResponseObj(var name: String = "")