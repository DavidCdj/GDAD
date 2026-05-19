import java.awt.*;
import javax.swing.*;

public class PantallaActividadValores extends JFrame {
    private ControlValores control = new ControlValores();
    final JFrame principal;
    //final Integer[] respuestas = new Integer[60];    
    //ImageIcon video = new ImageIcon("video.gif"); 
    JLabel visor;

    
    final JLabel [][] simbolosNumeros= new JLabel[2][9];    
    final JLabel [][] simbolos = new JLabel[4][15];
    final JTextField [][] numeros = new JTextField[4][15];
    private JLabel  texto;

    public PantallaActividadValores(ControlValores control, JFrame principal) {
        this.control = control;
        this.principal = principal;
        setTitle("Atencion Dividida - Simbolos con Valores");
        setSize(1100, 800);       
        //agregados
        setLayout(new GridLayout(1,2,20,0));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        this.getContentPane().setBackground(new Color(179, 226, 255));
        visor = new JLabel(control.getVideo().getVideo());
        Componentes();
    }

    private void Componentes() {
        JPanel panel = new JPanel();
        //panel.setBackground(new Color(179, 226, 255));
        this.getContentPane().add(panel);
        
        try {
            visor.setBounds(125, 50, 800, 600); 
            add(visor);
            temporizador.start(); 
        } catch (Exception e) {
            System.out.println("No se pudo cargar el GIF: " + e.getMessage());
        }        
        
        // tablero muestra 
        control.generarPatrones();       
            for(int j=0; j<9; j++){
                simbolosNumeros[0][j]= new JLabel(control.getTablero()[j].simbolo);
                simbolosNumeros[0][j].setBounds( 300+(j*50), 100, 50, 50);
                simbolosNumeros[0][j].setBorder(BorderFactory.createLineBorder(Color.BLACK));                
                simbolosNumeros[0][j].setForeground(Color.BLACK);
                
                simbolosNumeros[1][j]= new JLabel(Integer.toString(control.getTablero()[j].valor), SwingConstants.CENTER);
                simbolosNumeros[1][j].setBounds( 300+(j*50), 150, 50, 50);
                simbolosNumeros[1][j].setFont(new Font("Arial", Font.BOLD, 24));
                simbolosNumeros[1][j].setBorder(BorderFactory.createLineBorder(Color.BLACK));                
                simbolosNumeros[1][j].setForeground(Color.BLACK);
                add(simbolosNumeros[1][j]);
                add(simbolosNumeros[0][j]);
            }
        
        texto = new JLabel("Completa los numeros en el siguiente tablero.", SwingConstants.CENTER);
        texto.setBounds(200, 200, 600, 50);
        texto.setFont(new Font("Arial", Font.BOLD, 16));
        texto.setForeground(Color.BLACK);
        add(texto);
        //tablero juego
        control.generarSecuenciaJuego();            
        int ysimbolo=300;
        int ynumero=350;            
        for(int i=0; i<4; i++){
            for(int j=0; j<15; j++){
                final int indiceRespuesta = i*15+j;                
                int idSimbolo = control.getRespuestaEn(indiceRespuesta);
                simbolos[i][j]= new JLabel(control.getTablero()[idSimbolo-1].simbolo);
                simbolos[i][j].setBounds( 150+(j*50), ysimbolo+(i*50), 50, 50);
                simbolos[i][j].setBorder(BorderFactory.createLineBorder(Color.BLACK));

                
                numeros[i][j]= new JTextField();
                numeros[i][j].setHorizontalAlignment(JTextField.CENTER);
                numeros[i][j].setFont(new Font("Arial", Font.BOLD, 24));
                numeros[i][j].setBorder(BorderFactory.createLineBorder(Color.BLACK));
                numeros[i][j].setBackground(Color.WHITE);
                numeros[i][j].setBounds( 150+(j*50), ynumero+(i*50), 50, 50);
                add(simbolos[i][j]);
                add(numeros[i][j]);
                configurarEventoValidacion(i, j);
            }
            ysimbolo+=50;
            ynumero+=50;
        }
    //numeros[0][0].requestFocus();
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
    
    private void configurarEventoValidacion(int f, int c) {
        JTextField campoActual = numeros[f][c]; 
        
        campoActual.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if(campoActual.isEditable()) {
                    campoActual.setBackground(new Color(255, 255, 180)); // Azul claro para enfoque
                }   
            }
            
        });

        numeros[f][c].addKeyListener(new java.awt.event.KeyAdapter() {
        @Override
        public void keyReleased(java.awt.event.KeyEvent evt){
            //JTextField campoActual = numeros[f][c];            
            campoActual.setForeground(Color.BLACK);            
            String input =campoActual.getText();
            if(!input.isEmpty()){
                try{
                    int valor = Integer.parseInt(input);
                    //int respuestaCorrecta = control.getRespuestaEn(f*15+c);
                    if(control.verficarRespuesta(f*15+c, valor)){
                        campoActual.setBackground(new Color(144, 238, 144)); // Verde claro para acierto
                        campoActual.setEditable(false);
                        new ValidarRespuesta(PantallaActividadValores.this, "¡Muy bien!", 1000);
                        int siguienteColumna = (c + 1) % 15;
                        int siguienteFila = f + (c + 1) / 15;
                        if(siguienteFila < 4) {
                            numeros[siguienteFila][siguienteColumna].requestFocus();
                        }else{
                            
                                new ValidarRespuesta(PantallaActividadValores.this, "¡Has completado el tablero!", 5000);
                                Timer cierre = new Timer(2000, e -> {
                                    Estadisticas stats = control.getEstadisticas();
                                    stats.setActividad("Simbolos con Valores");
                                    
                                    control.guardarProgreso(stats); 
                                    
                                    // 2. Cerrar esta pantalla
                                    dispose(); 
                                    
                                    // 3. Hacer visible la principal (si guardaste la referencia)
                                    principal.setVisible(true);
                                });
                                cierre.setRepeats(false);
                                cierre.start();
                                
                            }                                    
                    }else{                                    
                            new ValidarRespuesta(PantallaActividadValores.this, "Error, intenta de nuevo.", 1000);
                            campoActual.setBackground(new Color(255, 182, 193)); // Rosa claro para error
                            campoActual.setEditable(false);
                            Timer pausError = new Timer(800, e -> {
                                campoActual.setBackground(new Color(255, 255, 180));
                                campoActual.setEditable(true); 
                                campoActual.setText("");
                                campoActual.requestFocus();
                                //campoActual.requestFocus(false);
                                ((Timer)e.getSource()).stop();                                
                            });
                            pausError.start();                                                               
                        }
                    }catch(NumberFormatException ex){
                    campoActual.setText("");                    
                        }
                    }
                }
            }); 
    }
    
}
