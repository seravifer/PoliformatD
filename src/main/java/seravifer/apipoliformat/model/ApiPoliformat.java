package seravifer.apipoliformat.model;

import seravifer.apipoliformat.utils.Utils;

import java.io.*;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.net.ssl.HttpsURLConnection;

import ch.qos.logback.classic.Logger;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.slf4j.LoggerFactory;

/**
 * Api para PoliformaT de la UPV.
 * Created by Sergi Avila on 12/02/2016.
 */
public class ApiPoliformat {

    public static int attemps = 0;

    private static final Logger logger = (Logger) LoggerFactory.getLogger(ApiPoliformat.class);

    private List<String> cookies;
    private HttpsURLConnection conn;
    private Map<String, String> subjects;
    private DoubleProperty size;

    public ApiPoliformat(String dni, String pin) throws Exception {

        subjects = new HashMap<>();
        size = new SimpleDoubleProperty(0);

        if(dni.length()==8) attemps++;

        // Inicializa las cookies
        CookieHandler.setDefault(new CookieManager());
        // Extrae las cookies
        setCookies();
        // Manda las peticiones de login
        sendPost(dni, pin);
        // Busca las asignaturas
        getAsignaturas();

        Utils.getFiles("https://poliformat.upv.es/access/content/group/GRA_11546_2015/","");

    }

    private void setCookies() throws Exception{

        logger.info("Conexion con cookies...");

        URL link = new URL("https://intranet.upv.es/pls/soalu/est_intranet.NI_Indiv?P_IDIOMA=c&P_MODO=alumno&P_CUA=sakai&P_VISTA=MSE");
        conn = (HttpsURLConnection) link.openConnection();

        // Simula un navegador
        conn.setRequestMethod("GET");
        conn.setUseCaches(false);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        conn.setRequestProperty("Accept-Language", "es,en;q=0.8,gl;q=0.6");
        if (cookies != null) {
            for (String cookie : this.cookies) {
                conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
            }
        }

        new BufferedReader(new InputStreamReader(conn.getInputStream()));

        // Recoge las cookies
        setCookies(conn.getHeaderFields().get("Set-Cookie"));
    }

    private void sendPost(String username, String password) throws Exception {

        logger.info("Logeando...");

        String postParams = "&id=c&estilo=500&vista=MSE&cua=sakai&dni=" + username + "&clau=" + password+ "&=Entrar";

        URL link = new URL("https://www.upv.es/exp/aute_intranet");

        conn = (HttpsURLConnection) link.openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);

        DataOutputStream post = new DataOutputStream(conn.getOutputStream());
        post.writeBytes(postParams);
        post.flush();
        post.close();

        new BufferedReader(new InputStreamReader(conn.getInputStream()));

        logger.info("Logeo completado");

    }
    
    private void getAsignaturas() throws Exception {
        
        logger.info("Extrayendo asignaturas...");

        Document doc = Jsoup.connect("https://intranet.upv.es/pls/soalu/sic_asi.Lista_asig").get();

        Elements inputElements = doc.getElementsByClass("upv_enlace");

        for (Element inputElement : inputElements) {

            String oldName  = inputElement.ownText();
            String nexName  = oldName.substring(0, oldName.length()-2);
            String key      = inputElement.getElementsByTag("span").text().substring(1,6);

            subjects.put(nexName,key);

        }

        for(Map.Entry<String,String> entry : subjects.entrySet()) {
            logger.info( entry.getKey() + " - " + entry.getValue());
        }

        if(subjects.isEmpty()) { logger.warn("DNI o contraseña incorrectas!"); }

        logger.info("Extración completada");

    }
       
    public void download(String n) throws IOException {

        System.out.println("Descargando asignatura...");

        String key  = subjects.get(n); // ValueKey - Referencia de la asignatura
        String path = System.getProperty("user.dir") + File.separator;
        
        // Descargar zip
        URL url = new URL("https://poliformat.upv.es/sakai-content-tool/zipContent.zpc?collectionId=/group/GRA_" + key + "_" + Utils.getCurso() + "/&siteId=GRA_"+ key + "_" + Utils.getCurso());
        logger.debug(url.toString());

        InputStream in       = url.openStream();
        FileOutputStream fos = new FileOutputStream(new File(n + ".zip"));

        Platform.runLater(() -> size.set(0.0));
        int length;
        int downloadedSize = 0;
        byte[] buffer = new byte[2048];
        while ( (length = in.read(buffer)) > -1 ) {
            fos.write(buffer, 0, length);
            downloadedSize += length;
            final int tmp = downloadedSize;
            Platform.runLater(() -> size.set(tmp/(1024.0 * 1024.0 )));
        }
        final int tmp = downloadedSize;
        Platform.runLater(() -> size.set((tmp/(1024*1024.0))));
        fos.close();
        in.close();
        System.out.println("El zip descargado pesa " + Utils.round(Files.size(Paths.get(path + n + ".zip")) / (1024 * 1024.0), 2) + " MB");

        System.out.println("Extrayendo asignatura...");

        // Extrae los archivos del zip
        logger.info("Comenzando la extracción. El zip pesa {} MB", Utils.round(Files.size(Paths.get(path + n + ".zip")) / (1024 * 1024.0), 2));
        Utils.unZip( path + n + ".zip" );

        // Eliminar zip
        File file = new File( path + n + ".zip" );
        boolean deleted = file.delete();
        if(!deleted) {
            logger.error("EL ZIP NO HA SIDO BORRADO. SI NO ES BORRARO PUEDE ORIGINAR FALLOS EN FUTURAS DESCARGAS");
            throw new IOException("El zip de la asignatura no ha sido borrado");
        }

        System.out.println("Completado!");
        logger.info("Extracción con éxito");
    }

    private void setCookies(List<String> cookies) {
        this.cookies = cookies;
    }

    public List<String> getCookies() { return cookies; }

    public Map<String, String> getSubjects() { return subjects; }

    public DoubleProperty sizeProperty() { return size; }
}