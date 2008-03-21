import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.awt.image.*;

abstract class Video extends Device
{
	public boolean scanlineDone = false;
	public boolean screenDone = false;
	public boolean screenBegin = false;
	public int fps;

	public BufferedImage image;

	protected int[] pixels;

	public Video(int fps)
	{
		this.fps = fps;

		image = new BufferedImage(width(), height(), BufferedImage.TYPE_INT_ARGB);

		WritableRaster wr = image.getRaster();
		DataBuffer db = (DataBuffer)wr.getDataBuffer();
		DataBufferInt dbi = (DataBufferInt)db;
		pixels = dbi.getData();

		for(int i=0; i<(width()*height()); i++)
			pixels[i] = 0xff000000;

		drawScreen();
	}

	public void clearScreen()
	{
		for(int i=0; i<(width()*height()); i++)
			pixels[i] = 0xff000000;
		drawScreen();
	}

	public void drawScreen()
	{
		JEmu.platform.repaint();
	}

	public abstract int height();
	public abstract int width();
}
