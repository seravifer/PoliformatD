package seravifer.apipoliformat.model

import com.google.gson.reflect.TypeToken
import seravifer.apipoliformat.utils.GsonUtil
import seravifer.apipoliformat.utils.Utils

import java.io.*
import java.net.CookieHandler
import java.net.CookieManager
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.util.HashMap
import java.util.stream.Collectors
import javax.net.ssl.HttpsURLConnection

import org.slf4j.LoggerFactory
import javafx.application.Platform
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty

import org.jsoup.Jsoup
import tornadofx.*

/**
 * Api para PoliformaT de la UPV.
 * Created by Sergi Avila on 12/02/2016.
 */
object ApiPoliformat {

    var attemps = 0

    private val logger = LoggerFactory.getLogger(ApiPoliformat::class.java)

    val subjects: MutableMap<String, String> = HashMap()
    val sizeProperty: DoubleProperty = SimpleDoubleProperty(0.0)
    val size by property(sizeProperty)

    init {
        // Inicializa las cookies
        CookieHandler.setDefault(CookieManager())
    }

    /**
     * TODO: Usar Jsoup íntegramente
     * Método que logea en la intranet
     * @param dni El dni del usuario
     * @param pin El pin/password del usuario
     * */
    fun login(dni: String, pin: String): Boolean {
        logger.info("Logeando...")
        logger.debug("dni: {} / pin: {}", dni, pin)
        if (dni.length == 8) attemps++
        val postParams = "&id=c&estilo=500&vista=MSE&cua=sakai&dni=$dni&clau=$pin&=Entrar"

        val link = URL("https://www.upv.es/exp/aute_intranet")

        val conn = link.openConnection() as HttpsURLConnection
        conn.doOutput = true
        conn.doInput = true

        val post = DataOutputStream(conn.outputStream)
        post.writeBytes(postParams)
        post.flush()
        post.close()

        val result = BufferedReader(InputStreamReader(conn.inputStream)).lines().collect(Collectors.joining("\n"))
        logger.debug(result)
        val logged = Jsoup.parse(result).getElementsByClass("upv_textoerror").first() == null
        if (logged) {
            logger.info("Logeo completado")
        } else {
            logger.info("Logeo fallido")
        }
        return logged
    }

    /**
     * Almacena en subjects todas las asignaturas en curso junto con su nº de referencia del PoliformaT
     */
    val asignaturas: Map<String, String>
        get() {
            logger.info("Extrayendo asignaturas...")

            val doc = Jsoup.connect("https://intranet.upv.es/pls/soalu/sic_asi.Lista_asig").get()

            val inputElements = doc.getElementsByClass("upv_enlace")

            for (inputElement in inputElements) {

                val oldName = inputElement.ownText()
                val nexName = oldName.substring(0, oldName.length - 2)
                val key = inputElement.getElementsByTag("span").text().substring(1, 6)

                subjects.put(nexName, key)
            }

            for ((key, value) in subjects) {
                logger.info(key + " - " + value)
            }

            logger.info("Extracción completada!")
            return subjects
        }

