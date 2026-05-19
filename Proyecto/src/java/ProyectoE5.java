import com.formdev.flatlaf.FlatDarkLaf;

public class ProyectoE5 {
    public static void main(String[] args) {
        try {            
            FlatDarkLaf.setup();
        } catch( Exception ex ) {
            System.err.println( "Error al iniciar FlatLaf" );
        }
        PantallaPrincipal fr = new PantallaPrincipal();
        fr.setVisible(true);
        
    }    
}
