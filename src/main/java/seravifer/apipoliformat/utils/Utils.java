package seravifer.apipoliformat.utils;

import java.util.Calendar;

/**
 * Libreria de utilidades.
 * Created by David on 17/02/2016.
 */
public class Utils {
    public static String getCurso() {

        Calendar time = Calendar.getInstance();

        int year = time.get(Calendar.YEAR);
        int month = time.get(Calendar.MONTH);

        if(month<9) return Integer.toString(year-1);
        else return Integer.toString(year);

    }
}
