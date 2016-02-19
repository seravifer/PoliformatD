package seravifer.apipoliformat;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.util.Pair;
import seravifer.apipoliformat.controller.LoginController;
import seravifer.apipoliformat.controller.WindowController;
import seravifer.apipoliformat.model.ApiPoliformat;

import java.util.Optional;

public class Main extends Application{
    @Override
    public void start(Stage primaryStage) throws Exception {
        ApiPoliformat http = null;               // Inicia conexion
        do {
            LoginController loginDialog = new LoginController();
            Optional<Pair<String, String>> login = loginDialog.showAndWait();
            if(login.isPresent()) {
                Pair<String, String> data = login.get();
                http = new ApiPoliformat(data.getKey(), data.getValue());
            }
        } while (http != null && http.getAsignaturas().isEmpty() && ApiPoliformat.attemps < 5);

        WindowController windowController = new WindowController(http, primaryStage);
        windowController.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
