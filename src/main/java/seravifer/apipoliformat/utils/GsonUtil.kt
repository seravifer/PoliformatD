package seravifer.apipoliformat.utils

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.typedToJson
import com.google.gson.GsonBuilder

import java.io.*

/**
 * IO utilities for Gson library.
 * Created by David on 02/02/2016.
 */
object GsonUtil {

    /**
     * Método que deserializa un fichero json en un mapa de datos.
     * @param path El archivo donde se guarda el mapa a deserializar.
     * *
     * @return Devuelve el mapa deserializado.
     * *
     */
    fun <K, V> leerGson(path: File): Map<K, V> =
            GsonBuilder().setPrettyPrinting().create()
                    .fromJson<Map<K, V>>(FileReader(path))

    /**
     * Serializa un mapa de datos en un archivo json.
     * @param path Archivo donde se va a guardar el json.
     * *
     * @param list Mapa que va a ser serializado.
     * *
     */
    fun <K, V> writeGson(path: File, list: Map<K, V>) {
        GsonBuilder().setPrettyPrinting().create().typedToJson(list).replace("\\u0000", "").run {
            FileWriter(path).use {
                it.write(this)
            }
        }
    }

    /**
     * Añadir un mapa a otro existente y si no existe crea uno nuevo.
     * @param path El archivo donde se va añadir.
     * *
     * @param list El mapa a añadir.
     * *
     */
    fun <K, V> appendGson(path: File, list: Map<K, V>) {
        if (!path.exists()) {
            writeGson(path, list)
        } else {
            writeGson(path, leerGson<K, V>(path) + list)
        }
    }
}
