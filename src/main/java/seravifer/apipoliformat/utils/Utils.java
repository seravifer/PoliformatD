package seravifer.apipoliformat.utils;

import ch.qos.logback.classic.Logger;
import com.google.gson.reflect.TypeToken;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.Normalizer;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Libreria de utilidades.
 * Created by David on 17/02/2016.
 */
public class Utils {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(Utils.class);

    /**
     * Calcula el curso escolar actual.
     * @return El curso escolar actual en formato String.
     * */
    public static String getCurso() {
        Calendar time = Calendar.getInstance();

        int year = time.get(Calendar.YEAR);
        int month = time.get(Calendar.MONTH);

        if(month<9) return Integer.toString(year-1);
        else return Integer.toString(year);
    }


    /**
     * Extrae los archivos de un fichero Zip.
     * @param zipFile Representacion de la ruta del archivo Zip en formato String.
     * @return Devuelve el nombre de la carpeta donde ha sido extraído.
     * */
    public static String unZip(String zipFile) {
        byte[] buffer = new byte[1024];

        try{

            ZipInputStream zip =
                    new ZipInputStream(
                            new FileInputStream(zipFile)
                    );

            ZipEntry zipContent = zip.getNextEntry();

            String nameFolder = zipContent.toString().substring(0,zipContent.toString().indexOf("/")).toUpperCase();

            File folder = new File(System.getProperty("user.dir") + File.separator + nameFolder);

            if(!folder.exists()) folder.mkdir();

            while(zipContent!=null) {

                String fileName = zipContent.getName().replace("|", "-").replace("|", "").replace(" /", "/").replace(":", "").replace("\"", "");
                File newFile = new File(fileName);
                logger.info("Extrayendo {}", newFile.getAbsoluteFile());

                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);

                int lenght;
                while ((lenght = zip.read(buffer)) > 0) {
                    fos.write(buffer, 0, lenght);
                }

                fos.close();
                zipContent = zip.getNextEntry();

            }

            zip.closeEntry();
            zip.close();

            return nameFolder;
        } catch(IOException e){
            logger.warn("Ha fallado la descompresion de los archivos", e);
        }
        return "";
    }

    /**
    * Es llamado cuando se actualiza. Agarra los links de todos los archivos de una asignatura y su path teórico en el sistema. No coge los links de carpetas.
    * @param url Es la URL de la asignatura como String. Por ejemplo: https://poliformat.upv.es/access/content/group/GRA_11546_2015/
    * @return Devuelve un mapa con las URL de los archivos en el PoliformaT y sus nombres reales.
    */
    public static Map<String, String> getFilesURL(String url, String parent) {
        Map<String, String> map = new HashMap<>();
        try {
            Document doc = Jsoup.connect(url).get();
            Elements input = doc.getElementsByClass("folder");
            input.addAll(doc.getElementsByClass("file"));

            for (Element e : input) {
                Element a = e.child(0);
                String link = a.absUrl("href");
                if(e.className().equals("folder")) {
                    map.putAll(getFilesURL(link, parent + flattenToAscii(a.text()) + "/"));
                } else {
                    String extension = link.substring(link.lastIndexOf('.') + 1);
                    map.put(link, parent + flattenToAscii(a.text().contains(extension) ? a.text() : a.text() + "." + extension));
                }
            }
        } catch (IOException e) {
            if(e instanceof UnsupportedEncodingException) {
                logger.warn("Codificacion no soportada", e);
            } else {
                logger.warn("No ha sido posible recuperar la lista de archivos del servidor", e);
            }
        }
        return map;
    }

    /**
    * Redondea un double "x" a otro de "d" decimales.
    * @param x El double a redondear.
    * @param d El numero de decimales.
    * @return Un double x de d decimales.
    * */
    public static double round(double x, int d) {
        return Math.round(x*Math.pow(10, d))/Math.pow(10, d);
    }


    /**
     * Crea los mapas json que relacionan los nombres de las carpetas con su URL. Se usa en la comprobacion de archivos en la actualización.
     * @param remoteURL La URL en formato String de la carpeta de la asignatura. Por ejemplo: https://poliformat.upv.es/access/content/group/GRA_11546_2015/
     * @param localParent En la primera llamada al método debe valer la carpeta donde está descargada la asignatura. Ejemplos: AMA, FOE, TCO, FCO...
     * */
    public static void mkRightNameToURLMaps(String remoteURL, String localParent) {
        try {

            Map<String, String> nameURL = new HashMap<>();
            Document doc = Jsoup.connect(remoteURL).get();
            Elements input = doc.getElementsByClass("folder");
            input.addAll(doc.getElementsByClass("file"));

            boolean write = false;
            for (Element e : input) {
                Element a = e.child(0);
                String link = a.absUrl("href");
                if(e.className().equals("folder")) {
                    mkRightNameToURLMaps(link, localParent + flattenToAscii(a.text()) + File.separator);
                } else {
                    write = true;
                    String extension = link.substring(link.lastIndexOf('.') + 1);
                    nameURL.put(flattenToAscii(a.text().contains(extension) ? a.text() : a.text() + "." + extension), a.absUrl("href"));
                }
            }

            if (write) {
                File file = new File(localParent + ".namemap");
                GsonUtil.writeGson(file, nameURL);
            }

        } catch (IOException e) {
            logger.warn("El mapa de Nombre-URL no se ha completado.", e);
        }
    }

    /**
     * Método que compara el arbol de carpetas que tiene la asignatura en local y la que tiene en el PoliformaT devolviendo como resultado una lista con las URL de los que no están.
     * @param subjectFolder Carpeta donde se encuentran los archivos de la asignatura en formato Path.
     * @param subjectURL URL donde se ubican los archivos de la asignatura. Por ejemplo: https://poliformat.upv.es/access/content/group/GRA_11546_2015/
     * @return Mapa con las URL de los archivos no descargados y su path teórico en el sistema.
     * */
    public static Map<String, String> compareLocalFolderTreeAndRemote(Path subjectFolder, String subjectURL) {
        Map<String, String> nowRemoteFiles = getFilesURL(subjectURL, subjectFolder.toString() + "/");
        try {
            Files.walkFileTree(subjectFolder, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    Map<String, String> map;
                    if (Files.exists(file.getParent().resolve(".namemap"))) {
                        map = GsonUtil.leerGson(file.getParent().resolve(".namemap").toFile(), new TypeToken<Map<String, String>>() { }.getType());
                    } else {
                        map = new HashMap<>();
                    }
                    if (nowRemoteFiles.containsKey(map.get(file.getFileName().toString()))) {
                        nowRemoteFiles.remove(map.get(file.getFileName().toString()));
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            logger.warn("No ha sido posible comparar el arbol de carpetas local con el arbol remoto.", e);
        }
        return nowRemoteFiles;
    }

    /**
    * Elimina los acentos de un String.
    * Credits to David Conrad: http://stackoverflow.com/a/15191508
    * @param string El String a transformar.
    * @return El String transformado.
    * */
    public static String flattenToAscii(String string) {
        char[] out = new char[string.length()];
        string = Normalizer.normalize(string, Normalizer.Form.NFD);
        int j = 0;
        for (int i = 0, n = string.length(); i < n; ++i) {
            char c = string.charAt(i);
            if (c <= '\u007F') out[j++] = c;
        }
        return new String(out);
    }
}
