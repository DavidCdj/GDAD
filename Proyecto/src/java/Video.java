import javax.swing.ImageIcon;

public class Video {
    final ImageIcon video ;     

    public Video(String nVideo) {
        video = new ImageIcon(RutaAplicacion.recurso(nVideo + ".gif").getAbsolutePath());         
    }

    public ImageIcon getVideo() {
        return video;
    }
    
}
