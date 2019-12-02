package ar.com.smartnet.entity

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject
import java.util.concurrent.atomic.AtomicInteger


@DataObject(generateConverter = true)
data class Todo (val id: Int, val title: String, val completed: Boolean, val order: Int, val url: String) {



    companion object {
        val acc = AtomicInteger(0) // counter

        fun getIncId(): Int {
            return acc.get()
        }

        fun setIncIdWith(n: Int) {
            acc.set(n)
        }
    }

    fun setIncId() {
        id = acc.incrementAndGet()
    }

    fun getIncId(): Int {
        return acc.get()
    }

    fun Todo(obj: JsonObject?) {
        TodoConverter.fromJson(obj, this)
    }

    fun getId(): Int {

    }

    fun merge(newTodo: Todo): Any? {

    }

    fun setUrl(s: String) {

    }



}