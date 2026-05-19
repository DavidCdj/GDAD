//package com.tuproyecto.actividadDedos;

//import com.tuproyecto.persistencia.GestorExcel;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.swing.JFrame;

public class ControlDedosColores {
    
    public void ejecutarActividadPython(JFrame menuPrincipal) {
        // Inicializamos las variables que vamos a recuperar de Python
        int aciertos = 0;
        int errores = 0;
        
        try {
            // Ajusta la ruta a donde vayas a guardar tu archivo de Python
            // Si usas entorno virtual, recuerda poner la ruta del python del venv
            ProcessBuilder pb = new ProcessBuilder("python", "src/python/DedosColores.py");
            pb.redirectErrorStream(true); // Para ver los prints de Python en la consola de VS Code
            
            Process proceso = pb.start();
            
            // Leemos lo que Python imprima en la consola
            BufferedReader reader = new BufferedReader(new InputStreamReader(proceso.getInputStream()));
            String linea;
            
            while ((linea = reader.readLine()) != null) {
                System.out.println("[Python Core] " + linea);
                
                // Un truco limpio: si Python imprime algo como "RESULTADO:5,2", lo atrapamos
                if (linea.startsWith("RESULTADO:")) {
                    String[] datos = linea.replace("RESULTADO:", "").split(",");
                    aciertos = Integer.parseInt(datos[0].trim());
                    errores = Integer.parseInt(datos[1].trim());
                }
            }
            
            // Esperamos a que el proceso de Python muera físicamente
            int codigoSalida = proceso.waitFor();
            
            if (codigoSalida == 0) {
                // Guardamos en el mismo archivo CSV/Excel que ya estructuramos antes
                // GestorExcel.registrarActividad("Dedos de Colores", aciertos, errores);
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            // Pase lo que pase, al terminar Python, el menú principal de Java vuelve a aparecer
            menuPrincipal.setVisible(true);
        }
    }
}