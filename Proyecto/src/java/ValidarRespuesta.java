//de igual manera que la clase de estadisticas, esta clase se encargara de mostrar un mensaje de acierto o error al usuario, se mostrara por un tiempo determinado y luego se cerrara automaticamente
import java.awt.*;
import javax.swing.*;

public class ValidarRespuesta extends JDialog {
    public ValidarRespuesta(Window padre, String mensaje, int duracionMs) {
        super(padre); // 'false' para que no bloquee la pantalla principal
        setUndecorated(true); // Quita la barra de título y botones de cerrar
        setLayout(new BorderLayout());
        
        // Estilo del panel
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(50, 50, 50)); // Fondo oscuro
        panel.setBorder(BorderFactory.createLineBorder(new Color(179, 226, 255), 2));
        
        JLabel texto = new JLabel(mensaje, SwingConstants.CENTER);
        texto.setForeground(Color.WHITE);
        texto.setFont(new Font("Arial", Font.BOLD, 18));
        texto.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        panel.add(texto, BorderLayout.CENTER);
        add(panel);
        pack();
        
        
        setLocationRelativeTo(padre);
        
        
        Timer timer = new Timer(duracionMs, e -> {
            this.dispose();
        });
        timer.setRepeats(false);
        timer.start();

        pack();
        setLocationRelativeTo(padre);
        setVisible(true);
    }
}
