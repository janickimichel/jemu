import java.awt.*;
import java.awt.image.BufferedImage;

abstract class Video extends Device
{
	protected BufferedImage image;

	public Video()
	{
	    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    	GraphicsDevice gs = ge.getDefaultScreenDevice();
	    GraphicsConfiguration gc = gs.getDefaultConfiguration();
    
    	// Create an image that does not support transparency
	    image = gc.createCompatibleImage(width(), height(), Transparency.OPAQUE);
		Graphics g = image.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width(), height());
	}

	public void rebuildDebugger()
	{
		
	}

	public abstract int height();
	public abstract int width();
}
