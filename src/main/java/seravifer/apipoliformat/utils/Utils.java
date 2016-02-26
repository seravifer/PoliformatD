package seravifer.apipoliformat.utils;

import ch.qos.logback.classic.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.net.URLDecoder;

/**
 * Libreria de utilidades.
 * Created by David on 17/02/2016.
 */
public class Utils {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(Utils.class);

    public static String getCurso() {
        Calendar time = Calendar.getInstance();

        int year = time.get(Calendar.YEAR);
        int month = time.get(Calendar.MONTH);

        if(month<9) return Integer.toString(year-1);
        else return Integer.toString(year);
    }

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
            if(!folder.exists()){
                folder.mkdir();
            }

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

    public static List<String> getFiles(String url, String parent) {

        List<String> asig = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(url).get();
            Elements input = doc.getElementsByClass("folder");
            input.addAll(doc.getElementsByClass("file"));

            for (Element e :
                    input) {
                if(e.className().equals("folder")) {
                    asig.addAll(getFiles(e.child(0).absUrl("href"), parent + URLDecoder.decode(e.child(0).attr("href"), "UTF-8")));
                } else {
                    asig.add(parent + e.text());
                }
            }
        } catch (IOException e) {
            if(e instanceof UnsupportedEncodingException) {
                logger.warn("Codificacion no soportada", e);
            } else {
                logger.warn("No ha sido posible recuperar la lista de archivos del servidor", e);
            }
        }
        return asig;

    }

    public static double round(double x, int d) {
        return Math.round(x*Math.pow(10, d))/Math.pow(10, d);
    }

    public static void createURLMaps(String s, String parent) {
        try {

            Map<String, String> nameURL = new HashMap<>();
            Document doc = Jsoup.connect(s).get();
            Elements input = doc.getElementsByClass("folder");
            input.addAll(doc.getElementsByClass("file"));

            for (Element e :
                    input) {
                Element folder = e.child(0);
                if(e.className().equals("folder")) {
                    if (Files.notExists(Paths.get(parent + folder.text()))) {
                        Files.createDirectory(Paths.get(parent + folder.text()));
                    }
                    nameURL.put(folder.text(), folder.attr("href"));
                    createURLMaps(folder.absUrl("href"), parent + folder.text() + File.separator);
                } else {

                    nameURL.put(folder.text(), folder.attr("href"));
                }
            }

            File file = new File(parent + "namemap");
            GsonUtil.writeGson(file, nameURL);

        } catch (IOException e) {
            logger.warn("El mapa de Nombre-URL no se ha completado.", e);
        }
    }
}
