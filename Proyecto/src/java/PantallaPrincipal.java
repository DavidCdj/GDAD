import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.JOptionPane;

public class PantallaPrincipal extends JFrame {
    //pantalla principal con botones para cada opción
    private JButton jbSimbolosValores, jbCirculoColores, jbCirculoFormas, jbDedosColores, jbDedosArriba, jbadministrador;
    //private Boolean frame1=true;
    ImageIcon icono= new ImageIcon(RutaAplicacion.recurso("settings.png").getAbsolutePath());
    Image img = icono.getImage();
    Image nuevaImg = img.getScaledInstance(30, 30,  java.awt.Image.SCALE_SMOOTH);     
    JPanel jpPrincipal=(JPanel)this.getContentPane();
    JPanel principal=new JPanel();
    private Estadisticas estadisticas;
    private static final String REGEX_CURP = "^[A-Z]{4}\\d{6}[HM][A-Z]{5}[A-Z0-9]\\d$";


    
    public PantallaPrincipal() {
        // Si no hay CURP registrado, pedir registro antes de mostrar el menú
        asegurarRegistro();

        // Migrar cualquier CSV antiguo a nuevo almacenamiento central
        try { GestorEstadisticas.migrarDesdeCSV(); } catch (Exception e) { e.printStackTrace(); }

        setTitle("Pantalla Principal");
        setSize(1100, 800);
        setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Componentes();
    }

    private void asegurarRegistro() {
        try {
            if (!GestorEstadisticas.tieneCurp()) {
                while (true) {
                    String curp = JOptionPane.showInputDialog(null, "Registro inicial\nIngrese su CURP:", "Registro de Usuario", JOptionPane.PLAIN_MESSAGE);
                    if (curp == null) {
                        // Usuario canceló; salimos de la aplicación
                        System.exit(0);
                    }
                    curp = curp.trim().toUpperCase();
                    if (esCurpValida(curp)) {
                        GestorEstadisticas.escribirCurp(curp);
                        break;
                    } else {
                        JOptionPane.showMessageDialog(
                            null,
                            "CURP invalida. Debe tener 18 caracteres y formato oficial.\nEjemplo: XXXX123456XXXXXX02",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean esCurpValida(String curp) {
        return curp != null && curp.matches(REGEX_CURP);
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
                SwingUtilities.invokeLater(() -> new AtencionDivididaControlada(PantallaPrincipal.this).setVisible(true));
                dispose();
            }
        });

        jbCirculoFormas.addActionListener(e -> {
            new PantallaTutorial(null, () -> {
                PantallaActividadFormas ventanaNueva = new PantallaActividadFormas( PantallaPrincipal.this);
                ventanaNueva.setVisible(true);
                dispose();
            });
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