package seravifer.apipoliformat.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.util.Pair;
import seravifer.apipoliformat.utils.Reference;

import java.io.IOException;

/**
 * Login dialog controller. R is the result type.
 * Created by David on 17/02/2016.
 */
public class LoginController extends Dialog<Pair<String, String>>{

    @FXML
    private PasswordField txtDNI;
    @FXML
    private PasswordField txtPIN;

    public LoginController() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(Reference.VIEW_PATH + "Login.fxml"));
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
        setTitle("Login");
    }
}
