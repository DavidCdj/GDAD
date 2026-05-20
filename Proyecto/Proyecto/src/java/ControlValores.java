//Control
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;


public class ControlValores{
    final TablerodePatrones[] tablero;
    final Estadisticas estadisticas;
    private Video video; 
    //private TablerodePatrones[] tableroReferencia;
    final Integer[] respuestasCorrectas = new Integer[60];
    
    public ControlValores() {
        this.tablero = new TablerodePatrones[9];
        this.estadisticas = new Estadisticas();
        video = new Video("VideoValores");
            
        //generarPatrones();
    }

    public void generarPatrones() {
        List<Integer> numeros = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            numeros.add(i);
        }

        // 2. Desordenar la lista aleatoriamente
        Collections.shuffle(numeros);

        for(int i=0; i<9; i++){
            int numero = numeros.get(i);
            ImageIcon simbolo = new ImageIcon("recursos/"+(i+1)+".png");
            Image img = simbolo.getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT);
            simbolo = new ImageIcon(img);            
            tablero[numero-1] = new TablerodePatrones(simbolo, numero);
        }
    }

    public boolean verficarRespuesta(int indice, int respuesta) {
        if (respuestasCorrectas[indice] == respuesta) {
            estadisticas.setAciertos(estadisticas.getAciertos() + 1);
            return true;
        } else {
            estadisticas.setErrores(estadisticas.getErrores() + 1);
            return false;
        }
    }

    public void generarSecuenciaJuego() {
        for (int i = 0; i < 60; i++) {            
            int rand = (int) (Math.random() * 9) + 1;
            respuestasCorrectas[i] = rand;
        }
    }

    public void guardarProgreso(Estadisticas estadisticas) {
            File archivo = new File("Estadisticas.csv");
        boolean existe = archivo.exists();

        try (PrintWriter pw = new PrintWriter(new FileWriter(archivo, true))) {
            // Si el archivo es nuevo, ponemos los encabezados
            if (!existe) {
                pw.println("Fecha,Actividad,Aciertos,Errores");
            }
            // Insertamos los datos
            pw.printf("%s,%s,%d,%d%n", 
                estadisticas.getFecha(), 
                estadisticas.getActividad(), 
                estadisticas.getAciertos(), 
                estadisticas.getErrores());
                
        } catch (IOException e) {
            System.err.println("Error al guardar: " + e.getMessage());
        }

    }

    public Video getVideo() {
        return video;
    }

    public Integer getRespuestaEn(int indice) {
        return respuestasCorrectas[indice];
    }

    public TablerodePatrones[] getTablero() {
        return tablero;
    }

    public Estadisticas getEstadisticas() {
        return estadisticas;
    }
    
    
}