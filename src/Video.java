import java.awt.*;
import java.awt.image.BufferedImage;

abstract class Video extends Device
{
	public BufferedImage backImage;
	protected Graphics g;
	
	abstract int image_w();
	abstract int image_h();
	abstract boolean redraw();

	public Video()
	{
		backImage = new BufferedImage(image_w(), image_h(), BufferedImage.TYPE_INT_RGB);
		g = backImage.createGraphics();
	}
}
