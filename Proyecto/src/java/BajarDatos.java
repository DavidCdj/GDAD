
import java.util.*;

public class BajarDatos {
    
    private static final String RUTA_CSV = 
        System.getProperty("user.dir") + "/estadisticas.csv";
    
    public static void guardarEstadistica(String actividad, int rondasCompletadas, int totalErrores) {
        // Método legacy: mapeamos a GestorEstadisticas para unificar el almacenamiento
        try {
            Estadisticas e = new Estadisticas(actividad, rondasCompletadas, totalErrores, "", "");
            GestorEstadisticas.appendEstadistica(e);
        } catch (Exception ex) {
            System.out.println("Error al guardar estadistica (delegado): " + ex.getMessage());
        }
    }
    
    public static List<String[]> leerEstadisticas() {
        // Delegar la lectura al GestorEstadisticas
        return GestorEstadisticas.leerEstadisticas();
    }
    
    public static String getRutaCSV() {
        return RUTA_CSV;
    }
}