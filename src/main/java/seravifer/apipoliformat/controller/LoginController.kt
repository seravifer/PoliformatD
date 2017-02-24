package seravifer.apipoliformat.controller

import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory

import seravifer.apipoliformat.model.ApiPoliformat
import tornadofx.*

/**
 * Login dialog controller.
 * Created by David on 17/02/2016.
 */
class LoginController : Controller() {

    fun login(dni: String, pin: String): Boolean = ApiPoliformat.login(dni, pin)

    companion object {
        private val logger = LoggerFactory.getLogger(LoginController::class.java) as Logger
    }
}
