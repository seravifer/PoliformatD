import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Scanner;
import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//import net.lingala.zip4j.exception.*;
import net.lingala.zip4j.core.*;
import net.lingala.zip4j.model.*;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

/**
 * Api para PoliformaT de la UPV.
 *
 * @author Sergi Ávila
 * @version 0.1
 * @date 14/02/2016
 */
public class ApiPoliformat {
    
    private List<String> cookies;
    private HttpsURLConnection conn;
    public String[][] asig = new String[10][3]; // Array con las asignaturas
    
    public static void main( String[] args ) throws Exception {
        
        //Scanner input = new Scanner( System.in );
        
        String url = "https://intranet.upv.es/pls/soalu/est_intranet.NI_Indiv?P_IDIOMA=c&P_MODO=alumno&P_CUA=sakai&P_VISTA=MSE";
        String portal = "https://poliformat.upv.es/portal/tool/2cdd60e6-d777-4c10-a9da-45f76bc23d02/tab-dhtml-moresites";
        String dni = "";
        String pass = "";
        
        ApiPoliformat http = new ApiPoliformat();               // Inicia conexion

        CookieHandler.setDefault(new CookieManager());          // Procesa las Cookies
        
        // 1. Extrae la petecion de login
        String page = http.getPageContent(url);                 // Muestra el contenido en texto plano del HTML
        String postParams = http.getFormParams(page, dni, pass);// Extrae la petion del texto plano
        System.out.println(postParams);

        // 2. Manda las peticiones de login
        http.sendPost(postParams);

        // 3. Accede a PoliformaT
        String result = http.getPageContent(portal);            // Muestra el contenido en texto plano del HTML

        // 4. Busca las asignaturas
        http.getAsignaturas(result);                            // Extrae el nombre de las asignaturas

        http.control();                                         // Activa GUI

        //String n = input.nextLine();
        //int a = http.buscar(n);                               // Elegir asignatura a descargar
        
        // 5. Sincroniza los archivos
        //http.sync(a);
        
    }
    
    public void control() {
        JFrame mainFrame = new JFrame("PoliformaT");
         mainFrame.setBounds(500,250,300,250);
         mainFrame.setLayout(new GridLayout(3, 1));
        
        ActionListener listener = e -> {
            if (e.getSource() instanceof JButton) {
                String text = ((JButton) e.getSource()).getText();
                try {
                    int a = buscar(text);
                    sync(a);
                } catch(Exception u){
                     u.printStackTrace();
                }
            }
        };
        
        JButton[] array = new JButton[10];
        for (int i = 0; i < array.length; i++) {
            array[i] = new JButton(asig[i][0]);
            array[i].addActionListener(listener);
            mainFrame.add(array[i]);
        }
        
        mainFrame.setVisible(true); 
        mainFrame.setResizable(false);
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
        
        //int responseCode = conn.getResponseCode();
        //System.out.println("\nEnviando petición 'GET' a URL : " + url);
        //System.out.println("Respuesta : " + responseCode);

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
        
        System.out.println("Extrayendo datos del formulario...");
        
        Document doc = Jsoup.parse(html);

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
        //return result.toString(); // Imprime la los parametros de acceso
        
    }
    
    public void sendPost(String postParams) throws Exception {

        System.out.println("Logueando...");

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

        //int responseCode = conn.getResponseCode();
        //System.out.println("\nEnviando petición 'POST' a URL : " + url);
        //System.out.println("Parametros POST : " + postParams); // id=c&estilo=500&vista=MSE&cua=sakai&dni={DNI}&clau={CONTRASEÑA}&=Entrar
        //System.out.println("Respuesta : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //System.out.println(response.toString());

    }
    
    public List<String> getCookies() {
        return cookies;
    }

    public void setCookies(List<String> cookies) {
        this.cookies = cookies;
    }
    
    public void getAsignaturas(String html) throws Exception {
        
        System.out.println("Extrayendo asignaturas...");
        
        Document doc = Jsoup.parse(html);

        // Busca los campos del formulario
        Element loginform = doc.getElementById("tab-dhtml-more-sites");
        Elements inputElements = loginform.getElementsByTag("option");
        
        int a=0;
        for (Element inputElement : inputElements) {
            String name = inputElement.text().toUpperCase();
            String oldName = inputElement.text();
            String key = inputElement.attr("value");
            if (key.startsWith("GRA")) {
                asig[a][0] = name.substring(0, 3);
                asig[a][1] = key;
                asig[a][2] = oldName;
                a++;
            }
        }

        // Muestro la lista de asiganturas
        for(int i=0; i<asig.length; i++) {
            if ( asig[i][0] != null )  {
                System.out.println( asig[i][0] + " - " + asig[i][1] + " - " + asig[i][2] );
            }
        }
        
    }
    
    public int buscar(String asignatura) {
        for(int i=0; i<asig.length; i++) {
            if ( asig[i][0].equals(asignatura)) return i;
        }
        return -1;
    }
       
    public void sync( int n) throws Exception {

        String name     = asig[n][0];
        String key      = asig[n][1];
        String oldName  = asig[n][2];
        String path     = new File( "." ).getCanonicalPath() + "\\";
        
        // Descargar zip
        URL url = new URL("https://poliformat.upv.es/sakai-content-tool/zipContent.zpc?collectionId=/group/" + key + "/&siteId="+ key);
        System.out.println("Descargando: " + url);
        
        InputStream in = url.openStream();
        FileOutputStream fos = new FileOutputStream(new File(name + ".zip"));

        int length;
        byte[] buffer = new byte[1024];
        while ((length = in.read(buffer)) > -1) {
            fos.write(buffer, 0, length);
        }
        fos.close();
        in.close();
        
        //Extraer archivos del zip
        ZipFile zipFile = new ZipFile( path + name + ".zip" );
        zipFile.setFileNameCharset("UTF-8");

        @SuppressWarnings("unchecked")
        List<FileHeader> fileHeaders;
        fileHeaders = zipFile.getFileHeaders();
        for(FileHeader fileHeader : fileHeaders) {
            System.out.println(fileHeader.getFileName());
            String goodName = fileHeader.getFileName().replace("|", "-").replace("|", "").replace(" /", "/").replace(":", "");
            zipFile.extractFile(fileHeader, path, null, goodName);
        }
        
        
        // Eliminar zip
        File file = new File( path + name + ".zip" );
        file.delete();
        
        // Cambiar nombre carpeta extraida
        File dir = new File( path + oldName + "\\" );
        File newDir = new File( dir.getParent() + "/" + name );
        dir.renameTo(newDir);
        
        System.out.println("Completado");
        
    }

}