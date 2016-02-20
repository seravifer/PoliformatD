package seravifer.apipoliformat.model;

import java.io.*;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.net.ssl.HttpsURLConnection;

import ch.qos.logback.classic.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import net.lingala.zip4j.exception.*;
import net.lingala.zip4j.core.*;
import net.lingala.zip4j.model.*;

import org.slf4j.LoggerFactory;
import seravifer.apipoliformat.utils.Utils;

/**
 * Api para PoliformaT de la UPV.
 * Created by Sergi Avila on 12/02/2016.
 */
public class ApiPoliformat {

    public static int attemps = 0;

    private static final Logger logger = (Logger) LoggerFactory.getLogger(ApiPoliformat.class);

    private List<String> cookies;
    private HttpsURLConnection conn;
    private Map<String, String> subjects;               // Mapa de asignaturas <Nombre, Referencia>

    public ApiPoliformat(String dni, String pin) throws Exception {

        subjects = new HashMap<>();

        if(dni.length()==8) attemps++;

        CookieHandler.setDefault(new CookieManager());  // Inicializa las cookies

        setCookies();                                   // Extrae las cookies

        sendPost(dni, pin);                             // Manda las peticiones de login

        //Peta en este método.
        getAsignaturas();                               // Busca las asignaturas

    }

    public ApiPoliformat() { subjects = new HashMap<>(); }

    private void setCookies() throws Exception {

        System.err.println("Conexion con cookies...");

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

        setCookies(conn.getHeaderFields().get("Set-Cookie")); // Recoge las cookies

    }
    
    private void sendPost(String username, String password) throws Exception {

        System.err.println("Logueando...");

        String postParams = "&id=c&estilo=500&vista=MSE&cua=sakai&dni=" + username + "&clau=" + password+ "&=Entrar";

        URL link = new URL("https://www.upv.es/exp/aute_intranet");
        conn = (HttpsURLConnection) link.openConnection();

        // Simula un navegador
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Host", "www.upv.es");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        conn.setRequestProperty("Accept-Language", "es,en;q=0.8,gl;q=0.6");
        for (String cookie : this.cookies) {
            conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
        }
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Referer", "https://intranet.upv.es/pls/soalu/est_intranet.NI_Indiv?P_IDIOMA=c&P_MODO=alumno&P_CUA=sakai&P_VISTA=MSE");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", Integer.toString(postParams.length()));

        conn.setDoOutput(true);
        conn.setDoInput(true);

        // Envia la peticion
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(postParams);
        wr.flush();
        wr.close();
        new BufferedReader(new InputStreamReader(conn.getInputStream()));

    }
    
    private void getAsignaturas() throws Exception {
        
        System.err.println("Extrayendo asignaturas...");

        //Sí, justo en esta linea es donde peta. Con un bonito "java.net.SocketTimeoutException: Read timed out"
        Document doc = Jsoup.connect("https://intranet.upv.es/pls/soalu/sic_asi.Lista_asig").get();

        // Busca los campos del formulario
        try {
            Elements inputElements = doc.getElementsByClass("upv_enlace");

            for (Element inputElement : inputElements) {

                String oldName = inputElement.ownText();
                String name = oldName.substring(0, oldName.length()-2);
                String key = inputElement.getElementsByTag("span").text().substring(1,6);

                subjects.put(name,key);

            }

            for(Map.Entry<String,String> entry : subjects.entrySet()) {
                System.err.println( entry.getKey() + " - " + entry.getValue());
            }

        } catch (NullPointerException e) {
            System.out.println("DNI o contraseña incorrectas");
        }

    }
       
    public void download(String n) throws IOException, ZipException {

        System.out.println("Descargando asignatura...");

        String key      = subjects.get(n);      // ValueKey - Referencia de la asignatura
        String path     = System.getProperty("user.dir") + File.separator;
        
        // Descargar zip
        URL url = new URL("https://poliformat.upv.es/sakai-content-tool/zipContent.zpc?collectionId=/group/GRA_" + key + "_" + Utils.getCurso() + "/&siteId=GRA_"+ key + "_" + Utils.getCurso());
        
        InputStream in = url.openStream();
        FileOutputStream fos = new FileOutputStream(new File(n + ".zip"));

        int length;
        byte[] buffer = new byte[1024];
        while ((length = in.read(buffer)) > -1) {
            fos.write(buffer, 0, length);
        }
        fos.close();
        in.close();

        System.out.println("Extrayendo asignatura...");

        // Extraer archivos del zip
        ZipFile zipFile = new ZipFile( path + n + ".zip" );
        zipFile.setFileNameCharset("UTF-8");

        @SuppressWarnings("unchecked")
        List<FileHeader> fileHeaders;
        fileHeaders = zipFile.getFileHeaders();

        String oldName = fileHeaders.iterator().next().getFileName();
        String oldNameFolder = oldName.substring(0,oldName.indexOf("/"));
        String newNameFolder = oldName.substring(0,oldName.indexOf("/")).toUpperCase();
        //System.err.println(oldNameFolder + newNameFolder);

        for(FileHeader fileHeader : fileHeaders) {
            System.err.println(fileHeader.getFileName());
            String goodName = fileHeader.getFileName().replace("|", "-").replace("|", "").replace(" /", "/").replace(":", "").replace("\"", "");
            zipFile.extractFile(fileHeader, path, null, goodName);
        }

        // Eliminar zip
        File file = new File( path + n + ".zip" );
        boolean deleted = file.delete();
        if(!deleted) throw new IOException("El zip de la asignatura no ha sido borrado");
        
        // Cambiar nombre carpeta extraida
        File dir = new File( path + oldNameFolder + File.separator );
        File newDir = new File( dir.getParent() + File.separator + newNameFolder);
        dir.renameTo(newDir);
        
        System.out.println("Completado!");
        
    }

    private void setCookies(List<String> cookies) { this.cookies = cookies; }

    public Map<String, String> getSubjects() {
        return subjects;
    }

}