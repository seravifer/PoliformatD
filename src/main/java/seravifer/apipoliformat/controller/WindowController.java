package seravifer.apipoliformat.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import seravifer.apipoliformat.model.ApiPoliformat;
import seravifer.apipoliformat.utils.Reference;

import java.io.*;
import java.nio.file.Paths;

/**
 * Controller for Window.fxml
 * Created by David on 18/02/2016.
 */
public class WindowController {

    private Stage stage;
    private AnchorPane root;
    private ObservableList<String> choiceList;

    private ApiPoliformat api;

    @FXML
    private ChoiceBox<String> box;

    @FXML
    private Button downloadBtn;

    @FXML
    private TextArea txtConsole;

    @FXML
    private ImageView logo;

    public WindowController(ApiPoliformat api, Stage stage) {
        this.stage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource(Reference.VIEW_PATH + "Window.fxml"));
        loader.setController(this);
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.stage.setScene(new Scene(root));

        this.api = api;

        choiceList = FXCollections.observableArrayList(api.getAsignaturas().keySet());
    }

    @FXML
    void initialize() {
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                txtConsole.appendText(String.valueOf((char) b));
            }
        }));

        //logo.setImage();
        box.setItems(choiceList);
    }

    @FXML
    private void downloadHandler(ActionEvent event) {
        try {
            api.download(box.getSelectionModel().getSelectedItem());
        } catch (Exception e) {
            System.err.println("Error descargando los archivos.");
            e.printStackTrace();
        }
    }

    public void show() {
        stage.show();
    }
}
