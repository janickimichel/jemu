import java.awt.*;
import java.awt.image.BufferedImage;

abstract class Video extends Device
{
	public BufferedImage backImage;
	public boolean updateScreen = false;
	public boolean updateLine = false;
	protected Graphics g;
	// protected int[][] pixels;
	
	abstract int image_w();
	abstract int image_h();
	abstract boolean redraw();
	abstract void updateBuffers();

	public Video()
	{
		backImage = new BufferedImage(image_w(), image_h(), BufferedImage.TYPE_INT_RGB);
		g = backImage.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, image_w(), image_h());
		// pixels = new int[image_w()][image_h()];
	}

	public void clearBackImage()
	{
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, image_w(), image_h());
	}
}
