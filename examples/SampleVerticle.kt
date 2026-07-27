package demo

import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class BrokenOrderVerticle : AbstractVerticle() {
    private val workerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun start(startPromise: Promise<Void>) {
        vertx.createHttpServer()
            .requestHandler { request ->
                request.response().end("ok")
            }
            .listen(8080)

        vertx.eventBus().consumer<JsonObject>("orders.create") { message ->
            message.reply(JsonObject().put("status", "accepted"))
        }

        vertx.setPeriodic(1_000) {
            workerScope.launch {
                println("polling")
            }
        }
    }

    override fun stop(stopPromise: Promise<Void>) {
        println("stopping")
    }
}

class HealthyOrderVerticle : AbstractVerticle() {
    private val workerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun start(startPromise: Promise<Void>) {
        vertx.createHttpServer()
            .requestHandler { request ->
                request.response().end("ok")
            }
            .listen(8081)
            .onSuccess { startPromise.complete() }
            .onFailure { startPromise.fail(it) }

        vertx.eventBus().consumer<JsonObject>("orders.health") { message ->
            message.reply(JsonObject().put("status", "healthy"))
        }
    }

    override fun stop(stopPromise: Promise<Void>) {
        workerScope.cancel()
        stopPromise.complete()
    }
}

abstract class BaseManagedVerticle : AbstractVerticle()

class IndirectInventoryVerticle : BaseManagedVerticle() {
    override fun start(startPromise: Promise<Void>) {
        vertx.eventBus().request<JsonObject>("inventory.reserve", JsonObject().put("sku", "demo"))
    }
}

class BootstrapVerticle : AbstractVerticle() {
    override fun start() {
        vertx.deployVerticle(IndirectInventoryVerticle())
        vertx.deployVerticle("demo.HealthyOrderVerticle")
    }
}
