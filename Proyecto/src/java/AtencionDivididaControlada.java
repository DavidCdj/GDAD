import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

public class AtencionDivididaControlada extends JFrame {
    private final Frame principal;
    private Estadisticas estadisticas;
    private static final Color BG_MAIN = new Color(179, 226, 255);
    JLabel visor;

    private final Color[] secuencia = {
        Color.RED, 
        new Color(50, 120, 255), // Azul
        new Color(0, 180, 80),   // Verde
        Color.ORANGE, 
        Color.YELLOW
    };
    
    private int siguienteColorIndice = 0; // Controla qué color toca en la secuencia global
    private final int FILAS = 8;
    private final int COLUMNAS = 10;
    private final int TOTAL_CIRCULOS = FILAS * COLUMNAS;

    public AtencionDivididaControlada( Frame principal) {

        visor = new JLabel(new ImageIcon("recursos/VideoCirculoColores.gif"));
        

        try {
            visor.setBounds(0, 0, 700, 800); 
            add(visor);
            temporizador.start(); 
        } catch (Exception e) {
            System.out.println("No se pudo cargar el GIF: " + e.getMessage());
        } 
        
        estadisticas = new Estadisticas();
        this.principal = principal;        
        setTitle("Atención Dividida - Modo Estricto");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_MAIN);
        

        // Encabezado basado en image_dadfb8.png
        JPanel panelTexto = new JPanel(new GridLayout(3, 1));
        panelTexto.setBackground(BG_MAIN);
        
        JLabel titulo = new JLabel("ATENCIÓN DIVIDIDA", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 28));
        
        JLabel instruccion1 = new JLabel("Colorea los círculos siguiendo la secuencia. ", SwingConstants.CENTER);
        JLabel instruccion2 = new JLabel("Si te equivocas, deberás reintentar el color correcto.", SwingConstants.CENTER);
        
        panelTexto.add(titulo);
        panelTexto.add(instruccion1);
        panelTexto.add(instruccion2);        

        JPanel panelCirculos = new JPanel(new GridLayout(FILAS, COLUMNAS, 10, 10));
        panelCirculos.setBackground(BG_MAIN);
        panelCirculos.setBorder(BorderFactory.createEmptyBorder(20, 40, 40, 40));

        // Crear los círculos
        for (int i = 0; i < TOTAL_CIRCULOS; i++) {
            panelCirculos.add(new CirculoBoton(i));
        }        

        add(panelTexto, BorderLayout.NORTH);
        add(panelCirculos, BorderLayout.CENTER);

        setSize(700, 800);        
        setLocationRelativeTo(null);
        
    }
    

    private class CirculoBoton extends JPanel {
        private Color colorActual = Color.WHITE;
        private final int posicion;
        private boolean coloreado = false;

        public CirculoBoton(int pos) {
            this.posicion = pos;
            setPreferredSize(new Dimension(50, 50));
            setBackground(BG_MAIN);

            // Los primeros 5 ya están definidos por la imagen image_dadfb8.png
            if (pos < 5) {
                colorActual = secuencia[pos % 5];
                coloreado = true;
                // Si ya están los 5, el siguiente que toca es el índice 0 (Rojo)
                if(pos == 4) siguienteColorIndice = 0; 
            }

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    gestionarClick();
                }
            });
        }

        private void gestionarClick() {
            // Solo actuar si el círculo está vacío y es el siguiente en orden
            if (!coloreado) {
                // Comprobamos si el usuario intenta colorear fuera de orden (opcional)
                // Para este ejercicio, permitimos que el usuario elija el color al hacer click
                
                String[] opciones = {"Rojo", "Azul", "Verde", "Naranja", "Amarillo"};
                int seleccion = JOptionPane.showOptionDialog(null, 
                        "¿Qué color sigue?", "Selecciona Color",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, 
                        null, opciones, opciones[0]);

                if (seleccion != -1) {
                    if (seleccion == siguienteColorIndice) {
                        // Acierto
                        colorActual = secuencia[seleccion];
                        coloreado = true;
                        siguienteColorIndice = (siguienteColorIndice + 1) % 5;
                        estadisticas.registrarAcierto();
                        repaint();
                    } else {
                        // Error
                        JOptionPane.showMessageDialog(null, 
                            "¡Error! Ese no es el color que sigue en la secuencia. Intenta de nuevo.", 
                            "Error de Secuencia", 
                            JOptionPane.ERROR_MESSAGE);
                        estadisticas.registrarError();
                    }
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2d.setColor(colorActual);
            g2d.fillOval(5, 5, getWidth() - 10, getHeight() - 10);
            
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawOval(5, 5, getWidth() - 10, getHeight() - 10);
        }
    }

    public  Estadisticas getEstadisticas() {
        return estadisticas;
    }

    public void setEstadisticas( Estadisticas estadisticas) {
        this.estadisticas = estadisticas;        
    }

    private static class ImageInputStreamHelper {
        static void cargarGif(String rutaGif, List<BufferedImage> frames) throws Exception {
            try (javax.imageio.stream.ImageInputStream stream = ImageIO.createImageInputStream(new File(rutaGif))) {
                if (stream == null) {
                    return;
                }
                java.util.Iterator<javax.imageio.ImageReader> readers = ImageIO.getImageReaders(stream);
                if (!readers.hasNext()) {
                    return;
                }
                javax.imageio.ImageReader reader = readers.next();
                try {
                    reader.setInput(stream);
                    int total = reader.getNumImages(true);
                    for (int i = 0; i < total; i++) {
                        frames.add(reader.read(i));
                    }
                } finally {
                    reader.dispose();
                }
            }
        }
    }
    Timer temporizador = new Timer(14000, e -> {    
    this.remove(visor); 
    JLabel mensaje = new JLabel(" ", SwingConstants.CENTER);
    mensaje.setBounds(100, 50, 600, 400);
    this.add(mensaje); 
    this.revalidate();
    this.repaint();        
    ((Timer)e.getSource()).stop();
    });
}