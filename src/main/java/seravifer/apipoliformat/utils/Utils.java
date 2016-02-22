package seravifer.apipoliformat.utils;

import ch.qos.logback.classic.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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

    public static void unZip(String zipFile) {

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

        } catch(IOException e){
            logger.warn("Ha fallado la descompresion de los archivos", e);
        }
    }

    public static void getFiles(String url, String parent) throws IOException {

        List<String> asig = new ArrayList<>();

        if( !asig.contains(url) ) {

            Document doc = Jsoup.connect(url).get();

            Elements inputFolder = doc.getElementsByClass("folder");
            Elements inputFile   = doc.getElementsByClass("file");

            for (Element inputElement : inputFile) {

                String name = inputElement.text();
                asig.add(parent + name);
            }

            for (String asigs : asig) {
                logger.debug("Archivo: {}", asigs);
            }

            Elements nextLinks = inputFolder.select("a[href]");

            for (Element next : nextLinks) {
                getFiles(next.absUrl("href"),parent + URLDecoder.decode(next.attr("href"), "UTF-8"));
            }

        }

    }

    public static double round(double x, int d) {
        return Math.round(x*Math.pow(10, d))/Math.pow(10, d);
    }
}
