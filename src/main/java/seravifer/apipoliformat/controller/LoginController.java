package seravifer.apipoliformat.controller;

import seravifer.apipoliformat.utils.Reference;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;

import javafx.util.Pair;
import java.io.IOException;
import java.net.URL;

/**
 * Login dialog controller.
 * Created by David on 17/02/2016.
 */
public class LoginController extends Dialog<Pair<String, String>> {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(LoginController.class);

    @FXML
    private PasswordField txtDNI;
    @FXML
    private PasswordField txtPIN;

    public LoginController() {
        URL fxml = Reference.getResourceAsURL("Login.fxml");
        logger.info("Loading Login.fxml from {}", fxml.toString());
        FXMLLoader loader = new FXMLLoader(fxml);
        loader.setController(this);
        DialogPane pane = new DialogPane();
        try {
            pane = loader.load();
        } catch (IOException e) {
            System.err.println("Falló el constructor del Login.");
            e.printStackTrace();
        }
        setDialogPane(pane);
        ButtonType loginType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().addAll(loginType, ButtonType.CANCEL);

        setResultConverter(type -> {
            if(type == loginType) {
                return new Pair<>(txtDNI.getText(), txtPIN.getText());
            } else if(type == ButtonType.CANCEL) {
                return new Pair<>("", "");
            }
            return null;
        });
    }

    @FXML
    void initialize() {
        setTitle("UPV");
    }
}
