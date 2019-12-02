package ar.com.smartnet.entity

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject
import java.util.concurrent.atomic.AtomicInteger


@DataObject(generateConverter = true)
class Todo {

    private val acc = AtomicInteger(0) // counter


    private var id: Int? = null
    private var title: String? = null
    private var completed: Boolean? = null
    private var order: Int? = null
    private var url: String? = null

    constructor() {}

    constructor(other: Todo) {
        this.id = other.id
        this.title = other.title
        this.completed = other.completed
        this.order = other.order
        this.url = other.url
    }

    constructor(obj: JsonObject?) {
        // TodoConverter.fromJson(obj, this)
    }

    constructor(jsonStr: String?) {
        // TodoConverter.fromJson(JsonObject(jsonStr), this)
    }

    constructor(id: Int, title: String?, completed: Boolean?, order: Int?, url: String?) {
        this.id = id
        this.title = title
        this.completed = completed
        this.order = order
        this.url = url
    }

    fun toJson(): JsonObject? {
        val json = JsonObject()
        // TodoConverter.toJson(this, json)
        return json
    }

    fun getId(): Int {
        return id!!
    }

    fun setId(id: Int) {
        this.id = id
    }

    fun setIncId() {
        id = acc.incrementAndGet()
    }

    fun getIncId(): Int {
        return acc.get()
    }

    fun setIncIdWith(n: Int) {
        acc.set(n)
    }

    fun getTitle(): String? {
        return title
    }

    fun setTitle(title: String?) {
        this.title = title
    }

    fun isCompleted(): Boolean? {
        return getOrElse<Boolean?>(completed, false)
    }

    fun setCompleted(completed: Boolean?) {
        this.completed = completed
    }

    fun getOrder(): Int? {
        return getOrElse<Int?>(order, 0)
    }

    fun setOrder(order: Int) {
        this.order = order
    }

    fun getUrl(): String? {
        return url
    }

    fun setUrl(url: String) {
        this.url = url
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val todo = o as Todo
        if (id != todo.id) return false
        if (title != todo.title) return false
        if (if (completed != null) completed != todo.completed else todo.completed != null) return false
        return if (order != null) order == todo.order else todo.order == null
    }

    override fun hashCode(): Int {
        var result: Int = id!!
        result = 31 * result + title.hashCode()
        result = 31 * result + if (completed != null) completed.hashCode() else 0
        result = 31 * result + if (order != null) order.hashCode() else 0
        return result
    }

    override fun toString(): String {
        return "Todo -> {" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", completed=" + completed +
                ", order=" + order +
                ", url='" + url + '\'' +
                '}'
    }

    private fun <T> getOrElse(value: T?, defaultValue: T): T {
        return value ?: defaultValue
    }

    fun merge(todo: Todo): Todo? {
        return ar.com.smartnet.entity.Todo(
            id!!,
            getOrElse(todo.title, title),
            getOrElse(todo.completed, completed),
            getOrElse(todo.order, order),
            url
        )
    }
}