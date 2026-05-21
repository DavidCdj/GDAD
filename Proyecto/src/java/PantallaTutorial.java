

import java.awt.*;
import javax.swing.*;

public class PantallaTutorial extends JDialog {

    private static final Color BG_DARK     = new Color(10, 12, 30);
    private static final Color ACCENT_GOLD = new Color(255, 200, 50);
    private static final Color TEXT_LIGHT  = new Color(220, 230, 255);
    private static final Color ACCENT_BLUE = new Color(60, 140, 240);

    private final Runnable onFinished;
    private JLabel labelCuentaRegresiva;
    private Timer timer;
    private int segundos = 5;

    public PantallaTutorial(Frame parent, Runnable onFinished) {
        super(parent, "Tutorial - Circulos con Formas", true);
        this.onFinished = onFinished;
        mostrarBienvenida();
    }

    private void mostrarBienvenida() {
        setSize(800, 600);
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 20));
        root.setBackground(BG_DARK);
        root.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        setContentPane(root);

        JLabel titulo = new JLabel("Bienvenido al ejercicio", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titulo.setForeground(ACCENT_GOLD);
        root.add(titulo, BorderLayout.NORTH);

        JTextArea mensaje = new JTextArea(
            "¡Hola! Bienvenido a este ejercicio.\n\n" +
            "Verás 5 imágenes en la parte de arriba — esa es tu secuencia.\n\n" +
            "Usa los botones de abajo para repetir esa misma secuencia en el mismo orden.\n\n" +
            "¡Tómate tu tiempo, no hay prisa!"
        );
        mensaje.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        mensaje.setForeground(TEXT_LIGHT);
        mensaje.setBackground(BG_DARK);
        mensaje.setEditable(false);
        mensaje.setLineWrap(true);
        mensaje.setWrapStyleWord(true);
        root.add(mensaje, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new BorderLayout(0, 10));
        panelInferior.setBackground(BG_DARK);

        labelCuentaRegresiva = new JLabel("Continuando en: 5", SwingConstants.CENTER);
        labelCuentaRegresiva.setFont(new Font("Segoe UI", Font.BOLD, 20));
        labelCuentaRegresiva.setForeground(ACCENT_BLUE);
        panelInferior.add(labelCuentaRegresiva, BorderLayout.NORTH);

        JButton btnOmitir = new JButton("Omitir y ver demostración");
        btnOmitir.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnOmitir.setBackground(ACCENT_GOLD);
        btnOmitir.setForeground(Color.BLACK);
        btnOmitir.setFocusPainted(false);
        btnOmitir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnOmitir.addActionListener(e -> {
            if (timer != null) timer.stop();
            mostrarGif();
        });
        panelInferior.add(btnOmitir, BorderLayout.CENTER);
        root.add(panelInferior, BorderLayout.SOUTH);

        timer = new Timer(1000, e -> {
            segundos--;
            labelCuentaRegresiva.setText("Continuando en: " + segundos);
            if (segundos <= 0) {
                timer.stop();
                mostrarGif();
            }
        });
        timer.start();

        setVisible(true);
    }

    private void mostrarGif() {
        // Limpiar la pantalla actual
        getContentPane().removeAll();

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(BG_DARK);
        root.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        setContentPane(root);

        JLabel titulo = new JLabel("Observa como se realiza el ejercicio", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titulo.setForeground(ACCENT_GOLD);
        root.add(titulo, BorderLayout.NORTH);

        // Cargar el GIF
        String rutaGif = System.getProperty("user.dir") + "/recursos/Tutorial.gif";
        ImageIcon gif = new ImageIcon(rutaGif);
        JLabel labelGif = new JLabel(gif, SwingConstants.CENTER);
        root.add(labelGif, BorderLayout.CENTER);

        JButton btnContinuar = new JButton("Comenzar actividad");
        btnContinuar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnContinuar.setBackground(ACCENT_GOLD);
        btnContinuar.setForeground(Color.BLACK);
        btnContinuar.setFocusPainted(false);
        btnContinuar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnContinuar.addActionListener(e -> cerrarYContinuar());
        root.add(btnContinuar, BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    private void cerrarYContinuar() {
        dispose();
        if (onFinished != null) onFinished.run();
    }
}