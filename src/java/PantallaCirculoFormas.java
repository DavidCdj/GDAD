import java.awt.*;
import javax.swing.*;

public class PantallaCirculoFormas extends JFrame {

    public PantallaCirculoFormas() {
        setTitle("Pantalla Principal");
        setSize(1100, 800);        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        Componentes();
    }

    private void Componentes() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(179, 226, 255));
        this.getContentPane().add(panel);
    }

    
}

