import javax.swing.ImageIcon;

public class Video {
    final ImageIcon video ;     

    public Video(String nVideo) {
        video = new ImageIcon("recursos/"+nVideo+".gif");         
    }

    public ImageIcon getVideo() {
        return video;
    }
    
}
