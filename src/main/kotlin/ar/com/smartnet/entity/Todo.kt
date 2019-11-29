package ar.com.smartnet.entity

import io.vertx.core.json.JsonObject

data class Todo (val id: Int, val title: String, val completed: Boolean, val order: Int, val url: String) {

}