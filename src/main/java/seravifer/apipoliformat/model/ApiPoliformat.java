package seravifer.apipoliformat.model;

import java.io.*;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javafx.util.Pair;
import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import net.lingala.zip4j.exception.*;
import net.lingala.zip4j.core.*;
import net.lingala.zip4j.model.*;

import seravifer.apipoliformat.utils.Reference;


/**
 * Api para PoliformaT de la UPV.
 * Created by Sergi Avila on 19/02/2016.
 */
public class ApiPoliformat {
    public static int attemps = 0;

    private List<String> cookies;
    private HttpsURLConnection conn;
    private Map<String, Pair<String, String>> subjects; // Lista de subjects

    public ApiPoliformat(String dni, String pin) throws Exception {
        subjects = new HashMap<>();
        if(dni.length() == 8) {
            attemps++;
        }

        CookieHandler.setDefault(new CookieManager());                          // Procesa las Cookies

        // 1. Extrae la petecion de login
        String page = getPageContent(Reference.LOGIN_INTRANET);                 // Muestra el contenido en texto plano del HTML
        String postParams = getFormParams(page, dni, pin);                      // Extrae la petion del texto plano
        //System.out.println(postParams);

        // 2. Manda las peticiones de login
        sendPost(postParams);

        // 3. Busca las asignaturas
        getAsignaturas();

    }

    public ApiPoliformat() {
        subjects = new HashMap<>();
    }

    public String getPageContent(String url) throws Exception {

        URL link = new URL(url);
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

        BufferedReader in =  new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        
        setCookies(conn.getHeaderFields().get("Set-Cookie")); // Recoge las cookies

        return response.toString(); // Imprime todas las lineas del HTML

    }

    public String getFormParams(String html, String username, String password) throws Exception {
        
        System.err.println("Extrayendo datos del formulario...");
        
        Document doc = Jsoup.parse(html);
        //Document doc = Jsoup.connect("https://intranet.upv.es/pls/soalu/sic_asi.Lista_asig").get();

        // Busca los campos del formulario
        Element loginForm = doc.getElementById("pagina");
        Elements inputElements = loginForm.getElementsByTag("input");

        StringBuilder log = new StringBuilder();

        for (Element inputElement : inputElements) {

            String key   = inputElement.attr("name");
            String value = inputElement.attr("value");

            if (key.equals("dni")) {
                value = username;
            } else if (key.equals("clau")) {
                value = password;
            }

            log.append("&" + key + "=" + URLEncoder.encode(value, "ISO-8859-1"));

        }

        return log.toString();
        
    }
    
    public void sendPost(String postParams) throws Exception {

        System.err.println("Logueando...");

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
    
    public List<String> getCookies() {
        return cookies;
    }

    public void setCookies(List<String> cookies) {
        this.cookies = cookies;
    }
    
    public void getAsignaturas() throws Exception {
        
        System.err.println("Extrayendo subjects...");
        Document doc = Jsoup.connect("https://intranet.upv.es/pls/soalu/sic_asi.Lista_asig").get();

        // Busca los campos del formulario
        try {
            Elements inputElements = doc.getElementsByClass("upv_enlace");

            for (Element inputElement : inputElements) {

                String oldName = inputElement.ownText();
                String name = oldName.substring(0, oldName.length()-2);
                String key = inputElement.getElementsByTag("span").text().substring(1,6);

                subjects.put(name, new Pair<>(key, null));

            }

            for(Map.Entry<String, Pair<String, String>> entry : subjects.entrySet()) {
                System.err.println( entry.getKey() + " - " + entry.getValue().getKey());
            }

        } catch (NullPointerException e) {
            System.out.println("DNI o contraseña incorrectas");
        }

    }
       
    public void download(String n) throws IOException, ZipException {

        String key      = subjects.get(n).getKey();      // ValueKey - Referencia de la asignatura
        //String oldName  = subjects.get(n).getValue();    // ValueValue - Nombre orignal de la asignatura
        String path     = System.getProperty("user.dir") + File.separator;
        
        // Descargar zip
        URL url = new URL("https://poliformat.upv.es/sakai-content-tool/zipContent.zpc?collectionId=/group/GRA_" + key + "_2015/&siteId=GRA_"+ key + "_2015");
        System.err.println(url);
        //https://poliformat.upv.es/sakai-content-tool/zipContent.zpc?collectionId=/group/GRA_11546_2015/&siteId=GRA_11546_2015
        //https://poliformat.upv.es/sakai-content-tool/zipContent.zpc?collectionId=/group/GRA_11538_2015/&siteId=11538_2015
        System.out.println("Descargando asignatura...");
        
        InputStream in = url.openStream();
        FileOutputStream fos = new FileOutputStream(new File(n + ".zip"));

        int length;
        byte[] buffer = new byte[1024];
        while ((length = in.read(buffer)) > -1) {
            fos.write(buffer, 0, length);
        }
        fos.close();
        in.close();
        
        //Extraer archivos del zip
        ZipFile zipFile = new ZipFile( path + n + ".zip" );
        zipFile.setFileNameCharset("UTF-8");

        @SuppressWarnings("unchecked")
        List<FileHeader> fileHeaders;
        fileHeaders = zipFile.getFileHeaders();
        for(FileHeader fileHeader : fileHeaders) {
            System.err.println(fileHeader.getFileName());
            String goodName = fileHeader.getFileName().replace("|", "-").replace("|", "").replace(" /", "/").replace(":", "").replace("\"", "");
            zipFile.extractFile(fileHeader, path, null, goodName);
        }

        // Eliminar zip
        File file = new File( path + n + ".zip" );
        file.delete();
        
        // Cambiar nombre carpeta extraida
        //File dir = new File( path + oldName + File.separator );
        //File newDir = new File( dir.getParent() + File.separator + n);
        //dir.renameTo(newDir);
        
        System.out.println("Completado!");
        
    }

    public Map<String, Pair<String, String>> getSubjects() {
        return subjects;
    }

    public static void main( String[] args ) throws Exception {

        String url = "https://intranet.upv.es/pls/soalu/est_intranet.NI_Indiv?P_IDIOMA=c&P_MODO=alumno&P_CUA=sakai&P_VISTA=MSE";
        String dni = "";
        String pass = "";

        ApiPoliformat http = new ApiPoliformat();               // Inicia conexion

        CookieHandler.setDefault(new CookieManager());          // Procesa las Cookies

        // 1. Extrae la petecion de login
        String page = http.getPageContent(url);                 // Muestra el contenido en texto plano del HTML
        String postParams = http.getFormParams(page, dni, pass);// Extrae la petion del texto plano
        //System.out.println(postParams);

        // 2. Manda las peticiones de login
        http.sendPost(postParams);

        // 3. Busca las asignaturas
        http.getAsignaturas();                            // Extrae el nombre de las subjects

        // 4. Descargo la asignatura
        http.download("Introducción a la informática y a la programación");

    }

}