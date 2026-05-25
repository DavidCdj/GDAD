

public class ControlFormas {
    
    private TablerodeSimbolos tablero;
    private Estadisticas estadisticas;
    private int posicionActual;
    
    public ControlFormas() {
        tablero = new TablerodeSimbolos();
        estadisticas = new Estadisticas();
        posicionActual = 0;
    }
    
    public boolean verificarSeleccion(String formaSeleccionada) {
        String formaEsperada = tablero.getFormaEsperada(posicionActual);
        
        if (formaSeleccionada.equals(formaEsperada)) {
            posicionActual++;
            estadisticas.registrarAcierto();
            return true;
        } else {
            estadisticas.registrarError();
            return false;
        }
    }
    
    public boolean rondaCompleta() {
        return posicionActual >= 5;
    }
    
    public void siguienteRonda() {        
        tablero.avanzarRonda();
        posicionActual = 0;
    }
    
    public TablerodeSimbolos getTablero() {
        return tablero;
    }
    
    public Estadisticas getEstadisticas() {
        return estadisticas;
    }
    
    public int getPosicionActual() {
        return posicionActual;
    }
}