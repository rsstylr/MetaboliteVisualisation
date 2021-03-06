package view;
import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JLabel;

class TransparentLabel extends JLabel {
	
	public TransparentLabel(String text) { 
	    super(text);
	    setOpaque(true); 
	} 
	   
	public void paint(Graphics g) { 
	    Graphics2D g2 = (Graphics2D) g.create(); 
	    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f)); 
	    super.paint(g2); 
	    g2.dispose(); 
	} 
}
