import java.awt.*;
import java.awt.image.BufferedImage;

abstract class Video extends Device
{
	public boolean scanlineDone = false;
	public boolean screenDone = false;
	public boolean screenBegin = false;
	public BufferedImage backImage;
	public int fps;

	protected BufferedImage image;

	public Video(int fps)
	{
		this.fps = fps;

	    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    	GraphicsDevice gs = ge.getDefaultScreenDevice();
	    GraphicsConfiguration gc = gs.getDefaultConfiguration();
    
    	// Create an image that does not support transparency
	    image = gc.createCompatibleImage(width(), height(), Transparency.OPAQUE);
		Graphics g = image.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width(), height());

		backImage = gc.createCompatibleImage(width(), height(), Transparency.OPAQUE);
		
		drawScreen();
	}

	public void clearScreen()
	{
		Graphics g = image.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width(), height());
		drawScreen();
	}

	public void drawScreen()
	{
		Graphics g = backImage.createGraphics();
		g.drawImage(image, 0, 0, null);
		JEmu.platform.repaint();
	}

	public abstract int height();
	public abstract int width();
}
