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
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import seravifer.apipoliformat.utils.Reference;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Set;

/**
 * Controller for Window.fxml
 * Created by David on 18/02/2016.
 */
public class WindowController {

    private Stage stage;
    private AnchorPane root;
    private ObservableList<String> choiceList;

    @FXML
    private ChoiceBox<String> box;

    @FXML
    private Button downloadBtn;

    @FXML
    private TextArea txtConsole;

    public WindowController(Set<String> keySet, Stage stage) {
        this.stage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource(Reference.VIEW_PATH + "Window.fxml"));
        loader.setController(this);
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.stage.setScene(new Scene(root));

        choiceList = FXCollections.observableArrayList(keySet);
    }

    @FXML
    void initialize() {
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                txtConsole.appendText(String.valueOf((char) b));
            }
        }));

        box.setItems(choiceList);
    }

    @FXML
    private void downloadHandler(ActionEvent event) {
        box.getSelectionModel().getSelectedItem();
    }

    public void show() {
        stage.show();
    }
}
