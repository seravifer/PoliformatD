package seravifer.apipoliformat.utils;

import seravifer.apipoliformat.Main;

import java.io.InputStream;
import java.net.URL;

/**
 * Class for final references.
 * Created by David on 18/02/2016.
 */
public class Reference {
    public static final String DEFAULT_CHOICE = "Elige asignatura";

    /**
     * Referencia a los archivos en seravifer.apipoliformat.view en formato URL.
     * @param name El nombre del fichero del que quiere la referencia.
     * @return Devuelve la referencia al archivo en formato URL.
     * */
    public static URL getResourceAsURL(String name) {
        return Main.class.getResource("view/" + name);
    }

    /**
     * Referencia a los archivos en seravifer.apipoliformat.view en formato InputStream.
     * @param name El nombre del fichero del que quiere la referencia.
     * @return Devuelve la referencia del archivo en formato InputStream.
     * */
    public static InputStream getResourceAsStream(String name) {
        return Main.class.getResourceAsStream("view/" + name);
    }
}