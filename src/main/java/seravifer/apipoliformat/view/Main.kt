package seravifer.apipoliformat.view

import javafx.geometry.Pos
import javafx.scene.control.ChoiceBox
import javafx.scene.image.Image
import javafx.scene.layout.Priority
import javafx.util.Duration
import seravifer.apipoliformat.controller.MainController
import seravifer.apipoliformat.model.Subject
import seravifer.apipoliformat.utils.Utils
import tornadofx.*
import java.io.OutputStream
import java.io.PrintStream

/**
 * Created by David on 22/02/2017.
 */
class Main : View("PoliformaT") {
    val controller: MainController by inject()

    lateinit var choicebox: ChoiceBox<Subject>
    override val root = vbox {
        paddingAll = 10
        spacing = 10.0
        alignment = Pos.CENTER
        imageview {
            fitWidth = 230.0
            fitHeight = 55.0
            image = Image(resources["logo.png"])
        }
        hbox {
            choicebox(controller.subjects) {
                useMaxWidth = true
                hgrow = Priority.ALWAYS
                choicebox = this
            }
            button("Descargar") {

            }
            button("Actualizar") {

            }
            spacing = 5.0
        }
        label {
            useMaxWidth = true
            alignment = Pos.CENTER_RIGHT
            bind(controller.downloadProgressProperty, readonly = true)
        }
        textarea {
            vgrow = Priority.ALWAYS
            useMaxSize = true
            isEditable = false
            System.setOut(PrintStream(object : OutputStream() {
                override fun write(b: Int) = appendText(Utils.flattenToAscii((b.toChar().toString())))
            }))
        }
        prefHeight = 240.0
        prefWidth = 320.0
        usePrefSize = true
    }

    override fun onDock() {
        super.onDock()
        root.requestFocus()
        find(Login::class).loginTask.ui { logged ->
            if (logged) {
                runAsync {
                    controller.loadView()
                } ui {
                    choicebox.selectionModel.select(0)
                }
            } else {
                replaceWith(Login::class, ViewTransition.Slide(Duration.seconds(0.3), ViewTransition.Direction.RIGHT))
            }
        }
    }
}
