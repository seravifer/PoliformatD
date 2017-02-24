package seravifer.apipoliformat.model

import tornadofx.*
import javax.json.JsonObject

/**
 * Created by David on 23/02/2017.
 */
class Subject : JsonModel {
    var name: String by singleAssign()
    var id: Int by singleAssign()

    override fun updateModel(json: JsonObject) {
        with(json) {
            name = string("name") ?: ""
            id = int("id") ?: 0
        }
    }

    override fun toJSON(json: JsonBuilder) {
        with(json) {
            add("name", name)
            add("id", id)
        }
    }

    override fun toString(): String = name

}