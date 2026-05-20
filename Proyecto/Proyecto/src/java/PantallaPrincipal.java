import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
//import javax.swing.border.Border;
//import com.formdev.flatlaf.FlatDarkLaf;    

public class PantallaPrincipal extends JFrame {

    //pantalla principal con botones para cada opción
    private JButton jbSimbolosValores, jbCirculoColores, jbCirculoFormas, jbDedosColores, jbDedosArriba, jbadministrador;
    //private Boolean frame1=true;
    ImageIcon icono= new ImageIcon("recursos/settings.png");
    Image img = icono.getImage();
    Image nuevaImg = img.getScaledInstance(30, 30,  java.awt.Image.SCALE_SMOOTH);     
    JPanel jpPrincipal=(JPanel)this.getContentPane();
    JPanel principal=new JPanel();


    
    public PantallaPrincipal() {
        setTitle("Pantalla Principal");
        setSize(1100, 800);
        setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Componentes();
    }

    private void Componentes() {
        principal.setLayout(null);
        principal.setBackground(new Color(179, 226, 255));
        jpPrincipal.add(principal);

        jbSimbolosValores = new JButton("Simbolos con Valores");
        jbSimbolosValores.setForeground(Color.BLACK);
        jbSimbolosValores.setBackground(new Color(255, 246, 179));
        jbSimbolosValores.setBounds(450, 120, 180, 30);
        principal.add(jbSimbolosValores);

        jbCirculoColores = new JButton("Circulo de Colores");
        jbCirculoColores.setForeground(Color.BLACK);
        jbCirculoColores.setBackground(new Color(255, 246, 179));
        jbCirculoColores.setBounds(450, 230, 180, 30);
        principal.add(jbCirculoColores);

        jbCirculoFormas = new JButton("Circulo con Formas");
        jbCirculoFormas.setForeground(Color.BLACK);
        jbCirculoFormas.setBackground(new Color(255, 246, 179));
        jbCirculoFormas.setBounds(450, 330, 180, 30);
        principal.add(jbCirculoFormas);

        jbDedosColores = new JButton("Dedos de Colores");
        jbDedosColores.setForeground(Color.BLACK);
        jbDedosColores.setBackground(new Color(255, 246, 179));
        jbDedosColores.setBounds(450, 560, 180, 30);
        principal.add(jbDedosColores);

        jbDedosArriba = new JButton("Dedos Arriba");
        jbDedosArriba.setForeground(Color.BLACK);
        jbDedosArriba.setBackground(new Color(255, 246, 179));
        jbDedosArriba.setBounds(450, 450, 180, 30);
        principal.add(jbDedosArriba);

        icono = new ImageIcon(nuevaImg); 
        jbadministrador = new JButton(icono);
        jbadministrador.setBackground(new Color(179, 226, 255));
        jbadministrador.setBounds(10, 720, 35, 35);
        jbadministrador.setBorderPainted(false);
        principal.add(jbadministrador);
        
        
        //acciones de los botones
        jbSimbolosValores.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                PantallaActividadValores ventanaNueva = new PantallaActividadValores(new ControlValores(), PantallaPrincipal.this);
                ventanaNueva.setVisible(true);
                dispose();
            }
        });

        jbCirculoColores.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                PantallaCirculoColores ventanaNueva = new PantallaCirculoColores(PantallaPrincipal.this, new ControlColores());
                ventanaNueva.setVisible(true);
                dispose();
            }
        });

        jbCirculoFormas.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                PantallaCirculoFormas ventanaNueva = new PantallaCirculoFormas();
                ventanaNueva.setVisible(true);
                dispose();
            }
        });

        //dedos de colores es especial porque se ejecuta un script de Python, así que lo manejamos con un hilo para no congelar la interfaz
        jbDedosColores.addActionListener(e-> {
            this.setVisible(false); // Ocultamos el menú principal mientras se ejecuta Python
            ControlDedosColores controlador = new ControlDedosColores(PantallaPrincipal.this);
            new Thread(() -> {
                controlador.iniciarActividad();
            }).start();

        });

        jbDedosArriba.addActionListener(e -> {

            this.setVisible(false); // Ocultamos el menú principal mientras se ejecuta Python
            ControlDedosArriba controlador = new ControlDedosArriba(PantallaPrincipal.this);
            new Thread(() -> {
                controlador.iniciarActividad();
            }).start();
        });

            jbadministrador.addActionListener(new ActionListener() {
                
                public void actionPerformed(ActionEvent e) {
                    PantallaAdministrador ventanaNueva = new PantallaAdministrador();
                    ventanaNueva.setVisible(true);
                    dispose();
                }
            });
    }
}