package seravifer.apipoliformat.model;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.util.Pair;
import seravifer.apipoliformat.controller.WindowController;
import seravifer.apipoliformat.utils.Utils;

import java.io.File;
import java.util.Scanner;
import java.net.CookieHandler;
import java.net.CookieManager;

public class Main extends Application{
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        WindowController windowController = new WindowController(null, primaryStage);
    }
}
