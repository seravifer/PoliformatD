package seravifer.apipoliformat.controller

import javafx.beans.property.SimpleIntegerProperty
import javafx.collections.ObservableList
import seravifer.apipoliformat.model.ApiPoliformat
import seravifer.apipoliformat.model.Subject
import tornadofx.*

/**
 * Created by David on 22/02/2017.
 */
class MainController : Controller() {

    val subjects: ObservableList<Subject> = mutableListOf<Subject>().observable()
    val downloadProgressProperty = SimpleIntegerProperty(0)

    fun loadView() {
        subjects.addAll(ApiPoliformat.asignaturas //Llamada de red
                .map { Subject().apply {
                    name = it.key
                    id = it.value.toInt()
                } }
        )
    }
}