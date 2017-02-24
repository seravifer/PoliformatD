package seravifer.apipoliformat

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import seravifer.apipoliformat.view.Login

/**
 * Clase principal del programa. Punto de entrada para la JVM.
 */
class App : tornadofx.App(primaryView = Login::class) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java.`package`.name)

    init {
        Thread.setDefaultUncaughtExceptionHandler { t, e -> logger.error("UncaughtException in thread: ${t.name}", e) }
    }
}
