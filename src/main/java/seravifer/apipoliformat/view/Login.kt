package seravifer.apipoliformat.view

import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.util.Duration
import javafx.concurrent.Task
import seravifer.apipoliformat.controller.LoginController
import tornadofx.*

/**
 * Created by David on 24/02/2017.
 */
class Login : View("Login") {
    val controller: LoginController by inject()

    val dniProperty = SimpleStringProperty()
    var dni by dniProperty

    val pinProperty = SimpleStringProperty()
    var pin by pinProperty

    val errorProperty = SimpleStringProperty()
    var error by errorProperty

    lateinit var loginTask: Task<Boolean>


    override val root = form {
        fieldset {
            field {
                passwordfield {
                    promptText = "DNI"
                    bind(dniProperty)
                }
            }
            field {
                passwordfield {
                    promptText = "PIN"
                    bind(pinProperty)
                }
            }
        }
        button("Login") {
            setOnMouseClicked {
                isDisable = true
                error = ""
                loginTask = runAsync {
                    controller.login(dni, pin)
                }
                replaceWith(Main::class, ViewTransition.Slide(Duration.seconds(0.3), ViewTransition.Direction.LEFT))
            }
            isDefaultButton = true
            alignment = Pos.BOTTOM_RIGHT
        }
        alignment = Pos.TOP_RIGHT
        paddingAll = 10
        prefHeight = 240.0
        prefWidth = 320.0
        usePrefSize = true
    }

    init {
        Platform.runLater { root.requestFocus() }
    }
}
