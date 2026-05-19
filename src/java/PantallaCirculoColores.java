import java.awt.*;
import javax.swing.*;

public class PantallaCirculoColores extends JFrame {
    private JFrame principal;
    private ControlColores control;

    private JLabel  texto;
    final JLabel [][] circulos = new JLabel[8][10];


    public PantallaCirculoColores(JFrame principal, ControlColores control) {
        this.principal = principal;
        this.control = control;
        setTitle("Atencion Dividida - Circulo de Colores");
        setSize(1100, 800);   
        
        setLayout(new GridLayout(1,2,20,0));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        this.getContentPane().setBackground(new Color(179, 226, 255)); 
        Componentes();
    }

    private void Componentes() {
        JPanel panel = new JPanel();
        //panel.setBackground(new Color(179, 226, 255));
        this.getContentPane().add(panel);

        

    }



    
}
