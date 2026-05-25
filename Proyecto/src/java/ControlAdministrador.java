
import java.io.*;
import java.util.List;
import java.nio.charset.StandardCharsets;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ControlAdministrador {

    public void guardarEstadisticasActividad(Estadisticas estadisticas) {
        // Guarda una estadistica genérica (método legacy). Se puede usar GestorEstadisticas directamente.
        // Ejemplo temporal: mapear a BajarDatos si se desea mantener compatibilidad
        try {
            GestorEstadisticas.appendEstadistica(estadisticas);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String[]> obtenerEstadisticas() {
        return GestorEstadisticas.leerEstadisticas();
    }

    public String generarExcel() {
        return generarExcel(RutaAplicacion.recurso("estadisticas.xlsx").getAbsolutePath());
    }

    public String generarExcel(String rutaDestino) {
        List<String[]> datos = GestorEstadisticas.leerEstadisticas();
        try {
            // Intentar generar .xlsx usando Apache POI (si está disponible)
            try {
                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("Estadisticas");

                // Encabezados
                Row encabezado = sheet.createRow(0);
                encabezado.createCell(0).setCellValue("Fecha");
                encabezado.createCell(1).setCellValue("Actividad");
                encabezado.createCell(2).setCellValue("Aciertos");
                encabezado.createCell(3).setCellValue("Errores");
                encabezado.createCell(4).setCellValue("Tiempo");
                encabezado.createCell(5).setCellValue("CURP");

                // Datos
                int fila = 1;
                for (String[] dato : datos) {
                    Row row = sheet.createRow(fila++);
                    for (int i = 0; i < 6; i++) {
                        String v = (i < dato.length) ? dato[i] : "";
                        row.createCell(i).setCellValue(v);
                    }
                }

                // Autoajustar columnas
                for (int i = 0; i < 6; i++) sheet.autoSizeColumn(i);

                try (FileOutputStream fileOut = new FileOutputStream(rutaDestino)) {
                    workbook.write(fileOut);
                } finally {
                    workbook.close();
                }
                return rutaDestino;
            } catch (Throwable t) {
                // Si falla (p. ej. falta Apache POI), caemos a CSV que Excel puede abrir
                System.out.println("Apache POI no disponible o falló: " + t.getMessage() + " - Generando CSV como alternativa.");
                String rutaCsv = cambiarExtension(rutaDestino, ".csv");
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(rutaCsv), java.nio.charset.StandardCharsets.UTF_8))) {
                    bw.write("Fecha,Actividad,Aciertos,Errores,Tiempo,CURP"); bw.newLine();
                    for (String[] dato : datos) {
                        String[] row = new String[6];
                        for (int i = 0; i < 6; i++) row[i] = i < dato.length ? dato[i] : "";
                        bw.write(String.join(",", row)); bw.newLine();
                    }
                }
                return rutaCsv;
            }
        } catch (IOException e) {
            System.out.println("Error al generar Excel/CSV: " + e.getMessage());
            return null;
        }
    }

    private String cambiarExtension(String ruta, String nuevaExtension) {
        int punto = ruta.lastIndexOf('.');
        if (punto <= ruta.lastIndexOf(File.separatorChar)) {
            return ruta + nuevaExtension;
        }
        return ruta.substring(0, punto) + nuevaExtension;
    }
}