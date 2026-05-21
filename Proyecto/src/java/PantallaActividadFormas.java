import java.awt.*; 
import javax.swing.*;
import javax.swing.border.*;

public class PantallaActividadFormas extends JFrame {
    final JFrame principal;

    // ── Paleta de colores ─────────────────────────────────────────────────────
    private static final Color BG_MAIN       = new Color(0xFFF8F0);   // crema cálido
    private static final Color BG_PANEL      = new Color(0xFFFDF8);   // blanco cálido
    private static final Color BG_BOTONES    = new Color(0xFFF0E0);   // melocotón suave
    private static final Color ACENTO        = new Color(0xE07B39);   // naranja terracota
    private static final Color ACENTO_HOVER  = new Color(0xC5622A);
    private static final Color AZUL_TITULO   = new Color(0x3B4B8C);   // azul pizarrón
    private static final Color VERDE_OK      = new Color(0x2E7D32);
    private static final Color ROJO_ERROR    = new Color(0xC62828);
    private static final Color BORDE_CASILLA = new Color(0xC9B99A);
    private static final Color CASILLA_ACTIVA = new Color(0xFFF176);  // amarillo suave
    private static final Color CASILLA_OK    = new Color(0xA5D6A7);   // verde menta
    private static final Color CASILLA_BASE  = new Color(0xFFF8F0);

    private final ControlFormas control;
    private JPanel panelReferencia;
    private JPanel panelRespuesta;
    private JPanel panelBotones;
    private JLabel labelMensaje;
    private JLabel labelRonda;
    private Runnable onActividadTerminada;

    public PantallaActividadFormas(JFrame principal) {
        this.principal = principal;
        control = new ControlFormas();
        iniciarUI();
    }
    // ── Carga y escala imágenes ───────────────────────────────────────────────
    private ImageIcon cargarImagen(String nombre, int size) {
        try {
            String ruta = System.getProperty("user.dir") + "/recursos/" + nombre + ".png";
            ImageIcon icono = new ImageIcon(ruta);
            Image img = icono.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) {
            System.out.println("Error al cargar la imagen: " + nombre);
            return null;
        }
    }

    private ImageIcon cargarImagen(String nombre) {
        return cargarImagen(nombre, 72);
    }

