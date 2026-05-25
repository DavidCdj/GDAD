

import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class ControlDedosArriba {

    private JFrame menuPrincipal;
    private Process procesoPython;
    private Estadisticas estadisticas;
    private long tiempoInicio;

    // El constructor recibe el menú de Java
    public ControlDedosArriba(JFrame menuPrincipal) {
        this.menuPrincipal = menuPrincipal;
    }

    public void iniciarActividad() {
        System.out.println("Ocultando menú y lanzando Python...");
        tiempoInicio = System.currentTimeMillis();
        // Ocultamos el menú principal para que no estorbe
        if (menuPrincipal != null) {
            menuPrincipal.setVisible(false);
        }

        // Abrimos Python en un hilo secundario para que Java no se congele
        new Thread(() -> {
            try {
                // Ejecuta tu Python usando tu entorno virtual (.venv)
                //ProcessBuilder pb = new ProcessBuilder(".venv311\\Scripts\\python.exe", "src/python/main/DedosArriba.py"); //este es antes de hacer .exe al de python
                // En --onedir, el ejecutable queda dentro de la carpeta del mismo nombre.
                ProcessBuilder pb = new ProcessBuilder(RutaAplicacion.archivo("dist", "DedosArriba", "DedosArriba.exe").getAbsolutePath());
                pb.directory(RutaAplicacion.baseDir());
                
                // Redirige errores para que podamos verlos en la consola de VS Code
                pb.redirectErrorStream(true);
                procesoPython = pb.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(procesoPython.getInputStream(), "UTF-8"));
                String linea;

                // Nos quedamos escuchando todo lo que imprima Python
                while ((linea = reader.readLine()) != null) {
                    linea = linea.trim();
                    System.out.println("[Python LOG]: " + linea);
                    
                    if (linea.startsWith("RESULTADO:")) {
                        System.out.println("Datos finales listos para Excel: " + linea);
                        procesarResultadoPython(linea);
                        // Aquí conectarás tu GestorExcel más adelante
                    }
                }

                // Espera a que el proceso muera por completo (por ESC o por terminar las rondas)
                procesoPython.waitFor();

            } catch (Exception e) {
                e.printStackTrace();
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

    private void procesarResultadoPython(String linea) {
        try {
            // Formato esperado: "RESULTADO:aciertos,errores"
            String[] partes = linea.split(":");
            if (partes.length < 2) {
                System.out.println("Formato incorrecto de resultado");
                return;
            }

            String[] valores = partes[1].split(",");
            if (valores.length < 2) {
                System.out.println("Faltan aciertos o errores en resultado");
                return;
            }

            int aciertos = Integer.parseInt(valores[0].trim());
            int errores = Integer.parseInt(valores[1].trim());
            long tiempoTranscurrido = System.currentTimeMillis() - tiempoInicio;
            String tiempoActividad = formatearTiempo(tiempoTranscurrido);

            this.estadisticas = new Estadisticas("DedosArriba", aciertos, errores, "", tiempoActividad);
            System.out.println("Estadísticas capturadas: Aciertos=" + aciertos + ", Errores=" + errores + ", Tiempo=" + tiempoActividad);
            // Guardar en archivo compartido
            try {
                GestorEstadisticas.appendEstadistica(this.estadisticas);
            } catch (Exception ex) {
                System.out.println("Error guardando estadisticas: " + ex.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Error al procesar resultado de Python: " + e.getMessage());
            e.printStackTrace();
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