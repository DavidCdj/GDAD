

import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class ControlDedosArriba {

    private JFrame menuPrincipal;
    private Process procesoPython;

    // El constructor recibe el menú de Java
    public ControlDedosArriba(JFrame menuPrincipal) {
        this.menuPrincipal = menuPrincipal;
    }

    public void iniciarActividad() {
        System.out.println("Ocultando menú y lanzando Python...");
        
        // Ocultamos el menú principal para que no estorbe
        if (menuPrincipal != null) {
            menuPrincipal.setVisible(false);
        }

        // Abrimos Python en un hilo secundario para que Java no se congele
        new Thread(() -> {
            try {
                // Ejecuta tu Python usando tu entorno virtual (.venv)
                ProcessBuilder pb = new ProcessBuilder(".venv311\\Scripts\\python.exe", "src/python/main/DedosArriba.py");
                
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
}