    /**
     * TODO: Pasar al controller
     * Método que recibe el nombre de una asignatura, la descarga en formato Zip desde el PoliformaT y la descomprime en la carpeta donde se ejecuta el programa.
     * @param n Nombre de la asignatura. PRECONDICION: Que esté como key en subjects.
     * *
     */
    fun download(n: String) {
        println("Descargando asignatura...")

        val key = subjects[n] // ValueKey - Referencia de la asignatura
        val path = System.getProperty("user.dir") + File.separator

        // Descargar zip
        val url = URL("https://poliformat.upv.es/sakai-content-tool/zipContent.zpc?collectionId=/group/GRA_" + key + "_" + Utils.getCurso() + "/&siteId=GRA_" + key + "_" + Utils.getCurso())
        logger.debug(url.toString())

        val input = url.openStream()
        val fos = FileOutputStream(File(n + ".zip"))

        Platform.runLater { sizeProperty.set(0.0) }

        var length: Int = 0
        var downloadedSize = 0
        val buffer = ByteArray(2048)
        while (input.read(buffer).apply { length = this } > -1) {
            fos.write(buffer, 0, length)
            downloadedSize += length
            val tmp = downloadedSize
            Platform.runLater { sizeProperty.set(tmp / (1024.0 * 1024.0)) }
        }
        val tmp = downloadedSize
        Platform.runLater { sizeProperty.set(tmp / (1024 * 1024.0)) }
        fos.close()
        input.close()
        println("El zip descargado pesa " + Utils.round(Files.size(Paths.get(path + n + ".zip")) / (1024 * 1024.0), 2) + " MB")

        println("Extrayendo asignatura...")

        // Extrae los archivos del zip
        logger.info("Comenzando la extracción. El zip pesa {} MB", Utils.round(Files.size(Paths.get(path + n + ".zip")) / (1024 * 1024.0), 2))
        val nameFolder = Utils.unZip(path + n + ".zip")
        val nameToAcronym = HashMap<String, String>()
        nameToAcronym.put(n, nameFolder)
        GsonUtil.appendGson(Paths.get(".namemap").toFile(), nameToAcronym)
        Utils.mkRightNameToURLMaps("https://poliformat.upv.es/access/content/group/GRA_" + key + "_" + Utils.getCurso(), nameFolder + File.separator)

        // Eliminar zip
        val file = File(path + n + ".zip")
        val deleted = file.delete()
        if (!deleted) {
            logger.error("EL ZIP NO HA SIDO BORRADO. SI NO ES BORRARO PUEDE ORIGINAR FALLOS EN FUTURAS DESCARGAS. DEBE BORRARLO MANUALMENTE")
            throw IOException("El zip de la asignatura no ha sido borrado")
        }

        println("Descarga completada")
        logger.info("Extracción con éxito")
    }

    /**
     * TODO: Pasar al controller
     * Método que descarga las diferencias entre la carpeta local de la asignatura y la carpeta del PoliformaT.
     * PRECONDICION: La asignatura ha debido descargarse antes.
     */
    fun update() {
        val nameToAcronymPath = Paths.get(".namemap")
        try {
            if (!Files.exists(nameToAcronymPath)) throw IOException("No existe el mapa de nombre-acronimo de las carpetas de asignatura.")
        } catch (e: IOException) {
            logger.warn("No se ha encontrado el mapa traductor nombre-acronimo en: " + nameToAcronymPath, e)
        }

        val map = GsonUtil.leerGson<String, String>(nameToAcronymPath.toFile(), object : TypeToken<Map<String, String>>() {}.type)
        for ((name, acronym) in map) {
            try {
                logger.info("Comienza la actualización de {}", name)
                println("Actualizando " + name)
                val updateList = Utils.compareLocalFolderTreeAndRemote(Paths.get(acronym), "https://poliformat.upv.es/access/content/group/GRA_" + subjects[name] + "_" + Utils.getCurso())
                for ((key, value) in updateList) {
                    val url = URL(key)
                    val downloadStream = url.openStream()
                    val filePath = Paths.get(Utils.flattenToAscii(value))
                    val file = FileOutputStream(File(filePath.toString()))

                    var length: Int = 0
                    val buffer = ByteArray(2048)
                    while (downloadStream.read(buffer).apply { length = this } > -1) {
                        file.write(buffer, 0, length)
                    }
                    downloadStream.close()
                    file.close()

                    val tmpMap = HashMap<String, String>()
                    tmpMap.put(filePath.fileName.toString(), key)
                    GsonUtil.appendGson(filePath.parent.resolve(".namemap").toFile(), tmpMap)

                    logger.info("Descargado {} desde {}", value, key)
                }
                logger.info("La actualizacion del {} ha acabado sin problemas", name)
                println(name + " actualizado")
            } catch (e: IOException) {
                logger.warn("No se ha podido actualizar la asignatura: " + name, e)
            }

        }
    }
}