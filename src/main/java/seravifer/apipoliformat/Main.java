package seravifer.apipoliformat;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.util.Pair;
import seravifer.apipoliformat.controller.LoginController;
import seravifer.apipoliformat.controller.WindowController;
import seravifer.apipoliformat.model.ApiPoliformat;

import java.util.Optional;

/**
 * Clase principal del programa. Punto de entrada para la JVM.
 * */
public class Main extends Application{
    private static final Logger logger = (Logger) LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            ApiPoliformat http = null;
            do {
                LoginController loginDialog = new LoginController();
                Optional<Pair<String, String>> login = loginDialog.showAndWait();
                if(login.isPresent()) {
                    Pair<String, String> data = login.get();
                    if(data.getKey().equals("")) { System.exit(0); }
                    long time = System.currentTimeMillis();
                    http = new ApiPoliformat(data.getKey(), data.getValue());
                    logger.debug("Se ha tardado en conectarse al PoliformaT {} milisegundos", System.currentTimeMillis() - time);
                }
            } while ((http == null || http.getSubjects().isEmpty()) && ApiPoliformat.attemps < 5);

            WindowController windowController = new WindowController(http, primaryStage);
            windowController.show();
        } catch (Exception e) {
            logger.warn("Error inesperado", e);
        }
    }

    public static void main(String[] args) { launch(args); }
}
