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
	public BufferedImage backImage;

	protected int[] pixels;

	public Video(int fps)
	{
		this.fps = fps;

		image = new BufferedImage(width(), height(), BufferedImage.TYPE_INT_ARGB);
		backImage = new BufferedImage(width(), height(), BufferedImage.TYPE_INT_ARGB);

		WritableRaster wr = image.getRaster();
		DataBuffer db = (DataBuffer)wr.getDataBuffer();
		DataBufferInt dbi = (DataBufferInt)db;
		pixels = dbi.getData();

		int w = width();
		int h = height();

		for(int i=0; i<(w*h); i++)
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
//		JEmu.painting = true;
		JEmu.platform.repaint();
//		while(JEmu.painting)
//			;
	}

	public abstract int height();
	public abstract int width();
}
