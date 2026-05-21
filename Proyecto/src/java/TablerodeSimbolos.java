
import java.util.Random;

public class TablerodeSimbolos {
    
    // Las 5 formas posibles
    public static final String[] FORMAS = {
        "Tierra", "Sol", "Meteoro", "Cometa", "Astro"
    };
    
    private String[] secuenciaActual;
    private int rondaActual = 1;
    private int totalRondas = 5;
    private Random random = new Random();
    
    public TablerodeSimbolos() {
        generarNuevaSecuencia();
    }
    
    public void generarNuevaSecuencia() {
        secuenciaActual = new String[5];
        for (int i = 0; i < 5; i++) {
            secuenciaActual[i] = FORMAS[random.nextInt(FORMAS.length)];
        }
    }
    
    public String getFormaEsperada(int posicion) {
        return secuenciaActual[posicion];
    }
    
    public String[] getSecuenciaActual() {
        return secuenciaActual;
    }
    
    public int getRondaActual() {
        return rondaActual;
    }
    
    public int getTotalRondas() {
        return totalRondas;
    }
    
    public boolean hayMasRondas() {
        return rondaActual < totalRondas;
    }
    
    public void avanzarRonda() {
        if (hayMasRondas()) {
            rondaActual++;
            generarNuevaSecuencia();
        }
    }
}