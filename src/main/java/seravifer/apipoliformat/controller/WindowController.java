package seravifer.apipoliformat.controller;

import seravifer.apipoliformat.model.ApiPoliformat;
import seravifer.apipoliformat.utils.Reference;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
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
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import javafx.util.converter.NumberStringConverter;
import seravifer.apipoliformat.utils.Utils;

import java.io.*;
import java.net.URL;

/**
 * Controller for Window.fxml
 * Created by David on 18/02/2016.
 */
public class WindowController {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(WindowController.class);

    private final Stage stage;
    private AnchorPane root;

    private Service<Void> downloadService;
    private Service<Void> updateService;
    private final ApiPoliformat api;

    @FXML
    private ChoiceBox<String> box;

    @FXML
    private Button downloadBtn;
    @FXML
    private Button updateBtn;

    @FXML
    private TextArea txtConsole;

    @FXML
    private Label lblDownloaded;

    @FXML
    private ImageView logo;

    public WindowController(ApiPoliformat api, Stage stage) {
        this.api = api;
        this.stage = stage;
        URL fxml = Reference.getResourceAsURL("Window.fxml");
        logger.info("Loading Window.fxml from {}", fxml.toString());
        FXMLLoader loader = new FXMLLoader(fxml);
        loader.setController(this);
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.stage.setScene(new Scene(root));
        this.stage.setTitle("PoliFormaT");
        this.stage.setResizable(false);

        ObservableList<String> choiceList = FXCollections.observableArrayList(api.getSubjects().keySet());
        choiceList.add(Reference.DEFAULT_CHOICE);
        box.setItems(choiceList);
        box.getSelectionModel().select(Reference.DEFAULT_CHOICE);

        Bindings.bindBidirectional(lblDownloaded.textProperty(), api.sizeProperty(), new NumberStringConverter() {
            @Override
            public String toString(Number value) {
                return Utils.round(value.doubleValue(), 2) + " MB";
            }
        });
    }

    @FXML
    void initialize() {
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                txtConsole.appendText(Utils.flattenToAscii(String.valueOf((char) b)));
            }
        }));

        logger.info("Loading logo.png");
        logo.setImage(new Image(Reference.getResourceAsStream("logo.png")));

        downloadService = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        try {
                            String selected = box.getSelectionModel().getSelectedItem();
                            if (!selected.equals(Reference.DEFAULT_CHOICE)) {
                                api.download(selected);
                            } else {
                                System.out.println("Selecciona una asignatura");
                            }
                        } catch (IOException e) {
                            logger.warn("Ha fallado el servicio de descarga en algún punto.", e);
                            Platform.runLater(() -> downloadBtn.setDisable(false));
                        }
                        Platform.runLater(() -> downloadBtn.setDisable(false));
                        return null;
                    }
                };
            }
        };

        updateService = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        try {
                            api.update();
                            Platform.runLater(() -> updateBtn.setDisable(false));
                        } catch (Exception e) {
                            logger.warn("El servicio de actualización de archivos ha fallado.", e);
                            updateBtn.setDisable(false);
                        }
                        return null;
                    }
                };
            }
        };
    }

    @FXML
    private void downloadHandler(ActionEvent event) {
        if (!downloadService.isRunning()) {
            downloadBtn.setDisable(true);
            downloadService.reset();
            downloadService.start();
        }
    }

    @FXML
    private void updateHandler(ActionEvent event) {
        if(!updateService.isRunning()) {
            updateBtn.setDisable(true);
            updateService.reset();
            updateService.start();
        }
    }

    public void show() {
        stage.show();
    }
}
