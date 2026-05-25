

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class ControlDedosColores {

    private JFrame menuPrincipal;
    private Process procesoPython;
        private Estadisticas estadisticas;
        private long tiempoInicio;
    private static final String RESULTADO_PATH = RutaAplicacion.recurso("resultado_dedos_colores.txt").getAbsolutePath();
    // El constructor recibe el menú de Java
    public ControlDedosColores(JFrame menuPrincipal) {
        this.menuPrincipal = menuPrincipal;
    }

    public void iniciarActividad() {
        tiempoInicio = System.currentTimeMillis();
        
        // Ocultamos el menú principal para que no estorbe
        if (menuPrincipal != null) {
            menuPrincipal.setVisible(false);
        }

        // Abrimos Python en un hilo secundario para que Java no se congele
        new Thread(() -> {
            try {
                new File(RESULTADO_PATH).delete();
                // En --onedir, el ejecutable queda dentro de la carpeta del mismo nombre.
                //ProcessBuilder pb = new ProcessBuilder(".venv311\\Scripts\\python.exe", "src/python/main/DedosColores.py");
                ProcessBuilder pb = new ProcessBuilder(RutaAplicacion.archivo("dist", "DedosColores", "DedosColores.exe").getAbsolutePath());
                pb.directory(RutaAplicacion.baseDir());
                pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                pb.redirectError(ProcessBuilder.Redirect.DISCARD);
                procesoPython = pb.start();
                // Espera a que el proceso muera por completo (por ESC o por terminar las rondas)
                procesoPython.waitFor();
                procesarResultadoDesdeArchivo();

            } catch (Exception e) {
                // Sin salida en consola
            } finally {
                // AL FINALIZAR: Despierta el menú principal de vuelta en la pantalla
                SwingUtilities.invokeLater(() -> {
                    if (menuPrincipal != null) {
                        menuPrincipal.setVisible(true);
                        menuPrincipal.toFront();
                        menuPrincipal.repaint();
                    }
                });
            }
        }).start();
    }

    private void procesarResultadoDesdeArchivo() {
        try {
            File archivo = new File(RESULTADO_PATH);
            if (!archivo.exists()) {
                return;
            }

            BufferedReader reader = new BufferedReader(new FileReader(archivo));
            String linea = reader.readLine();
            reader.close();

            if (linea == null || !linea.startsWith("RESULTADO:")) {
                return;
            }

            String[] partes = linea.split(":");
            if (partes.length < 2) {
                return;
            }

            String[] valores = partes[1].split(",");
            if (valores.length < 2) {
                return;
            }

            int aciertos = Integer.parseInt(valores[0].trim());
            int errores = Integer.parseInt(valores[1].trim());
            long tiempoTranscurrido = System.currentTimeMillis() - tiempoInicio;
            String tiempoActividad = formatearTiempo(tiempoTranscurrido);

            this.estadisticas = new Estadisticas("DedosColores", aciertos, errores, "", tiempoActividad);
            // Guardar en archivo compartido
            try {
                GestorEstadisticas.appendEstadistica(this.estadisticas);
            } catch (Exception ex) {
            }
            archivo.delete();
        } catch (Exception e) {
        }
    }

    private String formatearTiempo(long ms) {
        long segundos = ms / 1000;
        long minutos = segundos / 60;
        long segs = segundos % 60;
        return minutos + ":" + (segs < 10 ? "0" : "") + segs;
    }

    public Estadisticas getEstadisticas() {
        return estadisticas;
    }
}