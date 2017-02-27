package seravifer.apipoliformat.utils

import ch.qos.logback.classic.Logger
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.slf4j.LoggerFactory

import java.io.*
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.text.Normalizer
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.collections.HashMap

/**
 * Libreria de utilidades.
 * Created by David on 17/02/2016.
 */
object Utils {
    private val logger = LoggerFactory.getLogger(Utils::class.java) as Logger

    /**
     * Extrae los archivos de un fichero Zip.
     * @param zipFile Representacion de la ruta del archivo Zip en formato String.
     * *
     * @return Devuelve el nombre de la carpeta donde ha sido extraído.
     * *
     */
    fun unZip(zipFile: String): String {
        val buffer = ByteArray(1024)

        try {

            val zip = ZipInputStream(FileInputStream(zipFile))

            var zipContent: ZipEntry? = zip.nextEntry

            val nameFolder = zipContent!!.toString().substring(0, zipContent.toString().indexOf("/")).toUpperCase()

            val folder = File(System.getProperty("user.dir") + File.separator + nameFolder)

            if (!folder.exists()) folder.mkdir()

            while (zipContent != null) {

                val fileName = zipContent.name.replace("|", "-").replace("|", "").replace(" /", "/").replace(":", "").replace("\"", "")
                val newFile = File(fileName)
                logger.info("Extrayendo {}", newFile.absoluteFile)

                File(newFile.parent).mkdirs()
                val fos = FileOutputStream(newFile)

                var lenght: Int = 0
                while (zip.read(buffer).apply { lenght = this} > 0) {
                    fos.write(buffer, 0, lenght)
                }

                fos.close()
                zipContent = zip.nextEntry

            }

            zip.closeEntry()
            zip.close()

            return nameFolder
        } catch (e: IOException) {
            logger.warn("Ha fallado la descompresion de los archivos", e)
        }

        return ""
    }

    /**
     * Es llamado cuando se actualiza. Agarra los links de todos los archivos de una asignatura y su path teórico en el sistema. No coge los links de carpetas.
     * @param url Es la URL de la asignatura como String. Por ejemplo: https://poliformat.upv.es/access/content/group/GRA_11546_2015/
     * *
     * @return Devuelve un mapa con las URL de los archivos en el PoliformaT y sus nombres reales.
     */
    fun getFilesURL(url: String, parent: String): Map<String, String> {
        return try {
            val doc = Jsoup.connect(url).get()
            val input = doc.getElementsByClass("folder")
            input.addAll(doc.getElementsByClass("file"))

            input.fold(HashMap<String, String>()) { acc, it ->
                val a = it.child(0)
                val link = a.absUrl("href")
                if (it.className() == "folder") {
                    acc.apply {
                        putAll(getFilesURL(link, parent + flattenToAscii(a.text()) + "/"))
                    }
                } else {
                    val extension = link.substring(link.lastIndexOf('.') + 1)
                    acc.apply {
                        put(link, (parent + flattenToAscii(if (a.text().contains(extension)) a.text() else a.text() + "." + extension)))
                    }
                }
            }.toMap()
        } catch (e: IOException) {
            if (e is UnsupportedEncodingException) {
                logger.warn("Codificacion no soportada", e)
            } else {
                logger.warn("No ha sido posible recuperar la lista de archivos del servidor", e)
            }
            mapOf<String, String>()
        }
    }

    /**
     * Crea los mapas json que relacionan los nombres de las carpetas con su URL. Se usa en la comprobacion de archivos en la actualización.
     * @param remoteURL La URL en formato String de la carpeta de la asignatura. Por ejemplo: https://poliformat.upv.es/access/content/group/GRA_11546_2015/
     * *
     * @param localParent En la primera llamada al método debe valer la carpeta donde está descargada la asignatura. Ejemplos: AMA, FOE, TCO, FCO...
     * *
     */
    fun mkRightNameToURLMaps(remoteURL: String, localParent: String) {
        try {

            val nameURL = HashMap<String, String>()
            val doc = Jsoup.connect(remoteURL).get()
            val input = doc.getElementsByClass("folder")
            input.addAll(doc.getElementsByClass("file"))

            var write = false
            for (e in input) {
                val a = e.child(0)
                val link = a.absUrl("href")
                if (e.className() == "folder") {
                    mkRightNameToURLMaps(link, localParent + flattenToAscii(a.text()) + File.separator)
                } else {
                    write = true
                    val extension = link.substring(link.lastIndexOf('.') + 1)
                    nameURL.put(flattenToAscii(if (a.text().contains(extension)) a.text() else a.text() + "." + extension), a.absUrl("href"))
                }
            }

            if (write) {
                val file = File(localParent + ".namemap")
                GsonUtil.writeGson(file, nameURL)
            }

        } catch (e: IOException) {
            logger.warn("El mapa de Nombre-URL no se ha completado.", e)
        }

    }

    /**
     * Método que compara el arbol de carpetas que tiene la asignatura en local y la que tiene en el PoliformaT devolviendo como resultado una lista con las URL de los que no están.
     * @param subjectFolder Carpeta donde se encuentran los archivos de la asignatura en formato Path.
     * *
     * @param subjectURL URL donde se ubican los archivos de la asignatura. Por ejemplo: https://poliformat.upv.es/access/content/group/GRA_11546_2015/
     * *
     * @return Mapa con las URL de los archivos no descargados y su path teórico en el sistema.
     * *
     */
    fun compareLocalFolderTreeAndRemote(subjectFolder: Path, subjectURL: String): Map<String, String> {
        val nowRemoteFiles = getFilesURL(subjectURL, subjectFolder.toString() + "/")
        try {
            Files.walkFileTree(subjectFolder, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, object : SimpleFileVisitor<Path>() {
                override fun visitFile(file: Path, attributes: BasicFileAttributes): FileVisitResult {
                    val map: Map<String, String>
                    if (Files.exists(file.parent.resolve(".namemap"))) {
                        map = GsonUtil.leerGson<String, String>(file.parent.resolve(".namemap").toFile())
                    } else {
                        map = HashMap<String, String>()
                    }
                    val stdName = trimFileName(file.fileName.toString())
                    if (nowRemoteFiles.containsKey(map[stdName])) {
                        nowRemoteFiles.remove(map[stdName])
                    }
                    return FileVisitResult.CONTINUE
                }
            })
        } catch (e: IOException) {
            logger.warn("No ha sido posible comparar el arbol de carpetas local con el arbol remoto.", e)
        }

        return nowRemoteFiles
    }

    /**
     * Elimina los acentos de un String.
     * Credits to David Conrad: http://stackoverflow.com/a/15191508
     * @param string El String a transformar.
     * *
     * @return El String transformado.
     * *
     */
    fun flattenToAscii(string: String): String {
        var string = string
        val out = CharArray(string.length)
        string = Normalizer.normalize(string, Normalizer.Form.NFD)
        var j = 0
        var i = 0
        val n = string.length
        while (i < n) {
            val c = string[i]
            if (c <= '\u007F') out[j++] = c
            ++i
        }
        return String(out)
    }

    /**
     * Quita espacios entre el nombre y la extension del archivo. Ejemplo: "hola .txt" --> "hola.txt"
     * @param s El String a transformar.
     * *
     * @return El nombre del archivo sin espacios delante y detras.
     * *
     */
    fun trimFileName(s: String): String {
        val extension = s.substring(s.lastIndexOf('.'))
        val name = s.substring(0, s.lastIndexOf('.'))
        return name.trim { it <= ' ' } + extension
    }
}
