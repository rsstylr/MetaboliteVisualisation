package view;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.JLayeredPane;

public class PathwayPanel extends JLayeredPane{

    private static BufferedImage image;

    public PathwayPanel() {
    	super();
    }
    
    public void setImage(BufferedImage img, int width, int height){
    	
    	Image scaledImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    	BufferedImage bimg = new BufferedImage(scaledImage.getWidth(null),
				scaledImage.getHeight(null),
				BufferedImage.TYPE_INT_RGB);
//				BufferedImage.TYPE_BYTE_GRAY);
    	bimg.createGraphics().drawImage(scaledImage, 0, 0, null);
    	image = bimg;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null);
    }

}