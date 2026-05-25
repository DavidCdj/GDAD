
import java.awt.*;
import java.io.File;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class PantallaAdministrador extends JFrame {

    private static final Color BG_MAIN    = new Color(0xFFF8F0);
    private static final Color AZUL_TITULO = new Color(0x3B4B8C);
    private static final Color ACENTO     = new Color(0xE07B39);
    private static final Color TEXT_COLOR = new Color(0x333333);

    private ControlAdministrador controlAdmin;
    private JPanel panelDatos;
    private JLabel labelEstado;

    public PantallaAdministrador() {
        controlAdmin = new ControlAdministrador();
        iniciarUI();
    }

    public PantallaAdministrador(Estadisticas estadisticas) {
        controlAdmin = new ControlAdministrador();
        controlAdmin.guardarEstadisticasActividad(estadisticas);
        iniciarUI();
    }

    private void iniciarUI() {
        setTitle("Panel de Administrador");
        setSize(1100, 700);
        setMinimumSize(new Dimension(980, 620));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_MAIN);

        // Encabezado
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AZUL_TITULO);
        header.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));
        JLabel titulo = new JLabel("Panel de Administrador");
        titulo.setFont(new Font("Georgia", Font.BOLD, 22));
        titulo.setForeground(Color.WHITE);
        header.add(titulo, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Panel central con tabla de datos (6 columnas)
        panelDatos = new JPanel();
        panelDatos.setLayout(new BoxLayout(panelDatos, BoxLayout.Y_AXIS));
        panelDatos.setBackground(BG_MAIN);
        panelDatos.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        actualizarDatos();

        JScrollPane scroll = new JScrollPane(panelDatos);
        scroll.setBackground(BG_MAIN);
        add(scroll, BorderLayout.CENTER);

        // Panel inferior
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBackground(BG_MAIN);
        panelInferior.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        labelEstado = new JLabel(" ", SwingConstants.CENTER);
        labelEstado.setFont(new Font("Georgia", Font.BOLD, 14));
        panelInferior.add(labelEstado, BorderLayout.NORTH);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        panelBotones.setBackground(BG_MAIN);

        JButton btnBajarDatos = new JButton("Bajar datos en Excel");
        btnBajarDatos.setFont(new Font("Georgia", Font.BOLD, 16));
        btnBajarDatos.setBackground(ACENTO);
        btnBajarDatos.setForeground(Color.WHITE);
        btnBajarDatos.setFocusPainted(false);
        btnBajarDatos.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBajarDatos.addActionListener(e -> bajarDatos());

        JButton btnRegresar = new JButton("Regresar al menú principal");
        btnRegresar.setFont(new Font("Georgia", Font.BOLD, 16));
        btnRegresar.setBackground(AZUL_TITULO);
        btnRegresar.setForeground(Color.WHITE);
        btnRegresar.setFocusPainted(false);
        btnRegresar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRegresar.addActionListener(e -> regresarMenuPrincipal());

        panelBotones.add(btnBajarDatos);
        panelBotones.add(btnRegresar);
        panelInferior.add(panelBotones, BorderLayout.CENTER);

        add(panelInferior, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void actualizarDatos() {
        panelDatos.removeAll();

        JLabel subtitulo = new JLabel("Estadisticas registradas:");
        subtitulo.setFont(new Font("Georgia", Font.BOLD, 16));
        subtitulo.setForeground(AZUL_TITULO);
        panelDatos.add(subtitulo);
        panelDatos.add(Box.createVerticalStrut(10));

        List<String[]> datos = controlAdmin.obtenerEstadisticas();

        if (datos.isEmpty()) {
            JLabel sinDatos = new JLabel("No hay estadisticas registradas aun.");
            sinDatos.setFont(new Font("Georgia", Font.PLAIN, 14));
            sinDatos.setForeground(TEXT_COLOR);
            panelDatos.add(sinDatos);
        } else {
            // Encabezados
            JPanel encabezado = crearFila(new String[]{"Fecha","Actividad","Aciertos","Errores","Tiempo","CURP"}, true);
            panelDatos.add(encabezado);

            for (String[] dato : datos) {
                JPanel fila = crearFila(dato, false);
                panelDatos.add(fila);
            }
        }

        panelDatos.revalidate();
        panelDatos.repaint();
    }

    private JPanel crearFila(String[] valores, boolean esEncabezado) {
        JPanel fila = new JPanel(new GridLayout(1, 6, 10, 0));
        fila.setBackground(esEncabezado ? AZUL_TITULO : BG_MAIN);
        fila.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        Color colorTexto = esEncabezado ? Color.WHITE : TEXT_COLOR;
        Font fuente = new Font("Georgia", esEncabezado ? Font.BOLD : Font.PLAIN, 13);

        for (int i = 0; i < 6; i++) {
            String texto = (i < valores.length) ? valores[i] : "";
            JLabel label = new JLabel(texto, SwingConstants.CENTER);
            label.setFont(fuente);
            label.setForeground(colorTexto);
            fila.add(label);
        }

        return fila;
    }

    private void bajarDatos() {
        JFileChooser chooser = new JFileChooser(RutaAplicacion.recurso("estadisticas.xlsx").getParentFile());
        chooser.setDialogTitle("Guardar archivo de estadísticas");
        chooser.setSelectedFile(new File("estadisticas.xlsx"));
        chooser.setFileFilter(new FileNameExtensionFilter("Excel (*.xlsx)", "xlsx"));

        int resultado = chooser.showSaveDialog(this);
        if (resultado != JFileChooser.APPROVE_OPTION) {
            labelEstado.setForeground(new Color(120, 120, 120));
            labelEstado.setText("Guardado cancelado.");
            return;
        }

        File archivo = chooser.getSelectedFile();
        String ruta = archivo.getAbsolutePath();
        if (!ruta.toLowerCase().endsWith(".xlsx")) {
            ruta += ".xlsx";
        }

        String rutaGenerada = controlAdmin.generarExcel(ruta);
        if (rutaGenerada != null) {
            labelEstado.setForeground(new Color(0, 150, 0));
            labelEstado.setText("Archivo guardado en: " + rutaGenerada);
        } else {
            labelEstado.setForeground(Color.RED);
            labelEstado.setText("Error al generar el archivo.");
        }
    }

    private void regresarMenuPrincipal() {
        PantallaPrincipal menu = new PantallaPrincipal();
        menu.setVisible(true);
        dispose();
    }
}