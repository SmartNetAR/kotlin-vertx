package ar.com.smartnet.Verticles

import ar.com.smartnet.*
import ar.com.smartnet.entity.Todo
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.DecodeException
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.Router.router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.redis.RedisClient
import io.vertx.redis.RedisOptions
import java.util.stream.Collectors


class SingleApplicationVerticle : AbstractVerticle() {
    private var redis: RedisClient? = null
    private val HTTP_HOST = "0.0.0.0"
    private val REDIS_HOST = "127.0.0.1"
    private val HTTP_PORT = 8082
    private val REDIS_PORT = 6379
    

    private fun initData() {
        val config = RedisOptions()
            .setHost(config().getString("redis.host", REDIS_HOST))
            .setPort(config().getInteger("redis.port", REDIS_PORT))

        this.redis = RedisClient.create(vertx, config)

        this.redis!!.hset(REDIS_TODO_KEY, "24", Json.encodePrettily(
            Todo(1, "Something todo ...", false, 1, "todo/24"))
        ) { r ->
            if (r.succeeded()) {
                println("Saved OK!!")
            } else {
                println("Connection or Operation Failed ${r.cause()}")
            }
        }
    }

    override fun start(future: Future<Void?>?) {
        initData()

        val router = router(vertx)

        val allowHeaders = HashSet<String>()
        allowHeaders.add("x-requested-with")
        allowHeaders.add("Access-Control-Allow-Origin")
        allowHeaders.add("origin");
        allowHeaders.add("Content-Type");
        allowHeaders.add("accept");

        val allowMethods = HashSet<HttpMethod>()
        allowMethods.add(HttpMethod.GET);
        allowMethods.add(HttpMethod.POST);
        allowMethods.add(HttpMethod.DELETE);
        allowMethods.add(HttpMethod.PATCH);

        router.route().handler(CorsHandler.create("*")
            .allowedHeaders(allowHeaders)
            .allowedMethods(allowMethods)
        );
        router.route().handler(BodyHandler.create());

        // routes
        router.get(API_GET).handler(this::handleGetTodo);
        router.get(API_LIST_ALL).handler(this::handleGetAll);
        router.post(API_CREATE).handler(this::handleCreateTodo);
        router.patch(API_UPDATE).handler(this::handleUpdateTodo);
        router.delete(API_DELETE).handler(this::handleDeleteOne);
        router.delete(API_DELETE_ALL).handler(this::handleDeleteAll);





    }


    private fun handleGetTodo(context: RoutingContext) {
        val todoID = context.request().getParam("todoId")
        if (todoID == null) sendError(400, context.response()) else {
            redis!!.hget(
                REDIS_TODO_KEY, todoID
            ) { x: AsyncResult<String?> ->
                if (x.succeeded()) {
                    val result = x.result()
                    if (result == null) sendError(404, context.response()) else {
                        context.response()
                            .putHeader("content-type", "application/json")
                            .end(result)
                    }
                } else sendError(503, context.response())
            }
        }
    }

    private fun handleGetAll(context: RoutingContext) {
        redis!!.hvals(
            REDIS_TODO_KEY
        ) { res: AsyncResult<JsonArray> ->
            if (res.succeeded()) {
                val encoded = Json.encodePrettily(
                    res.result().stream()
                        .map<Any> { x: Any? ->
                            Todo(
                                x as String?
                            )
                        }
                        .collect(Collectors.toList())
                )
                context.response()
                    .putHeader("content-type", "application/json")
                    .end(encoded)
            } else sendError(503, context.response())
        }
    }

    private fun handleCreateTodo(context: RoutingContext) {
        try {
            val todo: Todo = wrapObject(Todo(context.bodyAsString), context)
            val encoded = Json.encodePrettily(todo)
            redis!!.hset(
                REDIS_TODO_KEY, java.lang.String.valueOf(todo.getId()),
                encoded
            ) { res: AsyncResult<Long?> ->
                if (res.succeeded()) context.response()
                    .setStatusCode(201)
                    .putHeader("content-type", "application/json")
                    .end(encoded) else sendError(503, context.response())
            }
        } catch (e: DecodeException) {
            sendError(400, context.response())
        }
    }

    private fun handleUpdateTodo(context: RoutingContext) {
        try {
            val todoID = context.request().getParam("todoId")
            val newTodo = Todo(context.bodyAsString)
            // handle error
            if (todoID == null || newTodo == null) {
                sendError(400, context.response())
                return
            }
            redis!!.hget(
                REDIS_TODO_KEY, todoID
            ) { x: AsyncResult<String?> ->
                if (x.succeeded()) {
                    val result = x.result()
                    if (result == null) sendError(404, context.response()) else {
                        val oldTodo = Todo(result)
                        val response = Json.encodePrettily(oldTodo.merge(newTodo))
                        redis!!.hset(
                            REDIS_TODO_KEY, todoID, response
                        ) { res: AsyncResult<Long?> ->
                            if (res.succeeded()) {
                                context.response()
                                    .putHeader("content-type", "application/json")
                                    .end(response)
                            }
                        }
                    }
                } else sendError(503, context.response())
            }
        } catch (e: DecodeException) {
            sendError(400, context.response())
        }
    }

    private fun handleDeleteOne(context: RoutingContext) {
        val todoID = context.request().getParam("todoId")
        redis!!.hdel(
            REDIS_TODO_KEY, todoID
        ) { res: AsyncResult<Long?> ->
            if (res.succeeded()) context.response().setStatusCode(
                204
            ).end() else sendError(503, context.response())
        }
    }

    private fun handleDeleteAll(context: RoutingContext) {
        redis!!.del(
            REDIS_TODO_KEY
        ) { res: AsyncResult<Long?> ->
            if (res.succeeded()) context.response().setStatusCode(
                204
            ).end() else sendError(503, context.response())
        }
    }

    private fun wrapObject(todo: Todo, context: RoutingContext): Todo? {
        val id = todo.getId()
        if (id > Todo.getIncId()) {
            Todo.setIncIdWith(id)
        } else todo.setIncId(id)
        todo.setUrl(context.request().absoluteURI() + "/" + todo.getId())
        return todo
    }

    private fun sendError(StatusCode: Int, response: HttpServerResponse) {
        response.setStatusCode(StatusCode).end()
    }

}