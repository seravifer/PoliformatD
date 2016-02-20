package seravifer.apipoliformat.utils;

import seravifer.apipoliformat.Main;

import java.io.InputStream;
import java.net.URL;

/**
 * Class for final references.
 * Created by David on 18/02/2016.
 */
public class Reference {
    public static final String DEFAULT_CHOICE = "------";

    public static URL getResourceAsURL(String name) {
        return Main.class.getResource("view/" + name);
    }

    public static InputStream getResourceAsStream(String name) {
        return Main.class.getResourceAsStream("view/" + name);
    }
}