    // ── Construcción de la interfaz ───────────────────────────────────────────
    private void iniciarUI() {
        setTitle("Círculos con Formas");
        setSize(960, 700);
        setMinimumSize(new Dimension(800, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BG_MAIN);

        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearPanelCentral(), BorderLayout.CENTER);
        add(crearPanelInferior(), BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── Encabezado ────────────────────────────────────────────────────────────
    private JPanel crearEncabezado() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AZUL_TITULO);
        header.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));

        JLabel titulo = new JLabel("Círculos con Formas");
        titulo.setFont(new Font("Georgia", Font.BOLD, 26));
        titulo.setForeground(Color.WHITE);

        labelRonda = new JLabel("Ronda 1 de 5");
        labelRonda.setFont(new Font("Georgia", Font.PLAIN, 20));
        labelRonda.setForeground(new Color(0xFFD9A8));
        labelRonda.setHorizontalAlignment(SwingConstants.RIGHT);

        header.add(titulo, BorderLayout.WEST);
        header.add(labelRonda, BorderLayout.EAST);
        return header;
    }

    // ── Panel central (referencia + respuesta) ────────────────────────────────
    private JPanel crearPanelCentral() {
        JPanel central = new JPanel(new GridLayout(2, 1, 0, 16));
        central.setBackground(BG_MAIN);
        central.setBorder(BorderFactory.createEmptyBorder(20, 28, 12, 28));

        panelReferencia = crearPanelSecuencia();
        panelReferencia.setBorder(crearBordeTitulo("Secuencia a seguir", AZUL_TITULO));
        actualizarReferencia();

        panelRespuesta = crearPanelSecuencia();
        panelRespuesta.setBorder(crearBordeTitulo("Tu respuesta", ACENTO));
        iniciarPanelRespuesta();

        central.add(panelReferencia);
        central.add(panelRespuesta);
        return central;
    }
    private JPanel crearPanelSecuencia() {
        JPanel p = new JPanel(new GridLayout(1, 5, 12, 0));
        p.setBackground(BG_PANEL);
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return p;
    }
    // ── Panel inferior (mensaje + botones) ───────────────────────────────────
    private JPanel crearPanelInferior() {
        JPanel inferior = new JPanel(new BorderLayout(0, 0));
        inferior.setBackground(BG_MAIN);
        inferior.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        labelMensaje = new JLabel(" ", SwingConstants.CENTER);
        labelMensaje.setFont(new Font("Georgia", Font.BOLD, 20));
        labelMensaje.setPreferredSize(new Dimension(0, 42));
        inferior.add(labelMensaje, BorderLayout.NORTH);

        panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 14));
        panelBotones.setBackground(BG_BOTONES);
        panelBotones.setBorder(crearBordeTitulo("Elige un símbolo", ACENTO));
        crearBotones();
        inferior.add(panelBotones, BorderLayout.CENTER);

        return inferior;
    }

    // ── Actualización de paneles ──────────────────────────────────────────────
    private void actualizarReferencia() {
        panelReferencia.removeAll();
        String[] secuencia = control.getTablero().getSecuenciaActual();
        for (String forma : secuencia) {
            panelReferencia.add(crearCasilla(cargarImagen(forma), false));
        }
        panelReferencia.revalidate();
        panelReferencia.repaint();
    }

    private void iniciarPanelRespuesta() {
        panelRespuesta.removeAll();
        for (int i = 0; i < 5; i++) {
            boolean activa = (i == control.getPosicionActual());
            JLabel label = crearCasillaRespuesta("?", activa);
            panelRespuesta.add(label);
        }
        panelRespuesta.revalidate();
        panelRespuesta.repaint();
    }

    private void crearBotones() {
        panelBotones.removeAll();
        for (String forma : TablerodeSimbolos.FORMAS) {
            JButton boton = new BotonRedondeado(cargarImagen(forma, 70));
            boton.setToolTipText(forma);
            boton.addActionListener(e -> manejarSeleccion(forma));
            panelBotones.add(boton);
        }
        panelBotones.revalidate();
        panelBotones.repaint();
    }

    // ── Lógica de selección ───────────────────────────────────────────────────
    private void manejarSeleccion(String forma) {
        boolean correcto = control.verificarSeleccion(forma);

        if (correcto) {
            mostrarMensaje("¡Muy bien!", VERDE_OK);
            actualizarCasilla(control.getPosicionActual() - 1, forma, true);

            if (control.rondaCompleta()) {
                if (control.getTablero().hayMasRondas()) {
                    control.siguienteRonda();
                    labelRonda.setText("Ronda " + control.getTablero().getRondaActual() + " de 5");
                    mostrarMensaje("¡Excelente! Avancemos a la siguiente ronda", AZUL_TITULO);
                    actualizarReferencia();
                    iniciarPanelRespuesta();
                } else {
                    Timer cierre = new Timer(2000, e -> 
                        {
                            mostrarMensaje("¡Excelente trabajo, terminamos la actividad!", VERDE_OK);
                            deshabilitarBotones();
                            // Guardar estadisticas de la actividad
                            try {
                                Estadisticas st = control.getEstadisticas();
                                st.setActividad("Circulos con Formas");
                                GestorEstadisticas.appendEstadistica(st);
                            } catch (Exception ex) {
                                System.out.println("Error guardando estadisticas: " + ex.getMessage());
                            }
                            dispose();                                     
                            principal.setVisible(true);
                            
                        });
                        cierre.setRepeats(false);
                        cierre.start();                    
                    if (onActividadTerminada != null) {
                    onActividadTerminada.run();
                    }
                }
            }
        } else {
            int casilla = control.getPosicionActual() + 1;
            mostrarMensaje("¡Cuidado, error en la casilla " + casilla + "! Inténtalo de nuevo", ROJO_ERROR);
        }
    }

    private void mostrarMensaje(String texto, Color color) {
        labelMensaje.setForeground(color);
        labelMensaje.setText(texto);
    }

    private void actualizarCasilla(int posicion, String forma, boolean correcto) {
        Component comp = panelRespuesta.getComponent(posicion);
        if (comp instanceof JLabel label) {
            label.setText("");
            label.setIcon(cargarImagen(forma));
            label.setBackground(correcto ? CASILLA_OK : new Color(0xEF9A9A));
        }
        if (posicion + 1 < 5) {
            Component sig = panelRespuesta.getComponent(posicion + 1);
            if (sig instanceof JLabel l) l.setBackground(CASILLA_ACTIVA);
        }
        panelRespuesta.revalidate();
        panelRespuesta.repaint();
    }

    private void deshabilitarBotones() {
        for (Component c : panelBotones.getComponents()) c.setEnabled(false);
    }

    // ── Helpers de UI ─────────────────────────────────────────────────────────
    private JLabel crearCasilla(ImageIcon icono, boolean activa) {
        JLabel label = new JLabel(icono, SwingConstants.CENTER);
        label.setBackground(activa ? CASILLA_ACTIVA : CASILLA_BASE);
        label.setOpaque(true);
        label.setBorder(new RoundedBorder(14, BORDE_CASILLA, 2));
        label.setPreferredSize(new Dimension(90, 90));
        return label;
    }

    private JLabel crearCasillaRespuesta(String texto, boolean activa) {
        JLabel label = new JLabel(texto, SwingConstants.CENTER);
        label.setFont(new Font("Georgia", Font.BOLD, 28));
        label.setForeground(new Color(0xA0896B));
        label.setBackground(activa ? CASILLA_ACTIVA : CASILLA_BASE);
        label.setOpaque(true);
        label.setBorder(new RoundedBorder(14, BORDE_CASILLA, 2));
        label.setPreferredSize(new Dimension(90, 90));
        return label;
    }

    private TitledBorder crearBordeTitulo(String titulo, Color colorTitulo) {
        TitledBorder border = BorderFactory.createTitledBorder(
            new LineBorder(colorTitulo, 2, true),
            titulo,
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Georgia", Font.BOLD, 15),
            colorTitulo
        );
        return border;
    }

    // ── Componentes personalizados ────────────────────────────────────────────
    
    private class BotonRedondeado extends JButton {
        private boolean hover = false;

        public BotonRedondeado(ImageIcon icono) {
            super(icono);
            setPreferredSize(new Dimension(110, 110));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    hover = true; repaint();
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    hover = false; repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int arc = 22;

            // Sombra suave
            if (isEnabled()) {
                g2.setColor(new Color(0, 0, 0, hover ? 40 : 20));
                g2.fillRoundRect(3, 5, w - 6, h - 6, arc, arc);
            }

            // Fondo del botón
            if (!isEnabled()) {
                g2.setColor(new Color(0xE0D8CC));
            } else if (hover) {
                g2.setColor(new Color(0xFFE0C0));
            } else {
                g2.setColor(Color.WHITE);
            }
            g2.fillRoundRect(1, 1, w - 4, h - 4, arc, arc);

            // Borde
            g2.setStroke(new BasicStroke(hover ? 2.5f : 2f));
            g2.setColor(hover ? ACENTO_HOVER : ACENTO);
        g2.drawRoundRect(1, 1, w - 4, h - 4, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;
        private final int thickness;

        public RoundedBorder(int radius, Color color, int thickness) {
            this.radius = radius;
            this.color = color;
            this.thickness = thickness;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x + 1, y + 1, w - 3, h - 3, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }
    }
    public void setOnActividadTerminada(Runnable callback) {
    this.onActividadTerminada = callback;    
}

    public Estadisticas getEstadisticas() {        
    return control.getEstadisticas();
}
}