import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class GestorEstadisticas {

    private static final String RUTA = "recursos/estadisticas.txt";

    public static boolean existeArchivo() {
        File f = new File(RUTA);
        return f.exists();
    }

    private static void normalizarArchivoSiNecesario() {
        try {
            File f = new File(RUTA);
            if (!f.exists()) return;
            byte[] bytes = Files.readAllBytes(Paths.get(RUTA));

            boolean tieneNulos = false;
            for (byte b : bytes) {
                if (b == 0) {
                    tieneNulos = true;
                    break;
                }
            }

            if (tieneNulos) {
                ByteArrayOutputStream limpio = new ByteArrayOutputStream(bytes.length);
                for (byte b : bytes) {
                    if (b != 0) limpio.write(b);
                }
                // Reescribir en UTF-8 para dejar el archivo en formato consistente.
                String contenido = new String(limpio.toByteArray(), StandardCharsets.UTF_8)
                    .replace("\uFEFF", "")
                    .replace("\u0000", "");
                Files.write(Paths.get(RUTA), contenido.getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean tieneCurp() {
        String curp = leerCurp();
        return curp != null && !curp.isEmpty();
    }

    public static String leerCurp() {
        normalizarArchivoSiNecesario();
        File f = new File(RUTA);
        if (!f.exists()) return null;
        try (BufferedReader br = Files.newBufferedReader(Paths.get(RUTA), StandardCharsets.UTF_8)) {
            String primera = br.readLine();
            if (primera != null) {
                primera = primera.replace("\uFEFF", "").replace("\u0000", "").trim();
            }
            if (primera != null && primera.startsWith("CURP:")) {
                return primera.substring(5).trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void escribirCurp(String curp) {
        try {
            normalizarArchivoSiNecesario();
            File f = new File(RUTA);
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(RUTA), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
                    bw.write("CURP:" + curp);
                    bw.newLine();
                }
                return;
            }

            // Si existe pero no tiene CURP, lo añadimos al inicio
            if (!tieneCurp()) {
                StringBuilder sb = new StringBuilder();
                sb.append("CURP:").append(curp).append(System.lineSeparator());
                try (BufferedReader br = Files.newBufferedReader(Paths.get(RUTA), StandardCharsets.UTF_8)) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append(System.lineSeparator());
                    }
                }
                try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(RUTA), StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING)) {
                    bw.write(sb.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void appendEstadistica(Estadisticas e) {
        try {
            normalizarArchivoSiNecesario();
            File f = new File(RUTA);
            f.getParentFile().mkdirs();
            if (!f.exists()) {
                // Crear archivo vacío si no existe (CURP deberá ser registrado por la app antes)
                f.createNewFile();
            }

            String curp = leerCurp();
            if (curp != null) e.setCurp(curp);

            String linea = String.format("%s,%s,%d,%d,%s,%s", e.getFecha(), e.getActividad(), e.getAciertos(), e.getErrores(), e.getTiempoActividad(), e.getCurp());
            try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(RUTA), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                bw.write(linea);
                bw.newLine();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static java.util.List<String[]> leerEstadisticas() {
        normalizarArchivoSiNecesario();
        java.util.List<String[]> lista = new java.util.ArrayList<>();
        File f = new File(RUTA);
        if (!f.exists()) return lista;
        try (BufferedReader br = Files.newBufferedReader(Paths.get(RUTA), StandardCharsets.UTF_8)) {
            String line;
            // Primera linea puede ser CURP
            boolean primera = true;
            while ((line = br.readLine()) != null) {
                line = line.replace("\uFEFF", "").replace("\u0000", "");
                if (primera) { primera = false; if (line.startsWith("CURP:")) continue; }
                line = line.trim();
                if (line.isEmpty()) continue;
                // Cada línea: fecha,actividad,aciertos,errores,tiempo,curp
                String[] parts = line.split(",");
                lista.add(parts);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return lista;
    }

    public static void migrarDesdeCSV() {
        String rutaCsv = System.getProperty("user.dir") + "/estadisticas.csv";
        File csv = new File(rutaCsv);
        if (!csv.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(csv))) {
            String linea;
            boolean primera = true;
            while ((linea = br.readLine()) != null) {
                if (primera) { primera = false; continue; } // saltar encabezado
                linea = linea.trim();
                if (linea.isEmpty()) continue;
                // Formato esperado: Fecha,Actividad,Rondas Completadas,Total Errores
                String[] partes = linea.split(",");
                if (partes.length >= 4) {
                    String fecha = partes[0];
                    String actividad = partes[1];
                    int rondas = 0;
                    int errores = 0;
                    try { rondas = Integer.parseInt(partes[2].trim()); } catch (Exception ex) {}
                    try { errores = Integer.parseInt(partes[3].trim()); } catch (Exception ex) {}

                    Estadisticas e = new Estadisticas(actividad, rondas, errores, "", "");
                    e.setFecha(fecha);
                    appendEstadistica(e);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Intentar borrar el CSV antiguo
        try {
            if (!csv.delete()) {
                System.out.println("No se pudo eliminar el CSV antiguo: " + rutaCsv);
            } else {
                System.out.println("CSV antiguo migrado y eliminado: " + rutaCsv);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
