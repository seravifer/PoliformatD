package seravifer.apipoliformat.utils;

import ch.qos.logback.classic.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * IO utilities for Gson library.
 * Created by David on 02/02/2016.
 */
public class GsonUtil {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(GsonUtil.class);

    /**
     * Método que deserializa un fichero json en un mapa de datos.
     * @param path El archivo donde se guarda el mapa a deserializar.
     * @param type El tipo del mapa a deserializar. Pasarle el tipo usando el TypeToken de Gson.
     * @return Devuelve el mapa deserializado.
     * */
    public static <K, V> Map<K, V> leerGson(File path, Type type) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.fromJson(new FileReader(path), type);
        } catch (FileNotFoundException e) {
            logger.warn("El archivo con la traduccion del nombre a URL no ha sido encontrado", e);
        }
        return new HashMap<>();
    }

    /**
     * Serializa un mapa de datos en un archivo json.
     * @param path Archivo donde se va a guardar el json.
     * @param list Mapa que va a ser serializado.
     * */
    public static <K, V> void writeGson(File path, Map<K, V> list) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Type collectionType = new TypeToken<Map<K, V>>(){}.getType();
        String json = gson.toJson(list, collectionType);
        json = json.replace("\\u0000", "");
        try {
            FileWriter file = new FileWriter(path);
            file.write(json);
            file.close();
        } catch (IOException e) {
            logger.warn("El archivo con la traducción del nombre a URL no ha sido encontrado", e);
        }
    }

    /**
     * Añadir un mapa a otro existente y si no existe crea uno nuevo.
     * @param path El archivo donde se va añadir.
     * @param list El mapa a añadir.
     * */
    public static <K, V> void appendGson(File path, Map<K, V> list) {
        if(!path.exists()) {
            writeGson(path, list);
        } else {
            Map<K, V> map = leerGson(path, new TypeToken<Map<K, V>>(){}.getType());
            map.putAll(list);
            writeGson(path, map);
        }
    }
}
