package seravifer.apipoliformat.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
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
import net.lingala.zip4j.exception.ZipException;
import seravifer.apipoliformat.model.ApiPoliformat;
import seravifer.apipoliformat.utils.Reference;

import java.io.*;

/**
 * Controller for Window.fxml
 * Created by David on 18/02/2016.
 */
public class WindowController {

    private final Stage stage;
    private AnchorPane root;

    private Service<Void> downloadService;
    private final ApiPoliformat api;

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

        this.stage.setTitle("PoliFormaT");

        this.stage.setResizable(false);

        ObservableList<String> choiceList = FXCollections.observableArrayList(api.getSubjects().keySet());
        choiceList.add(Reference.DEFAULT_CHOICE);
        box.setItems(choiceList);
        box.getSelectionModel().select(Reference.DEFAULT_CHOICE);
    }

    @FXML
    void initialize() {
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                txtConsole.appendText(String.valueOf((char) b));
            }
        }));

        logo.setImage(new Image(getClass().getResourceAsStream(Reference.VIEW_PATH + "logo.png")));

        downloadService = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        Platform.runLater(() -> downloadBtn.setDisable(true));
                        try {
                            String selected = box.getSelectionModel().getSelectedItem();
                            if (!selected.equals(Reference.DEFAULT_CHOICE)) {
                                api.download(selected);
                            } else {
                                System.out.println("Selecciona una asignatura");
                            }
                        } catch (IOException | ZipException e) {
                            System.out.println("Algo ha fallado");
                            Platform.runLater(() -> downloadBtn.setDisable(false));
                            e.printStackTrace();
                        }
                        Platform.runLater(() -> downloadBtn.setDisable(false));
                        return null;
                    }
                };
            }
        };
    }

    @FXML
    private void downloadHandler(ActionEvent event) {
        try {
            downloadService.reset();
            downloadService.start();
        } catch (Exception e) {
            System.err.println("Error descargando los archivos");
            e.printStackTrace();
        }
    }

    public void show() {
        stage.show();
    }
}
