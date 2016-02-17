package seravifer.apipoliformat.utils;

import com.sun.istack.internal.Nullable;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.util.Pair;
import jdk.internal.dynalink.support.TypeUtilities;
import org.apache.commons.lang3.reflect.TypeUtils;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.net.URL;
import java.util.Optional;

/**
 * Libreria de utilidades.
 * Created by David on 17/02/2016.
 * Credits: http://code.makery.ch/blog/javafx-dialogs-official/
 */
public class Utils {

    @Nullable
    public static Optional<Pair<String, String>> customDialogLogin(URL fxmlLocationPane) throws IOException {
        FXMLLoader loader = new FXMLLoader(fxmlLocationPane);
        DialogPane pane = loader.load();

        Dialog<Pair<String, String>> ventana = new Dialog<>();
        ventana.setDialogPane(pane);
        /*ventana.setResultConverter(button -> {
            if(button.getButtonData().getTypeCode()) {

            }
        });*/
        return null;
    }
}
